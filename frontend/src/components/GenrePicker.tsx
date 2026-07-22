import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, ActivityIndicator } from 'react-native';
import { CirclePlus } from 'lucide-react-native';
import { useGenres } from '../hooks/useGenres';
import { COLORS } from '../constants/Theme';

type Props = {
  onSelectionChange?: (selectedIds: string[]) => void;
  initialSelection?: string[]; // Por si queremos editar perfil luego
};

export default function GenrePicker({ onSelectionChange, initialSelection = [] }: Props) {
  const { genres, loading } = useGenres();
  const [selectedIds, setSelectedIds] = useState<string[]>(initialSelection);

  // Avisar al padre (RegisterScreen) cuando cambie la selección
  useEffect(() => {
    onSelectionChange?.(selectedIds);
  }, [selectedIds]);

  const toggleGenre = (id: string) => {
    setSelectedIds(prev =>
        prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
    );
  };

  if (loading) return <ActivityIndicator color={COLORS.secondary} />;

  return (
      <View className="mb-5">
        <Text className="text-base text-secondary font-semibold mb-4">
          ¿Cuáles son tus géneros favoritos?
        </Text>
        <View className="flex-row flex-wrap gap-2">
          {genres.map((genre) => {
            const isSelected = selectedIds.includes(genre.id.toString());
            return (
                <TouchableOpacity
                    key={genre.id}
                    onPress={() => toggleGenre(genre.id.toString())}
                    className={`px-4 py-2 rounded-full border ${
                        isSelected ? "bg-secondary border-secondary" : "bg-transparent border-gray-500"
                    }`}
                >
                  <Text className={`text-sm font-medium ${isSelected ? "text-primary" : "text-gray-400"}`}>
                    {genre.title}
                  </Text>
                </TouchableOpacity>
            );
          })}
        </View>
      </View>
  );
}