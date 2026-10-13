import React from 'react';
import { Modal, View, Text, Pressable, ActivityIndicator } from 'react-native';
import { Eye, ImagePlus } from 'lucide-react-native';

interface PhotoOptionsModalProps {
  visible: boolean;
  onClose: () => void;
  onViewImage: () => void;
  onPickImage: () => void;
  isLoading?: boolean;
}

const PRIMARY = '#C1853C';

type OptionRowProps = {
  icon: React.ReactNode;
  label: string;
  onPress: () => void;
  disabled?: boolean;
};

/**
 * Fila de opción a todo el ancho de la tarjeta: el separador superior llega de borde a borde
 * porque la tarjeta no tiene padding horizontal (lo lleva cada fila).
 */
const OptionRow = ({ icon, label, onPress, disabled }: OptionRowProps) => (
  <Pressable
    onPress={onPress}
    disabled={disabled}
    className={`flex-row items-center gap-4 px-5 py-4 border-t border-secondary/15 active:bg-secondary/10 ${disabled ? 'opacity-50' : ''}`}
  >
    <View className="w-9 h-9 rounded-full bg-primary/15 items-center justify-center">
      {icon}
    </View>
    <Text className="text-base text-secondary font-medium">{label}</Text>
  </Pressable>
);

/**
 * Componente Modal: Opciones de foto (perfil y listas)
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
        {/* Sin padding horizontal ni vertical: lo ponen la cabecera y cada fila */}
        <Pressable
          className="w-72 rounded-2xl bg-background border border-primary/60 overflow-hidden"
          onPress={() => {}}
        >
          <View className="px-5 pt-5 pb-4">
            <Text className="text-center text-lg font-semibold text-secondary">
              Foto
            </Text>
            <Text className="text-center text-sm text-secondary/60 mt-1">
              ¿Qué quieres hacer con la imagen?
            </Text>
          </View>

          <OptionRow
            icon={<Eye size={18} color={PRIMARY} />}
            label="Ver foto ampliada"
            onPress={onViewImage}
          />

          <OptionRow
            icon={isLoading ? <ActivityIndicator size="small" color={PRIMARY} /> : <ImagePlus size={18} color={PRIMARY} />}
            label={isLoading ? 'Subiendo foto...' : 'Subir nueva foto'}
            onPress={onPickImage}
            disabled={isLoading}
          />

          {/* Cancelar separado del resto, como en las hojas de acciones nativas */}
          <Pressable
            onPress={onClose}
            className="py-4 items-center border-t border-secondary/15 active:bg-secondary/10"
          >
            <Text className="text-base text-primary font-semibold">Cancelar</Text>
          </Pressable>
        </Pressable>
      </Pressable>
    </Modal>
  );
};
