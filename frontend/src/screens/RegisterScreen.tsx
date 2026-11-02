import React, { useState } from "react";
import { View, Text, ActivityIndicator, TextInput, Pressable } from "react-native";
import FadeScrollView from "../components/FadeScrollView";
import { Mail, UserRound, Speech } from "lucide-react-native";
import { useNavigation } from "@react-navigation/native";

import AppLayout from "../components/AppLayout";
import GenrePicker from "../components/GenrePicker";
import InstrumentPicker from "../components/InstrumentPicker";
import { COLORS } from "../constants/Theme";
import BackButton from "../components/iconbuttons/BackButton";
import PasswordVisibilityToggle from "../components/iconbuttons/PasswordVisibilityToggle";
import { useRegister } from "../hooks/useRegister"; // Importamos el especialista
import { HELD_OPACITY, useHeldPress } from "../hooks/useHeldPress";
import { useAppAlert } from "../context/AlertModalContext";
import EmailVerificationModal from "../components/EmailVerificationModal";

export default function RegisterScreen() {
  const navigation = useNavigation();
  const { requestCode, resendCode, register, isLoading } = useRegister();
  const { showAlert } = useAppAlert();
  const { held: submitHeld, ...submitPress } = useHeldPress();

  const [form, setForm] = useState({
    name: '',
    email: '',
    username: '',
    password: '',
    confirmPassword: '',
    favouriteGenresIds: [] as string[],
    favouriteInstrumentsIds: [] as string[]
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  // Paso de verificación: abierto tras enviar el código, con el error del último intento
  const [verifying, setVerifying] = useState(false);
  const [codeError, setCodeError] = useState<string | null>(null);

  // Paso 1: validar el formulario y enviar el código al email
  const handleRegister = async () => {
    try {
      await requestCode(form);
      setCodeError(null);
      setVerifying(true);
    } catch (error: any) {
      if (error.status === 429) {
        // Ya se envió un código hace menos de un minuto (p.ej. se cerró el modal para corregir
        // el usuario) y sigue siendo válido: se vuelve al paso del código
        setCodeError('Ya te enviamos un código hace poco, revisa tu email');
        setVerifying(true);
        return;
      }
      showAlert({ title: 'Error', message: error.message });
    }
  };

  // Paso 2: crear la cuenta con el código
  const handleCodeSubmit = async (code: string) => {
    setCodeError(null);
    try {
      await register(form, code);
      setVerifying(false);
      // No navegamos manualmente; el AppNavigator reaccionará al cambio de AuthStore
      showAlert({ title: 'Exito', message: 'Creando tu perfil musical...' });
    } catch (error: any) {
      setCodeError(error.message);
    }
  };

  return (
      <AppLayout backgroundColor="bg-primary">
        <View className="flex-row items-center mt-8 mb-8 gap-2">
          <BackButton onPress={() => navigation.goBack()} size={30} color={COLORS.secondary} />
          <Text className="text-3xl font-bold text-secondary">Registrarse</Text>
        </View>

        <FadeScrollView showsVerticalScrollIndicator={false} contentContainerStyle={{ paddingBottom: 120 }}>

          <RegisterInput
              placeholder="Nombre"
              icon={<Speech size={22} color={COLORS.secondary}/>}
              value={form.name}
              onChange={(v: any) => setForm({...form, name: v})}
          />

          <RegisterInput
              placeholder="Email"
              icon={<Mail size={22} color={COLORS.secondary}/>}
              keyboardType="email-address"
              autoCapitalize="none"
              autoCorrect={false}
              autoComplete="email"
              value={form.email}
              onChange={(v: any) => setForm({...form, email: v})}
          />

          <RegisterInput
              placeholder="Usuario"
              icon={<UserRound size={22} color={COLORS.secondary}/>}
              autoCapitalize="none"
              autoCorrect={false}
              value={form.username}
              onChange={(v: any) => setForm({...form, username: v})}
          />

          <RegisterInput
              placeholder="Contraseña"
              secureTextEntry={!showPassword}
              autoCapitalize="none"
              autoCorrect={false}
              autoComplete="new-password"
              textContentType="newPassword"
              rightElement={
                <PasswordVisibilityToggle
                  visible={showPassword}
                  onToggle={() => setShowPassword((prev) => !prev)}
                  disabled={isLoading}
                />
              }
              value={form.password}
              onChange={(v: any) => setForm({...form, password: v})}
          />

          <RegisterInput
              placeholder="Repetir contraseña"
              secureTextEntry={!showConfirmPassword}
              autoCapitalize="none"
              autoCorrect={false}
              autoComplete="new-password"
              textContentType="newPassword"
              rightElement={
                <PasswordVisibilityToggle
                  visible={showConfirmPassword}
                  onToggle={() => setShowConfirmPassword((prev) => !prev)}
                  disabled={isLoading}
                />
              }
              value={form.confirmPassword}
              onChange={(v: any) => setForm({...form, confirmPassword: v})}
          />

          <View className="mt-8 gap-y-2">
            <GenrePicker onSelectionChange={(ids) => setForm(prev => ({...prev, favouriteGenresIds: ids}))} />
            <InstrumentPicker onSelectionChange={(ids) => setForm(prev => ({...prev, favouriteInstrumentsIds: ids}))} />
          </View>

          {/* Pressable con la opacidad en la View interior: se calcula junto al estado deshabilitado
              y la sombra. Solo se atenúa al mantener el dedo, no con un toque. */}
          <Pressable onPress={handleRegister} disabled={isLoading} className="mt-5" {...submitPress}>
            <View
                className="bg-secondary p-4 rounded-lg shadow-sm"
                style={{ opacity: isLoading ? 0.6 : submitHeld ? HELD_OPACITY : 1 }}
            >
              {isLoading ? (
                  <ActivityIndicator color={COLORS.primary} />
              ) : (
                  <Text className="text-primary text-center font-medium text-lg">Crear cuenta</Text>
              )}
            </View>
          </Pressable>
        </FadeScrollView>

        {verifying && (
            <EmailVerificationModal
                email={form.email.trim()}
                submitting={isLoading}
                error={codeError}
                onSubmit={handleCodeSubmit}
                onResend={() => resendCode(form.email)}
                onClose={() => setVerifying(false)}
            />
        )}
      </AppLayout>
  );
}

// Átomo reutilizable para inputs de registro
const RegisterInput = ({ icon, rightElement, onChange, ...props }: any) => (
    <View className="relative mb-3">
      <TextInput
          className="text-secondary border border-secondary/30 rounded-lg p-4 pr-12 bg-black/10"
          placeholderTextColor={COLORS.placeholder}
          onChangeText={onChange}
          underlineColorAndroid="transparent"
          selectionColor={COLORS.secondary}
          {...props}
      />
      <View className="absolute right-4 top-4 flex-row items-center gap-3">
        {rightElement}
        {icon}
      </View>
    </View>
);