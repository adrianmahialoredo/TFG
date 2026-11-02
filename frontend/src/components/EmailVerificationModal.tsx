import React, { useEffect, useState } from 'react';
import { ActivityIndicator, Platform, Text, TextInputProps, View } from 'react-native';
import Touchable from './Touchable';
import { CodeField, Cursor, useBlurOnFulfill, useClearByFocusCell } from 'react-native-confirmation-code-field';
import BaseModal from './BaseModal';
import { COLORS } from '../constants/Theme';

const CELL_COUNT = 6;
/** Igual que RESEND_COOLDOWN en el backend (EmailVerificationServiceImpl) */
const RESEND_SECONDS = 60;

// Autocompletado del código desde el teclado (iOS lo lee del email/SMS si puede)
const autoComplete = Platform.select<TextInputProps['autoComplete']>({
  android: 'sms-otp',
  default: 'one-time-code',
});

type EmailVerificationModalProps = {
  email: string;
  /** Registro en curso con el código: se bloquea el campo y se muestra el spinner */
  submitting: boolean;
  /** Error del último intento (código incorrecto, caducado...) */
  error: string | null;
  onSubmit: (code: string) => void;
  onResend: () => Promise<void>;
  onClose: () => void;
};

/**
 * Paso final del registro: el usuario escribe el código de 6 dígitos que le llegó por email.
 * Al completar las 6 cifras se envía solo. El padre lo monta solo mientras está abierto,
 * así que cada apertura empieza con el campo vacío y la cuenta atrás del reenvío a cero.
 */
export default function EmailVerificationModal({
  email,
  submitting,
  error,
  onSubmit,
  onResend,
  onClose,
}: EmailVerificationModalProps) {
  const [code, setCode] = useState('');
  const [secondsLeft, setSecondsLeft] = useState(RESEND_SECONDS);
  const [resendError, setResendError] = useState<string | null>(null);
  const [resending, setResending] = useState(false);

  const ref = useBlurOnFulfill({ value: code, cellCount: CELL_COUNT });
  const [fieldProps, getCellOnLayoutHandler] = useClearByFocusCell({ value: code, setValue: setCode });

  // Cuenta atrás para poder pedir otro código
  useEffect(() => {
    if (secondsLeft <= 0) return;
    const timer = setTimeout(() => setSecondsLeft((s) => s - 1), 1000);
    return () => clearTimeout(timer);
  }, [secondsLeft]);

  // Un intento fallido vacía el campo para escribir el código de nuevo (también si el error se repite)
  useEffect(() => {
    if (!submitting && error) setCode('');
  }, [submitting, error]);

  const handleChange = (value: string) => {
    const digits = value.replace(/\D/g, '');
    setCode(digits);
    if (digits.length === CELL_COUNT) onSubmit(digits);
  };

  const handleResend = async () => {
    setResending(true);
    setResendError(null);
    try {
      await onResend();
      setCode('');
      setSecondsLeft(RESEND_SECONDS);
    } catch (e: any) {
      setResendError(e?.message ?? 'No se pudo reenviar el código');
    } finally {
      setResending(false);
    }
  };

  const message = resendError ?? error;

  return (
    <BaseModal visible onClose={submitting ? () => {} : onClose} maxWidth={340}>
      <Text className="text-primary text-xl font-bold text-center mb-2">Verifica tu email</Text>
      <Text className="text-secondary text-center mb-5">
        Hemos enviado un código de {CELL_COUNT} dígitos a{'\n'}
        <Text className="font-semibold">{email}</Text>
      </Text>

      <CodeField
        ref={ref}
        {...fieldProps}
        value={code}
        onChangeText={handleChange}
        cellCount={CELL_COUNT}
        editable={!submitting}
        autoFocus
        keyboardType="number-pad"
        textContentType="oneTimeCode"
        autoComplete={autoComplete}
        rootStyle={{ justifyContent: 'space-between', gap: 6 }}
        renderCell={({ index, symbol, isFocused }) => (
          <View
            key={index}
            onLayout={getCellOnLayoutHandler(index)}
            style={{
              width: 40,
              height: 50,
              borderRadius: 10,
              borderWidth: 1.5,
              borderColor: isFocused ? COLORS.primary : 'rgba(236, 236, 236, 0.3)',
              backgroundColor: 'rgba(193, 133, 60, 0.1)',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Text style={{ color: COLORS.secondary, fontSize: 24, fontWeight: '600' }}>
              {symbol || (isFocused ? <Cursor /> : null)}
            </Text>
          </View>
        )}
      />

      <View className="min-h-6 mt-3 items-center justify-center">
        {submitting ? (
          <ActivityIndicator color={COLORS.primary} />
        ) : (
          message && <Text className="text-red-500 text-sm text-center">{message}</Text>
        )}
      </View>

      <View className="flex-row justify-between items-center mt-3">
        <Touchable onPress={onClose} disabled={submitting} style={{ paddingVertical: 8 }}>
          <Text className="text-secondary font-semibold">Cancelar</Text>
        </Touchable>

        <Touchable
          onPress={handleResend}
          disabled={secondsLeft > 0 || resending || submitting}
          style={{ paddingVertical: 8 }}
        >
          <Text
            style={{ color: secondsLeft > 0 || resending ? COLORS.artistName : COLORS.primary, fontWeight: '600' }}
          >
            {resending ? 'Enviando...' : secondsLeft > 0 ? `Reenviar en ${secondsLeft}s` : 'Reenviar código'}
          </Text>
        </Touchable>
      </View>
    </BaseModal>
  );
}
