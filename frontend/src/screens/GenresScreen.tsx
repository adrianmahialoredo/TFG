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
        <ProfileHeader title="Géneros" />
        {loading ? (
            <ActivityIndicator />
        ) : (
            <ScrollView>
              <SearchBar value={query} onChangeText={setQuery} />
              <VerticalButtonsCarousel data={formattedData} />
            </ScrollView>
        )}
      </AppLayout>
  );
}