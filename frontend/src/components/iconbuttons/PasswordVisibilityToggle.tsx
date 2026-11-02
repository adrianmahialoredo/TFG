import React from 'react';
import Touchable from '../Touchable';
import { Eye, EyeOff } from 'lucide-react-native';
import { COLORS } from '../../constants/Theme';

type PasswordVisibilityToggleProps = {
  /** La contraseña se está mostrando: se pinta el ojo tachado (pulsar la oculta). */
  visible: boolean;
  onToggle: () => void;
  disabled?: boolean;
  size?: number;
  color?: string;
};

const HIT_SLOP = { top: 8, bottom: 8, left: 8, right: 8 };

/**
 * Ojo de mostrar/ocultar contraseña (login y registro).
 */
export default function PasswordVisibilityToggle({
  visible,
  onToggle,
  disabled = false,
  size = 20,
  color = COLORS.secondary,
}: PasswordVisibilityToggleProps) {
  const Icon = visible ? EyeOff : Eye;

  return (
    <Touchable
      onPress={onToggle}
      disabled={disabled}
      hitSlop={HIT_SLOP}
      accessibilityRole="button"
      accessibilityLabel={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
    >
      <Icon size={size} color={color} />
    </Touchable>
  );
}
