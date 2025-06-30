import { ScrollView, View, TouchableOpacity, Image, Text} from "react-native";
import AppLayout from "../components/AppLayout";
import { useState } from "react";
import SearchBar from "../components/SearchBar";
import HorizontalSongsCarousel from "../components/carousels/SongsCarousel";
import ProfileHeader from "../components/ProfileHeader";

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

        <ProfileHeader title="Canciones" />

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