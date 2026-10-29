import { useState } from 'react';
import { View, Text, Image, TouchableOpacity } from 'react-native';
import { ChevronRight } from 'lucide-react-native';
import { COLORS, GOLD_CARD_STYLE } from '../constants/Theme';
import InstrumentIcon from './InstrumentIcon';

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
 * del item, al estilo de las sugerencias de Spotify. Mismo estilo dorado transparente que la
 * card de los carruseles vacíos.
 *
 * TouchableOpacity sin className (con className NativeWind pisa la opacidad animada);
 * los estilos van en la View interior.
 */
export default function SuggestionCard({ label, title, imageUrl, onPress }: SuggestionCardProps) {
  const [imageFailed, setImageFailed] = useState(false);
  const showImage = Boolean(imageUrl) && !imageFailed;

  return (
    <TouchableOpacity onPress={onPress} activeOpacity={0.7} accessibilityRole="button">
      <View className="p-4" style={GOLD_CARD_STYLE}>
        <Text className="text-primary text-base font-semibold mb-3">{label}</Text>
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
          <ChevronRight size={24} color={COLORS.primary} />
        </View>
      </View>
    </TouchableOpacity>
  );
}
