/**
 * DTO para Sección de Lección (LessonSection)
 * Representa una sección de una lección; su contenido es HTML (incluye su propio título)
 */
export interface LessonSectionDTO {
  id: string;
  lessonId: string;
  title: string;
  htmlContent: string;
  orderIndex: number;
}
