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

  const titleHeader = genre && (
      <View className="mb-6">
        <Text className="text-[24px] font-semibold text-primary text-center">
          {genre.title}
        </Text>
      </View>
  );

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

              <View className="flex-row items-center gap-2 px-4 mb-10 mt-6">
                <ProfileHeader title="" compact />

                {['Instrumentos', 'Canciones', 'Género'].map((tab) => (
                    <TopButton
                        key={tab}
                        name={tab}
                        isSelected={selectedTab === tab}
                        onPress={() => changeTab(tab as any)}
                    />
                ))}
              </View>

              {/* Vistas Dinámicas pasándole solo el ID. El título del género va dentro del scroll
                  de cada pestaña (cada una tiene el suyo), no fijo encima */}
              <View className="flex-1">
                {selectedTab === 'Instrumentos' && (
                    <GenreInstrumentsScreen genreId={genreId} header={titleHeader} />
                )}

                {selectedTab === 'Canciones' && (
                    <GenreSongsScreen genreId={genreId} header={titleHeader} />
                )}

                {selectedTab === 'Género' && (
                    <GenreDescriptionScreen genreId={genreId} header={titleHeader} />
                )}
              </View>
            </View>
        )}
      </AppLayout>
  );
}