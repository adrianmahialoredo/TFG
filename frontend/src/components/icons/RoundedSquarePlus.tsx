import React from 'react';
import { View } from 'react-native';
import { Plus } from 'lucide-react-native';

type RoundedSquarePlusProps = {
  size?: number;
  color: string;
  strokeWidth?: number;
};

export default function RoundedSquarePlus({
  size = 24,
  color,
  strokeWidth = 1.8,
}: RoundedSquarePlusProps) {
  const plusSize = Math.round(size * 0.58);

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
      <Plus size={plusSize} color={color} strokeWidth={strokeWidth} />
    </View>
  );
}
