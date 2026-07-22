import axios from 'axios';
import { UserDTO } from '../types';
import { API_ENDPOINTS } from '../config/apiConfig';

export const getById = async (id: string): Promise<UserDTO> => {
  const response = await axios.get(API_ENDPOINTS.USERS.BY_ID(id));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<UserDTO[]> => {
  if (ids.length === 0) return [];
  
  const response = await axios.get(API_ENDPOINTS.USERS.BY_IDS, { 
    params: { ids } 
  });
  return response.data;
};

export const updateUserProfile = async (user: UserDTO): Promise<UserDTO> => {
  const response = await axios.put(API_ENDPOINTS.USERS.BY_ID(user.id), user);
  return response.data;
};
