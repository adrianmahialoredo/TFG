import { FlatList, Image, Text, View } from 'react-native';
import Touchable from '../Touchable';
import { Search } from 'lucide-react-native';
import { COLORS } from '../../constants/Theme';
import { CarouselItemDTO } from '../../types';
import AddButton from '../iconbuttons/AddButton';

type ListCarouselProps = {
  data: CarouselItemDTO[];
  onItemPress?: (itemId: string) => void;
  onEmptyPress?: () => void;
  /** Con más de MAX_VISIBLE_LISTS listas, el botón final es una lupa que abre todas. */
  onSeeAllPress?: () => void;
};

type ListRowItem =
  | { type: 'list'; value: CarouselItemDTO }
  | { type: 'add' }
  | { type: 'seeAll' };

const ITEM_SIZE = 72;
const ADD_ICON_SIZE = Math.round(ITEM_SIZE * 0.8);
/** Listas que se ven en el carrusel; si hay más, el + se cambia por la lupa. */
export const MAX_VISIBLE_LISTS = 6;

/**
 * Carrusel de las listas del usuario. Al final, un botón + para crear una lista o, si hay
 * más de MAX_VISIBLE_LISTS, una lupa para verlas todas (y crear desde allí).
 */
export default function ListCarousel({ data, onItemPress, onEmptyPress, onSeeAllPress }: ListCarouselProps) {
  const lists = data ?? [];
  const tooMany = lists.length > MAX_VISIBLE_LISTS;
  const rows: ListRowItem[] = [
    ...(tooMany ? lists.slice(0, MAX_VISIBLE_LISTS) : lists).map((item) => ({ type: 'list' as const, value: item })),
    { type: tooMany ? 'seeAll' : 'add' },
  ];

  const renderItem = ({ item }: { item: ListRowItem }) => {
    if (item.type !== 'list') {
      const seeAll = item.type === 'seeAll';
      return (
        <View className="bg-surface rounded-xl" style={{ width: ITEM_SIZE }}>
          <Touchable
            onPress={seeAll ? onSeeAllPress : onEmptyPress}
            accessibilityRole="button"
            accessibilityLabel={seeAll ? 'Ver todas las listas' : 'Crear lista'}
            style={{ width: ITEM_SIZE, height: ITEM_SIZE, borderRadius: 6, alignItems: 'center', justifyContent: 'center' }}
          >
            {seeAll ? (
              <View
                style={{
                  width: ADD_ICON_SIZE, height: ADD_ICON_SIZE, borderWidth: 1.2, borderColor: COLORS.primary,
                  borderRadius: Math.round(ADD_ICON_SIZE * 0.26), alignItems: 'center', justifyContent: 'center',
                }}
              >
                <Search size={Math.round(ADD_ICON_SIZE * 0.5)} color={COLORS.primary} strokeWidth={1.2} />
              </View>
            ) : (
              <AddButton size={ADD_ICON_SIZE} color={COLORS.primary} strokeWidth={1.2} />
            )}
          </Touchable>
        </View>
      );
    }

    return (
      <View className="bg-surface rounded-xl" style={{ width: ITEM_SIZE }}>
        <Touchable onPress={() => onItemPress?.(item.value.id)}>
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
        </Touchable>
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
        keyExtractor={(item) => (item.type === 'list' ? item.value.id : `__list_${item.type}__`)}
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