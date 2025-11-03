import { Image, View, Text, FlatList, TouchableOpacity } from 'react-native';
import React from 'react';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';

type Item = {
  id: string;
  title: string;
  imgUrl: string;
};

type CarouselProps = {
  data: Item[];
};

export default function ListCarousel({ data }: CarouselProps) {

  const renderItem = ({ item }: { item: Item }) => (
    <View className="w-[72px] bg-surface rounded-xl">
      <TouchableOpacity>
        <Image
          source={{ uri: item.imgUrl }}
          className="w-[72px] h-[72px] mb-1"
          resizeMode="cover"
          borderRadius={6}
        />
      </TouchableOpacity>
      <Text className="text-songTitle text-center text-[10px] leading-tight"
        numberOfLines={2}
        ellipsizeMode="tail"
        style={{ flexWrap: 'wrap' }}
      >
        {item.title}
      </Text>
    </View>
  );

  return (
    <View className="my-4">
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