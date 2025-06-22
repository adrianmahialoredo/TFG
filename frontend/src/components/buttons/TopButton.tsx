import { TouchableOpacity, Text, StyleSheet } from 'react-native';
import { useState } from 'react';

const styles = StyleSheet.create({
  buttonNormal: {
    borderColor: '#C1853C',
    backgroundColor: '#000000',
    color: '#C1853C'
  },
  buttonPressed: {
    borderColor: '#C1853C',
    backgroundColor: '#C1853C',
    color: '#FFFFFF',
  },
});

interface TopButtonProps {
  name: string;
  isSelected: boolean;
  onPress: () => void;
}

export default function TopButton({ name, isSelected, onPress }: TopButtonProps) {

  return (
    <TouchableOpacity 
      className="h-11 px-3 border rounded-full justify-center"
      style={[
        isSelected ? styles.buttonPressed : styles.buttonNormal // Usa isSelected
      ]}
      onPress={onPress} // Cambia el estado al tocar
    >
      <Text 
        className="text-sm text-center"
        style={isSelected ? styles.buttonPressed : styles.buttonNormal}
      >
        {name}
      </Text>
    </TouchableOpacity>
  );
};
