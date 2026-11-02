import { View, Text, Image } from "react-native";
import Touchable from "./Touchable";
import { X } from "lucide-react-native";
import AddButton from "./iconbuttons/AddButton";
import { COLORS } from "../constants/Theme";

/** Tamaño de las filas de una lista (ListScreen), de las que se añaden a ella (AddItemScreen) y de los resultados del buscador. */
export const LIST_ITEM_SCALE = 0.85;

type ItemProps = {
  item: {
    id: string;
    title: string;
    description: string;
    imageUrl: string;
    info: string; // Additional information to display
  };
  onPress: () => void;
  /** Si se pasa, muestra un botón para quitar el item (p.ej. de una lista de estudio). */
  onRemove?: () => void;
  /** Si se pasa, muestra un botón + para añadir el item a una lista (p.ej. en los resultados de búsqueda). */
  onAdd?: () => void;
  inverted?: boolean;
  /** Factor de tamaño de la fila y todo su contenido (p.ej. LIST_ITEM_SCALE en ListScreen). */
  scale?: number;
};

/**
 * Fila de un item de una lista (ListScreen). Estilos en línea y sin className, como AddListItem:
 * `inverted` cambia cuando se quita un item de encima y NativeWind a veces no aplicaba el cambio
 * de clases (fila sin fondo o con colores mezclados).
 */
export default function ListItem({ item, onPress, onRemove, onAdd, inverted = false, scale = 1 }: ItemProps ) {
  const textColor = inverted ? COLORS.secondary : COLORS.primary;
  const size = (value: number) => Math.round(value * scale);

  return (
    <View
      style={{
        flexDirection: 'row',
        borderRadius: size(16),
        overflow: 'hidden',
        backgroundColor: inverted ? COLORS.primary : COLORS.secondary,
        borderWidth: 0,
      }}
    >
      <Touchable onPress={onPress} style={{ flexDirection: 'row', flex: 1 }}>
        <Image
          source={{ uri: item.imageUrl }}
          style={{ width: size(96), height: size(96) }}
        />
        <View style={{ flex: 1, alignSelf: 'center', alignItems: 'center', paddingHorizontal: size(12) }}>
          <Text style={{ fontSize: size(16), lineHeight: size(28), fontWeight: '400', color: textColor, textAlign: 'center' }}>
            {item.info}
          </Text>
        </View>
      </Touchable>
      {onAdd && (
        <Touchable
          onPress={onAdd}
          accessibilityRole="button"
          accessibilityLabel={`Añadir ${item.title} a una lista`}
          hitSlop={8}
          style={{ justifyContent: 'center', paddingHorizontal: size(14) }}
        >
          <AddButton size={size(30)} color={textColor} />
        </Touchable>
      )}
      {onRemove && (
        <Touchable
          onPress={onRemove}
          accessibilityRole="button"
          accessibilityLabel={`Quitar ${item.title} de la lista`}
          hitSlop={8}
          style={{ justifyContent: 'center', paddingHorizontal: size(10) }}
        >
          <X size={size(22)} color={textColor} />
        </Touchable>
      )}
    </View>
  );
}
