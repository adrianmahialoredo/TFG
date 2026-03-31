/**
 * DTO para Sección de Tab (TabSection)
 * Representa una sección de tab en una lección de canción
 */
export interface TabSectionDTO {
  id: string;
  songLessonId: string;
  identifier: string; // ej: "intro", "solo", "chorus", etc
  content: string; // contenido del tab en formato multilinea
}

