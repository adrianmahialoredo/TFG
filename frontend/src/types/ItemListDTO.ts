/**
 * DTO para Lista de Items (ItemList)
 * Representa una lista de items personalizada del usuario
 */
export interface ItemListDTO {
  id: string;
  userId: string;
  name: string;
  imageUrl?: string;
  itemIds: string[];
}

