import { View } from "react-native";
import AppLayout from "../components/AppLayout";
import { Image, Text, TouchableOpacity } from "react-native";
import { StyleSheet } from "react-native";
import TopButton from "../components/buttons/TopButton";
import { useState } from "react";
import GenreInstruments from "../components/GenreInstruments";
import GenreSongs from "../components/GenreSongs";
import GenreDescription from "../components/GenreDescription";

type Genre = {
  id: string;
  name: string;
  bannerUrl: string;
  welcomeVideoId: string;
  welcomeSongId: string;
  description: string;
}

const poppunk: Genre = {
  id: '1',
  name: 'Pop Punk',
  bannerUrl: 'https://i.ibb.co/wr4BSxk6/36ffe640309721-5790d08d50f7d.png',
  welcomeVideoId: 'hoKPrLBDcl8',
  welcomeSongId: 'CMX2lPum_pg',
  description: `El pop punk es un subgénero del punk rock que combina melodías pegajosas con la energía cruda del punk. Surgió en la década de 1970 y ha influido en bandas como Blink-182 y Green Day. Es conocido por sus letras emocionales y ritmos rápidos, convirtiéndose en un favorito entre los jóvenes. El pop punk ha evolucionado a lo largo de los años, incorporando elementos de otros géneros como el emo y el pop rock, pero siempre manteniendo su esencia rebelde y divertida.

El pop punk es un subgénero del punk rock que combina melodías pegajosas con la energía cruda del punk. Surgió en la década de 1970 y ha influido en bandas como Blink-182 y Green Day. Es conocido por sus letras emocionales y ritmos rápidos, convirtiéndose en un favorito entre los jóvenes. El pop punk ha evolucionado a lo largo de los años, incorporando elementos de otros géneros como el emo y el pop rock, pero siempre manteniendo su esencia rebelde y divertida.`
};

export default function GenreMainScreen() {

  const [selectedTab, setSelectedTab] = useState<'Instrumentos' | 'Canciones' | 'Género'>('Género');

  return (
    <AppLayout>
      <View className="bg-background flex-1 pt-2">

        {/* Icono de perfil y botones */}
        <View className="flex-row justify-left items-center mt-6 mb-11 gap-2.5">

          <TouchableOpacity>
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
                {poppunk.name}
            </Text>
        </View>

        {selectedTab === 'Instrumentos' && <GenreInstruments description="instrumentos" />}
        {selectedTab === 'Canciones' && <GenreSongs description="songds" />}
        {selectedTab === 'Género' && 
          <GenreDescription 
            description= {poppunk.description}
            bannerUrl={poppunk.bannerUrl} 
            welcomeVideoId={poppunk.welcomeVideoId} 
            welcomeSongId={poppunk.welcomeSongId} 
          />}

      </View>
    </AppLayout>
  );
}