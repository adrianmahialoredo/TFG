import { View, Text, ActivityIndicator } from 'react-native';
import { COLORS } from '../constants/Theme';

/**
 * Componente de Estado: Muestra mientras se cargan los datos
 * 
 * Nivel 7: Componente de UI
 * - Skeleton screen simple
 * - Estados de carga
 */
export const LoadingPlaceholder = () => {
  return (
    <View className="flex-1 justify-center items-center">
      <ActivityIndicator size="large" color={COLORS.primary} />
      <Text className="text-secondary mt-4 text-lg">
        Cargando perfil musical...
      </Text>
    </View>
  );
};

