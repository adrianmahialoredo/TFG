import { ActivityIndicator, View } from "react-native";
import FadeScrollView from "../components/FadeScrollView";
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
        {/* Misma estructura que Canciones y Aprender: título fijo y buscador dentro del scroll */}
        <View className="bg-background flex-1">

          <ProfileHeader title="Géneros" />

          <FadeScrollView
              showsVerticalScrollIndicator={false}
              keyboardShouldPersistTaps="handled"
              contentContainerStyle={{ paddingBottom: 120 }}
          >
            {/* Barra de búsqueda */}
            <View className="bg-background mt-1 mb-4">
              <SearchBar value={query} onChangeText={setQuery} />
            </View>

            {loading ? (
                <View className="justify-center items-center py-20">
                  <ActivityIndicator size="large" color="#ffffff" />
                </View>
            ) : (
                <VerticalButtonsCarousel data={formattedData} />
            )}
          </FadeScrollView>
        </View>
      </AppLayout>
  );
}