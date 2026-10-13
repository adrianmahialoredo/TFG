import React, { useState } from 'react';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import BaseModal from './BaseModal';
import GenrePicker from './GenrePicker';
import InstrumentPicker from './InstrumentPicker';
import { MIN_FAVOURITE_GENRES, MIN_FAVOURITE_INSTRUMENTS } from '../constants/Preferences';

type FavoritesPickerModalProps = {
  kind: 'genres' | 'instruments';
  initialIds: (string | number)[];
  saving: boolean;
  onSave: (ids: string[]) => void;
  onClose: () => void;
};

export default function FavoritesPickerModal({
  kind,
  initialIds,
  saving,
  onSave,
  onClose,
}: FavoritesPickerModalProps) {
  // El padre solo monta este modal mientras está abierto, así que cada vez que
  // se abre arranca de nuevo con la selección actual del usuario.
  const initialSelection = initialIds.map(String);
  const [selectedIds, setSelectedIds] = useState<string[]>(initialSelection);

  const Picker = kind === 'genres' ? GenrePicker : InstrumentPicker;
  const minSelected = kind === 'genres' ? MIN_FAVOURITE_GENRES : MIN_FAVOURITE_INSTRUMENTS;
  const canSave = !saving && selectedIds.length >= minSelected;

  return (
    <BaseModal visible onClose={onClose} maxWidth={300}>
      <ScrollView className="max-h-96" showsVerticalScrollIndicator={false}>
        <Picker initialSelection={initialSelection} onSelectionChange={setSelectedIds} />
      </ScrollView>

      {selectedIds.length < minSelected && (
        <Text className="text-red-500 text-sm">
          Elige al menos {minSelected} {kind === 'genres' ? 'géneros' : 'instrumentos'}
        </Text>
      )}

      <View className="flex-row justify-end gap-3 mt-2">
        <TouchableOpacity
          className="px-4 py-2 rounded-full border border-secondary"
          onPress={onClose}
          disabled={saving}
        >
          <Text className="text-secondary font-semibold">Cancelar</Text>
        </TouchableOpacity>

        <TouchableOpacity
          className={`px-4 py-2 rounded-full ${canSave ? 'bg-primary' : 'bg-gray-600'}`}
          onPress={() => onSave(selectedIds)}
          disabled={!canSave}
        >
          <Text className="text-background font-semibold">
            {saving ? 'Guardando...' : 'Guardar'}
          </Text>
        </TouchableOpacity>
      </View>
    </BaseModal>
  );
}
