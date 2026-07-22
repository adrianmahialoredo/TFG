import { useState } from 'react';
import { useAuthStore, useUserEntitiesStore } from '../stores/entities';
import * as authRepository from '../repositories/auth.repository';

export const useAuth = () => {
    // Selectores ATÓMICOS: solo nos re-renderizamos cuando cambia el valor concreto,
    // y cada uno devuelve una referencia estable (no rompe getSnapshot).
    const currentUserId = useAuthStore((state) => state.currentUserId);
    const token = useAuthStore((state) => state.token);
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    const isHydrated = useAuthStore((state) => state.isHydrated);

    const [isLoading, setIsLoading] = useState(false);

    const user = useUserEntitiesStore((state) =>
        currentUserId ? state.users[currentUserId] : null
    );

    const login = async (email: string, pass: string) => {
        setIsLoading(true);
        try {
            return await authRepository.login(email, pass);
        } finally {
            setIsLoading(false);
        }
    };

    const logout = async () => {
        return await authRepository.logout();
    };

    return {
        user,
        token,
        isAuthenticated,
        isReady: isHydrated,
        isLoading,
        login,
        logout
    };
};