// src/screens/HomeScreen.tsx

import { Dimensions, View, Text, TouchableOpacity, Image, ScrollView } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { Activity, CircleCheckBig, Headphones, List } from 'lucide-react-native';
import HorizontalItemsCarousel from '../components/carousels/HorizontalItemsCarousel'; // Asegúrate de que la ruta es correcta
import AppLayout from '../components/AppLayout'; // Asegúrate de que la ruta es correcta
import { ToolButton } from '../components/buttons/ToolButton';
import { useEffect, useState } from 'react';
import { UserDTO } from '../types/UserDTO';
import { getStoredUser } from '../services/storageService';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../types/RootStackParamList';

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

export default function HomeScreen() {

  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const [user, setUser] = useState<UserDTO | null>(null);

  useEffect(() => {
      const loadUser = async () => {
        const data = await getStoredUser();
        if (data) {
          setUser(data.user);
        }
      };
  
      loadUser();
    }, []);
  
  return (
    <AppLayout>
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingBottom: 120 }}
      >
      <View className="flex-1 bg-background">
        {/* Icono de perfil */}
        <View className="flex-row justify-center my-7">
          <TouchableOpacity onPress={() => navigation.navigate('ProfileScreen')}>
            <Image
              source={{ uri: user?.profileImageUrl }}
              className="w-20 h-20 rounded-full border"
            />
          </TouchableOpacity>
        </View>

        {/* Mensaje de bienvenida */}
        <Text className="text-3xl font-roboto font-bold text-primary text-center mb-11">Bienvenido, {user?.name}</Text>

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

      </View>
      </ScrollView>
    </AppLayout>
  );
}
