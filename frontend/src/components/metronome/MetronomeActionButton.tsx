import {Text, TouchableOpacity} from "react-native";
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
 *
 * TouchableOpacity con estilos en línea y sin className a propósito (ver MetronomeTempoButton):
 * con NativeWind 4.1, TouchableOpacity + className se queda "pulsado" y Pressable con estilo
 * en función pierde el estilo.
 */
export default function MetronomeActionButton({label, onPress, selected = false, large = false}: MetronomeActionButtonProps) {
    return (
        <TouchableOpacity
            onPress={onPress}
            activeOpacity={0.7}
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
        </TouchableOpacity>
    );
}
