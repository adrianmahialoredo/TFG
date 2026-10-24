import { View, Text, ActivityIndicator } from "react-native";
import FadeScrollView from "../components/FadeScrollView";
import AppLayout from "../components/AppLayout";
import { useState, useMemo } from "react";
import SearchBar from "../components/SearchBar";
import GeneralLessonsCarousel from "../components/carousels/GeneralLessonsCarousel";
import ProfileHeader from "../components/ProfileHeader";
import { useHomeLessons } from "../hooks/useHomeLessons";
import { useCatalogSearch } from "../hooks/useCatalogSearch";
import SearchResults from "../components/SearchResults";
import { ItemType } from "../types";
import { SearchField } from "../utils/search.utils";

// Campos por los que busca el buscador, en orden de prioridad
const SEARCH_FIELDS: SearchField[] = ['title', 'genre', 'instrument'];
const SEARCH_PLACEHOLDER = 'Busca por título, género o instrumento';

export default function LearnScreen() {
  const [query, setQuery] = useState('');

  // Nivel 6: Hook que orquesta con los repositorios y se suscribe a la Entity Store
  const { populars, news, recommendations, favouriteInstrument, loading, error } = useHomeLessons();

  // Con búsqueda se muestran los resultados de todo el catálogo en vez de los carruseles
  const searchResults = useCatalogSearch(query, ItemType.LESSON, SEARCH_FIELDS);

  const categories = useMemo(() => [
    { title: 'Populares', data: populars },
    { title: 'Nuevas', data: news },
    { title: 'Para ti', data: recommendations },
    // Solo si el usuario tiene algún instrumento favorito (y ya se conoce su nombre)
    ...(favouriteInstrument?.title
      ? [{ title: `Para ${favouriteInstrument.title}`, data: favouriteInstrument.data }]
      : []),
  ], [populars, news, recommendations, favouriteInstrument]);

  return (
    <AppLayout>
      <View className="bg-background flex-1 pt-2">

        <ProfileHeader title="Aprender" />

        {/* Barra de búsqueda */}
        <View className="bg-background mt-1 mb-4">
          <SearchBar value={query} onChangeText={setQuery} placeholder={SEARCH_PLACEHOLDER} />
        </View>

        {query.trim() ? (
          <FadeScrollView
            showsVerticalScrollIndicator={false}
            keyboardShouldPersistTaps="handled"
            contentContainerStyle={{ paddingBottom: 120 }}
          >
            <SearchResults sections={searchResults} emptyText="No hay lecciones que coincidan con tu búsqueda" />
          </FadeScrollView>
        ) : loading ? (
          <View className="flex-1 justify-center items-center">
            <ActivityIndicator size="large" color="#ffffff" />
          </View>
        ) : error ? (
          <View className="flex-1 justify-center items-center px-4">
            <Text className="text-red-500 text-center">{error}</Text>
          </View>
        ) : (
          <FadeScrollView
            showsVerticalScrollIndicator={false}
            contentContainerStyle={{ paddingBottom: 120 }}
          >
            {categories.some((category) => category.data.length > 0) ? (
              categories.map((category) => (
                <View key={category.title} className="mb-8">
                  {category.data.length > 0 && (
                    <GeneralLessonsCarousel title={category.title} data={category.data} />
                  )}
                </View>
              ))
            ) : (
              // Todas las categorías vacías (ya cargadas): sin esto la pantalla quedaba en blanco
              <View className="flex-1 justify-center items-center py-20">
                <Text className="text-secondary text-center">
                  Todavía no hay lecciones disponibles
                </Text>
              </View>
            )}

          </FadeScrollView>
        )}
      </View>
    </AppLayout>
  );
}