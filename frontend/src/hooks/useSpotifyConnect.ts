import { useCallback, useState } from 'react';
import { AppState } from 'react-native';
import * as WebBrowser from 'expo-web-browser';
import * as spotifyRepository from '../repositories/spotify.repository';
import { useAuthStore } from '../stores/entities';

/**
 * Resuelve cuando la app vuelve a primer plano. En Android el navegador saca la app a
 * segundo plano y `openBrowserAsync` resuelve nada más abrirlo, así que la vuelta se detecta aquí.
 */
const waitForAppActive = () => {
    let subscription: ReturnType<typeof AppState.addEventListener> | undefined;
    const promise = new Promise<void>((resolve) => {
        subscription = AppState.addEventListener('change', (state) => {
            if (state === 'active') resolve();
        });
    });
    return { promise, cancel: () => subscription?.remove() };
};

/**
 * Nivel 6: flujo de "Conectar con Spotify" (abrir la autorización en el navegador y,
 * al volver, refrescar el usuario). `connect` lanza si algo falla; la UI decide cómo avisar.
 */
export const useSpotifyConnect = () => {
    const currentUserId = useAuthStore((state) => state.currentUserId);
    const [connecting, setConnecting] = useState(false);

    const connect = useCallback(async () => {
        const appActive = waitForAppActive();
        setConnecting(true);
        try {
            const authUrl = await spotifyRepository.getAuthUrl();

            // En iOS resuelve al cerrar el navegador ('cancel'/'dismiss'); en Android, al abrirlo ('opened')
            const result = await WebBrowser.openBrowserAsync(authUrl);
            if (result.type === 'opened') await appActive.promise;

            // El usuario vuelve del navegador: se recarga para saber si ya tiene Spotify vinculado
            if (currentUserId) await spotifyRepository.refreshLinkedUser(currentUserId);
        } finally {
            appActive.cancel();
            setConnecting(false);
        }
    }, [currentUserId]);

    return { connect, connecting } as const;
};
