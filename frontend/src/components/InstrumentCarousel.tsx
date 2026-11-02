import { ScrollView, Text, View } from 'react-native';
import Touchable from './Touchable';
import { ChevronLeft, ChevronRight } from 'lucide-react-native';
import InstrumentIcon from './InstrumentIcon';
import { COLORS } from '../constants/Theme';
import { useSnapSelector } from '../hooks/useSnapSelector';

type InstrumentOption = {
  id: string;
  name: string;
};

type InstrumentCarouselProps = {
  options: InstrumentOption[];
  selectedId: string | undefined;
  onSelect: (id: string) => void;
};

/** Ancho de cada instrumento e instrumentos visibles: el del centro es el seleccionado. */
export const CAROUSEL_ITEM_WIDTH = 84;
const VISIBLE_ITEMS = 3;
const ICON_SIZE = 36;
const ARROW_SIZE = 28;
/** Opacidad de los instrumentos que no están en el centro. */
const DIMMED_OPACITY = 0.3;

/**
 * Carrusel horizontal de instrumentos (como la rueda del afinador, pero en horizontal): el del
 * centro en dorado y a sus lados, atenuados, el anterior y el siguiente si los hay. Se elige
 * deslizando, pulsando un instrumento o con las flechas, que solo salen si hay algo hacia ese lado.
 *
 * Cada instrumento lleva su nombre bajo el icono: guitarra, bajo y ukelele comparten icono.
 */
export default function InstrumentCarousel({ options, selectedId, onSelect }: InstrumentCarouselProps) {
  const selectedIndex = Math.max(0, options.findIndex((option) => option.id === selectedId));
  const { scrollRef, centerIndex, select, scrollProps } = useSnapSelector({
    length: options.length,
    selectedIndex,
    itemSize: CAROUSEL_ITEM_WIDTH,
    horizontal: true,
    onSelect: (index) => onSelect(options[index].id),
  });

  const arrow = (direction: -1 | 1) => {
    const target = centerIndex + direction;
    const Icon = direction < 0 ? ChevronLeft : ChevronRight;
    // Sin instrumento hacia ese lado se deja el hueco: el carrusel no se mueve del centro
    if (target < 0 || target >= options.length) return <View style={{ width: ARROW_SIZE }} />;
    return (
      <Touchable
        onPress={() => select(target)}
        accessibilityRole="button"
        accessibilityLabel={direction < 0 ? 'Instrumento anterior' : 'Instrumento siguiente'}
        hitSlop={12}
      >
        <Icon size={ARROW_SIZE} color={COLORS.primary} />
      </Touchable>
    );
  };

  return (
    <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'center', marginBottom: 12 }}>
      {arrow(-1)}
      <View style={{ width: CAROUSEL_ITEM_WIDTH * VISIBLE_ITEMS }}>
        <ScrollView
          ref={scrollRef}
          testID="instrument-carousel"
          showsHorizontalScrollIndicator={false}
          {...scrollProps}
          // Un hueco a cada lado: el primero y el último también llegan al centro
          contentContainerStyle={{ paddingHorizontal: CAROUSEL_ITEM_WIDTH }}
        >
          {options.map((option, index) => {
            const opacity = index === centerIndex ? 1 : DIMMED_OPACITY;
            return (
              <Touchable
                key={option.id}
                onPress={() => select(index)}
                accessibilityRole="button"
                accessibilityLabel={option.name}
                accessibilityState={{ selected: index === selectedIndex }}
                style={{ width: CAROUSEL_ITEM_WIDTH, alignItems: 'center', paddingVertical: 4 }}
              >
                <View style={{ opacity }}>
                  <InstrumentIcon name={option.name} size={ICON_SIZE} color={COLORS.primary} strokeWidth={1.5} />
                </View>
                <Text
                  numberOfLines={1}
                  style={{ marginTop: 4, fontSize: 13, fontWeight: '600', color: COLORS.primary, opacity }}
                >
                  {option.name}
                </Text>
              </Touchable>
            );
          })}
        </ScrollView>
      </View>
      {arrow(1)}
    </View>
  );
}
