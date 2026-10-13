import { ScrollView, View, Text, ActivityIndicator } from "react-native";
import AppLayout from "../components/AppLayout";
import { useState, useMemo } from "react";
import SearchBar from "../components/SearchBar";
import GeneralLessonsCarousel from "../components/carousels/GeneralLessonsCarousel";
import ProfileHeader from "../components/ProfileHeader";
import { useHomeLessons } from "../hooks/useHomeLessons";

export default function LearnScreen() {
  const [query, setQuery] = useState('');

  // Nivel 6: Hook que orquesta con los repositorios y se suscribe a la Entity Store
  const { populars, news, recommendations, favouriteInstrument, loading, error } = useHomeLessons();

  // Filtrado por query si hay búsqueda (useMemo para evitar recálculos innecesarios)
  const filteredCategories = useMemo(() => {
    const categories = [
      { title: 'Populares', data: populars },
      { title: 'Nuevas', data: news },
      { title: 'Para ti', data: recommendations },
      // Solo si el usuario tiene algún instrumento favorito (y ya se conoce su nombre)
      ...(favouriteInstrument?.title
        ? [{ title: `Para ${favouriteInstrument.title}`, data: favouriteInstrument.data }]
        : []),
    ];

    if (!query) return categories;

    return categories.map(cat => ({
      ...cat,
      data: cat.data.filter(item =>
        item.title.toLowerCase().includes(query.toLowerCase())
      )
    })).filter(cat => cat.data.length > 0);
  }, [populars, news, recommendations, favouriteInstrument, query]);

  return (
    <AppLayout>
      <View className="bg-background flex-1 pt-2">

        <ProfileHeader title="Aprender" />

        {/* Barra de búsqueda */}
        <View className="bg-background mt-3 mb-4">
          <SearchBar value={query} onChangeText={setQuery} />
        </View>

        {loading ? (
          <View className="flex-1 justify-center items-center">
            <ActivityIndicator size="large" color="#ffffff" />
          </View>
        ) : error ? (
          <View className="flex-1 justify-center items-center px-4">
            <Text className="text-red-500 text-center">{error}</Text>
          </View>
        ) : (
          <ScrollView
            showsVerticalScrollIndicator={false}
            contentContainerStyle={{ paddingBottom: 120 }}
          >
            {filteredCategories.length > 0 ? (
              filteredCategories.map((category) => (
                <View key={category.title} className="mb-8">
                  {category.data.length > 0 && (
                    <GeneralLessonsCarousel title={category.title} data={category.data} />
                  )}
                </View>
              ))
            ) : (
              <View className="flex-1 justify-center items-center py-20">
                <Text className="text-secondary text-center">
                  {query ? 'No hay lecciones que coincidan con tu búsqueda' : 'Cargando lecciones...'}
                </Text>
              </View>
            )}

          </ScrollView>
        )}
      </View>
    </AppLayout>
  );
}