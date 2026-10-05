import React from 'react';
import { View, Text, TouchableOpacity, FlatList, Image, StyleSheet } from 'react-native';

type GenreButton = {
  id: string;
  imageUrl: string;
  onPress: () => void;
};

export default function VerticalButtonsCarousel({ data }: { data: GenreButton[] }) {
  const renderItem = ({ item }: { item: GenreButton }) => (
    <TouchableOpacity
      className="mb-3 rounded-xl"
      onPress={item.onPress}
    >
      <Image
        source={{ uri: item.imageUrl }}
        className="w-[170px] h-[170px]"
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
        columnWrapperStyle={{ columnGap: 9 }}
      />
    </View>
  );
}
