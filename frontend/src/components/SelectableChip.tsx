import React from 'react';
import { Pressable, Text, View } from 'react-native';
import { HELD_OPACITY, useHeldPress } from '../hooks/useHeldPress';

/** Opacidad de un chip no seleccionado. */
const UNSELECTED_OPACITY = 0.4;

type Props = {
  label: string;
  selected: boolean;
  onPress: () => void;
};

/**
 * Chip de selección múltiple (géneros, instrumentos…).
 *
 * Los colores no cambian: la selección se marca con la opacidad (los no seleccionados
 * quedan atenuados) y mantener el dedo encima la reduce un poco más (un toque no la cambia).
 *
 * Usa Pressable en vez de Touchable: la opacidad de la selección y la de mantener pulsado se
 * calculan juntas en una View interior, así que nunca interfieren.
 */
export default function SelectableChip({ label, selected, onPress }: Props) {
  const { held, ...pressHandlers } = useHeldPress();

  return (
    <Pressable
      onPress={onPress}
      {...pressHandlers}
      hitSlop={6}
      accessibilityRole="checkbox"
      accessibilityState={{ checked: selected }}
    >
      <View
        className="px-3 py-1 rounded-lg bg-secondary"
        style={{ opacity: (selected ? 1 : UNSELECTED_OPACITY) * (held ? HELD_OPACITY : 1) }}
      >
        <Text className="text-sm font-medium text-primary">{label}</Text>
      </View>
    </Pressable>
  );
}
