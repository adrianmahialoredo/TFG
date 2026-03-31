import React, { useState, useEffect } from 'react';
import { ScrollView, TouchableOpacity, View, Text, Image, ActivityIndicator } from "react-native";
import { useRoute, RouteProp } from '@react-navigation/native';
import AppLayout from "../components/AppLayout";
import ListItem from "../components/ListItem";
import { CirclePlus } from "lucide-react-native";
import { COLORS } from "../constants/Theme";
import { RootStackParamList, ItemListDTO, ItemDTO } from '../types';
import * as itemListRepository from '../repositories/itemList.repository';

type ListScreenRouteProp = RouteProp<RootStackParamList, 'ListScreen'>;

interface ListScreenState {
  list: ItemListDTO | null;
  items: ItemDTO[];
  loading: boolean;
  error: string | null;
}

export default function ListScreen() {
  const route = useRoute<ListScreenRouteProp>();
  const { itemListId } = route.params;

  const [screenState, setScreenState] = useState<ListScreenState>({
    list: null,
    items: [],
    loading: true,
    error: null,
  });

  useEffect(() => {
    loadListData();
  }, [itemListId]);

  const loadListData = async () => {
    try {
      setScreenState(prev => ({ ...prev, loading: true, error: null }));

      // Obtener la lista con sus items (con cache/backend fallback)
      const { list, items } = await itemListRepository.getItemListWithItems(itemListId);

      if (!list) {
        setScreenState(prev => ({
          ...prev,
          loading: false,
          error: 'No se encontró la lista',
        }));
        return;
      }

      setScreenState({
        list,
        items,
        loading: false,
        error: null,
      });
    } catch (error) {
      console.error('Error loading list screen data:', error);
      setScreenState(prev => ({
        ...prev,
        loading: false,
        error: 'Error al cargar la lista',
      }));
    }
  };

  if (screenState.loading) {
    return (
      <AppLayout>
        <View className="bg-background flex-1 justify-center items-center">
          <ActivityIndicator size="large" color="#ffffff" />
        </View>
      </AppLayout>
    );
  }

  if (screenState.error || !screenState.list) {
    return (
      <AppLayout>
        <View className="bg-background flex-1 justify-center items-center">
          <Text className="text-secondary text-lg">{screenState.error || 'Lista no encontrada'}</Text>
        </View>
      </AppLayout>
    );
  }

  return (
    <AppLayout>
      <View className="bg-background flex-1 pt-2">
        <View className="flex-row justify-left items-center mt-6 mb-7">
          <TouchableOpacity>
            <Image
              source={{ uri: 'https://i.pravatar.cc/100' }}
              className="w-14 h-14 rounded-full border"
            />
          </TouchableOpacity>
          <Text className="text-[24px] font-roboto font-bold text-primary text-center ml-3">Listas</Text>
        </View>
        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={{ paddingBottom: 120 }}
        >
          {/* Información de la lista */}
          <View className="items-center mt-5 mb-10">
            {screenState.list.imageUrl && (
              <Image
                source={{ uri: screenState.list.imageUrl }}
                className="w-24 h-24 rounded-lg mb-2"
                resizeMode="cover"
              />
            )}
            <Text className="text-secondary text-base text-center">{screenState.list.name}</Text>
          </View>

          {/* Items de la lista */}
          <View className="flex-column gap-3 mb-7">
            {screenState.items.length > 0 ? (
              screenState.items.map((item) => (
                <ListItem
                  key={item.id}
                  item={{
                    id: item.id,
                    title: item.name || 'Sin nombre',
                    description: '',
                    imageUrl: item.imageUrl,
                    info: `${item.type}${item.level ? ` - Nivel ${item.level}` : ''}`,
                  }}
                  onPress={() => console.log(`Selected item: ${item.name}`)}
                />
              ))
            ) : (
              <View className="items-center py-8">
                <Text className="text-secondary text-center">No hay items en esta lista</Text>
              </View>
            )}

            {/* Botón para añadir a la lista */}
            <TouchableOpacity className="flex-row h-24 bg-secondary rounded-2xl shadow-md p-4 items-center justify-center gap-2">
              <Text className="text-2xl font-semibold text-primary text-center">Añadir a la lista</Text>
              <CirclePlus size={28} color={COLORS.primary} />
            </TouchableOpacity>
          </View>
        </ScrollView>
      </View>
    </AppLayout>
  );
}

      if (!list) {
        setScreenState(prev => ({
          ...prev,
          loading: false,
          error: 'No se encontró la lista',
        }));
        return;
      }

      setScreenState({
        list,
        items,
        loading: false,
        error: null,
      });
    } catch (error) {
      console.error('Error loading list screen data:', error);
      setScreenState(prev => ({
        ...prev,
        loading: false,
        error: 'Error al cargar la lista',
      }));
    }
  };

  if (screenState.loading) {
    return (
      <AppLayout>
        <View className="bg-background flex-1 justify-center items-center">
          <ActivityIndicator size="large" color="#ffffff" />
        </View>
      </AppLayout>
    );
  }

  if (screenState.error || !screenState.list) {
    return (
      <AppLayout>
        <View className="bg-background flex-1 justify-center items-center">
          <Text className="text-secondary text-lg">{screenState.error || 'Lista no encontrada'}</Text>
        </View>
      </AppLayout>
    );
  }

  return (
    <AppLayout>
      <View className="bg-background flex-1 pt-2">
        <View className="flex-row justify-left items-center mt-6 mb-7">
          <TouchableOpacity>
            <Image
              source={{ uri: 'https://i.pravatar.cc/100' }}
              className="w-14 h-14 rounded-full border"
            />
          </TouchableOpacity>
          <Text className="text-[24px] font-roboto font-bold text-primary text-center ml-3">Listas</Text>
        </View>
        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={{ paddingBottom: 120 }}
        >
          {/* Información de la lista */}
          <View className="items-center mt-5 mb-10">
            {screenState.list.imageUrl && (
              <Image
                source={{ uri: screenState.list.imageUrl }}
                className="w-24 h-24 rounded-lg mb-2"
                resizeMode="cover"
              />
            )}
            <Text className="text-secondary text-base text-center">{screenState.list.name}</Text>
          </View>

          {/* Items de la lista */}
          <View className="flex-column gap-3 mb-7">
            {screenState.items.length > 0 ? (
              screenState.items.map((item) => (
                <ListItem
                  key={item.id}
                  item={{
                    id: item.id,
                    title: item.name || 'Sin nombre',
                    description: '',
                    imageUrl: item.imageUrl,
                    info: `${item.type}${item.level ? ` - Nivel ${item.level}` : ''}`,
                  }}
                  onPress={() => console.log(`Selected item: ${item.name}`)}
                />
              ))
            ) : (
              <View className="items-center py-8">
                <Text className="text-secondary text-center">No hay items en esta lista</Text>
              </View>
            )}

            {/* Botón para añadir a la lista */}
            <TouchableOpacity className="flex-row h-24 bg-secondary rounded-2xl shadow-md p-4 items-center justify-center gap-2">
              <Text className="text-2xl font-semibold text-primary text-center">Añadir a la lista</Text>
              <CirclePlus size={28} color={COLORS.primary} />
            </TouchableOpacity>
          </View>
        </ScrollView>
      </View>
    </AppLayout>
  );
}