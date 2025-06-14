import React from 'react';
import { View, TextInput, TextInputProps } from 'react-native';
import { Search } from 'lucide-react-native';
import { COLORS } from '../constants/Theme';

type SearchBarProps = {
  placeholder?: string;
  value: string;
  onChangeText: (text: string) => void;
};

export default function SearchBar({ placeholder = '¿Que quieres tocar?', value, onChangeText }: SearchBarProps) {
  return (
    <View className="flex-row items-center bg-primary rounded-lg px-4 py-1">
      <Search size={30} color={COLORS.secondary} />
      <TextInput
        className="ml-2 flex-1 text-secondary text-base font-semibold"
        placeholder={placeholder}
        placeholderTextColor={COLORS.secondary}
        value={value}
        onChangeText={onChangeText}
      />
    </View>
  );
}
