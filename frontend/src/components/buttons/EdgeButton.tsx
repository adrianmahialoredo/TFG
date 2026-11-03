import { Text, StyleSheet } from 'react-native';
import Touchable from '../Touchable';
import { COLORS } from '../../constants/Theme';

/** Radio de las esquinas exteriores: el mismo que el cajón de EdgePanel (rounded-r-xl) */
export const EDGE_RADIUS = 12;

const styles = StyleSheet.create({
  button: {
    height: 55,
    paddingHorizontal: 12,
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: COLORS.primary,
  },
  buttonNormal: {
    backgroundColor: COLORS.background,
    color: COLORS.primary,
  },
  buttonPressed: {
    backgroundColor: COLORS.primary,
    color: COLORS.songTitle,
  },
  text: {
    fontSize: 14,
    textAlign: 'center',
  },
});

interface EdgeButtonProps {
  name: string;
  isSelected: boolean;
  isTopEdge: boolean;
  isBottomEdge: boolean;
  onPress: () => void;
}

/**
 * Botón de lección del panel lateral. El panel sale del borde izquierdo, así que solo se
 * redondean las esquinas derechas de los extremos (arriba la del primero, abajo la del último),
 * con el radio del cajón para que el borde dorado siga su forma.
 */
export default function EdgeButton({ name, isSelected, isTopEdge, isBottomEdge, onPress }: EdgeButtonProps) {
  const colors = isSelected ? styles.buttonPressed : styles.buttonNormal;

  return (
    // Estilos en línea, sin className: NativeWind sobre el Pressable perdía el estilo
    <Touchable
      style={[
        styles.button,
        { backgroundColor: colors.backgroundColor },
        // Sin borde superior salvo el primero: entre dos botones no se suma una línea doble
        !isTopEdge && { borderTopWidth: 0 },
        isTopEdge && { borderTopRightRadius: EDGE_RADIUS },
        isBottomEdge && { borderBottomRightRadius: EDGE_RADIUS },
      ]}
      onPress={onPress}
    >
      <Text style={[styles.text, colors]}>{name}</Text>
    </Touchable>
  );
}
