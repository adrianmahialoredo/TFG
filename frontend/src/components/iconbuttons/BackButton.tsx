import React from 'react';
import { TouchableOpacity } from 'react-native';
import { ArrowLeft } from 'lucide-react-native';

type BackButtonProps = {
  onPress: () => void;
  size?: number;
  color?: string;
};

const HIT_SLOP = { top: 8, bottom: 8, left: 8, right: 8 };

/**
 * Flecha de "volver" de las cabeceras.
 * Estilos en línea y sin className: TouchableOpacity + className (NativeWind 4.1) se queda "pulsado".
 */
export default function BackButton({ onPress, size = 24, color = 'white' }: BackButtonProps) {
  return (
    <TouchableOpacity onPress={onPress} hitSlop={HIT_SLOP} accessibilityRole="button" accessibilityLabel="Volver">
      <ArrowLeft size={size} color={color} />
    </TouchableOpacity>
  );
}
