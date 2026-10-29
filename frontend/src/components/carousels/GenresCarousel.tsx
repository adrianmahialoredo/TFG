import React, { useState } from 'react';
import { View, TouchableOpacity, FlatList, Image, LayoutChangeEvent } from 'react-native';

type GenreButton = {
  id: string;
  imageUrl: string;
  onPress: () => void;
};

/** Separación entre botones, la misma en horizontal y en vertical. */
const GAP = 12;

export default function VerticalButtonsCarousel({ data }: { data: GenreButton[] }) {
  // Lado de cada botón: dos columnas que ocupan todo el ancho disponible (de margen a margen).
  // Se mide el contenedor porque el ancho depende de la pantalla y del padding del layout
  // (hasta medirlo, los botones miden 0 y no se ven).
  const [size, setSize] = useState(0);
  const handleLayout = (event: LayoutChangeEvent) => {
    const width = event.nativeEvent.layout.width;
    setSize(Math.floor((width - GAP) / 2));
  };

  const renderItem = ({ item }: { item: GenreButton }) => (
    // TouchableOpacity con estilo en línea y sin className (NativeWind 4.1):
    //  - TouchableOpacity + className pisa la opacidad animada al re-renderizar y el botón se quedaba "pulsado".
    //  - Pressable con `style={({ pressed }) => ...}` pierde el estilo (el interop no soporta la función).
    <TouchableOpacity
      onPress={item.onPress}
      activeOpacity={0.7}
      style={{ marginBottom: GAP, borderRadius: 12 }}
    >
      <Image
        source={{ uri: item.imageUrl }}
        style={{ width: size, height: size, borderRadius: 12 }}
        resizeMode="cover"
      />
    </TouchableOpacity>
  );

  return (
    <View className="mb-6" onLayout={handleLayout}>
      <FlatList
        data={data}
        renderItem={renderItem}
        keyExtractor={(item) => item.id}
        numColumns={2}
        scrollEnabled={false} // si está dentro de un ScrollView externo
        columnWrapperStyle={{ columnGap: GAP }}
        extraData={size} // re-pinta los botones al medir el ancho
      />
    </View>
  );
}
