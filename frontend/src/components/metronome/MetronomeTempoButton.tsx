import {TouchableOpacity, Text} from "react-native";
import {COLORS} from "../../constants/Theme";

/**
 * Botón de ajuste fino del tempo (-10, -1, +1, +10).
 *
 * Estilos en línea y sin className a propósito (NativeWind 4.1):
 *  - TouchableOpacity + className pisa la opacidad animada al re-renderizar y se queda "pulsado".
 *  - Pressable con `style={({pressed}) => ...}` pierde el estilo (el interop no soporta la función).
 */
export default function MetronomeTempoButton({ title, onPress }: { title: string, onPress: () => void }) {
    return (
        <TouchableOpacity
            onPress={onPress}
            activeOpacity={1}
            hitSlop={6}
            style={{
                flex: 1,
                height: 28,
                justifyContent: 'center',
                borderWidth: 1,
                borderColor: COLORS.primary,
                borderRadius: 16,
            }}
        >
            <Text style={{fontSize: 14, textAlign: 'center', color: COLORS.primary}}>{title}</Text>
        </TouchableOpacity>
    );
}
