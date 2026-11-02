import { View, Text } from 'react-native';
import Touchable from './Touchable';
import { COLORS } from '../constants/Theme';

type Option<T extends string> = {
  value: T;
  label: string;
};

type OptionSelectorProps<T extends string> = {
  options: Option<T>[];
  selected: T | undefined;
  onSelect: (value: T) => void;
};

/**
 * Selector de opción única (p.ej. la dificultad) como una sola píldora dividida en secciones,
 * con los colores de los TopButton: la elegida en dorado con texto blanco y las demás en negro
 * con texto dorado.
 */
export default function OptionSelector<T extends string>({ options, selected, onSelect }: OptionSelectorProps<T>) {
  return (
    <View
      style={{
        flexDirection: 'row',
        alignSelf: 'center',
        height: 40,
        marginBottom: 8,
        borderWidth: 1,
        borderColor: COLORS.primary,
        borderRadius: 9999,
        overflow: 'hidden',
      }}
    >
      {options.map((option, index) => {
        const isSelected = option.value === selected;
        return (
          <Touchable
            key={option.value}
            onPress={() => onSelect(option.value)}
            accessibilityRole="button"
            accessibilityState={{ selected: isSelected }}
            style={{
              justifyContent: 'center',
              paddingHorizontal: 18,
              backgroundColor: isSelected ? COLORS.primary : COLORS.background,
              // Separador dorado entre secciones
              borderLeftWidth: index > 0 ? 1 : 0,
              borderLeftColor: COLORS.primary,
            }}
          >
            <Text style={{ fontSize: 14, fontWeight: '600', color: isSelected ? 'white' : COLORS.primary }}>
              {option.label}
            </Text>
          </Touchable>
        );
      })}
    </View>
  );
}
