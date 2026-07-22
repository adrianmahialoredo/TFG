import React, { useMemo } from 'react';
import { ScrollView, View, ActivityIndicator, Text } from 'react-native';
import SongsCarousel from '../components/carousels/SongsCarousel';
import { useGenreSongs } from '../hooks/useGenreSongs';

interface GenreSongsProps {
  genreId: string;
}

export default function GenreSongsScreen({ genreId }: GenreSongsProps) {
  const { populars, classics, news, loading, error } = useGenreSongs(genreId);

  // Filtramos categorías vacías para no ensuciar la UI
  const categories = useMemo(() => [
    { title: 'Populares', data: populars },
    { title: 'Clásicos del Género', data: classics },
    { title: 'Novedades', data: news },
  ].filter(cat => cat.data.length > 0), [populars, classics, news]);

  if (loading) {
    return (
        <View className="flex-1 justify-center items-center py-20">
          <ActivityIndicator size="large" color="#ffffff" />
        </View>
    );
  }

  if (error) {
    return (
        <View className="flex-1 justify-center items-center py-20 px-6">
          <Text className="text-red-500 text-center font-roboto">{error}</Text>
        </View>
    );
  }

  return (
      <ScrollView
          className="flex-1 mt-4"
          showsVerticalScrollIndicator={false}
          contentContainerStyle={{ paddingBottom: 120 }}
      >
        {categories.map((category) => (
            <View key={category.title} className="mb-8">
              <SongsCarousel
                  title={category.title}
                  data={category.data}
              />
            </View>
        ))}

        {categories.length === 0 && (
            <Text className="text-secondary text-center mt-10 font-roboto">
              Próximamente más canciones...
            </Text>
        )}
      </ScrollView>
  );
}