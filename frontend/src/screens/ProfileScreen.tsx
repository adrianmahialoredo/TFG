import { ScrollView, View, Image, Text, TextInput, TouchableOpacity } from "react-native";
import AppLayout from "../components/AppLayout";
import { Copy } from "lucide-react-native";
import React from "react";
import Slider from '@react-native-community/slider';
import GenreViewer from "../components/GenreViewer";
import InstrumentViewer from "../components/InstrumentViewer";
import { COLORS } from "../constants/Theme";

const user = {
    id: "12345678",
    name: "John Doe",
    email: "adriansporarw",
    username: "adromeister",
    password: "password123",
    instrument: "Guitar",
    lessonsCompleted: 5,
    totalLessons: 20,
    profilePicture: "https://i.ibb.co/wr4BSxk6/36ffe640309721-5790d08d50f7d.png",
    bio: "Music enthusiast and aspiring guitarist.",
}

export default function LoginScreen() {
  return (
    <AppLayout>
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingBottom: 120 }}
      >
        <View className="flex-1 items-center justify-center mt-8">
            <Image
            source={{ uri: 'https://medialab.news/wp-content/uploads/2022/02/SSQIMD2N6RAD5EEXEVYSFYDVGM-1.jpg' }}
            className="h-32 w-32 rounded-full mb-4"
            />
            <Text className="text-2xl font-bold text-primary mb-3">@{user.username}</Text>
            <View className="flex-row gap-2">
              <Text className="text-xl font-bold text-secondary mb-3">#{user.id}</Text>
              <Copy size={20} color="white" strokeWidth={1} />
            </View>
        </View>
        
        <View className="flex-row justify-center">
          <Slider
            style={{ width: 200, height: 40 }}
            minimumValue={0}
            maximumValue={100}
            minimumTrackTintColor= {COLORS.primary}
            maximumTrackTintColor= {COLORS.primary}
            thumbTintColor= {COLORS.secondary}
          />
        </View>

        <View className="h-px bg-primary my-4 h-2.5 w-full" />

        <View>
          <View className="flex-row mb-4 items-center gap-8">
            <Text className="font-semibold text-lg text-secondary mb-2">Nombre:</Text>
            <TextInput
              className="text-primary border border-primary rounded-lg p-3 mb-2 flex-1"
              placeholder={user.name}
              placeholderTextColor={COLORS.primary}
            />
          </View>
          <View className="flex-row mb-4 items-center gap-9">
            <Text className="font-semibold text-lg text-secondary mb-2">Usuario:</Text>
            <TextInput
              className="text-primary border border-primary rounded-lg p-3 mb-2 flex-1"
              placeholder={user.username}
              placeholderTextColor={COLORS.primary}
            />
          </View>
          <View className="flex-row items-center gap-12">
            <Text className="font-semibold text-lg text-secondary mb-2 mr-1">Email:</Text>
            <TextInput
              className="text-primary border border-primary rounded-lg p-3 mb-2 flex-1"
              placeholder={user.email}
              placeholderTextColor={COLORS.primary}
            />
          </View>  
        </View>

        <View className="h-px bg-primary mb-4 mt-2 h-2.5 w-full" />

        <GenreViewer />

        <InstrumentViewer />

        <View className="h-px bg-primary mb-4 h-2.5 w-full" />

        <View className="flex-column justify-center align-center mt-4 gap-2">
          <TouchableOpacity className="bg-secondary px-4 py-3 rounded-full self-center">
            <Text className="text-primary text-lg self-center">Guardar ajustes</Text>
          </TouchableOpacity>
          <TouchableOpacity className="bg-spotify px-4 py-3 rounded-full self-center">
            <Text className="text-secondary text-lg self-center">Conectar con Spotify</Text>
          </TouchableOpacity>
          <TouchableOpacity className="bg-secondary px-4 py-3 rounded-full self-center">
            <Text className="text-primary text-lg self-center">Cambiar contraseña</Text>
          </TouchableOpacity>
          <TouchableOpacity className="bg-secondary px-4 py-3 rounded-full self-center">
            <Text className="text-primary text-lg self-center">Cerrar sesión</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </AppLayout>
  );
}