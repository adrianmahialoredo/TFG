import { TabSectionDTO } from "../types";
import { getData, storeData } from "./storage.local";

const TAB_SECTIONS_KEY = 'tabSections';

/* Guarda todas las secciones de tabs */
export const storeTabSections = async (tabSections: TabSectionDTO[]): Promise<void> => {
  return storeData(TAB_SECTIONS_KEY, tabSections);
};

/* Obtiene todas las secciones de tabs */
export const getStoredTabSections = async (): Promise<TabSectionDTO[] | null> => {
  return getData<TabSectionDTO[]>(TAB_SECTIONS_KEY);
};

/* Obtiene una sección de tab por su ID */
export const getStoredTabSectionById = async (id: string): Promise<TabSectionDTO | null> => {
  const tabSections = await getStoredTabSections();
  return tabSections?.find((section) => section.id === id) || null;
};

/* Obtiene todas las secciones de una lección */
export const getStoredTabSectionsByLesson = async (songLessonId: string): Promise<TabSectionDTO[]> => {
  const tabSections = await getStoredTabSections();
  return tabSections?.filter((section) => section.songLessonId === songLessonId) || [];
};

/* Obtiene varias secciones a partir de una lista de IDs */
export const getStoredTabSectionsByIdList = async (ids: string[]): Promise<TabSectionDTO[]> => {
  const tabSections = await getStoredTabSections();
  return tabSections?.filter((section) => ids.includes(section.id)) || [];
};

