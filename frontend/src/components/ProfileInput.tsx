import { View, Text, TextInput } from 'react-native';
import { COLORS } from '../constants/Theme';

interface ProfileInputProps {
  label: string;
  value: string;
  onChange: (val: string) => void;
  placeholder?: string;
  editable?: boolean;
  /** Campo de solo lectura (p.ej. el email verificado): nunca editable y con estilo apagado */
  readOnly?: boolean;
}

/**
 * Componente de Átomo: Input reutilizable para el perfil
 * 
 * Nivel 7: Componente de UI
 * - Solo renderiza
 * - Recibe props
 * - Emite onChange
 */
export const ProfileInput = ({
  label,
  value,
  onChange,
  placeholder,
  editable = true,
  readOnly = false,
}: ProfileInputProps) => {
  return (
    <View className="flex-row mb-4 items-center gap-4">
      <Text className="font-semibold text-lg text-secondary min-w-[80px]">
        {label}:
      </Text>
      <TextInput
        className={`rounded-lg p-3 flex-1 border ${readOnly ? 'text-artistName border-primary/40' : 'text-primary border-primary'}`}
        value={value}
        onChangeText={onChange}
        placeholder={placeholder}
        placeholderTextColor={COLORS.primary}
        editable={editable && !readOnly}
        underlineColorAndroid="transparent"
        selectionColor={COLORS.primary}
      />
    </View>
  );
};

