/**
 * DTO para Afinación (Tune)
 * Afinación de un instrumento (p.ej. "Estándar" o "Drop D" de la guitarra). La usa el afinador.
 */
export interface TuneDTO {
  id: string;
  title: string;
  instrumentId: string;
  /** Nota de cada cuerda, de la más grave a la más aguda, en notación científica ("E2", "F#3"). */
  notes: string[];
}
