/**
 * DTO para items genéricos en carruseles
 * Reutilizable para carruseles de canciones, géneros, etc.
 */
export interface CarouselItemDTO {
  id: string;
  title: string;
  imgUrl: string;
  artist?: string;
  description?: string;
}

