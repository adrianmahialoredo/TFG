import { TouchableOpacity, Text, StyleSheet } from 'react-native';

const styles = StyleSheet.create({
  buttonNormal: {
    borderColor: '#C1853C',
    backgroundColor: 'transparent',
  },
  buttonPressed: {
    borderColor: '#C1853C',
    backgroundColor: '#C1853C',
  },
  textNormal: {
    color: '#C1853C',
  },
  textPressed: {
    color: '#FFFFFF',
  },
  baseButton: {
    borderColor: '#C1853C',
    borderWidth: 1,
    borderRadius: 999,
    height: 44,
    paddingHorizontal: 12,
    justifyContent: 'center',
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
      style={[styles.baseButton, isSelected ? styles.buttonPressed : styles.buttonNormal]}
      onPress={onPress}
      activeOpacity={1}
    >
      <Text 
        className="text-sm text-center"
        style={isSelected ? styles.textPressed : styles.textNormal}
      >
        {name}
      </Text>
    </TouchableOpacity>
  );
};
