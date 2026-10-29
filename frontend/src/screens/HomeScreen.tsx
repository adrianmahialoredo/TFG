// src/screens/HomeScreen.tsx

import { View, Text, TouchableOpacity, ActivityIndicator } from 'react-native';
import FadeScrollView from '../components/FadeScrollView';
import { useNavigation, NavigationProp } from '@react-navigation/native';
import { useCallback } from 'react';
import { Activity, CircleCheckBig, Headphones } from 'lucide-react-native';
import HorizontalItemsCarousel from '../components/carousels/HorizontalItemsCarousel';
import AppLayout from '../components/AppLayout';
import { ToolButton } from '../components/buttons/ToolButton';
import { RootStackParamList } from '../types';
import { useAuth } from '../context/AuthContext';
import { useHomeItems } from '../hooks/useHomeItems';
import { navigateToItem } from '../utils/navigation.utils';
import UserAvatar from "../components/UserAvatar";
import SuggestionCard from "../components/SuggestionCard";
import { SpotifyButton } from "../components/buttons/SpotifyButton";

const SPOTIFY_TITLE = 'Desde tu Spotify';

export default function HomeScreen() {
  const navigation = useNavigation<NavigationProp<RootStackParamList>>();

  // SSOT: el usuario actual sale de la AuthStore vía useAuth (ya no existe getStoredUser)
  const { user } = useAuth();

  // Nivel 6: hook que orquesta los repositorios y se suscribe a la Entity Store de items
  const {
    recentItems,
    recommendations,
    spotifyItems,
    spotifyConnected,
    favouriteCombination,
    mostUsedGenre,
    newFavouriteArtistSong,
    newFavouriteArtistSongs,
    popularFavouriteArtistSong,
    popularFavouriteArtistSongs,
    getItemById,
    loading,
    error,
  } = useHomeItems();

  // Resuelve el item completo (con su type) y navega a la pantalla de detalle adecuada
  const handleItemPress = useCallback(
    (itemId: string) => {
      const item = getItemById(itemId);
      if (item) navigateToItem(navigation, item);
    },
    [getItemById, navigation]
  );

  return (
    <AppLayout>
      <FadeScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingBottom: 120 }}
      >
        <View className="flex-1 bg-background">
          {/* Icono de perfil */}
          <View className="flex-row justify-center mt-2 mb-7">
            <TouchableOpacity onPress={() => navigation.navigate('ProfileScreen')}>
              <UserAvatar user={user} size={80} className="border" />
            </TouchableOpacity>
          </View>

          {/* Mensaje de bienvenida */}
          <Text className="text-3xl font-roboto font-bold text-primary text-center mb-11">
            Bienvenido, {user?.name}
          </Text>

          {/* Indicador de carga / error */}
          {loading && (
            <View className="py-6 items-center justify-center">
              <ActivityIndicator size="large" color="#ffffff" />
            </View>
          )}
          {error && !loading && (
            <Text className="text-red-500 text-center mb-6 px-4">{error}</Text>
          )}

          {/* Carrusel: Recientes (sin items, p.ej. la primera vez, muestra una card invitando a explorar) */}
          <HorizontalItemsCarousel
            title="Recientes"
            data={recentItems}
            onItemPress={handleItemPress}
            emptyMessage={loading ? undefined : 'Explora la app y empieza a aprender para que se muestren tus recientes'}
          />

          {/* Botones de herramientas */}
          <View className="flex-row justify-between mt-12 mb-11">
            <ToolButton
              icon={<Activity size={75} color="white" strokeWidth={1} />}
              label="Metrónomo"
              onPress={() => navigation.navigate('MetronomeScreen')}
            />
            <ToolButton
              icon={<CircleCheckBig size={75} color="white" strokeWidth={1} />}
              label="Afinador"
              onPress={() => navigation.navigate('TunerScreen')}
            />
            <ToolButton icon={<Headphones size={75} color="white" strokeWidth={1} />} label="Identificador" />
          </View>

          {/* Card: un género-instrumento que combina favoritos del usuario (solo si existe alguno).
              Sin imagen: las de los géneros-instrumento no existen, se muestra el icono del instrumento */}
          {favouriteCombination && (
            <View className="mb-6">
              <SuggestionCard
                label="Prueba esta combinación"
                title={favouriteCombination.title}
                onPress={() => navigateToItem(navigation, favouriteCombination)}
              />
            </View>
          )}

          {/* Carrusel: Recomendaciones (sin items muestra una card invitando a explorar) */}
          <HorizontalItemsCarousel
            title="Recomendaciones"
            data={recommendations}
            onItemPress={handleItemPress}
            emptyMessage={loading ? undefined : 'Explora la app y empieza a aprender para que se muestren tus recomendaciones'}
          />

          {/* Card: uno de los géneros más usados en las listas que aún no está en ellas (solo si hay alguno) */}
          {mostUsedGenre && (
              <View className="mb-10">
                <SuggestionCard
                    label="Explora este género"
                    title={mostUsedGenre.title}
                    imageUrl={mostUsedGenre.imageUrl}
                    onPress={() => navigateToItem(navigation, mostUsedGenre)}
                />
              </View>
          )}

          {/* Carrusel: Desde tu Spotify. Con la cuenta sin vincular se ofrece conectarla
              (mientras no se sabe, p. ej. usuario aún sin cargar, no se pinta nada) */}
          {spotifyConnected ? (
            <HorizontalItemsCarousel
              title={SPOTIFY_TITLE}
              data={spotifyItems}
              onItemPress={handleItemPress}
              emptyMessage={loading ? undefined : 'Escucha música en Spotify para que te recomendemos canciones, lecciones y géneros'}
            />
          ) : spotifyConnected === false && (
            <View className="my-4">
              <Text className="text-2xl font-semibold text-secondary mb-6">{SPOTIFY_TITLE}</Text>
              <SpotifyButton />
            </View>
          )}

          {/* Card: una canción nueva de uno de los artistas favoritos (los más repetidos en sus listas) */}
          {newFavouriteArtistSong && (
            <View className="mb-6">
              <SuggestionCard
                label={`Lo nuevo de ${newFavouriteArtistSong.artist ?? 'tus artistas favoritos'}`}
                title={newFavouriteArtistSong.title}
                imageUrl={newFavouriteArtistSong.imageUrl}
                onPress={() => navigateToItem(navigation, newFavouriteArtistSong)}
              />
            </View>
          )}

          {/* Carrusel: el resto de canciones nuevas de los artistas favoritos */}
          <HorizontalItemsCarousel
            title="Novedades de tus artistas"
            data={newFavouriteArtistSongs}
            onItemPress={handleItemPress}
            emptyMessage={loading ? undefined : 'Añade canciones a tus listas para descubrir las novedades de tus artistas favoritos'}
          />

          {/* Card: una canción popular de uno de los artistas favoritos */}
          {popularFavouriteArtistSong && (
            <View className="mb-6">
              <SuggestionCard
                label={`Lo más popular de ${popularFavouriteArtistSong.artist ?? 'tus artistas favoritos'}`}
                title={popularFavouriteArtistSong.title}
                imageUrl={popularFavouriteArtistSong.imageUrl}
                onPress={() => navigateToItem(navigation, popularFavouriteArtistSong)}
              />
            </View>
          )}

          {/* Carrusel: el resto de canciones populares de los artistas favoritos */}
          <HorizontalItemsCarousel
            title="Populares de tus artistas"
            data={popularFavouriteArtistSongs}
            onItemPress={handleItemPress}
            emptyMessage={loading ? undefined : 'Añade canciones a tus listas para ver los éxitos de tus artistas favoritos'}
          />
        </View>
      </FadeScrollView>
    </AppLayout>
  );
}
