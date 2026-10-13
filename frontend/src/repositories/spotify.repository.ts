import * as spotifyService from '../services/spotify.service';
import * as userRepository from './user.repository';

/**
 * Nivel 3: vinculación de la cuenta de Spotify.
 *
 * El OAuth lo resuelve el backend (Spotify redirige al navegador a su callback), así que aquí
 * solo se pide la URL de autorización y, a la vuelta, se recarga el usuario para saber si ya
 * tiene Spotify vinculado (el `UserDTO` refrescado se guarda en RAM + Disco).
 */

/** URL de autorización de Spotify para el usuario de la sesión (el JWT lo añade apiClient). */
export const getAuthUrl = async (): Promise<string> => {
    const { authUrl } = await spotifyService.spotifyLogin();
    if (!authUrl) {
        throw new Error('Spotify auth URL no disponible');
    }
    return authUrl;
};

/** Recarga el usuario tras volver del navegador para reflejar la vinculación. */
export const refreshLinkedUser = (userId: string) => userRepository.refresh(userId);
