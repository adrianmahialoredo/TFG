// src/screens/HomeScreen.tsx

import { Dimensions, View, Text, TouchableOpacity, Image, ScrollView } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { Activity, CircleCheckBig, Headphones, List } from 'lucide-react-native';
import HorizontalItemsCarousel from '../components/carousels/HorizontalItemsCarousel'; // Asegúrate de que la ruta es correcta
import AppLayout from '../components/AppLayout'; // Asegúrate de que la ruta es correcta
import { ToolButton } from '../components/buttons/ToolButton';
import ListItem from '../components/ListItem';

const recentItems = [
  { id: '1', title: 'Lección de guitarra', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '2', title: 'Canción favorita', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png'},
  { id: '3', title: 'Última práctica', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png'},
  { id: '4', title: 'Nuevo acorde aprendido', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '5', title: 'Técnica de pua', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '6', title: 'Escala mayor', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
];

const recommendations = [
  { id: '1', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '2', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '3', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '4', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '5', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '6', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
];

const item = {
  id: '1',
  title: 'Lección de guitarra',
  description: 'Aprende los acordes básicos de guitarra.',
  imageUrl: 'https://m.media-amazon.com/images/I/71Z0rLIvpuL._UF894,1000_QL80_.jpg',
  info: `American Idiot
  Cancion - Green Day`,
};

/* NO TOCAR POR DIOS */
const screenWidth = Dimensions.get('window').width;
const padding = 18;
const spacing = 18;
const buttonSize = (screenWidth - padding * 2 - spacing * 2) / 3;

export default function HomeScreen() {
  const navigation = useNavigation();

  return (
    <AppLayout>
      <ScrollView
                  showsVerticalScrollIndicator={false}
                  contentContainerStyle={{ paddingBottom: 120 }}
              >
      <View className="flex-1 bg-background">
        {/* Icono de perfil */}
        <View className="flex-row justify-center my-7">
          <TouchableOpacity>
            <Image
              source={{ uri: 'https://i.pravatar.cc/100' }}
              className="w-20 h-20 rounded-full border"
            />
          </TouchableOpacity>
        </View>

        {/* Mensaje de bienvenida */}
        <Text className="text-3xl font-roboto font-bold text-primary text-center mb-11">Bienvenido Adrián!</Text>

        {/* Carrusel: Recientes */}
        <HorizontalItemsCarousel title="Recientes" data={recentItems} />

        {/* Botones de herramientas */}
        <View className="flex-row justify-between mt-12 mb-11">
          <ToolButton icon={<Activity size={75} color="white" strokeWidth={1} />} label="Metrónomo" />
          <ToolButton icon={<CircleCheckBig size={75} color="white" strokeWidth={1} />} label="Afinador" />
          <ToolButton icon={<Headphones size={75} color="white" strokeWidth={1} />} label="Identificador" />
        </View>

        {/* Carrusel: Recomendaciones */}
        <HorizontalItemsCarousel title="Recomendaciones" data={recommendations} />
        <ListItem item={item} onPress={() => {}}/>
      </View>
      </ScrollView>
    </AppLayout>
  );
}
