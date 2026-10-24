/**
 * DTO para Acorde (Chord)
 * Representa un acorde musical con sus propiedades
 */
export interface ChordDTO {
  id: string;
  name: string;
  notes: string[];
  imageUrl: string;
  open?: boolean;
  barre?: boolean;
  powerChord?: boolean;
  minor?: boolean;
  major?: boolean;
  seventh?: boolean;
  capo?: boolean;
  /** Traste en el que va el capo (0 = sin capo) */
  capoFret?: number;
}

