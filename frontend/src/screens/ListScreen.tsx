import React from 'react';
import { ScrollView, TouchableOpacity, View, Text, Image, ActivityIndicator} from "react-native";
import {useRoute, RouteProp, useNavigation, NavigationProp, useFocusEffect} from '@react-navigation/native';
import { CirclePlus } from "lucide-react-native";
import AppLayout from "../components/AppLayout";
import ProfileHeader from "../components/ProfileHeader";
import ListItem from "../components/ListItem";
import { COLORS } from "../constants/Theme";
import { RootStackParamList, AnyItemDTO, ItemType, SongDTO } from '../types';
import { useItemListDetail } from '../hooks/useItemListDetail';
import { navigateToItem } from '../utils/navigation.utils';

type ListScreenRouteProp = RouteProp<RootStackParamList, 'ListScreen'>;

export default function ListScreen() {
  const navigation = useNavigation<NavigationProp<RootStackParamList>>();
  const route = useRoute<ListScreenRouteProp>();
  const { itemListId } = route.params;

  const { list, items, loading, error, refresh } = useItemListDetail(itemListId);

  // Refresca la lista al volver a esta pantalla (p. ej. tras añadir un item),
  // omitiendo el primer foco porque el hook ya carga en el montaje.
  const didMountRef = React.useRef(false);
  useFocusEffect(
    React.useCallback(() => {
      if (!didMountRef.current) {
        didMountRef.current = true;
        return;
      }
      refresh();
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [itemListId])
  );

  /**
   * Determina qué texto mostrar como descripción según el tipo de item
   */
  const getItemDescription = (item: AnyItemDTO): string => {
    switch (item.type) {
      case ItemType.SONG:
        return (item as SongDTO).artist || 'Artista desconocido';
      case ItemType.LESSON:
        return 'Lección práctica';
      case ItemType.GENRE:
        return 'Explorar género';
      case ItemType.INSTRUMENT_GENRE:
        return 'Técnica de instrumento';
      default:
        return '';
    }
  };

  /**
   * Maneja la navegación según el tipo de item pulsado
   */
  const handlePress = (item: AnyItemDTO) => {
    navigateToItem(navigation, item);
  };

  // Estado de Carga
  if (loading) {
    return (
        <AppLayout>
          <View className="bg-background flex-1 justify-center items-center">
            <ActivityIndicator size="large" color={COLORS.primary} />
          </View>
        </AppLayout>
    );
  }

  // Estado de Error o Lista No Encontrada
  if (error || !list) {
    return (
        <AppLayout>
          <View className="bg-background flex-1 justify-center items-center px-4">
            <Text className="text-secondary text-lg text-center">
              {error || 'No se pudo encontrar la lista seleccionada'}
            </Text>
          </View>
        </AppLayout>
    );
  }

  return (
      <AppLayout>
        <View className="bg-background flex-1 pt-2">
          {/* Header con botón de volver atrás */}
          <ProfileHeader title="Listas" />

          <ScrollView
              showsVerticalScrollIndicator={false}
              contentContainerStyle={{ paddingBottom: 120 }}
          >
            {/* Cabecera con Info de la Lista */}
            <View className="items-center mt-6 mb-8 px-4">
              {list.imageUrl ? (
                  <Image
                      source={{ uri: list.imageUrl }}
                      className="w-40 h-40 rounded-3xl mb-4 shadow-lg"
                      resizeMode="cover"
                  />
              ) : (
                  <View className="w-40 h-40 bg-secondary rounded-3xl mb-4 items-center justify-center">
                    <Text className="text-primary text-4xl font-bold">
                      {list.name.charAt(0)}
                    </Text>
                  </View>
              )}
              <Text className="text-secondary text-2xl font-bold text-center">
                {list.name}
              </Text>
              {list.description && (
                  <Text className="text-secondary opacity-60 text-sm text-center mt-1">
                    {list.description}
                  </Text>
              )}
            </View>

            {/* Listado de Items Polimórficos */}
            <View className="flex-column gap-3 mb-7 px-4">
              {items.length > 0 ? (
                  items.map((item) => (
                      <ListItem
                          key={item.id}
                          item={{
                            id: item.id,
                            title: item.title,
                            description: getItemDescription(item),
                            imageUrl: item.imageUrl,
                            info: `${item.type}${item.level ? ` • Lvl ${item.level}` : ''}`,
                          }}
                          onPress={() => handlePress(item)}
                      />
                  ))
              ) : (
                  <View className="items-center py-12">
                    <Text className="text-secondary opacity-50 text-base">
                      Esta lista aún no tiene contenido
                    </Text>
                  </View>
              )}

              {/* Botón de Acción para añadir contenido */}
              <TouchableOpacity
                  className="flex-row h-20 bg-secondary rounded-2xl shadow-sm p-4 items-center justify-center gap-3 mt-4"
                  activeOpacity={0.8}
                  onPress={() => navigation.navigate('AddItemScreen', { itemListId })}
              >
                <Text className="text-xl font-semibold text-primary">
                  Añadir a la lista
                </Text>
                <CirclePlus size={26} color={COLORS.primary} />
              </TouchableOpacity>
            </View>
          </ScrollView>
        </View>
      </AppLayout>
  );
}