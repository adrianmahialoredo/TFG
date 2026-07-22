import { create } from 'zustand';
import { UserDTO } from '../../types';

interface UserEntitiesState {
  users: Record<string, UserDTO>;

  setUsers: (users: UserDTO[]) => void;
  updateUser: (id: string, data: Partial<UserDTO>) => void;
  removeUsers: (ids: string[]) => void;
  clearUsers: () => void;
}

export const useUserEntitiesStore = create<UserEntitiesState>((set) => ({
  users: {},

  setUsers: (newUsers) =>
    set((state) => {
      const updatedUsers = { ...state.users };

      newUsers.forEach((user) => {
        updatedUsers[user.id.toString()] = user;
      });

      return { users: updatedUsers };
    }),

  updateUser: (id, data) =>
    set((state) => {
      const user = state.users[id];
      if (!user) return state;

      return {
        users: {
          ...state.users,
          [id]: { ...user, ...data },
        },
      };
    }),

  removeUsers: (ids) =>
    set((state) => {
      const updatedUsers = { ...state.users };
      ids.forEach((id) => delete updatedUsers[id]);
      return { users: updatedUsers };
    }),

  clearUsers: () => set({ users: {} }),
}));

