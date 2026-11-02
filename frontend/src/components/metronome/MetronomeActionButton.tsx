import {Text} from "react-native";
import Touchable from "../Touchable";
import {COLORS} from "../../constants/Theme";

type MetronomeActionButtonProps = {
    label: string;
    onPress: () => void;
    /** Relleno dorado (p. ej. el sonido de hi-hat activo). */
    selected?: boolean;
    /** Texto grande (TAP TEMPO). */
    large?: boolean;
};

/**
 * Botón ancho de la parte inferior del metrónomo (TAP TEMPO, Hi-Hat).
 */
export default function MetronomeActionButton({label, onPress, selected = false, large = false}: MetronomeActionButtonProps) {
    return (
        <Touchable
            onPress={onPress}
            style={{
                width: 320,
                alignSelf: 'center',
                paddingVertical: large ? 16 : 20,
                borderRadius: 6,
                backgroundColor: selected ? COLORS.primary : COLORS.secondary,
            }}
        >
            <Text style={{
                alignSelf: 'center',
                color: selected ? COLORS.background : COLORS.primary,
                fontSize: large ? 30 : 18,
                fontWeight: large ? '400' : '700',
            }}>
                {label}
            </Text>
        </Touchable>
    );
}
