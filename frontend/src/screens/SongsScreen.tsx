import { ScrollView, View, TouchableOpacity, Image, Text} from "react-native";
import AppLayout from "../components/AppLayout";
import { useState } from "react";
import SearchBar from "../components/SearchBar";
import HorizontalSongsCarousel from "../components/HorizontalSongsCarousel";

const recommendations = [
  { id: '1', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '2', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '3', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '4', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '5', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '6', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
];

export default function SongScreen() {

  const [query, setQuery] = useState('');

  return (
    <AppLayout>
      <View className = "bg-background flex-1 pt-2">

        {/* Icono de perfil y titulo */}
        <View className="flex-row justify-left items-center mt-6 mb-7">
          <TouchableOpacity>
            <Image
              source={{ uri: 'https://i.pravatar.cc/100' }}
              className="w-14 h-14 rounded-full border"
            />
          </TouchableOpacity>
          <Text className="text-[24px] font-roboto font-bold text-primary text-center ml-3">Canciones</Text>
        </View> 

        {/* Barra de búsqueda */}
        <View className="bg-background mt-3 mb-4">
          <SearchBar value={query} onChangeText={setQuery} />
          {/* Aquí puedes mostrar resultados filtrados */}
        </View>
    
        <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingBottom: 120 }}
      >
        {/* Carrusel de canciones populares */}
        <HorizontalSongsCarousel title="Populares" data={recommendations} />

        {/* Carrusel de canciones populares */}
        <HorizontalSongsCarousel title="Nuevas" data={recommendations} />

        {/* Carrusel de canciones populares */}
        <HorizontalSongsCarousel title="Para ti" data={recommendations} />

        {/* Carrusel de canciones populares */}
        <HorizontalSongsCarousel title="Populares" data={recommendations} />

        {/* Carrusel de canciones populares */}
        <HorizontalSongsCarousel title="Nuevas" data={recommendations} />

        {/* Carrusel de canciones populares */}
        <HorizontalSongsCarousel title="Para ti" data={recommendations} />
        </ScrollView>
      </View>
    </AppLayout>
  )

}