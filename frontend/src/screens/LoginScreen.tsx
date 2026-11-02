import { Image, Text, TextInput, View, ActivityIndicator } from "react-native";
import Touchable from "../components/Touchable";
import AppLayout from "../components/AppLayout";
import { UserRound, Lock } from "lucide-react-native";
import { COLORS } from "../constants/Theme";
import PasswordVisibilityToggle from "../components/iconbuttons/PasswordVisibilityToggle";
import { useAuth } from "../context/AuthContext";
import { useState } from "react";
import { useNavigation } from "@react-navigation/native";
import { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { RootStackParamList } from "../types/RootStackParamList";
import { useAppAlert } from "../context/AlertModalContext";

export default function LoginScreen() {
  const { login } = useAuth();
  const { showAlert } = useAppAlert();
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList, 'LoginScreen'>>();

  // Email o nombre de usuario
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const validateForm = (): boolean => {
    if (!identifier.trim()) {
      showAlert({ title: 'Error', message: 'Por favor ingresa tu email o tu usuario' });
      return false;
    }
    if (!password.trim()) {
      showAlert({ title: 'Error', message: 'Por favor ingresa tu contrasena' });
      return false;
    }
    return true;
  };

  const handleLogin = async () => {
    if (!validateForm()) return;

    setLoading(true);
    try {
      await login(identifier, password);
      // La navegación se hace automáticamente desde RootNavigator al cambiar isAuthenticated
      navigation.reset({
        index: 0,
        routes: [{ name: 'BottomTab' }],
      });
    } catch (error: unknown) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : 'Usuario, email o contraseña incorrectos';
      showAlert({ title: 'Error de Inicio de Sesion', message: errorMessage });
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppLayout backgroundColor="bg-primary">
      <View className="items-center mt-16 mb-20">
        <Image
          source={{ uri: 'https://storage.googleapis.com/pr-newsroom-wp/1/2023/05/Spotify_Primary_Logo_RGB_White.png' }}
          className="h-32 w-32 mb-7"
        />
        <Text className="text-4xl font-bold text-secondary mb-4">Bienvenido a Toco</Text>
        <Text className="text-xl font-bold text-secondary">¡Empieza a aprender música!</Text>
      </View>

      <View className="relative mb-2">
        <TextInput
          value={identifier}
          onChangeText={setIdentifier}
          className="text-secondary border border-secondary rounded-lg p-4 mb-2 pr-12"
          placeholder="Email o Usuario"
          placeholderTextColor={COLORS.placeholder}
          // Teclado normal: también se puede escribir el nombre de usuario
          autoCapitalize="none"
          autoCorrect={false}
          autoComplete="username"
          editable={!loading}
          underlineColorAndroid="transparent"
          selectionColor={COLORS.secondary}
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
          secureTextEntry={!showPassword}
          editable={!loading}
          underlineColorAndroid="transparent"
          selectionColor={COLORS.secondary}
        />
        <View className="absolute right-4 top-1/3 transform -translate-y-1/4 flex-row items-center gap-3">
          <PasswordVisibilityToggle
            visible={showPassword}
            onToggle={() => setShowPassword((prev) => !prev)}
            disabled={loading}
            size={22}
          />
        </View>
      </View>

      <View className="flex-column justify-center gap-3 mt-4">
        <Touchable
          className="bg-secondary px-6 py-3 rounded-lg w-full disabled:opacity-50"
          onPress={handleLogin}
          disabled={loading}
        >
          {loading ? (
            <ActivityIndicator color={COLORS.primary} />
          ) : (
            <Text className="text-primary text-lg self-center">Iniciar Sesión</Text>
          )}
        </Touchable>
        <Touchable
          className="bg-primary border border-secondary px-6 py-3 rounded-lg w-full"
          onPress={() => navigation.navigate("RegisterScreen")}
          disabled={loading}
        >
          <Text className="text-secondary text-lg self-center">Registrarse</Text>
        </Touchable>
      </View>
    </AppLayout>
  );
}