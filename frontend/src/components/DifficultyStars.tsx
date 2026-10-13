import React from "react";
import {View} from "react-native";
import {Ionicons} from "@expo/vector-icons";
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
        <Ionicons
          key={i}
          name={i < filledStars ? 'star' : 'star-outline'}
          size={18}
          color={COLORS.primary}
        />
      ))}
    </View>
  );
}
