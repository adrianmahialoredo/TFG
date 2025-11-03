import axios from 'axios';
import { UserDTO } from '../types/UserDTO';

const API_URL = 'http://192.168.0.22:8080/api/users';

export const updateUserProfile = async (user: UserDTO): Promise<UserDTO> => {
  const response = await axios.put(`${API_URL}/${user.id}`, user);
  return response.data;
};
