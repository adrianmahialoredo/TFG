import React from 'react';
import { Pressable, Text, View } from 'react-native';

/** Opacidad de un chip no seleccionado. */
const UNSELECTED_OPACITY = 0.4;

/** Factor que se aplica a la opacidad mientras se mantiene pulsado. */
const PRESSED_FACTOR = 0.7;

type Props = {
  label: string;
  selected: boolean;
  onPress: () => void;
};

/**
 * Chip de selección múltiple (géneros, instrumentos…).
 *
 * Los colores no cambian: la selección se marca con la opacidad (los no seleccionados
 * quedan atenuados) y la pulsación la reduce un poco más mientras se mantiene el dedo.
 *
 * Usa Pressable en vez de TouchableOpacity: TouchableOpacity anima su propia opacidad al
 * pulsar y se pisaba con la de la selección. Aquí ambas se calculan juntas en una View
 * interior, así que nunca interfieren.
 */
export default function SelectableChip({ label, selected, onPress }: Props) {
  return (
    <Pressable
      onPress={onPress}
      hitSlop={6}
      accessibilityRole="checkbox"
      accessibilityState={{ checked: selected }}
    >
      {({ pressed }) => (
        <View
          className="px-3 py-1 rounded-lg bg-secondary"
          style={{ opacity: (selected ? 1 : UNSELECTED_OPACITY) * (pressed ? PRESSED_FACTOR : 1) }}
        >
          <Text className="text-sm font-medium text-primary">{label}</Text>
        </View>
      )}
    </Pressable>
  );
}
