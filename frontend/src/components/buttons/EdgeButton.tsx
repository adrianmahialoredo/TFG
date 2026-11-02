import { Text, StyleSheet } from 'react-native';
import Touchable from '../Touchable';

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
  isTopEdge: boolean;
  isBottomEdge: boolean;
  onPress: () => void;
}

export default function EdgeButton({ name, isSelected, isTopEdge, isBottomEdge, onPress }: TopButtonProps) {

  return (
    <Touchable 
      className={`h-11 px-3 border justify-center 
        ${isTopEdge ? 'rounded-t-lg' : ''} 
        ${isBottomEdge ? 'rounded-b-lg' : ''}`
    }
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
    </Touchable>
  );
};
