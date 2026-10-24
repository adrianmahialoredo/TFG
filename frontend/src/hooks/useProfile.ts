import { useState, useEffect, useRef } from 'react';
import * as ImagePicker from 'expo-image-picker';
import {useAuthStore, useUserEntitiesStore} from '../stores/entities';
import * as userRepository from '../repositories/user.repository';
import { UserDTO } from '../types';

export const useProfile = () => {
    const currentUserId = useAuthStore((state) => state.currentUserId);

    // Fuente Única de Verdad: el user vive en la store de entidades.
    // Selector atómico: devuelve SIEMPRE la misma referencia mientras la entidad
    // no cambie, por lo que no provoca el warning "getSnapshot should be cached".
    const user = useUserEntitiesStore((state) =>
        currentUserId ? state.users[currentUserId] : undefined
    );

    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // --- HIDRATACIÓN (Cache-First) ---
    // La store de entidades NO se persiste, así que tras reiniciar la app
    // tenemos el `currentUserId` (de la AuthStore persistida) pero NO el objeto
    // del usuario en RAM. Si falta, lo recuperamos vía Repository (RAM -> Local -> API).
    // El ref evita disparos duplicados/concurrentes (incluido React StrictMode).
    const hydratingRef = useRef(false);
    useEffect(() => {
        if (!currentUserId || user || hydratingRef.current) return;

        hydratingRef.current = true;
        userRepository
            .getById(currentUserId)
            .catch((err) => console.error('[useProfile] Error hidratando usuario:', err))
            .finally(() => {
                hydratingRef.current = false;
            });
    }, [currentUserId, user]);

    // --- ACCIONES ---

    const updateProfileData = async (updates: Partial<UserDTO>) => {
        if (!currentUserId) return;
        setIsLoading(true);
        try {
            // El repositorio se encarga de hablar con la API y actualizar la Store
            await userRepository.updateProfile(currentUserId, updates);
        } catch (err) {
            setError('Error al actualizar el perfil');
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Abre la galería y sube la foto elegida.
     * @returns true si se subió; false si el usuario canceló
     * @throws si falla la subida (la pantalla avisa al usuario)
     */
    const pickAndUploadImage = async (): Promise<boolean> => {
        const result = await ImagePicker.launchImageLibraryAsync({
            allowsEditing: true,
            aspect: [1, 1],
            quality: 0.5, // Comprimimos para no saturar el servidor
        });

        const uri = result.canceled ? undefined : result.assets[0]?.uri;
        if (!uri || !currentUserId) return false;

        setIsLoading(true);
        try {
            await userRepository.updateProfileImage(currentUserId, uri);
            return true;
        } catch (err) {
            setError('No se pudo actualizar la foto de perfil');
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const updateFavorites = async (kind: 'genres' | 'instruments', ids: string[]) => {
        if (!currentUserId) return;
        setIsLoading(true);
        try {
            await userRepository.updateFavorites(currentUserId, kind, ids);
        } catch (err) {
            setError('Error al actualizar los favoritos');
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    return {
        user,
        isLoading,
        error,
        updateProfileData,
        pickAndUploadImage, // La pantalla ahora solo llama a esto
        updateFavoriteGenres: (ids: string[]) => updateFavorites('genres', ids),
        updateFavoriteInstruments: (ids: string[]) => updateFavorites('instruments', ids),
    };
};