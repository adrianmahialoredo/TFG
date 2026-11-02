import React from 'react';
import { Pressable, PressableProps, StyleProp, StyleSheet, ViewStyle } from 'react-native';
import { HELD_OPACITY, useHeldPress } from '../hooks/useHeldPress';

type TouchableProps = Omit<PressableProps, 'style' | 'children'> & {
  style?: StyleProp<ViewStyle>;
  className?: string;
  children?: React.ReactNode;
  /** Opacidad mientras se mantiene pulsado; 1 = sin atenuar. */
  activeOpacity?: number;
};

/**
 * Botón de toda la app (sustituye a TouchableOpacity): al tocarlo actúa directamente, sin la
 * animación de pulsación; solo si se mantiene el dedo encima se atenúa un poco.
 *
 * Pressable con estilo estático y la opacidad en estado de React: con NativeWind 4.1,
 * TouchableOpacity + className pisaba su opacidad animada y se quedaba "pulsado", y Pressable con
 * `style` en función perdía el estilo.
 */
export default function Touchable({ style, activeOpacity = HELD_OPACITY, onPressIn, onPressOut, ...rest }: TouchableProps) {
  const { held, ...pressHandlers } = useHeldPress(onPressIn, onPressOut);
  // Respeta la opacidad que ya tenga el botón (p.ej. deshabilitado) y la atenúa sobre ella
  const baseOpacity = StyleSheet.flatten(style)?.opacity ?? 1;

  return (
    <Pressable
      {...rest}
      {...pressHandlers}
      style={held && activeOpacity < 1 ? [style, { opacity: baseOpacity * activeOpacity }] : style}
    />
  );
}
