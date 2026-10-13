import React from 'react';
import { GestureResponderEvent, Pressable, PressableProps } from 'react-native';
import Animated, { useAnimatedStyle, useSharedValue, withSequence, withTiming } from 'react-native-reanimated';
import type { BottomTabBarButtonProps } from '@react-navigation/bottom-tabs';

/** Escala máxima del "pop" al pulsar y duraciones de subida y vuelta (ms). */
const POP_SCALE = 1.15;
const POP_IN_MS = 90;
const POP_OUT_MS = 140;

/**
 * Botón de la barra de pestañas inferior: al pulsarlo, el icono y el texto crecen un poco y
 * vuelven enseguida a su tamaño ("pop").
 *
 * Reemplaza al botón por defecto de React Navigation conservando su estilo y sus props de
 * accesibilidad. Es un Pressable con estilo estático (no en función: NativeWind 4.1 lo pierde)
 * y la escala va con Reanimated en el hilo de UI.
 */
export default function AnimatedTabBarButton({
  children,
  onPress,
  style,
  // Props del PlatformPressable de React Navigation que el Pressable de React Native no conoce
  href: _href,
  pressOpacity: _pressOpacity,
  hoverEffect: _hoverEffect,
  pressColor: _pressColor,
  ...rest
}: BottomTabBarButtonProps) {
  const scale = useSharedValue(1);
  const animatedStyle = useAnimatedStyle(() => ({ transform: [{ scale: scale.value }] }));

  const handlePress = (event: GestureResponderEvent) => {
    scale.value = withSequence(
      withTiming(POP_SCALE, { duration: POP_IN_MS }),
      withTiming(1, { duration: POP_OUT_MS }),
    );
    onPress?.(event as any);
  };

  return (
    <Pressable {...(rest as PressableProps)} onPress={handlePress} style={style}>
      <Animated.View style={[{ alignItems: 'center' }, animatedStyle]}>
        {children}
      </Animated.View>
    </Pressable>
  );
}
