import axios from 'axios';
import { API_ENDPOINTS } from '../config/apiConfig';
import { TabSectionDTO } from '../types';

export const getAll = async (): Promise<TabSectionDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.TAB_SECTIONS.BASE);
  return response.data;
};

export const getById = async (id: string): Promise<TabSectionDTO> => {
  const response = await axios.get(API_ENDPOINTS.TAB_SECTIONS.BY_ID(id));
  return response.data;
};

export const getByLesson = async (lessonId: string): Promise<TabSectionDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.TAB_SECTIONS.BY_LESSON(lessonId));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<TabSectionDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.TAB_SECTIONS.BASE, {
    params: { ids },
  });
  return response.data;
};

export const createTabSection = async (tabSection: TabSectionDTO): Promise<TabSectionDTO> => {
  const response = await axios.post(API_ENDPOINTS.TAB_SECTIONS.BASE, tabSection);
  return response.data;
};

export const updateTabSection = async (id: string, tabSection: TabSectionDTO): Promise<TabSectionDTO> => {
  const response = await axios.put(API_ENDPOINTS.TAB_SECTIONS.BY_ID(id), tabSection);
  return response.data;
};

export const deleteTabSection = async (id: string): Promise<void> => {
  await axios.delete(API_ENDPOINTS.TAB_SECTIONS.BY_ID(id));
};
