import React, { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { GenreDTO } from '../types';
import { CirclePlus } from 'lucide-react-native';
import { getStoredGenres } from '../storage';

type Props = {
  onSelectionChange?: (selected: string[]) => void;
};

export default function GenrePicker({ onSelectionChange }: Props) {
  const [genres, setGenres] = useState<GenreDTO[]>([]);
  const [selectedGenres, setSelectedGenres] = useState<string[]>([]);

  useEffect(() => {
    const loadGenres = async () => {
      const stored = await getStoredGenres();
      if (stored) setGenres(stored);
    };
    loadGenres();
  }, []);

  useEffect(() => {
    onSelectionChange?.(selectedGenres);
  }, [selectedGenres]);

  const toggleGenre = (id: string) => {
    setSelectedGenres((prev) =>
      prev.includes(id) ? prev.filter((g) => g !== id) : [...prev, id]
    );
  };

  return (
    <View className="mb-5">
      <Text className="text-base text-secondary font-semibold mb-4">
        Escoge al menos un género
      </Text>
      <View className="flex-row flex-wrap justify-left gap-2">
        {genres.map((genre) => {
          const isSelected = selectedGenres.includes(genre.id);
          return (
            <TouchableOpacity
              key={genre.id}
              className={`px-3 py-1 bg-secondary rounded-lg ${isSelected ? "opacity-60" : ""}`}
              onPress={() => toggleGenre(genre.id)}
            >
              <Text className="text-primary text-base">{genre.name}</Text>
            </TouchableOpacity>
          );
        })}
        <TouchableOpacity className="py-1">
          <CirclePlus size={20} color="white" strokeWidth={1} />
        </TouchableOpacity>
      </View>
    </View>
  );
}
