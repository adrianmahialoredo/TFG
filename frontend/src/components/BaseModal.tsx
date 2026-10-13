import React from 'react';
import { Modal, ModalProps, Pressable, View } from 'react-native';

type BaseModalProps = {
  visible: boolean;
  onClose: () => void;
  children: React.ReactNode;
  animationType?: ModalProps['animationType'];
  /** Ancho máximo de la caja en px (por defecto max-w-sm, 384) */
  maxWidth?: number;
};

export default function BaseModal({
  visible,
  onClose,
  children,
  animationType = 'fade',
  maxWidth,
}: BaseModalProps) {
  return (
    <Modal
      visible={visible}
      transparent
      animationType={animationType}
      onRequestClose={onClose}
    >
      <Pressable className="flex-1 bg-black/70 items-center justify-center px-6" onPress={onClose}>
        <Pressable onPress={() => {}}>
          <View
            className="w-full max-w-sm rounded-2xl bg-background border border-primary p-5"
            style={maxWidth ? { maxWidth } : undefined}
          >
            {children}
          </View>
        </Pressable>
      </Pressable>
    </Modal>
  );
}
