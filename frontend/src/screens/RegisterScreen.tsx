import React, { useState } from "react";
import { ScrollView, View, Text, TouchableOpacity, Alert, ActivityIndicator, TextInput } from "react-native";
import { ArrowLeft, Mail, UserRound, Lock, Speech } from "lucide-react-native";
import { useNavigation } from "@react-navigation/native";

import AppLayout from "../components/AppLayout";
import GenrePicker from "../components/GenrePicker";
import InstrumentPicker from "../components/InstrumentPicker";
import { COLORS } from "../constants/Theme";
import { useRegister } from "../hooks/useRegister"; // Importamos el especialista

export default function RegisterScreen() {
  const navigation = useNavigation();
  const { register, isLoading } = useRegister();

  const [form, setForm] = useState({
    name: '',
    email: '',
    username: '',
    password: '',
    confirmPassword: '',
    favouriteGenresIds: [] as string[],
    favouriteInstrumentsIds: [] as string[]
  });

  const handleRegister = async () => {
    try {
      await register(form);
      // No navegamos manualmente; el AppNavigator reaccionará al cambio de AuthStore
      Alert.alert('¡Éxito!', 'Creando tu perfil musical...');
    } catch (error: any) {
      Alert.alert('Error', error.message);
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
              value={form.email}
              onChange={(v: any) => setForm({...form, email: v})}
          />

          <RegisterInput
              placeholder="Usuario"
              icon={<UserRound size={22} color={COLORS.secondary}/>}
              value={form.username}
              onChange={(v: any) => setForm({...form, username: v})}
          />

          <RegisterInput
              placeholder="Contraseña"
              icon={<Lock size={22} color={COLORS.secondary}/>}
              secureTextEntry
              value={form.password}
              onChange={(v: any) => setForm({...form, password: v})}
          />

          <RegisterInput
              placeholder="Repetir contraseña"
              icon={<Lock size={22} color={COLORS.secondary}/>}
              secureTextEntry
              value={form.confirmPassword}
              onChange={(v: any) => setForm({...form, confirmPassword: v})}
          />

          <View className="mt-4 gap-y-4">
            <GenrePicker onSelectionChange={(ids) => setForm({...form, favouriteGenresIds: ids})} />
            <InstrumentPicker onSelectionChange={(ids) => setForm({...form, favouriteInstrumentsIds: ids})} />
          </View>

          <TouchableOpacity
              className="bg-secondary p-5 rounded-xl mt-8 shadow-sm"
              onPress={handleRegister}
              disabled={isLoading}
          >
            {isLoading ? (
                <ActivityIndicator color={COLORS.primary} />
            ) : (
                <Text className="text-primary text-center font-bold text-lg">CREAR CUENTA</Text>
            )}
          </TouchableOpacity>
        </ScrollView>
      </AppLayout>
  );
}

// Átomo reutilizable para inputs de registro
const RegisterInput = ({ icon, onChange, ...props }: any) => (
    <View className="relative mb-4">
      <TextInput
          className="text-secondary border border-secondary/30 rounded-xl p-4 pr-12 bg-black/10"
          placeholderTextColor={COLORS.placeholder}
          onChangeText={onChange}
          {...props}
      />
      <View className="absolute right-4 top-4">{icon}</View>
    </View>
);