import React, {useState, useEffect, useMemo} from "react";
import { ScrollView, View, Image, TouchableOpacity, Modal, Pressable, Text } from "react-native";
import { useNavigation, NavigationProp } from '@react-navigation/native';
import * as Clipboard from 'expo-clipboard';
import { Copy } from "lucide-react-native";
import AppLayout from "../components/AppLayout";
import { ProfileInput } from "../components/ProfileInput";
import { LoadingPlaceholder } from "../components/LoadingPlaceholder";
import { PhotoOptionsModal } from "../components/PhotoOptionsModal";
import GenreViewer from "../components/GenreViewer";
import InstrumentViewer from "../components/InstrumentViewer";
import ListCarousel from "../components/carousels/ListCarousel";
import CreateListMiniModal from "../components/CreateListMiniModal";
import { SpotifyButton } from "../components/buttons/SpotifyButton";
import { useProfile } from "../hooks/useProfile";
import { useAuth } from "../hooks/useAuth";
import {useItemLists} from "../hooks/useItemLists";
import { RootStackParamList } from "../types";
import { useAppAlert } from "../context/AlertModalContext";

export default function ProfileScreen() {
  const navigation = useNavigation<NavigationProp<RootStackParamList>>();
  const {
    user,
    isLoading,
    updateProfileData,
    pickAndUploadImage
  } = useProfile();

  const { logout } = useAuth();
  const { showAlert } = useAppAlert();

  // Estado local para el formulario
  const [form, setForm] = useState(() => ({
    name: user?.name || "",
    username: user?.username || "",
    email: user?.email || "",
  }));

  const { lists, creating, createList } = useItemLists(user?.id);

  const [createListModalVisible, setCreateListModalVisible] = useState(false);
  const [newListName, setNewListName] = useState('');
  const [createListError, setCreateListError] = useState<string | null>(null);

  const handleCreateListPress = () => {
    setCreateListError(null);
    setNewListName('');
    setCreateListModalVisible(true);
  };

  const handleCloseCreateListModal = () => {
    setCreateListModalVisible(false);
    setCreateListError(null);
  };

  const handleCreateList = async () => {
    const trimmedName = newListName.trim();
    if (trimmedName.length < 2) {
      setCreateListError('El nombre debe tener al menos 2 caracteres');
      return;
    }

    const created = await createList({ name: trimmedName });
    if (!created?.id) {
      setCreateListError('No se pudo crear la lista');
      return;
    }

    setCreateListModalVisible(false);
    setCreateListError(null);
    setNewListName('');
    navigation.navigate('ListScreen', { itemListId: created.id });
  };

  // Transformamos listas a DTO del carrusel con referencia estable.
  const carouselData = useMemo(() => lists.map((l) => ({
    id: l.id,
    title: l.name,
    imgUrl: l.imageUrl || '',
  })), [lists]);

  const [profileModalVisible, setProfileModalVisible] = useState(false);
  const [viewImageModal, setViewImageModal] = useState(false);

  useEffect(() => {
    if (user) {
      setForm(prev => {
        // SOLO actualizamos si los valores realmente han cambiado respecto al user de la store
        if (prev.name === user.name && prev.username === user.username && prev.email === user.email) {
          return prev;
        }
        return {
          name: user.name || '',
          username: user.username || '',
          email: user.email || '',
        };
      });
    }
  }, [user?.id, user?.name, user?.username, user?.email]);

  const handleSaveProfile = async () => {
    try {
      await updateProfileData(form);
      showAlert({ title: 'Exito', message: 'Perfil actualizado correctamente' });
    } catch (err) {
      showAlert({ title: 'Error', message: 'No se pudieron guardar los cambios' });
    }
  };

  const handleLogout = () => {
    showAlert({
      title: 'Cerrar sesion',
      message: 'Estas seguro?',
      actions: [
        { text: 'Cancelar', style: 'cancel' },
        { text: 'Cerrar sesion', style: 'destructive', onPress: logout },
      ],
    });
  };

  if (!user) return <LoadingPlaceholder />;

  return (
      <AppLayout>
        <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={{ paddingBottom: 120 }}>

          {/* CABECERA: FOTO Y USERNAME */}
          <View className="items-center mt-8">
            <TouchableOpacity onPress={() => setProfileModalVisible(true)}>
              <Image
                  source={{ uri: user.profileImageUrl || 'https://via.placeholder.com/128' }}
                  className="h-32 w-32 rounded-full mb-4 border-2 border-secondary"
              />
            </TouchableOpacity>

            <Text className="text-2xl font-bold text-primary mb-2">@{user.username}</Text>

            <View className="flex-row items-center gap-2">
              <Text className="text-xl text-secondary font-bold">id: #{user.id}</Text>
              <TouchableOpacity onPress={() => Clipboard.setStringAsync(user.id.toString())}>
                <Copy size={16} color="white" />
              </TouchableOpacity>
            </View>
          </View>

          {/* CAROUSEL */}
          <ListCarousel
              data={carouselData}
              onItemPress={(id) => navigation.navigate('ListScreen', { itemListId: id })}
              onEmptyPress={handleCreateListPress}
          />

          <View className="h-[5px] bg-primary my-2 mx-4" />

          {/* FORMULARIO DE EDICIÓN */}
          <View className="px-6 mt-4">
            <ProfileInput
                label="Nombre"
                value={form.name}
                onChange={(val) => setForm({ ...form, name: val })}
                editable={!isLoading}
            />
            <ProfileInput
                label="Usuario"
                value={form.username}
                onChange={(val) => setForm({ ...form, username: val })}
                editable={!isLoading}
            />
            <ProfileInput
                label="Email"
                value={form.email}
                onChange={(val) => setForm({ ...form, email: val })}
                editable={!isLoading}
            />
          </View>

          <View className="h-[5px] bg-primary my-1 mx-4" />

          {/* VIEWERS DE ENTIDADES */}
          <View className="px-6 mt-6">
            <GenreViewer
                ids={user.favouriteGenresIds || []}
                label={"Mis géneros favoritos:"}
            />

            <InstrumentViewer
                ids={user.favouriteInstrumentsIds || []}
                label="Instrumentos que toco:"
            />
          </View>

          <View className="h-[5px] bg-primary my-1 mx-4" />

          {/* BOTONES DE ACCIÓN */}
          <View className="px-10 mt-8 gap-y-3">
            <TouchableOpacity
                className={`px-7 py-4 rounded-full ${isLoading ? 'bg-gray-600' : 'bg-secondary'} self-center`}
                onPress={handleSaveProfile}
                disabled={isLoading}
            >
              <Text className="text-primary text-center font-bold text-lg self-center">
                {isLoading ? 'Guardando...' : 'Guardar ajustes'}
              </Text>
            </TouchableOpacity>

            <SpotifyButton />

            <TouchableOpacity className="rounded-full px-7 py-4 bg-red-600 self-center" onPress={handleLogout}>
              <Text className="text-white text-center font-semibold text-lg">Cerrar sesión</Text>
            </TouchableOpacity>
          </View>

        </ScrollView>

        {/* MODALES EXTRAÍDOS */}
        <PhotoOptionsModal
            visible={profileModalVisible}
            onClose={() => setProfileModalVisible(false)}
            onViewImage={() => {
              setViewImageModal(true);
              setProfileModalVisible(false);
            }}
            onPickImage={pickAndUploadImage} // Ahora el hook se encarga del Picker
        />

        <Modal visible={viewImageModal} transparent animationType="fade">
          <Pressable className="flex-1 bg-black justify-center items-center" onPress={() => setViewImageModal(false)}>
            <Image
                source={{ uri: user.profileImageUrl || 'https://via.placeholder.com/256' }}
                className="w-full h-full"
                resizeMode="contain"
            />
          </Pressable>
        </Modal>

        <CreateListMiniModal
            visible={createListModalVisible}
            name={newListName}
            error={createListError}
            creating={creating}
            onChangeName={(val) => {
              setNewListName(val);
              if (createListError) setCreateListError(null);
            }}
            onSubmit={handleCreateList}
            onClose={handleCloseCreateListModal}
        />
      </AppLayout>
  );
}