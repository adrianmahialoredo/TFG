import { useState } from 'react';
import { LayoutChangeEvent, useWindowDimensions } from 'react-native';
import { COLORS } from '../constants/Theme';
import { gradientColorAt } from '../utils/imageColor.utils';
import { useDominantColor } from './useDominantColor';

/** Parte de la pantalla que ocupa el degradado; por debajo, solo el fondo negro. */
const GRADIENT_SCREEN_FRACTION = 0.6;

/**
 * Nivel 6: fondo con degradado del color principal de una imagen al negro (ListScreen,
 * SongScreen, LessonScreen). Sin imagen, o si no se puede calcular su color, es dorado.
 *
 * Devuelve las props de `GradientBackground` (`color`, `height`) y el `fadeColor` del
 * FadeScrollView: el color del fondo a la altura a la que empieza el scroll, para que su
 * degradado superior se funda con él y no con el negro. Para calcular esa altura, la pantalla
 * pasa `onContainerLayout` a la View que contiene la cabecera y el scroll (hija directa del
 * AppLayout) y `onScrollLayout` a una View que envuelva el FadeScrollView.
 */
export const useImageGradient = (imageUrl: string | null | undefined) => {
  const color = useDominantColor(imageUrl, COLORS.primary);
  const { height: windowHeight } = useWindowDimensions();
  const height = Math.round(windowHeight * GRADIENT_SCREEN_FRACTION);

  const [containerY, setContainerY] = useState(0);
  const [scrollY, setScrollY] = useState(0);

  return {
    color,
    height,
    fadeColor: color ? gradientColorAt(color, containerY + scrollY, height) : COLORS.background,
    onContainerLayout: (event: LayoutChangeEvent) => setContainerY(event.nativeEvent.layout.y),
    onScrollLayout: (event: LayoutChangeEvent) => setScrollY(event.nativeEvent.layout.y),
  };
};
