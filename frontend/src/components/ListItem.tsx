import { TouchableOpacity, View, Text, Image } from "react-native";
import { X } from "lucide-react-native";
import AddButton from "./iconbuttons/AddButton";
import { COLORS } from "../constants/Theme";

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
};

/**
 * Fila de un item de una lista (ListScreen). Estilos en línea y sin className, como AddListItem:
 * `inverted` cambia cuando se quita un item de encima y NativeWind a veces no aplicaba el cambio
 * de clases (fila sin fondo o con colores mezclados).
 */
export default function ListItem({ item, onPress, onRemove, onAdd, inverted = false }: ItemProps ) {
  const textColor = inverted ? COLORS.secondary : COLORS.primary;

  return (
    <View
      style={{
        flexDirection: 'row',
        borderRadius: 16,
        overflow: 'hidden',
        backgroundColor: inverted ? COLORS.primary : COLORS.secondary,
        borderWidth: 1,
        borderColor: COLORS.secondary,
      }}
    >
      <TouchableOpacity onPress={onPress} style={{ flexDirection: 'row', flex: 1 }}>
        <Image
          source={{ uri: item.imageUrl }}
          style={{ width: 96, height: 96 }}
        />
        <View style={{ flex: 1, alignSelf: 'center', alignItems: 'center' }}>
          <Text style={{ fontSize: 18, lineHeight: 28, fontWeight: '600', color: textColor, textAlign: 'center' }}>
            {item.info}
          </Text>
        </View>
      </TouchableOpacity>
      {onAdd && (
        <TouchableOpacity
          onPress={onAdd}
          activeOpacity={0.6}
          accessibilityRole="button"
          accessibilityLabel={`Añadir ${item.title} a una lista`}
          hitSlop={8}
          style={{ justifyContent: 'center', paddingHorizontal: 14 }}
        >
          <AddButton size={30} color={textColor} />
        </TouchableOpacity>
      )}
      {onRemove && (
        // Estilos en línea: con className NativeWind pisa la opacidad animada del TouchableOpacity
        <TouchableOpacity
          onPress={onRemove}
          activeOpacity={0.6}
          accessibilityRole="button"
          accessibilityLabel={`Quitar ${item.title} de la lista`}
          hitSlop={8}
          style={{ justifyContent: 'center', paddingHorizontal: 14 }}
        >
          <X size={22} color={textColor} />
        </TouchableOpacity>
      )}
    </View>
  );
}
