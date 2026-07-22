import { Modal, View, Text, Pressable } from 'react-native';

interface PhotoOptionsModalProps {
  visible: boolean;
  onClose: () => void;
  onViewImage: () => void;
  onPickImage: () => void;
  isLoading?: boolean;
}

/**
 * Componente Modal: Opciones de foto de perfil
 * 
 * Nivel 7: Componente de UI
 * - Modal reutilizable
 * - Maneja interacciones
 * - Emite eventos
 */
export const PhotoOptionsModal = ({
  visible,
  onClose,
  onViewImage,
  onPickImage,
  isLoading = false,
}: PhotoOptionsModalProps) => {
  return (
    <Modal visible={visible} transparent animationType="slide">
      <Pressable
        className="flex-1 bg-black/50 justify-end"
        onPress={onClose}
      >
        <View className="bg-white p-4 rounded-t-2xl">
          <Text className="text-center text-lg font-semibold text-gray-800 mb-4">
            Opciones de imagen
          </Text>

          <Pressable
            onPress={onViewImage}
            className="w-full py-3 items-center border-b border-gray-200"
          >
            <Text className="text-lg text-blue-600 font-semibold">
              Ver foto ampliada
            </Text>
          </Pressable>

          <Pressable
            onPress={onPickImage}
            className="w-full py-3 items-center border-b border-gray-200"
            disabled={isLoading}
          >
            <Text className="text-lg text-blue-600 font-semibold">
              {isLoading ? 'Cargando...' : 'Subir nueva foto'}
            </Text>
          </Pressable>

          <Pressable
            onPress={onClose}
            className="w-full py-3 items-center mt-2"
          >
            <Text className="text-lg text-red-500 font-semibold">Cancelar</Text>
          </Pressable>
        </View>
      </Pressable>
    </Modal>
  );
};

