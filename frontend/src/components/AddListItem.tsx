import React from "react";
import { TouchableOpacity, View, Text, Image } from "react-native";
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
export default function AddListItem({ item, onPress, added = false, inverted = false }: ItemProps ) {
  const textColor = inverted ? COLORS.secondary : COLORS.primary;

  return (
    <View
      style={{
        flexDirection: 'row',
        flex: 1,
        borderRadius: 16,
        overflow: 'hidden',
        backgroundColor: inverted ? COLORS.primary : COLORS.secondary,
        borderWidth: 1,
        borderColor: COLORS.secondary,
      }}
    >
        <Image
          source={{ uri: item.imageUrl }}
          style={{ width: 96, height: 96 }}
        />
        <View style={{ flex: 1, alignSelf: 'center', alignItems: 'center' }}>
          <Text style={{ fontSize: 16, lineHeight: 24, color: textColor, textAlign: 'center' }}>{item.info}</Text>
        </View>
        <TouchableOpacity
          onPress={onPress}
          disabled={added}
          style={{ padding: 16, alignItems: 'center', justifyContent: 'center' }}
        >
          <AddButton size={36} color={textColor} checked={added} />
        </TouchableOpacity>
    </View>
  );
}
