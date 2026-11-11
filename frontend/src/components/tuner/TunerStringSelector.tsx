import {memo} from "react";
import {Text, View} from "react-native";
import Touchable from "../Touchable";
import {COLORS} from "../../constants/Theme";

type TunerStringSelectorProps = {
    /** Nota de cada cuerda, de la más grave a la más aguda. */
    notes: string[];
    /** Cuerda seleccionada: la que se está afinando. */
    selectedIndex: number;
    onSelect: (index: number) => void;
};

/**
 * Un botón por cuerda de la afinación (E2 A2 D3...). El usuario pulsa la cuerda que va a afinar,
 * que va rellena en dorado; el afinador compara siempre con esa cuerda.
 */
function TunerStringSelector({notes, selectedIndex, onSelect}: TunerStringSelectorProps) {
    return (
        <View style={{flexDirection: 'row', justifyContent: 'center', gap: 6}}>
            {notes.map((note, index) => {
                const active = index === selectedIndex;
                return (
                    <Touchable
                        key={`${index}-${note}`}
                        onPress={() => onSelect(index)}
                        accessibilityRole="button"
                        accessibilityState={{selected: active}}
                        accessibilityLabel={`Cuerda ${index + 1}: ${note}`}
                        style={{
                            flex: 1,
                            maxWidth: 56,
                            paddingVertical: 8,
                            borderRadius: 10,
                            borderWidth: active ? 2 : 1,
                            borderColor: COLORS.primary,
                            backgroundColor: active ? COLORS.primary : 'transparent',
                            alignItems: 'center',
                        }}
                    >
                        <Text style={{fontSize: 20, color: active ? COLORS.secondary : COLORS.primary}}>
                            {note}
                        </Text>
                    </Touchable>
                );
            })}
        </View>
    );
}

// memo: con cada lectura del micrófono solo se vuelve a pintar si cambia la cuerda resaltada
export default memo(TunerStringSelector);
