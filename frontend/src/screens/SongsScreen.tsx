import { ScrollView, View, Text, ActivityIndicator } from "react-native";
import AppLayout from "../components/AppLayout";
import { useState, useMemo } from "react";
import SearchBar from "../components/SearchBar";
import SongsCarousel from "../components/carousels/SongsCarousel";
import ProfileHeader from "../components/ProfileHeader";
import { SpotifyButton } from "../components/buttons/SpotifyButton";
import { useFeedSongs } from "../hooks/useFeedSongs";

const SPOTIFY_TITLE = 'Según tu Spotify';

export default function SongsScreen() {
  const [query, setQuery] = useState('');

  // Nivel 6: Hook que orquesta con los repositorios y se suscribe a las Entity Stores
  const { populars, news, recommendations, spotify, spotifyConnected, favouriteGenre, loading, error } = useFeedSongs();

  // Filtrado por query si hay búsqueda (useMemo para evitar recálculos innecesarios)
  const filteredCategories = useMemo(() => {
    const categories = [
      { title: 'Populares', data: populars },
      { title: 'Nuevas', data: news },
      ...(favouriteGenre
        ? [{ title: `Populares en ${favouriteGenre.title || 'tu género favorito'}`, data: favouriteGenre.data }]
        : []),
      { title: 'Según tus gustos', data: recommendations },
      { title: SPOTIFY_TITLE, data: spotify },
    ];

    if (!query) return categories;

    return categories.map(cat => ({
      ...cat,
      data: cat.data.filter(item => 
        item.title.toLowerCase().includes(query.toLowerCase())
      )
    })).filter(cat => cat.data.length > 0);
  }, [populars, news, recommendations, spotify, favouriteGenre, query]);

  return (
    <AppLayout>
      <View className="bg-background flex-1 pt-2">

        <ProfileHeader title="Canciones" />

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
                <View key={category.title} className="mb-6">
                  {category.data.length > 0 ? (
                    <SongsCarousel title={category.title} data={category.data} />
                  ) : category.title === SPOTIFY_TITLE && spotifyConnected === false && (
                    // Cuenta de Spotify sin vincular: se ofrece conectarla
                    <View className="my-4">
                      <Text className="text-2xl font-semibold text-secondary mb-6">{category.title}</Text>
                      <SpotifyButton />
                    </View>
                  )}
                </View>
              ))
            ) : (
              <View className="flex-1 justify-center items-center py-20">
                <Text className="text-secondary text-center">
                  {query ? 'No hay canciones que coincidan con tu búsqueda' : 'Cargando canciones...'}
                </Text>
              </View>
            )}

          </ScrollView>
        )}
      </View>
    </AppLayout>
  );
}

