import React, { useState } from 'react';
import { ActivityIndicator, Text, TextInput, View } from 'react-native';
import { Lock, Mail } from 'lucide-react-native';
import { RouteProp, useNavigation, useRoute } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';

import AppLayout from '../components/AppLayout';
import Touchable from '../components/Touchable';
import BackButton from '../components/iconbuttons/BackButton';
import PasswordVisibilityToggle from '../components/iconbuttons/PasswordVisibilityToggle';
import EmailVerificationModal from '../components/EmailVerificationModal';
import { COLORS } from '../constants/Theme';
import { useAppAlert } from '../context/AlertModalContext';
import { useForgotPassword } from '../hooks/useForgotPassword';
import { RootStackParamList } from '../types';

type Navigation = NativeStackNavigationProp<RootStackParamList, 'ForgotPasswordScreen'>;
type Route = RouteProp<RootStackParamList, 'ForgotPasswordScreen'>;

/**
 * "Olvidé mi contraseña" (se abre desde el login):
 * 1. se escribe el email y se envía un código de 6 dígitos;
 * 2. se introduce el código en el mismo modal que el registro;
 * 3. con el código correcto, se elige la contraseña nueva y se vuelve al login.
 */
export default function ForgotPasswordScreen() {
  const navigation = useNavigation<Navigation>();
  const route = useRoute<Route>();
  const { showAlert } = useAppAlert();
  const {
    step,
    email,
    setEmail,
    codeError,
    isLoading,
    requestCode,
    resendCode,
    submitCode,
    resetPassword,
    cancelCode,
  } = useForgotPassword(route.params?.email ?? '');

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleSendCode = async () => {
    try {
      await requestCode();
    } catch (error: any) {
      showAlert({ title: 'Error', message: error.message });
    }
  };

  const handleReset = async () => {
    try {
      await resetPassword(password, confirmPassword);
      showAlert({
        title: 'Contraseña cambiada',
        message: 'Ya puedes iniciar sesión con tu nueva contraseña',
      });
      navigation.goBack();
    } catch (error: any) {
      showAlert({ title: 'Error', message: error.message });
    }
  };

  const choosingPassword = step === 'password';

  return (
    <AppLayout backgroundColor="bg-primary">
      <View className="flex-row items-center mt-4 mb-8 gap-2">
        <BackButton onPress={() => navigation.goBack()} size={30} color={COLORS.secondary} />
        <Text className="text-3xl font-bold text-secondary">Recuperar contraseña</Text>
      </View>

      <Text className="text-secondary text-base mb-6">
        {choosingPassword
          ? 'Código verificado. Elige tu nueva contraseña.'
          : 'Escribe el email de tu cuenta y te enviaremos un código para elegir una contraseña nueva.'}
      </Text>

      {choosingPassword ? (
        <>
          <FormInput
            placeholder="Nueva contraseña"
            value={password}
            onChangeText={setPassword}
            secureTextEntry={!showPassword}
            editable={!isLoading}
            right={
              <PasswordVisibilityToggle
                visible={showPassword}
                onToggle={() => setShowPassword((v) => !v)}
                disabled={isLoading}
                size={22}
              />
            }
          />
          <FormInput
            placeholder="Repetir contraseña"
            value={confirmPassword}
            onChangeText={setConfirmPassword}
            secureTextEntry={!showConfirmPassword}
            editable={!isLoading}
            right={
              <PasswordVisibilityToggle
                visible={showConfirmPassword}
                onToggle={() => setShowConfirmPassword((v) => !v)}
                disabled={isLoading}
                size={22}
              />
            }
          />
          <Text className="text-secondary/70 text-sm mb-6">
            Mínimo 8 caracteres, con una mayúscula, un número y un símbolo, y sin espacios.
          </Text>
        </>
      ) : (
        <FormInput
          placeholder="Email"
          value={email}
          onChangeText={setEmail}
          keyboardType="email-address"
          autoCapitalize="none"
          autoCorrect={false}
          autoComplete="email"
          editable={!isLoading}
          right={<Mail size={24} color={COLORS.secondary} />}
        />
      )}

      <View className="gap-3 mt-4">
        <Touchable
          className="bg-secondary px-6 py-3 rounded-lg w-full"
          style={{ opacity: isLoading ? 0.6 : 1 }}
          onPress={choosingPassword ? handleReset : handleSendCode}
          disabled={isLoading}
        >
          {isLoading && step !== 'code' ? (
            <ActivityIndicator color={COLORS.primary} />
          ) : (
            <Text className="text-primary text-lg self-center">
              {choosingPassword ? 'Cambiar contraseña' : 'Enviar código'}
            </Text>
          )}
        </Touchable>
        <Touchable
          className="bg-primary border border-secondary px-6 py-3 rounded-lg w-full"
          onPress={() => navigation.goBack()}
          disabled={isLoading}
        >
          <Text className="text-secondary text-lg self-center">Volver a iniciar sesión</Text>
        </Touchable>
      </View>

      {step === 'code' && (
        <EmailVerificationModal
          email={email.trim()}
          title="Recupera tu contraseña"
          submitting={isLoading}
          error={codeError}
          onSubmit={submitCode}
          onResend={resendCode}
          onClose={cancelCode}
        />
      )}
    </AppLayout>
  );
}

type FormInputProps = React.ComponentProps<typeof TextInput> & { right?: React.ReactNode };

/** Campo con el mismo aspecto que los del login, con un icono o botón a la derecha. */
const FormInput = ({ right, ...props }: FormInputProps) => (
  <View className="relative mb-3">
    <TextInput
      className="text-secondary border border-secondary rounded-lg p-4 pr-12"
      placeholderTextColor={COLORS.placeholder}
      underlineColorAndroid="transparent"
      selectionColor={COLORS.secondary}
      {...props}
    />
    <View className="absolute right-4 top-4 flex-row items-center">
      {right ?? <Lock size={22} color={COLORS.secondary} />}
    </View>
  </View>
);
