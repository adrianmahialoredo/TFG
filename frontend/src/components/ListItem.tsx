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
};

export default function ListItem({ item, onPress }: ItemProps ) {
  return (
    <View className="bg-secondary rounded-2xl shadow-md">
      <TouchableOpacity onPress={onPress} className="flex-row flex-1">
        <Image
          source={{ uri: item.imageUrl }} // Placeholder image
          className="h-24 w-24 rounded-l-xl"
        />
        <View className="self-center items-center flex-1">
          <Text className="text-lg font-semibold text-primary justify-center text-center">{item.info}</Text>
        </View>
      </TouchableOpacity>
    </View>
  );
}