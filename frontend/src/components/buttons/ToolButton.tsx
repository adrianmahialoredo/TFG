
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
  buttonMarginRight?: number;
  labelMarginRight?: number;
};

export const ToolButton: React.FC<ToolButtonProps> = ({
  icon,
  label,
  onPress,
  buttonMarginRight = 8,
  labelMarginRight = 10,
}) => {
  return (
    <View style={{ width: buttonSize, alignItems: 'center', marginRight: buttonMarginRight }}>
      <TouchableOpacity
        onPress={onPress}
        style={{
          width: buttonSize,
          height: buttonSize,
          backgroundColor: '#C1853C', // bg-primary
          borderRadius: 6,
          justifyContent: 'center',
          alignItems: 'center',
        }}
      >
        {icon}
      </TouchableOpacity>
      <Text
        className="text-secondary text-sm font-semibold text-center mt-2"
        style={{ marginRight: labelMarginRight, width: '100%' }}
        numberOfLines={1}
      >
        {label}
      </Text>
    </View>
  );
};
