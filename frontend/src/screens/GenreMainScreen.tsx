import { View } from "react-native";
import AppLayout from "../components/AppLayout";
import { Image, Text, TouchableOpacity } from "react-native";
import { StyleSheet } from "react-native";
import TopButton from "../components/buttons/TopButton";
import { useEffect, useState } from "react";
import GenreInstruments from "../components/GenreInstruments";
import GenreSongs from "../components/GenreSongs";
import GenreDescription from "../components/GenreDescription";
import { GenreDTO } from "../types/GenreDTO";
import { getGenreById } from "../services/genreService";
import { useNavigation, useRoute } from "@react-navigation/native";
import { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { RootStackParamList } from "../types/RootStackParamList";


export default function GenreMainScreen() {

  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

  const route = useRoute();
  const { genreId } = route.params as { genreId: string };

  const [selectedTab, setSelectedTab] = useState<'Instrumentos' | 'Canciones' | 'Género'>('Género');
  const [genre, setGenre] = useState<GenreDTO>();

  useEffect(() => {
    const fetchGenre = async () => {
      try {
        const data = await getGenreById(genreId);
        setGenre(data);
      } catch (error) {
        console.error('Error al cargar el género:', error);
      }
    };

    fetchGenre();
  }, [genreId]);

  return (
    <AppLayout>
      <View className="bg-background flex-1 pt-2">

        {/* Icono de perfil y botones */}
        <View className="flex-row justify-left items-center mt-6 mb-11 gap-2.5">

          <TouchableOpacity
            onPress={() => navigation.navigate('ProfileScreen')}
          >
            <Image
              source={{ uri: 'https://i.pravatar.cc/100' }}
              className="w-14 h-14 rounded-full border"
            />
          </TouchableOpacity>

          {['Instrumentos', 'Canciones', 'Género'].map((tab) => (
            <TopButton
              key={tab}
              name={tab}
              isSelected={selectedTab === tab}
              onPress={() => setSelectedTab(tab as any)}
            />
          ))}
          
        </View>

        <View className="">
            <Text className="text-[24px] font-roboto font-semibold text-primary text-center mb-2">
                {genre?.name}
            </Text>
        </View>

        {selectedTab === 'Instrumentos' && <GenreInstruments description="instrumentos" />}
        {selectedTab === 'Canciones' && <GenreSongs description="songds" />}
        {selectedTab === 'Género' && 
          <GenreDescription 
            description={genre?.description ?? ""}
            bannerUrl={genre?.bannerUrl ?? ""} 
            welcomeVideoId={genre?.welcomeIntroVideoId ?? ""} 
            welcomeSongId={genre?.welcomeSongVideoId ?? ""} 
          />}

      </View>
    </AppLayout>
  );
}