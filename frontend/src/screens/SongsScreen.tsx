import { View, Text, ActivityIndicator } from "react-native";
import FadeScrollView from "../components/FadeScrollView";
import AppLayout from "../components/AppLayout";
import { useState, useMemo } from "react";
import SearchBar from "../components/SearchBar";
import SongsCarousel from "../components/carousels/SongsCarousel";
import ProfileHeader from "../components/ProfileHeader";
import { SpotifyButton } from "../components/buttons/SpotifyButton";
import { useFeedSongs } from "../hooks/useFeedSongs";
import { useCatalogSearch } from "../hooks/useCatalogSearch";
import SearchResults from "../components/SearchResults";
import { ItemType } from "../types";
import { SearchField } from "../utils/search.utils";

const SPOTIFY_TITLE = 'Según tu Spotify';

// Campos por los que busca el buscador, en orden de prioridad
const SEARCH_FIELDS: SearchField[] = ['title', 'artist', 'genre', 'instrument', 'decade'];
const SEARCH_PLACEHOLDER = 'Busca por título, artista, género, instrumento o década';

export default function SongsScreen() {
  const [query, setQuery] = useState('');

  // Nivel 6: Hook que orquesta con los repositorios y se suscribe a las Entity Stores
  const { populars, news, recommendations, spotify, spotifyConnected, favouriteGenre, loading, error } = useFeedSongs();

  // Con búsqueda se muestran los resultados de todo el catálogo en vez de los carruseles
  const searchResults = useCatalogSearch(query, ItemType.SONG, SEARCH_FIELDS);

  const categories = useMemo(() => [
    { title: 'Populares', data: populars },
    { title: 'Nuevas', data: news },
    ...(favouriteGenre
      ? [{ title: `Populares en ${favouriteGenre.title || 'tu género favorito'}`, data: favouriteGenre.data }]
      : []),
    { title: 'Según tus gustos', data: recommendations },
    { title: SPOTIFY_TITLE, data: spotify },
  ], [populars, news, recommendations, spotify, favouriteGenre]);

  return (
    <AppLayout>
      <View className="bg-background flex-1">

        <ProfileHeader title="Canciones" />

        {/* Un solo scroll con el buscador dentro: si cambiase al escribir, la barra se desmontaría y perdería el foco */}
        <FadeScrollView
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
          contentContainerStyle={{ paddingBottom: 120 }}
        >
          {/* Barra de búsqueda */}
          <View className="bg-background mt-1 mb-4">
            <SearchBar value={query} onChangeText={setQuery} placeholder={SEARCH_PLACEHOLDER} />
          </View>

          {query.trim() ? (
            <SearchResults sections={searchResults} emptyText="No hay canciones que coincidan con tu búsqueda" />
          ) : loading ? (
            <View className="justify-center items-center py-20">
              <ActivityIndicator size="large" color="#ffffff" />
            </View>
          ) : error ? (
            <View className="justify-center items-center px-4 py-20">
              <Text className="text-red-500 text-center">{error}</Text>
            </View>
          ) : (
            <>
              {/* Todas las categorías vacías (ya cargadas): sin esto la pantalla quedaba en blanco */}
              {!categories.some((category) => category.data.length > 0) && (
                <View className="justify-center items-center py-20">
                  <Text className="text-secondary text-center">
                    Todavía no hay canciones disponibles
                  </Text>
                </View>
              )}
              {categories.map((category) => (
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
              ))}
            </>
          )}
        </FadeScrollView>
      </View>
    </AppLayout>
  );
}

