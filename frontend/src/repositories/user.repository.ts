import * as userService from '../services/user.service';
import * as userLocal from '../local/user.local';
import { useUserEntitiesStore } from "../stores/entities";
import { UserDTO } from '../types';

/**
 * Helper para hidratar RAM y Local de forma atómica
 */
const hydrateUsers = async (users: UserDTO[]) => {
  if (users.length === 0) return;
  // 1. RAM
  useUserEntitiesStore.getState().setUsers(users);
  // 2. DISCO
  await userLocal.saveBatch(users).catch(console.error);
};

export const getById = async (id: string): Promise<UserDTO | null> => {
  // 1. Intentar RAM primero (Súper rápido)
  let user: UserDTO | null = useUserEntitiesStore.getState().users[id];
  if (user) return user;

  // 2. Intentar Local
  user = await userLocal.getById(id);

  // 3. Fallback a API
  if (!user) {
    try {
      user = await userService.getById(id);
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

export const getAllById = async (ids: string[]): Promise<UserDTO[]> => {
  // 1. Preguntar al local
  const { cached, missing } = await userLocal.getAllById(ids);

  let remoteUsers: UserDTO[] = [];

  // 2. Si faltan IDs, vamos a la API
  if (missing.length > 0) {
    try {
      // Usamos el servicio de usuarios, no el de canciones (obvio)
      remoteUsers = await userService.getAllById(missing);

      if (remoteUsers.length > 0) {
        // Usamos el helper que definimos antes
        await hydrateUsers(remoteUsers);
      }
    } catch (error) {
      console.error("[UserRepo] API Error al recuperar usuarios faltantes", error);
    }
  }

  // 3. Unificamos lo que había en caché y lo que vino de la API
  const all = [...cached, ...remoteUsers];

  // Aseguramos que la RAM esté al día con todo
  useUserEntitiesStore.getState().setUsers(all);

  // 4. Reordenar para que coincida con el array de IDs original
  const map = new Map(all.map(u => [u.id.toString(), u]));
  return ids.map(id => map.get(id)).filter((u): u is UserDTO => !!u);
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
      useUserEntitiesStore.getState().setUsers([backupUser]);
    }
    console.error(`[UserRepo] Error updating profile, rolled back:`, error);
    throw error;
  }
};
