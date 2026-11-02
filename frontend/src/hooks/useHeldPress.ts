import { useEffect, useRef, useState } from 'react';
import { GestureResponderEvent } from 'react-native';

/** Opacidad mientras se mantiene pulsado: un atenuado moderado. */
export const HELD_OPACITY = 0.8;

/** Tiempo con el dedo encima antes de atenuar: un toque normal actúa sin ningún parpadeo. */
export const HOLD_DELAY_MS = 150;

type PressHandler = ((event: GestureResponderEvent) => void) | null | undefined;

/**
 * Estado "mantenido" de un botón: pasa a true solo si el dedo sigue encima HOLD_DELAY_MS, y vuelve
 * a false al levantarlo. Así un toque no hace ninguna animación y mantener pulsado sí se ve.
 *
 * Devuelve `held` y los manejadores para el Pressable (encadenan los que se le pasen).
 */
export const useHeldPress = (onPressIn?: PressHandler, onPressOut?: PressHandler) => {
  const [held, setHeld] = useState(false);
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const clear = () => {
    if (timer.current) clearTimeout(timer.current);
    timer.current = null;
  };

  useEffect(() => clear, []);

  return {
    held,
    onPressIn: (event: GestureResponderEvent) => {
      clear();
      timer.current = setTimeout(() => setHeld(true), HOLD_DELAY_MS);
      onPressIn?.(event);
    },
    onPressOut: (event: GestureResponderEvent) => {
      clear();
      setHeld(false);
      onPressOut?.(event);
    },
  };
};
