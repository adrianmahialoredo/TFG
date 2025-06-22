import { TextInput, View, Text, TouchableOpacity, ScrollView } from "react-native";
import App from "../../App";
import AppLayout from "../components/AppLayout";
import { ArrowLeft, Mail, UserRound,Lock } from "lucide-react-native";
import GenrePicker from "../components/GenrePicker";
import InstrumentPicker from "../components/InstrumentPicker";
import { COLORS } from "../constants/Theme";
import { Colors } from "react-native/Libraries/NewAppScreen";

export default function RegisterScreen() {
  return (
    <AppLayout backgroundColor="bg-primary">
        <View className="flex-row justify-left items-center my-8 gap-2">
            <TouchableOpacity>
              <ArrowLeft size={30} color={COLORS.secondary} />  
            </TouchableOpacity>
            <Text className="text-3xl font-bold text-secondary">Registrarse</Text>
        </View>
        <ScrollView
            showsVerticalScrollIndicator={false}
            contentContainerStyle={{ paddingBottom: 120 }}
        >
            <View className="mb-7">
            <Text className="text-lg font-bold text-secondary">Bienvenido a Nombre</Text>
            <Text className="text-lg font-bold text-secondary mb-4">Regístrate y empieza a aprender música!</Text>
            </View>

            <View className="relative mb-4">
            <TextInput
                className="text-secondary border border-secondary rounded-lg p-4 mb-2 pr-12"
                placeholder="Email"
                placeholderTextColor={COLORS.placeholder}
                keyboardType="email-address"
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
            />
            <View className="absolute right-4 top-1/3 transform -translate-y-1/4">
                <Lock size={25} color={COLORS.secondary} />
            </View>
            </View>
            
            <GenrePicker />
            <InstrumentPicker/>

            <View className="flex-row justify-center mt-4">
              <TouchableOpacity className="bg-secondary px-6 py-5 rounded-lg w-full">
                <Text className="text-primary text-lg self-center">Crear Cuenta</Text>
              </TouchableOpacity>
            </View>
        </ScrollView>
    </AppLayout>
  );
}