import {ActivityIndicator, ScrollView, View} from "react-native";
import AppLayout from "../components/AppLayout";
import {useMemo, useState} from "react";
import SearchBar from "../components/SearchBar";
import VerticalButtonsCarousel from "../components/carousels/GenresCarousel";
import { useNavigation } from "@react-navigation/native";
import ProfileHeader from "../components/ProfileHeader";
import {useGenres} from "../hooks/useGenres";

export default function GenresScreen() {
  const navigation = useNavigation<any>();
  const [query, setQuery] = useState('');

  const { genres, loading } = useGenres(query);

  const formattedData = useMemo(() => genres.map(g => ({
    id: g.id,
    imageUrl: g.imageUrl,
    onPress: () => navigation.navigate('GenreMainScreen', { genreId: g.id })
  })), [genres, navigation]);

  return (
      <AppLayout>
        {/* Misma cabecera que Canciones y Aprender: título y buscador fijos, fuera del scroll */}
        <View className="bg-background flex-1 pt-2">

          <ProfileHeader title="Géneros" />

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
                <VerticalButtonsCarousel data={formattedData} />
              </ScrollView>
          )}
        </View>
      </AppLayout>
  );
}