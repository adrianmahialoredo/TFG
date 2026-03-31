import { Image, View, Text, FlatList, TouchableOpacity } from 'react-native';
import React from 'react';
import { useNavigation, NavigationProp } from '@react-navigation/native';
import { CarouselItemDTO, RootStackParamList } from '../../types';

type CarouselProps = {
  title: string;
  data: CarouselItemDTO[];
};

export default function SongsCarousel({ title, data }: CarouselProps) {
  const navigation = useNavigation<NavigationProp<RootStackParamList>>();
  
  const handleSongPress = (songId: string) => {
    navigation.navigate('SongScreen', { songId });
  };

  const renderItem = ({ item }: { item: CarouselItemDTO }) => (
    <View className="w-[72px] bg-surface rounded-xl">
      <TouchableOpacity onPress={() => handleSongPress(item.id)}>
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
      <Text className="text-artistName text-center text-[10px] leading-tight"
        numberOfLines={1}
        ellipsizeMode="tail"
        style={{ flexWrap: 'wrap' }}
      >
        {item.artist || 'Artista desconocido'}
      </Text>
    </View>
  );

  return (
    <View className="my-4">
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
        overScrollMode="never"
      />
    </View>
  );
}




