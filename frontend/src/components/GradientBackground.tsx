import { useEffect, useRef } from 'react';
import { Animated } from 'react-native';
import Svg, { Defs, LinearGradient, Rect, Stop } from 'react-native-svg';
import { GRADIENT_TOP_OPACITY } from '../utils/imageColor.utils';

// Cada degradado necesita un id de SVG propio
let gradientCount = 0;

type GradientBackgroundProps = {
  /** Color de arriba (`#rrggbb`); null mientras se calcula: no se pinta nada (fondo negro). */
  color: string | null;
  /** Alto del degradado: a partir de ahí, solo el fondo negro. */
  height: number;
};

/**
 * Fondo con un degradado vertical de `color` al negro (como la cabecera de una playlist de
 * Spotify). Va en posición absoluta detrás del contenido y aparece con un fundido cuando llega
 * el color.
 */
export default function GradientBackground({ color, height }: GradientBackgroundProps) {
  const gradientId = useRef(`background-gradient-${gradientCount++}`).current;
  const opacity = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (!color) {
      opacity.setValue(0);
      return;
    }
    Animated.timing(opacity, { toValue: 1, duration: 500, useNativeDriver: true }).start();
  }, [color, opacity]);

  if (!color) return null;

  return (
    <Animated.View
      pointerEvents="none"
      style={{ position: 'absolute', top: 0, left: 0, right: 0, height, opacity }}
    >
      <Svg width="100%" height="100%">
        <Defs>
          <LinearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
            <Stop offset="0" stopColor={color} stopOpacity={GRADIENT_TOP_OPACITY} />
            <Stop offset="1" stopColor={color} stopOpacity="0" />
          </LinearGradient>
        </Defs>
        <Rect x="0" y="0" width="100%" height="100%" fill={`url(#${gradientId})`} />
      </Svg>
    </Animated.View>
  );
}
