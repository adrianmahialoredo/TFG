import {useEffect, useRef, useState} from "react";
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

const clampIndex = (index: number, length: number) => Math.max(0, Math.min(length - 1, index));

/**
 * Rueda vertical de afinaciones (tipo selector de iOS): 3 filas visibles, la del centro en
 * dorado y las de arriba y abajo atenuadas. Se elige deslizando (se ajusta a la fila más
 * cercana) o pulsando una fila.
 *
 * `onSelect` se llama cuando la rueda se para en otra fila, no durante el deslizamiento:
 * así el afinador no cambia de afinación con cada fila que pasa.
 * Va dentro del scroll vertical de la pantalla: `nestedScrollEnabled` para Android.
 */
export default function TuneWheel({options, selectedIndex, onSelect}: TuneWheelProps) {
    const scrollRef = useRef<ScrollView>(null);
    // Fila en el centro mientras se desliza (solo para el resaltado)
    const [centerIndex, setCenterIndex] = useState(selectedIndex);

    // Selección desde fuera (p.ej. la afinación por defecto al cargar): mueve la rueda
    useEffect(() => {
        setCenterIndex(selectedIndex);
        scrollRef.current?.scrollTo({y: selectedIndex * WHEEL_ITEM_HEIGHT, animated: false});
    }, [selectedIndex, options.length]);

    const indexAt = (offsetY: number) => clampIndex(Math.round(offsetY / WHEEL_ITEM_HEIGHT), options.length);

    const handleScroll = (event: NativeSyntheticEvent<NativeScrollEvent>) => {
        const index = indexAt(event.nativeEvent.contentOffset.y);
        if (index !== centerIndex) setCenterIndex(index);
    };

    const commit = (index: number) => {
        if (index !== selectedIndex) onSelect(index);
    };

    const handleMomentumEnd = (event: NativeSyntheticEvent<NativeScrollEvent>) =>
        commit(indexAt(event.nativeEvent.contentOffset.y));

    // Si se suelta sin inercia no hay evento de momentum: se confirma aquí
    const handleDragEnd = (event: NativeSyntheticEvent<NativeScrollEvent>) => {
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
