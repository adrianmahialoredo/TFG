// src/screens/HomeScreen.tsx

import { Dimensions, View, Text, TouchableOpacity, Image } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { Activity, CircleCheckBig, Headphones } from 'lucide-react-native';
import HorizontalItemsCarousel from '../components/HorizontalItemsCarousel'; // Asegúrate de que la ruta es correcta
import AppLayout from '../components/AppLayout'; // Asegúrate de que la ruta es correcta

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

/* NO TOCAR POR DIOS */
const screenWidth = Dimensions.get('window').width;
const padding = 18;
const spacing = 18;
const buttonSize = (screenWidth - padding * 2 - spacing * 2) / 3;

export default function HomeScreen() {
  const navigation = useNavigation();

  return (
    <AppLayout>
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
          <View className="">
            <TouchableOpacity style={{
              width: buttonSize,
              height: buttonSize,
              marginRight: 8,
              backgroundColor: '#C1853C', // bg-primary
              borderRadius: 6,
              justifyContent: 'center',
              alignItems: 'center',
            }}>
              <Activity size={75} color="white" strokeWidth={1} />  
            </TouchableOpacity>
            <Text className="text-secondary text-xl font-semibold text-center mt-2 mr-2">Metrónomo</Text>
          </View>
          <View className="">
            <TouchableOpacity style={{
              width: buttonSize,
              height: buttonSize,
              marginRight: 8,
              backgroundColor: '#C1853C', // bg-primary
              borderRadius: 6,
              justifyContent: 'center',
              alignItems: 'center',
            }}>
              <CircleCheckBig size={75} color="white" strokeWidth={1} />
            </TouchableOpacity>
            <Text className="text-secondary text-xl font-semibold text-center mt-2 mr-2.5">Afinador</Text>
          </View>
          <View className="">
            <TouchableOpacity style={{
              width: buttonSize,
              height: buttonSize,
              marginRight: 8,
              backgroundColor: '#C1853C', // bg-primary
              borderRadius: 6,
              justifyContent: 'center',
              alignItems: 'center',
            }}>
              <Headphones size={75} color="white" strokeWidth={1} />  
            </TouchableOpacity>
            <Text className="text-secondary text-xl font-semibold text-center mt-2 mr-2.5">Identificador</Text>
          </View>
        </View>

        {/* Carrusel: Recomendaciones */}
        <HorizontalItemsCarousel title="Recomendaciones" data={recommendations} />
      </View>
    </AppLayout>
  );
}
