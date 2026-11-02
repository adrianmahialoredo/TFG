import {Text} from "react-native";
import Touchable from "../Touchable";
import {COLORS} from "../../constants/Theme";

/**
 * Botón de ajuste fino del tempo (-10, -1, +1, +10). No se atenúa al mantenerlo: se pulsa muy seguido.
 */
export default function MetronomeTempoButton({ title, onPress }: { title: string, onPress: () => void }) {
    return (
        <Touchable
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
        </Touchable>
    );
}
