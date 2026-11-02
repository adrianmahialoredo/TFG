import { useEffect, useRef, useState } from "react";
import { NativeScrollEvent, NativeSyntheticEvent, ScrollViewInstance } from "react-native";

/** Sin eventos de scroll durante este tiempo, el selector se da por parado. */
export const SETTLE_MS = 150;

type SnapSelectorOptions = {
  /** Número de opciones. */
  length: number;
  /** Opción elegida desde fuera: el selector se coloca en ella. */
  selectedIndex: number;
  /** Lado de cada opción en la dirección del scroll (alto en vertical, ancho en horizontal). */
  itemSize: number;
  horizontal?: boolean;
  onSelect: (index: number) => void;
};

const clampIndex = (index: number, length: number) => Math.max(0, Math.min(length - 1, index));

/**
 * Lógica de un selector deslizable con la opción elegida en el centro (rueda de afinaciones,
 * carrusel de instrumentos): se ajusta a la opción más cercana al soltar y se puede elegir
 * pulsando una opción o con `select`.
 *
 * `onSelect` se llama cuando el selector se para en otra opción, no durante el deslizamiento.
 * Los eventos de fin de deslizamiento no siempre llegan (Android, o un hilo de JS ocupado):
 * además, si deja de moverse SETTLE_MS se confirma la opción del centro.
 *
 * Devuelve la ref y las props para el ScrollView, la opción que está en el centro (para el
 * resaltado, también mientras se desliza) y `select` para elegir una opción desde código.
 */
export const useSnapSelector = ({ length, selectedIndex, itemSize, horizontal = false, onSelect }: SnapSelectorOptions) => {
  const scrollRef = useRef<ScrollViewInstance>(null);
  // Opción en el centro mientras se desliza (solo para el resaltado)
  const [centerIndex, setCenterIndex] = useState(selectedIndex);
  // Última opción confirmada: los temporizadores leen la actual, no la de su render
  const committedRef = useRef(selectedIndex);
  const settleTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  // Con el dedo encima no se confirma nada aunque el selector esté quieto
  const draggingRef = useRef(false);

  const offsetFor = (index: number) =>
    horizontal ? { x: index * itemSize, y: 0 } : { x: 0, y: index * itemSize };

  // Selección desde fuera (p.ej. la opción por defecto al cargar): mueve el selector
  useEffect(() => {
    committedRef.current = selectedIndex;
    setCenterIndex(selectedIndex);
    scrollRef.current?.scrollTo({ ...offsetFor(selectedIndex), animated: false });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedIndex, length]);

  useEffect(() => () => {
    if (settleTimer.current) clearTimeout(settleTimer.current);
  }, []);

  const indexAt = (event: NativeSyntheticEvent<NativeScrollEvent>) => {
    const { x, y } = event.nativeEvent.contentOffset;
    return clampIndex(Math.round((horizontal ? x : y) / itemSize), length);
  };

  const commit = (index: number) => {
    if (settleTimer.current) clearTimeout(settleTimer.current);
    settleTimer.current = null;
    if (index === committedRef.current) return;
    committedRef.current = index;
    onSelect(index);
  };

  const onScroll = (event: NativeSyntheticEvent<NativeScrollEvent>) => {
    const index = indexAt(event);
    if (index !== centerIndex) setCenterIndex(index);
    if (settleTimer.current) clearTimeout(settleTimer.current);
    if (!draggingRef.current) settleTimer.current = setTimeout(() => commit(index), SETTLE_MS);
  };

  const onScrollBeginDrag = () => {
    draggingRef.current = true;
    if (settleTimer.current) clearTimeout(settleTimer.current);
    settleTimer.current = null;
  };

  const onMomentumScrollEnd = (event: NativeSyntheticEvent<NativeScrollEvent>) => commit(indexAt(event));

  // Si se suelta sin inercia no hay evento de momentum: se confirma aquí
  const onScrollEndDrag = (event: NativeSyntheticEvent<NativeScrollEvent>) => {
    draggingRef.current = false;
    const velocity = event.nativeEvent.velocity;
    if (Math.abs((horizontal ? velocity?.x : velocity?.y) ?? 0) < 0.05) commit(indexAt(event));
  };

  const select = (index: number) => {
    const target = clampIndex(index, length);
    scrollRef.current?.scrollTo({ ...offsetFor(target), animated: true });
    setCenterIndex(target);
    commit(target);
  };

  return {
    scrollRef,
    centerIndex,
    select,
    scrollProps: {
      horizontal,
      snapToInterval: itemSize,
      decelerationRate: 'fast' as const,
      contentOffset: offsetFor(selectedIndex),
      onScroll,
      scrollEventThrottle: 16,
      onMomentumScrollEnd,
      onScrollBeginDrag,
      onScrollEndDrag,
    },
  };
};
