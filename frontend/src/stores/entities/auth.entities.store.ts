import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { isTokenExpired } from '../../utils/jwt.utils';

/**
 * AuthState: El contrato que define qué guarda esta store.
 * Solo guardamos el TOKEN y el ID para cumplir con la ley
 * de "Fuente Única de Verdad".
 */
interface AuthState {
    token: string | null;
    currentUserId: string | null; // El ID para buscar en useUserEntitiesStore
    isAuthenticated: boolean;
    isHydrated: boolean; // Control para saber si ya leyó de AsyncStorage

    // Acciones
    setAuth: (token: string, userId: string) => void;
    clearAuth: () => void;
    setHydrated: (hydrated: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            token: null,
            currentUserId: null,
            isAuthenticated: false,
            isHydrated: false,

            // Al hacer login, guardamos solo lo mínimo necesario
            setAuth: (token, userId) =>
                set({
                    token,
                    currentUserId: userId,
                    isAuthenticated: true,
                }),

            // Al cerrar sesión, limpiamos todo
            clearAuth: () =>
                set({
                    token: null,
                    currentUserId: null,
                    isAuthenticated: false,
                }),

            setHydrated: (hydrated) => set({ isHydrated: hydrated }),
        }),
        {
            name: 'auth-store', // Nombre de la clave en AsyncStorage
            storage: createJSONStorage(() => AsyncStorage), // Forma segura de usar AsyncStorage en Zustand
            onRehydrateStorage: () => (state) => {
                // Esto se ejecuta cuando Zustand termina de cargar los datos del disco.
                // Un token guardado que ya caducó se descarta ANTES de marcar la store como
                // hidratada: así el AppNavigator va directo al login y no se lanzan
                // peticiones que acabarían en 401.
                if (state?.token && isTokenExpired(state.token)) {
                    state.clearAuth();
                }
                state?.setHydrated(true);
            },
        }
    )
);