import AppLayout from "../components/AppLayout";
import ProfileHeader from "../components/ProfileHeader";
import { View, Text, ActivityIndicator } from "react-native";
import FadeScrollView from "../components/FadeScrollView";
import Animated, { FadeOut, LinearTransition } from "react-native-reanimated";
import { useRoute, RouteProp } from "@react-navigation/native";
import SearchBar from "../components/SearchBar";
import AddListItem from "../components/AddListItem";
import { RootStackParamList } from "../types";
import { useAddItemScreen } from "../hooks/useAddItemScreen";
import { itemToListItem } from "../utils/mappers.utils";

type AddItemScreenRouteProp = RouteProp<RootStackParamList, 'AddItemScreen'>;

export default function AddItemScreen() {
  const route = useRoute<AddItemScreenRouteProp>();
  const { itemListId } = route.params;

  // Nivel 6: Hook que orquesta recomendaciones, búsqueda y la acción de añadir
  const { items, query, setQuery, loading, searching, error, addItem, isAdded } =
    useAddItemScreen(itemListId);

  const sectionTitle = query.trim() ? 'Resultados' : 'Para ti';
  const isBusy = loading || searching;

  return (
    <AppLayout>
      <View className="bg-background flex-1 pt-2">
        {/* Icono de perfil y titulo */}
        <ProfileHeader title="Añadir a lista" />
        <FadeScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={{ paddingBottom: 120 }}
        >
          {/* Barra de búsqueda */}
          <View className="bg-background mt-1 mb-10">
            <SearchBar value={query} onChangeText={setQuery} />
          </View>

          <Text className="text-lg font-semibold text-secondary justify-center text-center mb-10">
            {sectionTitle}
          </Text>

          {isBusy ? (
            <View className="flex-1 justify-center items-center py-12">
              <ActivityIndicator size="large" color="#ffffff" />
            </View>
          ) : error ? (
            <View className="flex-1 justify-center items-center px-4 py-12">
              <Text className="text-red-500 text-center">{error}</Text>
            </View>
          ) : items.length > 0 ? (
            <View className="flex-column gap-3 mb-7">
              {items.map((item, index) => (
                // Al quitarse de la lista (ya añadido) se desvanece y el resto se desliza a su hueco
                <Animated.View key={item.id} exiting={FadeOut.duration(300)} layout={LinearTransition}>
                  <AddListItem
                    item={itemToListItem(item)}
                    added={isAdded(item.id)}
                    inverted={index % 2 !== 0}
                    onPress={() => addItem(item.id)}
                  />
                </Animated.View>
              ))}
            </View>
          ) : (
            <View className="items-center py-12">
              <Text className="text-secondary opacity-50 text-base text-center">
                {query.trim()
                  ? 'No se encontraron items para tu búsqueda'
                  : 'No hay recomendaciones disponibles'}
              </Text>
            </View>
          )}
        </FadeScrollView>
      </View>
    </AppLayout>
  );
}