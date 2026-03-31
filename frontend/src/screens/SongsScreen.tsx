import { ScrollView, View, TouchableOpacity, Image, Text, ActivityIndicator } from "react-native";
import AppLayout from "../components/AppLayout";
import { useState, useEffect } from "react";
import SearchBar from "../components/SearchBar";
import HorizontalSongsCarousel from "../components/carousels/SongsCarousel";
import ProfileHeader from "../components/ProfileHeader";
import { getPopularSongIds, getSongsByIds } from "../services";
import * as songStorageService from "../local/song.local";
import { SongDTO } from "../types";

export default function SongScreen() {
  const [query, setQuery] = useState('');
  const [popularSongs, setPopularSongs] = useState<SongDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadPopularSongs();
  }, []);

  const loadPopularSongs = async () => {
    try {
      setLoading(true);
      
      // 1. Obtener los IDs de las canciones populares
      const popularSongIds = await getPopularSongIds(10); // Obtener máximo 10 canciones populares
      
      // 2. Comprobar cuáles están en cache y cuáles faltan
      const { cached, missing } = await songStorageService.getCachedSongs(popularSongIds);
      
      let allSongs = [...cached];
      
      // 3. Si hay canciones que no están en caché, obtenerlas del backend
      if (missing.length > 0) {
        const fetchedSongs = await getSongsByIds(missing);
        
        // 4. Guardar las canciones obtenidas en caché
        await songStorageService.cacheSongs(fetchedSongs);
        
        // Combinar las canciones (cached + nuevas)
        allSongs = [...cached, ...fetchedSongs];
      }
      
      setPopularSongs(allSongs);
    } catch (error) {
      console.error('Error loading popular songs:', error);
    } finally {
      setLoading(false);
    }
  };

  const songRecommendations = popularSongs.map(song => ({
    id: String(song.id),
    title: song.title || 'Canción sin título',
    artist: song.artist || 'Artista desconocido',
    imgUrl: song.imageUrl || 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png'
  }));

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
        ) : (
          <ScrollView
            showsVerticalScrollIndicator={false}
            contentContainerStyle={{ paddingBottom: 120 }}
          >
            {/* Carrusel de canciones populares */}
            {songRecommendations.length > 0 && (
              <HorizontalSongsCarousel title="Populares" data={songRecommendations} />
            )}

            {/* Carrusel de canciones nuevas */}
            <HorizontalSongsCarousel title="Nuevas" data={songRecommendations} />

            {/* Carrusel de canciones para ti */}
            <HorizontalSongsCarousel title="Para ti" data={songRecommendations} />

            {/* Carrusel de canciones populares */}
            <HorizontalSongsCarousel title="Populares" data={songRecommendations} />

            {/* Carrusel de canciones nuevas */}
            <HorizontalSongsCarousel title="Nuevas" data={songRecommendations} />

            {/* Carrusel de canciones para ti */}
            <HorizontalSongsCarousel title="Para ti" data={songRecommendations} />
          </ScrollView>
        )}
      </View>
    </AppLayout>
  );
}

