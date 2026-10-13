import { Image, View, Text, FlatList, TouchableOpacity } from 'react-native';
import React, { useMemo } from 'react';
import { useNavigation, NavigationProp } from '@react-navigation/native';
import { CarouselItemDTO, RootStackParamList } from '../../types';
import EmptyCarouselCard from './EmptyCarouselCard';

export type CarouselSize = 'small' | 'medium' | 'large';

type CarouselProps = {
  title: string;
  data: CarouselItemDTO[];
  size?: CarouselSize;
  onItemPress?: (itemId: string) => void;
  showArtist?: boolean;
  borderSquare?: boolean;
  /** Si se pasa, con `data` vacío se muestra una card con este texto en lugar de ocultar el carrusel */
  emptyMessage?: string;
};

interface SizeConfig {
  containerWidth: number;
  imageWidth: number;
  imageHeight: number;
  fontSize: number;
}

const CAROUSEL_SIZES: Record<CarouselSize, SizeConfig> = {
  small: {
    containerWidth: 51,
    imageWidth: 51,
    imageHeight: 93,
    fontSize: 10,
  },
  medium: {
    containerWidth: 72,
    imageWidth: 72,
    imageHeight: 72,
    fontSize: 10,
  },
  large: {
    containerWidth: 100,
    imageWidth: 100,
    imageHeight: 100,
    fontSize: 12,
  },
};

/**
 * Componente Carrusel Genérico Reutilizable
 * Acepta CarouselItemDTO[] y se adapta a diferentes tamaños
 * Soporta navegación automática a SongScreen o callback personalizado
 */
export default function GenericCarousel({
  title,
  data,
  size = 'medium',
  onItemPress,
  showArtist = true,
  borderSquare = false,
  emptyMessage,
}: CarouselProps) {
  const navigation = useNavigation<NavigationProp<RootStackParamList>>();
  const config = CAROUSEL_SIZES[size];

  const handleItemPress = (item: CarouselItemDTO) => {
    // Si hay callback personalizado, usarlo
    if (onItemPress) {
      onItemPress(item.id);
      return;
    }

    // Por defecto, navegar a SongScreen
    try {
      navigation.navigate('SongScreen', { songId: item.id });
    } catch (e) {
      console.warn('[GenericCarousel] Navigation not available');
    }
  };

  const renderItem = ({ item }: { item: CarouselItemDTO }) => (
    <View
      className="bg-surface rounded-xl"
      style={{ width: config.containerWidth }}
    >
      <TouchableOpacity onPress={() => handleItemPress(item)}>
        <Image
          source={{ uri: item.imgUrl }}
          className="mb-1"
          style={{
            width: config.imageWidth,
            height: config.imageHeight,
            borderRadius: borderSquare ? 0 : 6,
          }}
          resizeMode="cover"
        />
      </TouchableOpacity>
      <Text
        className="text-songTitle text-center leading-tight"
        style={{ fontSize: config.fontSize }}
        numberOfLines={2}
        ellipsizeMode="tail"
      >
        {item.title}
      </Text>
      {showArtist && item.artist && (
        <Text
          className="text-artistName text-center leading-tight"
          style={{ fontSize: config.fontSize }}
          numberOfLines={1}
          ellipsizeMode="tail"
        >
          {item.artist}
        </Text>
      )}
    </View>
  );

  // Si no hay datos: card con el mensaje (si lo hay) o no renderizar
  if (!data || data.length === 0) {
    if (!emptyMessage) return null;

    return (
      <View className="my-4">
        <Text className="text-2xl font-semibold text-secondary mb-6">{title}</Text>
        <EmptyCarouselCard message={emptyMessage} minHeight={config.imageHeight} />
      </View>
    );
  }

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

