/**
 * DTO para Lista de Items (ItemList)
 * Representa una lista de items personalizada del usuario
 */
export interface ItemListDTO {
  id: string;
  userId: string;
  name: string;
  imageUrl?: string;
  description?: string;
  itemIds: string[];
}

/**
 * Payload mínimo para crear una lista en backend.
 * El backend genera el id y devuelve el objeto completo.
 */
export interface CreateItemListDTO {
  userId: string;
  name: string;
  imageUrl?: string;
  itemIds?: string[];
}

