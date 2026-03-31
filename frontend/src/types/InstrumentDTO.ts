/**
 * DTO para Instrumento (Instrument)
 * Representa un instrumento musical en el sistema
 */
export interface InstrumentDTO {
  id: string;
  name: string;
  imageUrl: string;
  songIds?: string[];
}

