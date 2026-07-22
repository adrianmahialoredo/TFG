// src/screens/HomeScreen.tsx

import { View, Text, TouchableOpacity, Image, ScrollView, ActivityIndicator } from 'react-native';
import { useNavigation, NavigationProp } from '@react-navigation/native';
import { useCallback } from 'react';
import { Activity, CircleCheckBig, Headphones } from 'lucide-react-native';
import HorizontalItemsCarousel from '../components/carousels/HorizontalItemsCarousel';
import AppLayout from '../components/AppLayout';
import { ToolButton } from '../components/buttons/ToolButton';
import { RootStackParamList } from '../types';
import { useAuth } from '../hooks/useAuth';
import { useHomeItems } from '../hooks/useHomeItems';
import { navigateToItem } from '../utils/navigation.utils';

export default function HomeScreen() {
  const navigation = useNavigation<NavigationProp<RootStackParamList>>();

  // SSOT: el usuario actual sale de la AuthStore vía useAuth (ya no existe getStoredUser)
  const { user } = useAuth();

  // Nivel 6: hook que orquesta los repositorios y se suscribe a la Entity Store de items
  const { recentItems, recommendations, getItemById, loading, error } = useHomeItems();

  // Resuelve el item completo (con su type) y navega a la pantalla de detalle adecuada
  const handleItemPress = useCallback(
    (itemId: string) => {
      const item = getItemById(itemId);
      if (item) navigateToItem(navigation, item);
    },
    [getItemById, navigation]
  );

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
                source={{ uri: user?.profileImageUrl || 'https://via.placeholder.com/150' }}
                className="w-20 h-20 rounded-full border"
              />
            </TouchableOpacity>
          </View>

          {/* Mensaje de bienvenida */}
          <Text className="text-3xl font-roboto font-bold text-primary text-center mb-11">
            Bienvenido, {user?.name}
          </Text>

          {/* Indicador de carga / error */}
          {loading && (
            <View className="py-6 items-center justify-center">
              <ActivityIndicator size="large" color="#ffffff" />
            </View>
          )}
          {error && !loading && (
            <Text className="text-red-500 text-center mb-6 px-4">{error}</Text>
          )}

          {/* Carrusel: Recientes (se auto-oculta si no hay items) */}
          <HorizontalItemsCarousel
            title="Recientes"
            data={recentItems}
            onItemPress={handleItemPress}
          />

          {/* Botones de herramientas */}
          <View className="flex-row justify-between mt-12 mb-11">
            <ToolButton
              icon={<Activity size={75} color="white" strokeWidth={1} />}
              label="Metrónomo"
              onPress={() => navigation.navigate('MetronomeScreen')}
            />
            <ToolButton icon={<CircleCheckBig size={75} color="white" strokeWidth={1} />} label="Afinador" />
            <ToolButton icon={<Headphones size={75} color="white" strokeWidth={1} />} label="Identificador" />
          </View>

          {/* Carrusel: Recomendaciones (esqueleto en backend; se auto-oculta si está vacío) */}
          <HorizontalItemsCarousel
            title="Recomendaciones"
            data={recommendations}
            onItemPress={handleItemPress}
          />
        </View>
      </ScrollView>
    </AppLayout>
  );
}
