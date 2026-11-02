import React from 'react';
import Touchable from '../Touchable';
import { ArrowLeft } from 'lucide-react-native';

type BackButtonProps = {
  onPress: () => void;
  size?: number;
  color?: string;
};

const HIT_SLOP = { top: 8, bottom: 8, left: 8, right: 8 };

/**
 * Flecha de "volver" de las cabeceras.
 */
export default function BackButton({ onPress, size = 24, color = 'white' }: BackButtonProps) {
  return (
    <Touchable onPress={onPress} hitSlop={HIT_SLOP} accessibilityRole="button" accessibilityLabel="Volver">
      <ArrowLeft size={size} color={color} />
    </Touchable>
  );
}
