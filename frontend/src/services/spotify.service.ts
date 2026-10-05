import axios from 'axios';
import { API_ENDPOINTS } from '../config/apiConfig';
import {SpotifyRecommendationDTO} from "../types";
import { useAuthStore } from '../stores/entities/auth.entities.store';

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
  const token = useAuthStore.getState().token;
  const response = await axios.get(API_ENDPOINTS.SPOTIFY.LOGIN, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  return response.data;
};

export const getSpotifyRecommendations = async (spotifyUserId: string): Promise<SpotifyRecommendationDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SPOTIFY.RECOMMENDATIONS(spotifyUserId));
  return response.data;
};

