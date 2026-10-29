import {memo} from "react";
import {Text, TouchableOpacity, View} from "react-native";
import {COLORS} from "../../constants/Theme";

type TunerStringSelectorProps = {
    /** Nota de cada cuerda, de la más grave a la más aguda. */
    notes: string[];
    /** Cuerda resaltada: la que se está afinando (null: ninguna). */
    activeIndex: number | null;
    /** Cuerda fijada a mano (se marca con un borde más grueso). */
    lockedIndex: number | null;
    onSelect: (index: number) => void;
};

/**
 * Un botón por cuerda de la afinación (E2 A2 D3...). La cuerda que se está afinando va rellena
 * en dorado; pulsar una la fija y pulsarla otra vez vuelve a la detección automática.
 *
 * TouchableOpacity con estilos en línea y sin className a propósito: con NativeWind,
 * TouchableOpacity + className se queda "pulsado" y Pressable con estilo en función pierde el estilo.
 */
function TunerStringSelector({notes, activeIndex, lockedIndex, onSelect}: TunerStringSelectorProps) {
    return (
        <View style={{flexDirection: 'row', justifyContent: 'center', gap: 6}}>
            {notes.map((note, index) => {
                const active = index === activeIndex;
                return (
                    <TouchableOpacity
                        key={`${index}-${note}`}
                        onPress={() => onSelect(index)}
                        activeOpacity={0.7}
                        accessibilityRole="button"
                        accessibilityState={{selected: index === lockedIndex}}
                        accessibilityLabel={`Cuerda ${index + 1}: ${note}`}
                        style={{
                            flex: 1,
                            maxWidth: 56,
                            paddingVertical: 8,
                            borderRadius: 10,
                            borderWidth: index === lockedIndex ? 2 : 1,
                            borderColor: COLORS.primary,
                            backgroundColor: active ? COLORS.primary : 'transparent',
                            alignItems: 'center',
                        }}
                    >
                        <Text style={{fontSize: 20, color: active ? COLORS.secondary : COLORS.primary}}>
                            {note}
                        </Text>
                    </TouchableOpacity>
                );
            })}
        </View>
    );
}

// memo: con cada lectura del micrófono solo se vuelve a pintar si cambia la cuerda resaltada
export default memo(TunerStringSelector);
