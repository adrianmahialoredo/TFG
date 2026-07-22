import { View, Text, ActivityIndicator } from "react-native";
import AppLayout from "../components/AppLayout";
import TopButton from "../components/buttons/TopButton";
import ProfileHeader from "../components/ProfileHeader";
import GenreInstrumentsScreen from "./GenreInstrumentsScreen";
import GenreSongsScreen from "./GenreSongsScreen";
import GenreDescriptionScreen from "./GenreDescriptionScreen";
import { useRoute } from "@react-navigation/native";
import { useGenreMain } from "../hooks/useGenreMain";

export default function GenreMainScreen() {
  const route = useRoute();
  const { genreId } = route.params as { genreId: string };

  const {
    genre,
    loading,
    error,
    selectedTab,
    changeTab,
  } = useGenreMain(genreId);

  return (
      <AppLayout>
        {/* 1. ESTADO DE CARGA */}
        {loading && !genre && (
            <View className="flex-1 justify-center items-center bg-background">
              <ActivityIndicator size="large" color="#ffffff" />
            </View>
        )}

        {/* 2. ESTADO DE ERROR */}
        {error && !genre && (
            <View className="flex-1 justify-center items-center bg-background p-4">
              <Text className="text-red-500 text-center">{error}</Text>
            </View>
        )}

        {/* 3. CONTENIDO PRINCIPAL */}
        {genre && (
            <View className="flex-1 bg-background pt-2">

              <View className="flex-row gap-2 px-4 mb-4 mt-6">
                <ProfileHeader title="" />

                {['Instrumentos', 'Canciones', 'Género'].map((tab) => (
                    <TopButton
                        key={tab}
                        name={tab}
                        isSelected={selectedTab === tab}
                        onPress={() => changeTab(tab as any)}
                    />
                ))}
              </View>

              <View className="mb-6">
                <Text className="text-[24px] font-semibold text-primary text-center">
                  {genre.title}
                </Text>
              </View>

              {/* Vistas Dinámicas pasándole solo el ID */}
              <View className="flex-1">
                {selectedTab === 'Instrumentos' && (
                    <GenreInstrumentsScreen genreId={genreId} />
                )}

                {selectedTab === 'Canciones' && (
                    <GenreSongsScreen genreId={genreId} />
                )}

                {selectedTab === 'Género' && (
                    <GenreDescriptionScreen genreId={genreId} />
                )}
              </View>
            </View>
        )}
      </AppLayout>
  );
}