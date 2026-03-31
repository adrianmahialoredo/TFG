import { ScrollView, View } from "react-native";
import AppLayout from "../components/AppLayout";
import { useEffect, useState } from "react";
import SearchBar from "../components/SearchBar";
import VerticalButtonsCarousel from "../components/carousels/GenresCarousel";
import { GenreDTO } from "../types";
import * as genreRepository from "../repositories/genre.repository";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from "../types";
import ProfileHeader from "../components/ProfileHeader";

export default function GenresScreen() {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

  const [query, setQuery] = useState('');
  const [genres, setGenres] = useState<
    { id: string; imageUrl: string; onPress: () => void }[]
  >([]);

  useEffect(() => {
    const fetchGenres = async () => {
      try {
        // Obtener del repositorio (cache first, backend fallback automático)
        const genresFromRepository: GenreDTO[] = await genreRepository.getAllGenres();
        
        const formattedGenres = genresFromRepository.map((genre) => ({
          id: genre.id,
          imageUrl: genre.imageUrl,
          onPress: () => navigation.navigate('GenreMainScreen', { genreId: genre.id }),
        }));
        setGenres(formattedGenres);
      } catch (error) {
        console.error('Error al obtener géneros:', error);
      }
    };

    fetchGenres();
  }, []);

  return (
    <AppLayout>
      <View className = "bg-background flex-1 pt-2">
        {/* Icono de perfil y titulo */}
        <ProfileHeader title="Géneros" />

        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={{ paddingBottom: 120 }}
        >
          {/* Barra de búsqueda */}
          <View className="bg-background mt-3 mb-10">
            <SearchBar value={query} onChangeText={setQuery} />
            {/* Aquí puedes mostrar resultados filtrados */}
          </View>

          {/* Carrusel vertical de géneros */}
          <VerticalButtonsCarousel data={genres} />
        </ScrollView>
      </View>
    </AppLayout>
  )
}