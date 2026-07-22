import React from "react";
import { TouchableOpacity, View, Text, Image } from "react-native";
import { CirclePlus, CircleCheck } from "lucide-react-native";
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
    added?: boolean;
};

export default function AddListItem({ item, onPress, added = false }: ItemProps ) {
  return (
    <View
      className="flex-row flex-1 bg-secondary rounded-2xl shadow-md"
      style={{ opacity: added ? 0.5 : 1 }}
    >
        <Image
          source={{ uri: item.imageUrl }} // Placeholder image
          className="h-24 w-24 rounded-l-xl"
        />
        <View className="self-center items-center flex-1">
          <Text className="text-base text-primary justify-center text-center">{item.info}</Text>
        </View>
        <TouchableOpacity onPress={onPress} disabled={added} className="p-4 items-center justify-center">
          {added ? (
            <CircleCheck size={36} color={COLORS.primary} />
          ) : (
            <CirclePlus size={36} color={COLORS.primary} />
          )}
        </TouchableOpacity>
    </View>
  );
}