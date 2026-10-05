import { TouchableOpacity, View, Text, Image } from "react-native";

type ItemProps = {
  item: {
    id: string;
    title: string;
    description: string;
    imageUrl: string;
    info: string; // Additional information to display
  };
  onPress: () => void;
  inverted?: boolean;
};

export default function ListItem({ item, onPress, inverted = false }: ItemProps ) {
  const containerClassName = inverted
    ? 'bg-primary border border-secondary rounded-2xl shadow-md'
    : 'bg-secondary rounded-2xl shadow-md';
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
    </View>
  );
}