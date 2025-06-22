// components/VerticalButtonsCarousel.tsx
import React from 'react';
import { View, Text, TouchableOpacity, FlatList, Image, StyleSheet } from 'react-native';

type ButtonItem = {
  id: string;
  label: string;
  imgUrl: string; // Puedes usar una URL o nombre de icono si usas vector icons
  onPress: () => void;
};

type Props = {
  title?: string;
  data: ButtonItem[];
};

export default function VerticalButtonsCarousel({ title, data }: Props) {
  const renderItem = ({ item }: { item: ButtonItem }) => (
    <TouchableOpacity
      className="mb-3 rounded-xl"
      onPress={item.onPress}
    >
      <Image
        source={{ uri: item.imgUrl }}
        className="w-[158px] h-[158px]"
        resizeMode="cover"
        borderRadius={12}
      />
    </TouchableOpacity>
  );

  return (
    <View className="mb-6">
      <FlatList
        data={data}
        renderItem={renderItem}
        keyExtractor={(item) => item.id}
        numColumns={2}
        scrollEnabled={false} // si está dentro de un ScrollView externo
        columnWrapperStyle={{ columnGap: 10 }}
      />
    </View>
  );
}
