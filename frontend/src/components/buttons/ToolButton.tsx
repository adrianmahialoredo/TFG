
import React from 'react';
import { View, Text, TouchableOpacity, Dimensions } from 'react-native';

/* NO TOCAR POR DIOS */
const screenWidth = Dimensions.get('window').width;
const padding = 18;
const spacing = 18;
const buttonSize = (screenWidth - padding * 2 - spacing * 2) / 3;
/* NO TOCAR POR DIOS */

type ToolButtonProps = {
  icon: React.ReactNode;
  label: string;
  onPress?: () => void;
};

export const ToolButton: React.FC<ToolButtonProps> = ({ icon, label, onPress }) => {
  return (
    <View>
      <TouchableOpacity
        onPress={onPress}
        style={{
          width: buttonSize,
          height: buttonSize,
          marginRight: 8,
          backgroundColor: '#C1853C', // bg-primary
          borderRadius: 6,
          justifyContent: 'center',
          alignItems: 'center',
        }}
      >
        {icon}
      </TouchableOpacity>
      <Text className="text-secondary text-xl font-semibold text-center mt-2 mr-2.5">
        {label}
      </Text>
    </View>
  );
};
