import * as userService from '../services/user.service';
import * as userLocal from '../local/user.local';
import { uploadProfileImageToCloudinary } from '../services/cloudinary.service';
import { useUserEntitiesStore } from "../stores/entities";
import { UserDTO } from '../types';
import { createEntityCache } from '../utils/entityCache.utils';

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<UserDTO>({
    name: 'UserRepo',
    getRam: () => useUserEntitiesStore.getState().users,
    setRam: (entities) => useUserEntitiesStore.getState().setUsers(entities),
    local: userLocal,
    fetchByIds: userService.getAllById,
});

/**
 * Helper para hidratar RAM y Local de forma atómica.
 * Toda escritura pasa por aquí o llama a `cache.markFresh`: así una revalidación
 * lanzada antes de la modificación no pisa el dato nuevo cuando llega tarde.
 */
const hydrateUsers = async (users: UserDTO[]) => {
  if (users.length === 0) return;
  // 1. RAM
  useUserEntitiesStore.getState().setUsers(users);
  cache.markFresh(users.map(u => u.id.toString()));
  // 2. DISCO
  await userLocal.saveBatch(users).catch(console.error);
};

export const getById = async (id: string): Promise<UserDTO | null> => {
  // 1. Intentar RAM primero (Súper rápido)
  let user: UserDTO | null = useUserEntitiesStore.getState().users[id];
  if (user) {
    cache.revalidate([id]);
    return user;
  }

  // 2. Intentar Local
  user = await userLocal.getById(id);
  if (user) cache.revalidate([id]);

  // 3. Fallback a API
  if (!user) {
    try {
      user = await userService.getById(id);
      cache.markFresh([id]);
      if (user) await hydrateUsers([user]);
    } catch (error) {
      console.error(`[UserRepo] Error fetching user ${id}:`, error);
      return null;
    }
  } else {
    // Si estaba en Local pero no en RAM, hidratamos RAM
    useUserEntitiesStore.getState().setUsers([user]);
  }

  return user;
};

export const getAllById = cache.getAllById;

/**
 * Pide el usuario a la API saltándose la caché y lo hidrata (RAM + Disco).
 * Para cambios que ocurren fuera de la app, p.ej. al vincular Spotify en el navegador.
 */
export const refresh = async (id: string): Promise<UserDTO> => {
  const user = await userService.getById(id);
  await hydrateUsers([user]);
  return user;
};

/**
 * Actualizar el perfil del usuario
 * Flujo: Backup -> Optimistic RAM -> API -> Hydrate All (RAM + DISK)
 */
export const updateProfile = async (userId: string, updates: Partial<UserDTO>): Promise<UserDTO | null> => {
  // 1. BACKUP: Guardamos el estado actual por si la API falla
  const backupUser = useUserEntitiesStore.getState().users[userId];
  if (!backupUser) return null;

  try {
    // 2. OPTIMISTIC UPDATE: Actualizamos RAM de inmediato
    // Esto hace que el usuario vea su nuevo nombre al instante
    cache.markFresh([userId]);
    useUserEntitiesStore.getState().updateUser(userId, updates);

    // 3. API CALL: Enviamos los cambios al servidor
    // Importante: Mandamos el merge del backup + los updates
    const apiResponse = await userService.updateUserProfile({ ...backupUser, ...updates });

    // 4. HIDRATACIÓN FINAL: Cuando la API confirma, guardamos en RAM y DISCO
    // La respuesta de la API es la Verdad Absoluta (Source of Truth)
    await hydrateUsers([apiResponse]);

    return apiResponse;
  } catch (error) {
    // 5. ROLLBACK: Si la API explota, devolvemos la RAM a su estado anterior
    if (backupUser) {
      cache.markFresh([userId]);
      useUserEntitiesStore.getState().setUsers([backupUser]);
    }
    console.error(`[UserRepo] Error updating profile, rolled back:`, error);
    throw error;
  }
};

type FavoriteKind = 'genres' | 'instruments';

const FAVORITE_ENDPOINTS = {
  genres: {
    field: 'favouriteGenresIds',
    add: userService.addFavoriteGenre,
    remove: userService.removeFavoriteGenre,
  },
  instruments: {
    field: 'favouriteInstrumentsIds',
    add: userService.addFavoriteInstrument,
    remove: userService.removeFavoriteInstrument,
  },
} as const;

/**
 * Sustituye los géneros o instrumentos favoritos del usuario por `newIds`.
 * Flujo: Diff -> Optimistic RAM -> POST/DELETE de cada cambio -> Hydrate All (RAM + DISK)
 */
export const updateFavorites = async (
  userId: string,
  kind: FavoriteKind,
  newIds: string[],
): Promise<UserDTO | null> => {
  const backupUser = useUserEntitiesStore.getState().users[userId];
  if (!backupUser) return null;

  const { field, add, remove } = FAVORITE_ENDPOINTS[kind];
  const currentIds = (backupUser[field] ?? []).map(String);
  const toAdd = newIds.filter(id => !currentIds.includes(id));
  const toRemove = currentIds.filter(id => !newIds.includes(id));
  if (toAdd.length === 0 && toRemove.length === 0) return backupUser;

  try {
    cache.markFresh([userId]);
    useUserEntitiesStore.getState().updateUser(userId, { [field]: newIds } as Partial<UserDTO>);

    // En serie: cada respuesta trae el usuario completo y la última es el estado final
    let apiResponse: UserDTO = backupUser;
    for (const id of toAdd) apiResponse = await add(userId, id);
    for (const id of toRemove) apiResponse = await remove(userId, id);

    await hydrateUsers([apiResponse]);
    return apiResponse;
  } catch (error) {
    // Si falla a mitad, el backend puede haber aplicado parte: recargamos su estado real
    cache.markFresh([userId]);
    useUserEntitiesStore.getState().setUsers([backupUser]);
    userService.getById(userId).then(user => hydrateUsers([user])).catch(console.error);
    console.error(`[UserRepo] Error updating favourite ${kind}, rolled back:`, error);
    throw error;
  }
};

/**
 * Sube una imagen de perfil a Cloudinary y actualiza el usuario con la URL resultante.
 */
export const updateProfileImage = async (userId: string, localUri: string): Promise<UserDTO | null> => {
  const secureUrl = await uploadProfileImageToCloudinary(localUri);
  return updateProfile(userId, { profileImageUrl: secureUrl });
};
