import React from "react";
import { TouchableOpacity, View, Text, Image } from "react-native";
import { CircleCheck } from "lucide-react-native";
import { COLORS } from "../constants/Theme";
import RoundedSquarePlus from "./icons/RoundedSquarePlus";

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

export default function AddListItem({ item, onPress, added = false, inverted = false }: ItemProps ) {
  const cardClassName = inverted
    ? 'flex-row flex-1 bg-primary border border-secondary rounded-2xl shadow-md'
    : 'flex-row flex-1 bg-secondary rounded-2xl shadow-md';
  const infoClassName = inverted
    ? 'text-base text-secondary justify-center text-center'
    : 'text-base text-primary justify-center text-center';
  const iconColor = inverted ? COLORS.secondary : COLORS.primary;

  return (
    <View
      className={cardClassName}
      style={{ opacity: added ? 0.5 : 1 }}
    >
        <Image
          source={{ uri: item.imageUrl }} // Placeholder image
          className="h-24 w-24 rounded-l-xl"
        />
        <View className="self-center items-center flex-1">
          <Text className={infoClassName}>{item.info}</Text>
        </View>
        <TouchableOpacity onPress={onPress} disabled={added} className="p-4 items-center justify-center">
          {added ? (
            <CircleCheck size={36} color={iconColor} />
          ) : (
            <RoundedSquarePlus size={36} color={iconColor} />
          )}
        </TouchableOpacity>
    </View>
  );
}