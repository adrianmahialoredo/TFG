import React from 'react';
import { View, Text } from 'react-native';
import Slider from '@react-native-community/slider';
import { COLORS } from '../constants/Theme';
import { MIN_DIFFICULTY_LEVEL, MAX_DIFFICULTY_LEVEL } from '../constants/Preferences';

type Props = {
  value: number;
  onChange: (level: number) => void;
  disabled?: boolean;
};

/**
 * Selector del nivel musical autopercibido del usuario (0 a 5, la misma escala que las
 * estrellas de los items). Slider dorado por pasos enteros.
 */
export default function DifficultyLevelSlider({ value, onChange, disabled = false }: Props) {
  return (
    <View className="mb-2">
      <View className="flex-row justify-between items-center mb-2">
        <Text className="font-semibold text-lg text-secondary">Mi nivel:</Text>
        <Text className="text-primary text-2xl font-bold">{value}</Text>
      </View>
      <Slider
        style={{ width: '100%', height: 32 }}
        minimumValue={MIN_DIFFICULTY_LEVEL}
        maximumValue={MAX_DIFFICULTY_LEVEL}
        step={1}
        value={value}
        disabled={disabled}
        minimumTrackTintColor={COLORS.primary}
        maximumTrackTintColor={COLORS.secondary}
        thumbTintColor={COLORS.primary}
        accessibilityLabel="Nivel musical"
        onValueChange={(level) => onChange(Math.round(level))}
      />
      <View className="flex-row justify-between">
        <Text className="text-artistName text-sm">{MIN_DIFFICULTY_LEVEL}</Text>
        <Text className="text-artistName text-sm">{MAX_DIFFICULTY_LEVEL}</Text>
      </View>
    </View>
  );
}
