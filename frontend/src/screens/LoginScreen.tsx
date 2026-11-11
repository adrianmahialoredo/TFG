import { Image, Text, TextInput, View, ActivityIndicator } from "react-native";
import Touchable from "../components/Touchable";
import AppLayout from "../components/AppLayout";
import { UserRound, Lock } from "lucide-react-native";
import { COLORS, ICON_URL } from "../constants/Theme";
import PasswordVisibilityToggle from "../components/iconbuttons/PasswordVisibilityToggle";
import { useAuth } from "../context/AuthContext";
import { useState } from "react";
import { useNavigation } from "@react-navigation/native";
import { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { RootStackParamList } from "../types";
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
      showAlert({ title: 'Error', message: 'Por favor ingresa tu contraseña' });
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
      showAlert({ title: 'Error de inicio de sesión', message: errorMessage });
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppLayout backgroundColor="bg-primary">
      <View className="items-center mt-16 mb-20">
        <Image
          source={{ uri: ICON_URL }}
          className="h-36 w-36 mb-7"
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

      <View className="relative">
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

      {/* Como en el mockup: a la derecha, bajo la contraseña. Si ya hay un email escrito, se lleva */}
      <View className="items-end mb-8">
        <Touchable
          onPress={() =>
            navigation.navigate('ForgotPasswordScreen', identifier.includes('@') ? { email: identifier.trim() } : undefined)
          }
          disabled={loading}
          hitSlop={8}
          style={{ paddingVertical: 6 }}
        >
          <Text
            className="text-secondary font-bold"
            style={{ textShadowColor: 'rgba(0, 0, 0, 0.35)', textShadowOffset: { width: 0, height: 2 }, textShadowRadius: 4 }}
          >
            Olvidé mi contraseña
          </Text>
        </Touchable>
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