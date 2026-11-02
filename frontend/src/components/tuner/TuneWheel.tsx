import {memo} from "react";
import {ScrollView, Text, View} from "react-native";
import Touchable from "../Touchable";
import {COLORS} from "../../constants/Theme";
import {useSnapSelector} from "../../hooks/useSnapSelector";

export {SETTLE_MS} from "../../hooks/useSnapSelector";

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

/**
 * Rueda vertical de afinaciones (tipo selector de iOS): 3 filas visibles, la del centro en
 * dorado y las de arriba y abajo atenuadas. Se elige deslizando (se ajusta a la fila más
 * cercana) o pulsando una fila; la lógica de selección está en `useSnapSelector`.
 *
 * Va dentro del scroll vertical de la pantalla: `nestedScrollEnabled` para Android.
 * Con `memo`: el afinador se vuelve a pintar con cada lectura del micrófono y la rueda no cambia.
 */
function TuneWheel({options, selectedIndex, onSelect}: TuneWheelProps) {
    const {scrollRef, centerIndex, select, scrollProps} = useSnapSelector({
        length: options.length,
        selectedIndex,
        itemSize: WHEEL_ITEM_HEIGHT,
        onSelect,
    });

    return (
        <View style={{height: WHEEL_ITEM_HEIGHT * VISIBLE_ITEMS}}>
            <ScrollView
                ref={scrollRef}
                testID="tune-wheel"
                nestedScrollEnabled
                showsVerticalScrollIndicator={false}
                {...scrollProps}
                // Una fila de margen arriba y abajo: la primera y la última también llegan al centro
                contentContainerStyle={{paddingVertical: WHEEL_ITEM_HEIGHT}}
            >
                {options.map((option, index) => {
                    const centered = index === centerIndex;
                    return (
                        <Touchable
                            key={option.id}
                            onPress={() => select(index)}
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
                        </Touchable>
                    );
                })}
            </ScrollView>
        </View>
    );
}

export default memo(TuneWheel);
