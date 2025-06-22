
import axios from 'axios';
import { UserDTO } from '../types/UserDTO';

const API_URL = 'http://192.168.0.22:8080/api'; // cambia por tu IP local o dominio en producción

export const login = async (email: string, password: string): Promise<{ token: string; user: UserDTO }> => {
  const response = await axios.post(`${API_URL}/auth/login`, { email, password });
  return response.data;
};

export const register = async (email: string, password: string): Promise<{ token: string; user: UserDTO }> => {
  const response = await axios.post(`${API_URL}/auth/register`, { email, password });
  return response.data;
};