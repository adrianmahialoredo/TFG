import React from "react";
import {View} from "react-native";
import {Star} from "lucide-react-native";
import {COLORS} from "../constants/Theme";

type Props = {
  level?: number; // entre 0 y 5 estrellas
};

/** Estrellas de dificultad (5 en total, rellenas según el nivel del item). */
export default function DifficultyStars({ level }: Props) {
  const filledStars = Math.max(0, Math.min(5, Math.round(level ?? 0)));

  return (
    <View className="flex-row justify-center">
      {[...Array(5)].map((_, i) => (
        // Iconos SVG de lucide (como el resto de la app): no cargan ninguna fuente
        <Star
          key={i}
          size={18}
          color={COLORS.primary}
          fill={i < filledStars ? COLORS.primary : 'none'}
        />
      ))}
    </View>
  );
}
