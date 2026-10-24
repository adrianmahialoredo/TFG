import { TouchableOpacity, View, Text, Image } from "react-native";
import { X } from "lucide-react-native";
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
  inverted?: boolean;
};

export default function ListItem({ item, onPress, onRemove, inverted = false }: ItemProps ) {
  const containerClassName = inverted
    ? 'flex-row bg-primary border border-secondary rounded-2xl shadow-md'
    : 'flex-row bg-secondary rounded-2xl shadow-md';
  const textClassName = inverted
    ? 'text-lg font-semibold text-secondary justify-center text-center'
    : 'text-lg font-semibold text-primary justify-center text-center';

  return (
    <View className={containerClassName}>
      <TouchableOpacity onPress={onPress} className="flex-row flex-1">
        <Image
          source={{ uri: item.imageUrl }} // Placeholder image
          className="h-24 w-24 rounded-l-xl"
        />
        <View className="self-center items-center flex-1">
          <Text className={textClassName}>{item.info}</Text>
        </View>
      </TouchableOpacity>
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
          <X size={22} color={inverted ? COLORS.secondary : COLORS.primary} />
        </TouchableOpacity>
      )}
    </View>
  );
}
