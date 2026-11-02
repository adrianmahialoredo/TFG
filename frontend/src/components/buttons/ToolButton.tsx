
import React from 'react';
import { View, Text, Dimensions } from 'react-native';
import Touchable from '../Touchable';

/* NO TOCAR POR DIOS */
const screenWidth = Dimensions.get('window').width;
const padding = 18;
const spacing = 18;
const buttonSize = (screenWidth - padding * 2 - spacing * 2) / 3;
/* NO TOCAR POR DIOS */

/** Lado del botón en una fila de tres. */
export const TOOL_BUTTON_SIZE = buttonSize;
/** Margen derecho por defecto de cada botón. */
export const TOOL_BUTTON_MARGIN = 8;

type ToolButtonProps = {
  icon: React.ReactNode;
  label: string;
  onPress?: () => void;
  buttonMarginRight?: number;
  labelMarginRight?: number;
  /** Ancho del botón (el alto no cambia); por defecto, el de una fila de tres. */
  width?: number;
};

export const ToolButton: React.FC<ToolButtonProps> = ({
  icon,
  label,
  onPress,
  buttonMarginRight = TOOL_BUTTON_MARGIN,
  labelMarginRight = 10,
  width = buttonSize,
}) => {
  return (
    <View style={{ width, alignItems: 'center', marginRight: buttonMarginRight }}>
      <Touchable
        onPress={onPress}
        style={{
          width,
          height: buttonSize,
          backgroundColor: '#C1853C', // bg-primary
          borderRadius: 6,
          justifyContent: 'center',
          alignItems: 'center',
        }}
      >
        {icon}
      </Touchable>
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
