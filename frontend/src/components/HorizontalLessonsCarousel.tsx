import { Image, View, Text, FlatList, TouchableOpacity } from 'react-native';
import React from 'react';

type Item = {
  id: string;
  title: string;
  imgUrl: string; 
};

type CarouselProps = {
  title: string;
  data: Item[];
};

export default function HorizontalItemsCarousel({ title, data }: CarouselProps) {

  const renderItem = ({ item }: { item: Item }) => (
    <View className="w-[51px] bg-surface rounded-xl">
      <TouchableOpacity>
        <Image
          source={{ uri: item.imgUrl }}
          className="w-[51px] h-[93px] mb-1"
          resizeMode="cover"
          borderRadius={6}
        />
      </TouchableOpacity>
      <Text className="text-songTitle text-center text-[10px] leading-tight"
        numberOfLines={2}
        ellipsizeMode="tail"
        style={{ flexWrap: 'wrap' }}
      >
        Pop Punk · Ruedas
      </Text>
    </View>
  );

  return (
    <View className="my-2">
      <Text className="text-2xl font-semibold text-secondary mb-6">{title}</Text>
      <FlatList
        data={data}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        horizontal
        showsHorizontalScrollIndicator={false}
        ItemSeparatorComponent={() => <View style={{ width: 18 }} />}
        contentContainerStyle={{ paddingLeft: 0, paddingRight: 0 }}
        snapToStart={true}
        decelerationRate="fast"
        snapToAlignment="start"
        bounces={false}
        overScrollMode="never" // Para Android
      />
    </View>
  );
}
