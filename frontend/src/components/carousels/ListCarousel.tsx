import { FlatList, Image, Text, TouchableOpacity, View } from 'react-native';
import { COLORS } from '../../constants/Theme';
import { CarouselItemDTO } from '../../types';
import AddButton from '../iconbuttons/AddButton';

type ListCarouselProps = {
  data: CarouselItemDTO[];
  onItemPress?: (itemId: string) => void;
  onEmptyPress?: () => void;
};

type ListRowItem =
  | { type: 'list'; value: CarouselItemDTO }
  | { type: 'add' };

const ITEM_SIZE = 72;
const ADD_ICON_SIZE = Math.round(ITEM_SIZE * 0.8);

/**
 * Carrusel especializado para Listas
 * Usa GenericCarousel con tamaño medio y sin mostrar artista
 */
export default function ListCarousel({ data, onItemPress, onEmptyPress }: ListCarouselProps) {
  const rows: ListRowItem[] = [
    ...(data ?? []).map((item) => ({ type: 'list' as const, value: item })),
    { type: 'add' },
  ];

  const renderItem = ({ item }: { item: ListRowItem }) => {
    if (item.type === 'add') {
      return (
        <View className="bg-surface rounded-xl" style={{ width: ITEM_SIZE }}>
          <TouchableOpacity
            onPress={onEmptyPress}
            activeOpacity={0.8}
            className="items-center justify-center"
            style={{ width: ITEM_SIZE, height: ITEM_SIZE, borderRadius: 6 }}
          >
            <AddButton size={ADD_ICON_SIZE} color={COLORS.primary} strokeWidth={1.2} />
          </TouchableOpacity>
        </View>
      );
    }

    return (
      <View className="bg-surface rounded-xl" style={{ width: ITEM_SIZE }}>
        <TouchableOpacity onPress={() => onItemPress?.(item.value.id)}>
          {item.value.imgUrl ? (
            <Image
              source={{ uri: item.value.imgUrl }}
              className="mb-1"
              style={{ width: ITEM_SIZE, height: ITEM_SIZE, borderRadius: 6 }}
              resizeMode="cover"
            />
          ) : (
            <View
              className="mb-1 bg-secondary items-center justify-center"
              style={{ width: ITEM_SIZE, height: ITEM_SIZE, borderRadius: 6 }}
            >
              <Text className="text-primary font-bold" style={{ fontSize: 28 }}>
                {item.value.title?.charAt(0)?.toUpperCase() || '?'}
              </Text>
            </View>
          )}
        </TouchableOpacity>
        <Text
          className="text-songTitle text-center leading-tight"
          style={{ fontSize: 10 }}
          numberOfLines={2}
          ellipsizeMode="tail"
        >
          {item.value.title}
        </Text>
      </View>
    );
  };

  return (
    <View className="my-4">
      <FlatList
        data={rows}
        keyExtractor={(item) => (item.type === 'add' ? '__list_add__' : item.value.id)}
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