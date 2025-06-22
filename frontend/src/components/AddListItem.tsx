import React from "react";
import { TouchableOpacity, View, Text, Image } from "react-native";
import { CirclePlus} from "lucide-react-native";
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
};

export default function ListItem({ item, onPress }: ItemProps ) {
  return (
    <View className="flex-row flex-1 bg-secondary rounded-2xl shadow-md">
        <Image
          source={{ uri: item.imageUrl }} // Placeholder image
          className="h-24 w-24 rounded-l-xl"
        />
        <View className="self-center items-center flex-1">
          <Text className="text-base text-primary justify-center text-center">{item.info}</Text>
        </View>
        <TouchableOpacity onPress={onPress} className="p-4 items-center justify-center">
          <CirclePlus size={36} color={COLORS.primary} />
        </TouchableOpacity>
    </View>
  );
}