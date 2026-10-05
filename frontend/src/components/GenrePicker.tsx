import React, { useState } from 'react';
import { View, Text, ActivityIndicator } from 'react-native';
import { useGenres } from '../hooks/useGenres';
import { COLORS } from '../constants/Theme';
import SelectableChip from './SelectableChip';

type Props = {
  onSelectionChange?: (selectedIds: string[]) => void;
  initialSelection?: string[]; // Por si queremos editar perfil luego
};

export default function GenrePicker({ onSelectionChange, initialSelection = [] }: Props) {
  const { genres, loading } = useGenres();
  const [selectedIds, setSelectedIds] = useState<string[]>(initialSelection);

  // Se avisa al padre en el mismo toque (no en un useEffect, que llegaría un render tarde)
  const toggleGenre = (id: string) => {
    const next = selectedIds.includes(id) ? selectedIds.filter(i => i !== id) : [...selectedIds, id];
    setSelectedIds(next);
    onSelectionChange?.(next);
  };

  if (loading) return <ActivityIndicator color={COLORS.secondary} />;

  return (
      <View className="mb-5">
        <Text className="text-base text-secondary font-semibold mb-4">
          ¿Cuáles son tus géneros favoritos?
        </Text>
        <View className="flex-row flex-wrap gap-2">
          {genres.map((genre) => (
              <SelectableChip
                  key={genre.id}
                  label={genre.title}
                  selected={selectedIds.includes(genre.id.toString())}
                  onPress={() => toggleGenre(genre.id.toString())}
              />
          ))}
        </View>
      </View>
  );
}
