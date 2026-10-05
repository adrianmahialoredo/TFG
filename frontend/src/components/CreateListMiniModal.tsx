import React from 'react';
import { Text, TextInput, TouchableOpacity, View } from 'react-native';
import BaseModal from './BaseModal';

type CreateListMiniModalProps = {
  visible: boolean;
  name: string;
  error: string | null;
  creating: boolean;
  title?: string;
  placeholder?: string;
  submitLabel?: string;
  onChangeName: (value: string) => void;
  onSubmit: () => void;
  onClose: () => void;
};

export default function CreateListMiniModal({
  visible,
  name,
  error,
  creating,
  title = 'Nueva lista',
  placeholder = 'Nombre de la lista',
  submitLabel = 'Crear',
  onChangeName,
  onSubmit,
  onClose,
}: CreateListMiniModalProps) {
  return (
    <BaseModal visible={visible} onClose={onClose}>
      <Text className="text-secondary text-lg font-bold mb-3">{title}</Text>

          <TextInput
            value={name}
            onChangeText={onChangeName}
            placeholder={placeholder}
            placeholderTextColor="#A3A3A3"
            className="border border-secondary rounded-xl px-3 py-2 text-secondary"
            maxLength={100}
            autoFocus
            underlineColorAndroid="transparent"
            selectionColor="#ECECEC"
          />

          {error ? <Text className="text-red-500 text-sm mt-2">{error}</Text> : null}

      <View className="flex-row justify-end gap-3 mt-5">
        <TouchableOpacity
          className="px-4 py-2 rounded-full border border-secondary"
          onPress={onClose}
          disabled={creating}
        >
          <Text className="text-secondary font-semibold">Cancelar</Text>
        </TouchableOpacity>

        <TouchableOpacity
          className={`px-4 py-2 rounded-full ${creating ? 'bg-gray-600' : 'bg-primary'}`}
          onPress={onSubmit}
          disabled={creating}
        >
          <Text className="text-background font-semibold">
            {creating ? 'Guardando...' : submitLabel}
          </Text>
        </TouchableOpacity>
      </View>
    </BaseModal>
  );
}
