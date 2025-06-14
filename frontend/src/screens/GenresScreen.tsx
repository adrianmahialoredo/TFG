import { ScrollView, View, TouchableOpacity, Image, Text} from "react-native";
import AppLayout from "../components/AppLayout";
import { useState } from "react";
import SearchBar from "../components/SearchBar";
import HorizontalSongsCarousel from "../components/HorizontalSongsCarousel";
import VerticalButtonsCarousel from "../components/VerticalGenresCarousel";

const genres = [
  { id: '1', label: 'Pop Punk', imgUrl: 'https://i.ibb.co/ccNvtdqm/poppunk.jpg', onPress: () => {} },
  { id: '2', label: 'Nueva Ola', imgUrl: 'https://i.ibb.co/rRNsSWQ9/nuevaola.jpg', onPress: () => {} },
  { id: '3', label: 'Pop Rock', imgUrl: 'https://i.ibb.co/Vc1VD22p/poprock.jpg', onPress: () => {} },
  { id: '4', label: 'Rock & Roll', imgUrl: 'https://i.ibb.co/67L3VGRr/rock2.jpg', onPress: () => {} },
  { id: '5', label: 'Country', imgUrl: 'https://i.ibb.co/mrhxjCTS/country.jpg', onPress: () => {} },
  { id: '6', label: 'Funk', imgUrl: 'https://i.ibb.co/Kc0Pkvd5/funk.jpg', onPress: () => {} },
  { id: '7', label: 'Pop Punk', imgUrl: 'https://i.ibb.co/ccNvtdqm/poppunk.jpg', onPress: () => {} },
  { id: '8', label: 'Nueva Ola', imgUrl: 'https://i.ibb.co/rRNsSWQ9/nuevaola.jpg', onPress: () => {} },
  { id: '9', label: 'Pop Rock', imgUrl: 'https://i.ibb.co/Vc1VD22p/poprock.jpg', onPress: () => {} },
  { id: '10', label: 'Rock & Roll', imgUrl: 'https://i.ibb.co/67L3VGRr/rock2.jpg', onPress: () => {} },
  { id: '11', label: 'Country', imgUrl: 'https://i.ibb.co/mrhxjCTS/country.jpg', onPress: () => {} },
  { id: '12', label: 'Funk', imgUrl: 'https://i.ibb.co/Kc0Pkvd5/funk.jpg', onPress: () => {} },
];


export default function GenresScreen() {

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
          <Text className="text-[24px] font-roboto font-bold text-primary text-center ml-3">Géneros</Text>
        </View> 

        {/* Barra de búsqueda */}
        <View className="bg-background mt-3 mb-10">
          <SearchBar value={query} onChangeText={setQuery} />
          {/* Aquí puedes mostrar resultados filtrados */}
        </View>
    
        <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingBottom: 120 }}
        >
        
        <VerticalButtonsCarousel title="Géneros" data={genres} />

        </ScrollView>
      </View>
    </AppLayout>
  )

}