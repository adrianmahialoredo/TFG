import { TextInput, View, Text, TouchableOpacity, ScrollView, Alert } from "react-native";
import App from "../../App";
import AppLayout from "../components/AppLayout";
import { ArrowLeft, Mail, UserRound,Lock, Speech } from "lucide-react-native";
import GenrePicker from "../components/GenrePicker";
import InstrumentPicker from "../components/InstrumentPicker";
import { COLORS } from "../constants/Theme";
import { useState } from "react";
import { register } from "../services/auth.service";
import { useNavigation } from "@react-navigation/native";
import { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { RootStackParamList } from "../types/RootStackParamList";

export default function RegisterScreen() {

  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [favouriteGenresIds, setFavouriteGenresIds] = useState<string[]>([]);

  const handleRegister = async () => {
    if (password !== confirmPassword) {
      Alert.alert('Error', 'Password does not match');
      return;
    }

    const newUser = { email, username, name, password, favouriteGenresIds }

    try {
      const { user } = await register(newUser);
      console.log('User registered:', user);
      navigation.navigate("LoginScreen")
    } catch (error: any) {
      console.error('Error al registrar:', error);
      Alert.alert('Error', error?.response?.data?.message || 'No se pudo registrar');
    }
  };

  return (
    <AppLayout backgroundColor="bg-primary">
        <View className="flex-row justify-left items-center my-8 gap-2">
          <TouchableOpacity>
            <ArrowLeft 
              size={30} 
              color={COLORS.secondary}
              onPress={() => navigation.navigate("LoginScreen")}
            />  
          </TouchableOpacity>
          <Text className="text-3xl font-bold text-secondary">Registrarse</Text>
        </View>
        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={{ paddingBottom: 120 }}
        >
            <View className="mb-7">
              <Text className="text-lg font-bold text-secondary">Bienvenido a Toco</Text>
              <Text className="text-lg font-bold text-secondary mb-4">Regístrate y empieza a aprender música!</Text>
            </View>

            <View className="relative mb-4">
            <TextInput
                className="text-secondary border border-secondary rounded-lg p-4 mb-2 pr-12"
                placeholder="Nombre"
                placeholderTextColor={COLORS.placeholder}
                keyboardType="email-address"
                value={name}
                onChangeText={setName}
            />
            <View className="absolute right-4 top-1/3 transform -translate-y-1/4">
                <Speech size={25} color={COLORS.secondary} />
            </View>
            </View>

            <View className="relative mb-4">
            <TextInput
                className="text-secondary border border-secondary rounded-lg p-4 mb-2 pr-12"
                placeholder="Email"
                placeholderTextColor={COLORS.placeholder}
                keyboardType="email-address"
                value={email}
                onChangeText={setEmail}
            />
            <View className="absolute right-4 top-1/3 transform -translate-y-1/4">
                <Mail size={25} color={COLORS.secondary} />
            </View>
            </View>

            <View className="relative mb-4">
            <TextInput
                className="text-secondary border border-secondary rounded-lg p-4 mb-2 pr-12"
                placeholder="Usuario"
                placeholderTextColor={COLORS.placeholder}
                keyboardType="email-address"
                value={username}
                onChangeText={setUsername}
            />
            <View className="absolute right-4 top-1/3 transform -translate-y-1/4">
                <UserRound size={25} color={COLORS.secondary} />
            </View>
            </View>

            <View className="relative mb-4">
            <TextInput
                className="text-secondary border border-secondary rounded-lg p-4 mb-2 pr-12"
                placeholder="Contraseña"
                placeholderTextColor={COLORS.placeholder}
                secureTextEntry
                value={password}
                onChangeText={setPassword}
            />
            <View className="absolute right-4 top-1/3 transform -translate-y-1/4">
                <Lock size={25} color={COLORS.secondary} />
            </View>
            </View>

            <View className="relative mb-8">
              <TextInput
                className="text-secondary border border-secondary rounded-lg p-4 mb-2 pr-12"
                placeholder="Repetir contraseña"
                placeholderTextColor={COLORS.placeholder}
                secureTextEntry
                value={confirmPassword}
                onChangeText={setConfirmPassword}
              />
              <View className="absolute right-4 top-1/3 transform -translate-y-1/4">
                <Lock size={25} color={COLORS.secondary} />
              </View>
            </View>
            
            <GenrePicker onSelectionChange={(ids) => setFavouriteGenresIds(ids)} />
            <InstrumentPicker/>

            <View className="flex-row justify-center mt-4">
              <TouchableOpacity className="bg-secondary px-6 py-5 rounded-lg w-full" onPress={handleRegister} >
                <Text className="text-primary text-lg self-center">Crear Cuenta</Text>
              </TouchableOpacity>
            </View>
        </ScrollView>
    </AppLayout>
  );
}