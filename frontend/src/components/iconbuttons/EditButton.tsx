import React from 'react';
import Touchable from '../Touchable';
import { Pencil } from 'lucide-react-native';
import { COLORS } from '../../constants/Theme';

type EditButtonProps = {
  onPress: () => void;
  size?: number;
  color?: string;
  strokeWidth?: number;
  accessibilityLabel?: string;
};

const HIT_SLOP = { top: 8, bottom: 8, left: 8, right: 8 };

/**
 * Lápiz de "editar" (géneros e instrumentos favoritos, nombre de una lista...).
 */
export default function EditButton({
  onPress,
  size = 20,
  color = COLORS.primary,
  strokeWidth = 1,
  accessibilityLabel = 'Editar',
}: EditButtonProps) {
  return (
    <Touchable
      onPress={onPress}
      hitSlop={HIT_SLOP}
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel}
      style={{ paddingVertical: 4 }}
    >
      <Pencil size={size} color={color} strokeWidth={strokeWidth} />
    </Touchable>
  );
}
