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
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable
        className="flex-1 bg-black/70 items-center justify-center px-6"
        onPress={onClose}
      >
        <Pressable className="w-full max-w-sm rounded-2xl bg-background border border-primary p-5" onPress={() => {}}>
          <Text className="text-center text-lg font-semibold text-secondary mb-4">
            Opciones de imagen
          </Text>

          <Pressable
            onPress={onViewImage}
            className="w-full py-3 items-center border-b border-secondary/25"
          >
            <Text className="text-lg text-secondary font-semibold">
              Ver foto ampliada
            </Text>
          </Pressable>

          <Pressable
            onPress={onPickImage}
            className="w-full py-3 items-center border-b border-secondary/25"
            disabled={isLoading}
          >
            <Text className="text-lg text-secondary font-semibold">
              {isLoading ? 'Cargando...' : 'Subir nueva foto'}
            </Text>
          </Pressable>

          <Pressable
            onPress={onClose}
            className="w-full py-3 items-center mt-2"
          >
            <Text className="text-lg text-primary font-semibold">Cancelar</Text>
          </Pressable>
        </Pressable>
      </Pressable>
    </Modal>
  );
};

