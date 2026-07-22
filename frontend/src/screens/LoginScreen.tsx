import { Image, Text, TextInput, TouchableOpacity, View, Alert, ActivityIndicator } from "react-native";
import AppLayout from "../components/AppLayout";
import { UserRound, Lock } from "lucide-react-native";
import { COLORS } from "../constants/Theme";
import { useAuth } from "../context/AuthContext";
import { useState } from "react";
import { useNavigation } from "@react-navigation/native";
import { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { RootStackParamList } from "../types/RootStackParamList";

export default function LoginScreen() {
  const { login } = useAuth();
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList, 'LoginScreen'>>();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const validateForm = (): boolean => {
    if (!email.trim()) {
      Alert.alert('Error', 'Por favor ingresa tu email');
      return false;
    }
    if (!password.trim()) {
      Alert.alert('Error', 'Por favor ingresa tu contraseña');
      return false;
    }
    if (!email.includes('@')) {
      Alert.alert('Error', 'Por favor ingresa un email válido');
      return false;
    }
    return true;
  };

  const handleLogin = async () => {
    if (!validateForm()) return;

    setLoading(true);
    try {
      await login(email, password);
      // La navegación se hace automáticamente desde RootNavigator al cambiar isAuthenticated
      navigation.reset({
        index: 0,
        routes: [{ name: 'BottomTab' }],
      });
    } catch (error: any) {
      console.error('Login failed:', error);
      const errorMessage = error?.response?.data?.message || 'Email o contraseña incorrectos';
      Alert.alert('Error de Inicio de Sesión', errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppLayout backgroundColor="bg-primary">
      <View className="items-center mt-24 mb-10">
        <Image
          source={{ uri: 'https://storage.googleapis.com/pr-newsroom-wp/1/2023/05/Spotify_Primary_Logo_RGB_White.png' }}
          className="h-32 w-32 mb-7"
        />
        <Text className="text-4xl font-bold text-secondary mb-4">Bienvenido</Text>
        <Text className="text-2xl font-bold text-secondary">Empieza a aprender música!</Text>
      </View>

      <View className="relative mb-4">
        <TextInput
          value={email}
          onChangeText={setEmail}
          className="text-secondary border border-secondary rounded-lg p-4 mb-2 pr-12"
          placeholder="Email o Usuario"
          placeholderTextColor={COLORS.placeholder}
          keyboardType="email-address"
          editable={!loading}
        />
        <View className="absolute right-4 top-1/3 transform -translate-y-1/4">
          <UserRound size={25} color={COLORS.secondary} />
        </View>
      </View>

      <View className="relative mb-10">
        <TextInput
          value={password}
          onChangeText={setPassword}
          className="text-secondary border border-secondary rounded-lg p-4 mb-2 pr-12"
          placeholder="Contraseña"
          placeholderTextColor={COLORS.placeholder}
          secureTextEntry
          editable={!loading}
        />
        <View className="absolute right-4 top-1/3 transform -translate-y-1/4">
          <Lock size={25} color={COLORS.secondary} />
        </View>
      </View>

      <View className="flex-column justify-center gap-4 mt-4">
        <TouchableOpacity
          className="bg-secondary px-6 py-3 rounded-lg w-full disabled:opacity-50"
          onPress={handleLogin}
          disabled={loading}
        >
          {loading ? (
            <ActivityIndicator color={COLORS.primary} />
          ) : (
            <Text className="text-primary text-lg self-center">Iniciar Sesión</Text>
          )}
        </TouchableOpacity>
        <TouchableOpacity
          className="bg-primary border border-secondary px-6 py-3 rounded-lg w-full"
          onPress={() => navigation.navigate("RegisterScreen")}
          disabled={loading}
        >
          <Text className="text-secondary text-lg self-center">Registrarse</Text>
        </TouchableOpacity>
      </View>
    </AppLayout>
  );
}