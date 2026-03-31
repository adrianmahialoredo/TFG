import axios from 'axios';
import { UserDTO } from '../types/UserDTO';
import { API_ENDPOINTS } from '../config/apiConfig';

export const getAllUsers = async (): Promise<UserDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.USERS.BASE);
  return response.data;
};

export const getUserById = async (id: string): Promise<UserDTO> => {
  const response = await axios.get(API_ENDPOINTS.USERS.BY_ID(id));
  return response.data;
};

export const updateUserProfile = async (user: UserDTO): Promise<UserDTO> => {
  const response = await axios.put(API_ENDPOINTS.USERS.BY_ID(user.id), user);
  return response.data;
};

export const deleteUser = async (id: string): Promise<void> => {
  await axios.delete(API_ENDPOINTS.USERS.BY_ID(id));
};

export const deleteAllUsers = async (): Promise<void> => {
  await axios.delete(API_ENDPOINTS.USERS.DELETE_ALL);
};

