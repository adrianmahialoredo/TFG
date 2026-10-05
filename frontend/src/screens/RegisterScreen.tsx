import React, { useState } from "react";
import { ScrollView, View, Text, TouchableOpacity, ActivityIndicator, TextInput, Pressable } from "react-native";
import { ArrowLeft, Mail, UserRound, Lock, Speech, Eye, EyeOff } from "lucide-react-native";
import { useNavigation } from "@react-navigation/native";

import AppLayout from "../components/AppLayout";
import GenrePicker from "../components/GenrePicker";
import InstrumentPicker from "../components/InstrumentPicker";
import { COLORS } from "../constants/Theme";
import { useRegister } from "../hooks/useRegister"; // Importamos el especialista
import { useAppAlert } from "../context/AlertModalContext";

export default function RegisterScreen() {
  const navigation = useNavigation();
  const { register, isLoading } = useRegister();
  const { showAlert } = useAppAlert();

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

  const handleRegister = async () => {
    try {
      await register(form);
      // No navegamos manualmente; el AppNavigator reaccionará al cambio de AuthStore
      showAlert({ title: 'Exito', message: 'Creando tu perfil musical...' });
    } catch (error: any) {
      showAlert({ title: 'Error', message: error.message });
    }
  };

  return (
      <AppLayout backgroundColor="bg-primary">
        <View className="flex-row items-center my-8 gap-2">
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <ArrowLeft size={30} color={COLORS.secondary} />
          </TouchableOpacity>
          <Text className="text-3xl font-bold text-secondary">Registrarse</Text>
        </View>

        <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={{ paddingBottom: 120 }}>

          <RegisterInput
              placeholder="Nombre completo"
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
              icon={<Lock size={22} color={COLORS.secondary} />}
              secureTextEntry={!showPassword}
              autoCapitalize="none"
              autoCorrect={false}
              autoComplete="new-password"
              textContentType="newPassword"
              rightElement={
                <TouchableOpacity
                  onPress={() => setShowPassword((prev) => !prev)}
                  disabled={isLoading}
                  hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                >
                  {showPassword ? (
                    <EyeOff size={20} color={COLORS.secondary} />
                  ) : (
                    <Eye size={20} color={COLORS.secondary} />
                  )}
                </TouchableOpacity>
              }
              value={form.password}
              onChange={(v: any) => setForm({...form, password: v})}
          />

          <RegisterInput
              placeholder="Repetir contraseña"
              icon={<Lock size={22} color={COLORS.secondary} />}
              secureTextEntry={!showConfirmPassword}
              autoCapitalize="none"
              autoCorrect={false}
              autoComplete="new-password"
              textContentType="newPassword"
              rightElement={
                <TouchableOpacity
                  onPress={() => setShowConfirmPassword((prev) => !prev)}
                  disabled={isLoading}
                  hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                >
                  {showConfirmPassword ? (
                    <EyeOff size={20} color={COLORS.secondary} />
                  ) : (
                    <Eye size={20} color={COLORS.secondary} />
                  )}
                </TouchableOpacity>
              }
              value={form.confirmPassword}
              onChange={(v: any) => setForm({...form, confirmPassword: v})}
          />

          <View className="mt-4 gap-y-4">
            <GenrePicker onSelectionChange={(ids) => setForm(prev => ({...prev, favouriteGenresIds: ids}))} />
            <InstrumentPicker onSelectionChange={(ids) => setForm(prev => ({...prev, favouriteInstrumentsIds: ids}))} />
          </View>

          {/* Pressable en vez de TouchableOpacity: su animación de opacidad se pisaba con el estado
              deshabilitado y la sombra. La opacidad se calcula aquí, en la View interior. */}
          <Pressable onPress={handleRegister} disabled={isLoading} className="mt-4">
            {({ pressed }) => (
                <View
                    className="bg-secondary p-5 rounded-xl shadow-sm"
                    style={{ opacity: isLoading ? 0.6 : pressed ? 0.7 : 1 }}
                >
                  {isLoading ? (
                      <ActivityIndicator color={COLORS.primary} />
                  ) : (
                      <Text className="text-primary text-center font-bold text-md">Crear cuenta</Text>
                  )}
                </View>
            )}
          </Pressable>
        </ScrollView>
      </AppLayout>
  );
}

// Átomo reutilizable para inputs de registro
const RegisterInput = ({ icon, rightElement, onChange, ...props }: any) => (
    <View className="relative mb-4">
      <TextInput
          className="text-secondary border border-secondary/30 rounded-xl p-4 pr-12 bg-black/10"
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