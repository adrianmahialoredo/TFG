import React from 'react';
import { View } from 'react-native';
import { Plus, Check } from 'lucide-react-native';

type AddButtonProps = {
  size?: number;
  color: string;
  strokeWidth?: number;
  /** Muestra un tic en lugar del más (p.ej. item ya añadido). */
  checked?: boolean;
};

export default function AddButton({
  size = 24,
  color,
  strokeWidth = 1.8,
  checked = false,
}: AddButtonProps) {
  const iconSize = Math.round(size * 0.58);
  const Icon = checked ? Check : Plus;

  return (
    <View
      style={{
        width: size,
        height: size,
        borderWidth: strokeWidth,
        borderColor: color,
        borderRadius: Math.round(size * 0.26),
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <Icon size={iconSize} color={color} strokeWidth={strokeWidth} />
    </View>
  );
}
