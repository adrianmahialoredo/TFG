import { ItemType } from "./ItemDTO";

/**
 * DTO resumen de cualquier item (Song, Lesson, Genre, InstrumentGenre).
 *
 * Equivale al `ItemSummaryDTO` del backend: lo devuelven todos los endpoints de
 * `/items` y `/recommendations/items`. Es lo justo para pintar una tarjeta y
 * navegar al detalle; el objeto completo de cada subtipo se pide a su repositorio.
 *
 * NO es la base de los DTOs completos (eso es `ItemDTO`): vive en su propia Store
 * (`ItemEntitiesStore`) y nunca se mezcla con las Stores de las entidades completas.
 */
export interface ItemSummaryDTO {
  id: string;
  title: string;
  imageUrl: string;
  type: ItemType;
  /** Nivel de 1 a 5 estrellas; 0 si no tiene (p.ej. canciones). */
  level?: number;
  /** Artista de la canción (subtítulo de la tarjeta). Solo en canciones. */
  artist?: string | null;
}
