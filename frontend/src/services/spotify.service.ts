import { apiClient } from './apiClient';
import { API_ENDPOINTS } from '../config/apiConfig';
import {SpotifyRecommendationDTO} from "../types";

export interface SpotifyRecommendation {
  id: string;
  name: string;
  artist?: string;
  spotifyUrl?: string;
  [key: string]: any;
}

/**
 * Pide al backend la URL de autorización de Spotify para el usuario autenticado.
 * El callback lo recibe directamente el backend (Spotify redirige al navegador a
 * API_ENDPOINTS.SPOTIFY.CALLBACK), así que la app solo tiene que abrir esta URL.
 */
export const spotifyLogin = async (): Promise<{ authUrl: string }> => {
  // El JWT lo añade apiClient: el backend asocia el `state` de OAuth a este usuario.
  const response = await apiClient.get(API_ENDPOINTS.SPOTIFY.LOGIN);
  return response.data;
};

export const getSpotifyRecommendations = async (spotifyUserId: string): Promise<SpotifyRecommendationDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.SPOTIFY.RECOMMENDATIONS(spotifyUserId));
  return response.data;
};

