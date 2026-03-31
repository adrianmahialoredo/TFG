// src/services/AuthService.ts
import axios from 'axios';
import { UserDTO } from '../types/UserDTO';
import { API_ENDPOINTS } from '../config/apiConfig';

export const login = async (
  email: string,
  password: string
): Promise<{ token: string; user: UserDTO }> => {
  const response = await axios.post(API_ENDPOINTS.AUTH.LOGIN, {
    email,
    password,
  });
  return response.data;
};

export const register = async (user: UserDTO): Promise<{ user: UserDTO }> => {
  const response = await axios.post(API_ENDPOINTS.AUTH.REGISTER, user);
  return response.data;
};