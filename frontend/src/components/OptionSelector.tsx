import { View, Text, TouchableOpacity } from 'react-native';

const SELECTED_COLOR = '#ca8a04'; // yellow-600
const UNSELECTED_COLOR = '#374151'; // gray-700

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
 * Fila de opciones de selección única (instrumento, dificultad…) con forma de píldora.
 *
 * Usa TouchableOpacity con estilos en línea y sin className: con className NativeWind
 * pisa la opacidad animada y el botón se queda atenuado tras pulsarlo.
 */
export default function OptionSelector<T extends string>({ options, selected, onSelect }: OptionSelectorProps<T>) {
  return (
    <View className="flex-row justify-center flex-wrap mb-2" style={{ gap: 8 }}>
      {options.map((option) => (
        <TouchableOpacity
          key={option.value}
          onPress={() => onSelect(option.value)}
          accessibilityRole="button"
          accessibilityState={{ selected: option.value === selected }}
          style={{
            paddingHorizontal: 16,
            paddingVertical: 4,
            borderRadius: 9999,
            marginBottom: 8,
            backgroundColor: option.value === selected ? SELECTED_COLOR : UNSELECTED_COLOR,
          }}
        >
          <Text className="text-white font-semibold">{option.label}</Text>
        </TouchableOpacity>
      ))}
    </View>
  );
}
