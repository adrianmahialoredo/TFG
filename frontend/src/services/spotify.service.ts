import axios from 'axios';
import { API_ENDPOINTS } from '../config/apiConfig';
import {SpotifyRecommendationDTO} from "../types";

export interface SpotifyRecommendation {
  id: string;
  name: string;
  artist?: string;
  spotifyUrl?: string;
  [key: string]: any;
}

export const spotifyLogin = async (): Promise<{ authUrl: string }> => {
  const response = await axios.get(API_ENDPOINTS.SPOTIFY.LOGIN);
  return response.data;
};

export const spotifyCallback = async (code: string, state: string): Promise<{ token: string; userId: string }> => {
  const response = await axios.post(API_ENDPOINTS.SPOTIFY.CALLBACK, { code, state });
  return response.data;
};

export const getSpotifyRecommendations = async (spotifyUserId: string): Promise<SpotifyRecommendationDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SPOTIFY.RECOMMENDATIONS(spotifyUserId));
  return response.data;
};

