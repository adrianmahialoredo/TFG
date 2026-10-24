import { Guitar, Keyboard, Drum, Music, LucideIcon } from 'lucide-react-native';
import { normalizeSearchText } from '../utils/search.utils';

/** Icono por instrumento; la clave es el nombre normalizado (sin tildes ni mayúsculas). */
const INSTRUMENT_ICONS: Record<string, LucideIcon> = {
  guitarra: Guitar,
  bajo: Guitar,
  ukelele: Guitar,
  piano: Keyboard,
  bateria: Drum,
};

type InstrumentIconProps = {
  /** Nombre del instrumento, o un título que lo contenga ("Batería Rock"). */
  name?: string;
  size: number;
  color: string;
  strokeWidth?: number;
};

/**
 * Icono del instrumento al que se refiere un nombre. Si no se reconoce, una nota musical.
 */
export default function InstrumentIcon({ name, size, color, strokeWidth = 1 }: InstrumentIconProps) {
  const normalized = normalizeSearchText(name ?? '');
  const key = Object.keys(INSTRUMENT_ICONS).find((instrument) => normalized.includes(instrument));
  const Icon = key ? INSTRUMENT_ICONS[key] : Music;
  return <Icon size={size} color={color} strokeWidth={strokeWidth} />;
}
