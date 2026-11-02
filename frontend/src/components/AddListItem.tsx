import React from "react";
import { View, Text, Image } from "react-native";
import Touchable from "./Touchable";
import { COLORS } from "../constants/Theme";
import AddButton from "./iconbuttons/AddButton";

type ItemProps = {
  item: {
    id: string;
    title: string;
    description: string;
    imageUrl: string;
    info: string; // Additional information to display
  };
  onPress: () => void;
  added?: boolean;
  inverted?: boolean;
  /** Factor de tamaño de la fila y todo su contenido (como en ListItem). */
  scale?: number;
};

/**
 * Fila de un item que se puede añadir a una lista (AddItemScreen).
 *
 * Estilos en línea y sin className: la fila va dentro de un Animated.View con animaciones de
 * entrada, salida y layout, y `inverted` cambia cuando el item sube un puesto. Con className,
 * NativeWind a veces no aplicaba el cambio de clases y la fila se quedaba sin fondo (negra) o
 * con colores mezclados. Tampoco lleva sombra: en Android la `elevation` se pinta como un
 * recuadro negro mientras la fila se desvanece.
 */
export default function AddListItem({ item, onPress, added = false, inverted = false, scale = 1 }: ItemProps ) {
  const textColor = inverted ? COLORS.secondary : COLORS.primary;
  const size = (value: number) => Math.round(value * scale);

  return (
    <View
      style={{
        flexDirection: 'row',
        flex: 1,
        borderRadius: size(16),
        overflow: 'hidden',
        backgroundColor: inverted ? COLORS.primary : COLORS.secondary,
        borderWidth: 0,
      }}
    >
        <Image
          source={{ uri: item.imageUrl }}
          style={{ width: size(96), height: size(96) }}
        />
        <View style={{ flex: 1, alignSelf: 'center', alignItems: 'center', paddingHorizontal: size(16) }}>
          <Text style={{ fontSize: size(14), lineHeight: size(24), color: textColor, textAlign: 'center', fontWeight: '400' }}>{item.info}</Text>
        </View>
        <Touchable
          onPress={onPress}
          disabled={added}
          style={{ padding: size(16), alignItems: 'center', justifyContent: 'center' }}
        >
          <AddButton size={size(36)} color={textColor} checked={added} />
        </Touchable>
    </View>
  );
}
