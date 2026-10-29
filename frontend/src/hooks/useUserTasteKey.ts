import { useAuthStore, useItemListEntitiesStore, useUserEntitiesStore } from '../stores/entities';

/**
 * Clave del contenido de las listas del usuario actual: cambia al añadir o quitar un item,
 * y al crear o borrar una lista. Vacía sin sesión.
 */
export const useItemListsKey = (): string => {
  const currentUserId = useAuthStore((state) => state.currentUserId);
  return useItemListEntitiesStore((state) =>
    Object.values(state.itemLists)
      .filter((list) => currentUserId && list.userId?.toString() === currentUserId.toString())
      .map((list) => `${list.id}:${(list.itemIds ?? []).join(',')}`)
      .sort()
      .join('|')
  );
};

/**
 * Clave de todo lo que usa el backend para personalizar las recomendaciones del usuario actual:
 * el contenido de sus listas, sus géneros e instrumentos favoritos y su nivel.
 *
 * Los hooks que piden carruseles o tarjetas personalizadas la ponen en las dependencias de su
 * `useEffect`: cuando cambia cualquiera de esos datos (en cualquier pantalla), se vuelven a pedir.
 * Las pantallas montadas en las pestañas se actualizan en segundo plano.
 */
export const useUserTasteKey = (): string => {
  const currentUserId = useAuthStore((state) => state.currentUserId);
  const itemListsKey = useItemListsKey();
  const profileKey = useUserEntitiesStore((state) => {
    const user = currentUserId ? state.users[currentUserId] : undefined;
    return [
      (user?.favouriteGenresIds ?? []).join(','),
      (user?.favouriteInstrumentsIds ?? []).join(','),
      user?.difficultyLevel ?? 0,
    ].join('|');
  });
  return `${profileKey}#${itemListsKey}`;
};
