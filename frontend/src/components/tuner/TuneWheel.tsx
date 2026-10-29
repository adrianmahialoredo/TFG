import {memo, useEffect, useRef, useState} from "react";
import {NativeScrollEvent, NativeSyntheticEvent, ScrollView, Text, TouchableOpacity, View} from "react-native";
import {COLORS} from "../../constants/Theme";

type TuneWheelOption = {
    id: string;
    label: string;
};

type TuneWheelProps = {
    options: TuneWheelOption[];
    selectedIndex: number;
    onSelect: (index: number) => void;
};

/** Alto de cada fila y filas visibles: la del centro es la seleccionada. */
export const WHEEL_ITEM_HEIGHT = 44;
const VISIBLE_ITEMS = 3;
/** Opacidad de las filas que no están en el centro. */
const DIMMED_OPACITY = 0.3;
/** Sin eventos de scroll durante este tiempo, la rueda se da por parada. */
export const SETTLE_MS = 150;

const clampIndex = (index: number, length: number) => Math.max(0, Math.min(length - 1, index));

/**
 * Rueda vertical de afinaciones (tipo selector de iOS): 3 filas visibles, la del centro en
 * dorado y las de arriba y abajo atenuadas. Se elige deslizando (se ajusta a la fila más
 * cercana) o pulsando una fila.
 *
 * `onSelect` se llama cuando la rueda se para en otra fila, no durante el deslizamiento:
 * así el afinador no cambia de afinación con cada fila que pasa.
 * Va dentro del scroll vertical de la pantalla: `nestedScrollEnabled` para Android.
 *
 * Los eventos de fin de deslizamiento no siempre llegan (Android, o un hilo de JS ocupado):
 * además, si la rueda deja de moverse SETTLE_MS se confirma la fila del centro.
 * Con `memo`: el afinador se vuelve a pintar con cada lectura del micrófono y la rueda no cambia.
 */
function TuneWheel({options, selectedIndex, onSelect}: TuneWheelProps) {
    const scrollRef = useRef<ScrollView>(null);
    // Fila en el centro mientras se desliza (solo para el resaltado)
    const [centerIndex, setCenterIndex] = useState(selectedIndex);
    // Última fila confirmada: los temporizadores leen la actual, no la de su render
    const committedRef = useRef(selectedIndex);
    const settleTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
    // Con el dedo encima no se confirma nada aunque la rueda esté quieta
    const draggingRef = useRef(false);

    // Selección desde fuera (p.ej. la afinación por defecto al cargar): mueve la rueda
    useEffect(() => {
        committedRef.current = selectedIndex;
        setCenterIndex(selectedIndex);
        scrollRef.current?.scrollTo({y: selectedIndex * WHEEL_ITEM_HEIGHT, animated: false});
    }, [selectedIndex, options.length]);

    useEffect(() => () => {
        if (settleTimer.current) clearTimeout(settleTimer.current);
    }, []);

    const indexAt = (offsetY: number) => clampIndex(Math.round(offsetY / WHEEL_ITEM_HEIGHT), options.length);

    const commit = (index: number) => {
        if (settleTimer.current) clearTimeout(settleTimer.current);
        settleTimer.current = null;
        if (index === committedRef.current) return;
        committedRef.current = index;
        onSelect(index);
    };

    const handleScroll = (event: NativeSyntheticEvent<NativeScrollEvent>) => {
        const index = indexAt(event.nativeEvent.contentOffset.y);
        if (index !== centerIndex) setCenterIndex(index);
        if (settleTimer.current) clearTimeout(settleTimer.current);
        if (!draggingRef.current) settleTimer.current = setTimeout(() => commit(index), SETTLE_MS);
    };

    const handleDragBegin = () => {
        draggingRef.current = true;
        if (settleTimer.current) clearTimeout(settleTimer.current);
        settleTimer.current = null;
    };

    const handleMomentumEnd = (event: NativeSyntheticEvent<NativeScrollEvent>) =>
        commit(indexAt(event.nativeEvent.contentOffset.y));

    // Si se suelta sin inercia no hay evento de momentum: se confirma aquí
    const handleDragEnd = (event: NativeSyntheticEvent<NativeScrollEvent>) => {
        draggingRef.current = false;
        if (Math.abs(event.nativeEvent.velocity?.y ?? 0) < 0.05) commit(indexAt(event.nativeEvent.contentOffset.y));
    };

    const handlePress = (index: number) => {
        scrollRef.current?.scrollTo({y: index * WHEEL_ITEM_HEIGHT, animated: true});
        setCenterIndex(index);
        commit(index);
    };

    return (
        <View style={{height: WHEEL_ITEM_HEIGHT * VISIBLE_ITEMS}}>
            <ScrollView
                ref={scrollRef}
                testID="tune-wheel"
                nestedScrollEnabled
                showsVerticalScrollIndicator={false}
                snapToInterval={WHEEL_ITEM_HEIGHT}
                decelerationRate="fast"
                contentOffset={{x: 0, y: selectedIndex * WHEEL_ITEM_HEIGHT}}
                // Una fila de margen arriba y abajo: la primera y la última también llegan al centro
                contentContainerStyle={{paddingVertical: WHEEL_ITEM_HEIGHT}}
                onScroll={handleScroll}
                scrollEventThrottle={16}
                onMomentumScrollEnd={handleMomentumEnd}
                onScrollBeginDrag={handleDragBegin}
                onScrollEndDrag={handleDragEnd}
            >
                {options.map((option, index) => {
                    const centered = index === centerIndex;
                    return (
                        <TouchableOpacity
                            key={option.id}
                            onPress={() => handlePress(index)}
                            activeOpacity={0.7}
                            accessibilityRole="button"
                            accessibilityState={{selected: index === selectedIndex}}
                            style={{height: WHEEL_ITEM_HEIGHT, justifyContent: 'center', alignItems: 'center'}}
                        >
                            <Text
                                numberOfLines={1}
                                style={{
                                    fontSize: 26,
                                    fontWeight: '500',
                                    color: COLORS.primary,
                                    opacity: centered ? 1 : DIMMED_OPACITY,
                                }}
                            >
                                {option.label}
                            </Text>
                        </TouchableOpacity>
                    );
                })}
            </ScrollView>
        </View>
    );
}

export default memo(TuneWheel);
