import React from 'react';
import { View, TouchableOpacity, FlatList, Image } from 'react-native';

type GenreButton = {
  id: string;
  imageUrl: string;
  onPress: () => void;
};

export default function VerticalButtonsCarousel({ data }: { data: GenreButton[] }) {
  const renderItem = ({ item }: { item: GenreButton }) => (
    // TouchableOpacity con estilo en línea y sin className (NativeWind 4.1):
    //  - TouchableOpacity + className pisa la opacidad animada al re-renderizar y el botón se quedaba "pulsado".
    //  - Pressable con `style={({ pressed }) => ...}` pierde el estilo (el interop no soporta la función).
    <TouchableOpacity
      onPress={item.onPress}
      activeOpacity={0.7}
      style={{ marginBottom: 12, borderRadius: 12 }}
    >
      <Image
        source={{ uri: item.imageUrl }}
        style={{ width: 170, height: 170, borderRadius: 12 }}
        resizeMode="cover"
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
