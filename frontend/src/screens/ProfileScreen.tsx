import { ScrollView, View, Image, Text, TextInput, TouchableOpacity, Modal, Pressable, Alert } from "react-native";
import AppLayout from "../components/AppLayout";
import { Copy, List } from "lucide-react-native";
import React, { useEffect, useState } from "react";
import Slider from '@react-native-community/slider';
import GenreViewer from "../components/GenreViewer";
import InstrumentViewer from "../components/InstrumentViewer";
import { COLORS } from "../constants/Theme";
import { getStoredUser, storeUserData } from "../local/user.local";
import { UserDTO } from "../types/UserDTO";
import * as ImagePicker from 'expo-image-picker';
import { updateUserProfile } from "../services/user.service";
import * as Clipboard from 'expo-clipboard';
import ListCarousel from "../components/carousels/ListCarousel";
import { SpotifyButton } from "../components/buttons/SpotifyButton";

export default function ProfileScreen() {

  const [user, setUser] = useState<UserDTO | null>(null);
  const [token, setToken] = useState<string>();

  const [name, setName] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');

  const [modalVisible, setModalVisible] = useState(false);
  const [viewImageModal, setViewImageModal] = useState(false)

  useEffect(() => {
    const loadUser = async () => {
      const data = await getStoredUser();
      if (data) {
        setUser(data.user);
        setToken(data.token);
        setName(data.user.name || '');
        setUsername(data.user.username || '');
        setEmail(data.user.email || '');
      }
    };

    loadUser();
  }, []);

  const changePic = async () => {
    const result = await ImagePicker.launchImageLibraryAsync({
      quality: 1,
      allowsEditing: true,
    });

    if (!result.canceled && result.assets.length > 0) {
      const newPic = result.assets[0].uri;

      const uploadedUrl = await uploadToCloudinary(newPic);
      if (!uploadedUrl) return;

      const updatedUser = { ...user, profileImageUrl: uploadedUrl } as UserDTO;
      setUser(updatedUser);
      if (token) {
        await storeUserData(token, updatedUser);
        await updateUserProfile(updatedUser);
        alert("Foto de perfil actualizada correctamente");
      }
      setModalVisible(false);
    }
  };

  const uploadToCloudinary = async (localUri: string): Promise<string | null> => {

    const formData = new FormData();
    formData.append('file', {
      uri: localUri,
      name: 'profile.jpg',
      type: 'image/jpeg',
    } as any);

    formData.append('upload_preset', 'upload_profile_pic');

    try {
      const response = await fetch('https://api.cloudinary.com/v1_1/dvm8qkmuo/image/upload', {
        method: 'POST',
        body: formData,
      });

      const data = await response.json();
      return data.secure_url;
    } catch (error) {
      console.error('Upload error:', error);
      return null;
    }

  };

  const updateUser = async () => {
    if (user && token) {
      const updatedUser: UserDTO = {
        ...user,
        name,
        username,
        email,  
      };
      await storeUserData(token, updatedUser);
      await updateUserProfile(updatedUser);
      setUser(updatedUser);
      Alert.alert("Perfil actualizado correctamente");
    }
  };

  const handleCopy = async () => {
    await Clipboard.setStringAsync(user?.id?.toString() || '');
    /* Alert.alert("El texto se ha copiado al portapapeles correctamente"); /* poner? */
  };

  return (
    <AppLayout>
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingBottom: 120 }}
      >
        <View className="flex-1 items-center justify-center mt-8">
          <TouchableOpacity onPress={() => setModalVisible(true)}>
            <Image
              source={{ uri: user?.profileImageUrl }}
              className="h-32 w-32 rounded-full mb-4"
            />
            </TouchableOpacity>
          <Text className="text-2xl font-bold text-primary mb-3">@{user?.username}</Text>
          <View className="flex-row gap-2">
            <Text className="text-xl font-bold text-secondary mb-3">#{user?.id}</Text>
            <Copy size={20} color="white" strokeWidth={1} onPress={handleCopy}/>
          </View>

          {/* Modal de opciones */}
          <Modal visible={modalVisible} transparent animationType="slide">
            <View className="flex-1 bg-black/50 justify-end">
              <View className="bg-white p-4 rounded-t-2xl">
                <Pressable
                  onPress={() => {
                    setViewImageModal(true);
                    setModalVisible(false);
                  }}
                  className="w-full py-3 items-center border-b border-gray-200"
                >
                  <Text className="text-lg text-blue-600 font-semibold">Ver foto</Text>
                </Pressable>
                <Pressable
                  onPress={changePic}
                  className="w-full py-3 items-center border-b border-gray-200"
                >
                  <Text className="text-lg text-blue-600 font-semibold">Cambiar foto</Text>
                </Pressable>
                <Pressable
                  onPress={() => setModalVisible(false)}
                  className="w-full py-3 items-center"
                >
                  <Text className="text-lg text-red-500 font-semibold">Cancelar</Text>
                </Pressable>
              </View>
            </View>
          </Modal>

          {/* Modal para ver imagen ampliada */}
          <Modal visible={viewImageModal} transparent animationType="fade">
            <Pressable
              className="flex-1 bg-black justify-center items-center"
              onPress={() => setViewImageModal(false)}
            >
              <Image
                source={{ uri: user?.profileImageUrl }}
                className="w-[90%] h-[90%]"
                resizeMode="contain"
              />
            </Pressable>
          </Modal>

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
        
        <ListCarousel data={[]} />

        <View className="h-px bg-primary my-4 h-2.5 w-full" />

        <View>
          <View className="flex-row mb-4 items-center gap-8">
            <Text className="font-semibold text-lg text-secondary mb-2">Nombre:</Text>
            <TextInput
              className="text-primary border border-primary rounded-lg p-3 mb-2 flex-1"
              value={name}
              onChangeText={setName}
              placeholder={user?.name}
              placeholderTextColor={COLORS.primary}
            />
          </View>
          <View className="flex-row mb-4 items-center gap-9">
            <Text className="font-semibold text-lg text-secondary mb-2">Usuario:</Text>
            <TextInput
              className="text-primary border border-primary rounded-lg p-3 mb-2 flex-1"
              value={username}
              onChangeText={setUsername}
              placeholder={user?.username}
              placeholderTextColor={COLORS.primary}
            />
          </View>
          <View className="flex-row items-center gap-12">
            <Text className="font-semibold text-lg text-secondary mb-2 mr-1">Email:</Text>
            <TextInput
              className="text-primary border border-primary rounded-lg p-3 mb-2 flex-1"
              value={email}
              onChangeText={setEmail}
              placeholder={user?.email}
              placeholderTextColor={COLORS.primary}
            />
          </View>  
        </View>

        <View className="h-px bg-primary mb-4 mt-2 h-2.5 w-full" />

        <GenreViewer />

        <InstrumentViewer />

        <View className="h-px bg-primary mb-4 h-2.5 w-full" />

        <View className="flex-column justify-center align-center mt-4 gap-2">
          <TouchableOpacity 
            className="bg-secondary px-4 py-3 rounded-full self-center"
            onPress={updateUser}
          >
            <Text className="text-primary text-lg self-center">Guardar ajustes</Text>
          </TouchableOpacity>
          <SpotifyButton />
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