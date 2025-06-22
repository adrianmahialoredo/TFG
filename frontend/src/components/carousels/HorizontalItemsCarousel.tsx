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
    <View className="w-[72px] bg-surface rounded-xl">
      <TouchableOpacity>
        <Image
          source={{ uri: item.imgUrl }}
          className="w-[72px] h-[72px] mb-1"
          resizeMode="cover"
        />
      </TouchableOpacity>
      <Text className="text-songTitle text-[8px] leading-tight"
        numberOfLines={1}
        ellipsizeMode="tail"
        style={{ flexWrap: 'wrap' }}
      >
        {item.title}
      </Text>
      <Text className="text-artistName text-[8px] leading-tight"
        numberOfLines={1}
        ellipsizeMode="tail"
        style={{ flexWrap: 'wrap' }}
      >
        Subtitulo aquí
      </Text>
    </View>
  );

  return (
    <View className="">
      <Text className="text-2xl font-semibold text-secondary mb-6">{title}</Text>
      <FlatList
        data={data}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        horizontal
        showsHorizontalScrollIndicator={false}
        ItemSeparatorComponent={() => <View style={{ width: 13 }} />}
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
