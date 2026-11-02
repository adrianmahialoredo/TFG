import { useState } from 'react';
import { View, Text, Image } from 'react-native';
import Touchable from './Touchable';
import { ChevronRight } from 'lucide-react-native';
import { CARD_FILL_OPACITY, COLORS, GOLD_CARD_STYLE } from '../constants/Theme';
import InstrumentIcon from './InstrumentIcon';
import { useDominantColor } from '../hooks/useDominantColor';
import { withOpacity } from '../utils/imageColor.utils';

type SuggestionCardProps = {
  /** Texto de la cabecera ("Prueba esta combinación", "Explora este género"…). */
  label: string;
  /** Nombre del item sugerido. */
  title: string;
  /** Imagen de la miniatura; sin ella (o si no carga) se muestra el icono del instrumento del título. */
  imageUrl?: string;
  onPress: () => void;
};

/**
 * Card de sugerencia del Home (un género-instrumento, un género…): miniatura cuadrada y nombre
 * del item, al estilo de las sugerencias de Spotify. Los estilos van en la View interior.
 *
 * La card toma el color principal de la imagen (el mismo del degradado de fondo de ListScreen)
 * con la opacidad del fondo de la card dorada de los carruseles vacíos, también en el borde. Sin imagen (los géneros-instrumento,
 * que llevan el icono dorado del instrumento) o si no se puede calcular, es dorada. Mientras se
 * calcula, transparente: así no se ve un color y luego otro.
 */
export default function SuggestionCard({ label, title, imageUrl, onPress }: SuggestionCardProps) {
  const [imageFailed, setImageFailed] = useState(false);
  const showImage = Boolean(imageUrl) && !imageFailed;
  const color = useDominantColor(imageUrl, COLORS.primary);
  const cardStyle = {
    ...GOLD_CARD_STYLE,
    backgroundColor: color ? withOpacity(color, CARD_FILL_OPACITY) : 'transparent',
    borderColor: color ? withOpacity(color, CARD_FILL_OPACITY) : 'transparent',
  };

  return (
    <Touchable onPress={onPress} accessibilityRole="button">
      <View className="p-4" style={cardStyle}>
        <Text className="text-secondary text-base font-semibold mb-3">{label}</Text>
        <View className="flex-row items-center">
          <View className="w-16 h-16 rounded-lg bg-primary/30 items-center justify-center overflow-hidden">
            {showImage ? (
              <Image
                source={{ uri: imageUrl }}
                className="w-16 h-16"
                resizeMode="cover"
                onError={() => setImageFailed(true)}
              />
            ) : (
              <InstrumentIcon name={title} size={36} color={COLORS.primary} strokeWidth={1.5} />
            )}
          </View>
          <Text className="text-secondary text-lg font-bold ml-4 flex-1" numberOfLines={2}>{title}</Text>
          <ChevronRight size={24} color={COLORS.secondary} />
        </View>
      </View>
    </Touchable>
  );
}
