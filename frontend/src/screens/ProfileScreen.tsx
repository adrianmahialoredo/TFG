import React, {useState, useEffect, useMemo} from "react";
import { View, Image, Modal, Pressable, Text } from "react-native";
import Touchable from "../components/Touchable";
import FadeScrollView from "../components/FadeScrollView";
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
import ItemListPickerModal from "../components/ItemListPickerModal";
import { COLORS } from "../constants/Theme";
import FavoritesPickerModal from "../components/FavoritesPickerModal";
import { SpotifyButton } from "../components/buttons/SpotifyButton";
import { useProfile } from "../hooks/useProfile";
import { useAuth } from "../context/AuthContext";
import {useItemLists} from "../hooks/useItemLists";
import { RootStackParamList } from "../types";
import { useAppAlert } from "../context/AlertModalContext";
import { MIN_FAVOURITE_GENRES, MIN_FAVOURITE_INSTRUMENTS } from "../constants/Preferences";
import UserAvatar from "../components/UserAvatar";
import DifficultyLevelSlider from "../components/DifficultyLevelSlider";

export default function ProfileScreen() {
  const navigation = useNavigation<NavigationProp<RootStackParamList>>();
  const {
    user,
    isLoading,
    updateProfileData,
    pickAndUploadImage,
    updateFavoriteGenres,
    updateFavoriteInstruments,
  } = useProfile();

  const { logout } = useAuth();
  const { showAlert } = useAppAlert();

  // Estado local para el formulario
  const [form, setForm] = useState(() => ({
    name: user?.name || "",
    username: user?.username || "",
    email: user?.email || "",
    difficultyLevel: user?.difficultyLevel ?? 0,
  }));

  const { lists, creating, createList } = useItemLists(user?.id);

  const [createListModalVisible, setCreateListModalVisible] = useState(false);
  const [newListName, setNewListName] = useState('');
  const [createListError, setCreateListError] = useState<string | null>(null);

  // Modal con todas las listas (lupa del carrusel cuando hay demasiadas)
  const [allListsVisible, setAllListsVisible] = useState(false);
  const sortedLists = useMemo(() => [...lists].sort((a, b) => a.name.localeCompare(b.name)), [lists]);

  const handleOpenListFromModal = (listId: string) => {
    setAllListsVisible(false);
    navigation.navigate('ListScreen', { itemListId: listId });
  };

  const handleCreateListPress = () => {
    setAllListsVisible(false);
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

  // Qué favoritos se están editando (null = modal cerrado)
  const [favoritesKind, setFavoritesKind] = useState<'genres' | 'instruments' | null>(null);

  const handleSaveFavorites = async (ids: string[]) => {
    try {
      if (favoritesKind === 'genres') await updateFavoriteGenres(ids);
      else await updateFavoriteInstruments(ids);
      setFavoritesKind(null);
    } catch (err) {
      showAlert({ title: 'Error', message: 'No se pudieron guardar los favoritos' });
    }
  };

  const [profileModalVisible, setProfileModalVisible] = useState(false);
  const [viewImageModal, setViewImageModal] = useState(false);

  // El modal sigue abierto (con "Subiendo foto...") hasta que la foto se sube; si falla, se avisa
  const handlePickImage = async () => {
    try {
      if (await pickAndUploadImage()) setProfileModalVisible(false);
    } catch (err) {
      console.error('[ProfileScreen] Error subiendo la foto de perfil:', err);
      showAlert({ title: 'Error', message: 'No se pudo actualizar la foto de perfil' });
    }
  };

  useEffect(() => {
    if (user) {
      setForm(prev => {
        // SOLO actualizamos si los valores realmente han cambiado respecto al user de la store
        if (prev.name === user.name && prev.username === user.username && prev.email === user.email
            && prev.difficultyLevel === (user.difficultyLevel ?? 0)) {
          return prev;
        }
        return {
          name: user.name || '',
          username: user.username || '',
          email: user.email || '',
          difficultyLevel: user.difficultyLevel ?? 0,
        };
      });
    }
  }, [user?.id, user?.name, user?.username, user?.email, user?.difficultyLevel]);

  const handleSaveProfile = async () => {
    // El backend rechaza el perfil sin el mínimo de favoritos (Preferences.MIN_NUMBER_OF_FAVOURITE_*)
    if ((user?.favouriteGenresIds?.length ?? 0) < MIN_FAVOURITE_GENRES
        || (user?.favouriteInstrumentsIds?.length ?? 0) < MIN_FAVOURITE_INSTRUMENTS) {
      showAlert({
        title: 'Faltan favoritos',
        message: `Elige al menos ${MIN_FAVOURITE_GENRES} géneros y ${MIN_FAVOURITE_INSTRUMENTS} instrumentos con los botones +`,
      });
      return;
    }
    try {
      await updateProfileData(form);
      showAlert({ title: 'Exito', message: 'Perfil actualizado correctamente' });
    } catch (err) {
      showAlert({ title: 'Error', message: 'No se pudieron guardar los cambios' });
    }
  };

  const handleLogout = () => {
    showAlert({
      title: 'Cerrar sesión',
      message: '¿Estás seguro?',
      actions: [
        { text: 'Cancelar', style: 'cancel' },
        { text: 'Cerrar sesión', style: 'destructive', onPress: logout },
      ],
    });
  };

  if (!user) return <LoadingPlaceholder />;

  return (
      <AppLayout>
        <FadeScrollView showsVerticalScrollIndicator={false} contentContainerStyle={{ paddingBottom: 120 }}>

          {/* CABECERA: FOTO Y USERNAME */}
          <View className="items-center mt-2">
            <Touchable onPress={() => setProfileModalVisible(true)}>
              <UserAvatar user={user} size={128} className="mb-4 border-2 border-secondary" />
            </Touchable>

            <Text className="text-2xl font-medium text-primary mb-2">@{user.username}</Text>

            <View className="flex-row items-center gap-2">
              <Text className="text-xl text-secondary font-medium">id: #{user.id}</Text>
              <Touchable onPress={() => Clipboard.setStringAsync(user.id.toString())}>
                <Copy size={16} color="white" />
              </Touchable>
            </View>
          </View>

          {/* CARRUSEL "MIS LISTAS" */}
          <ListCarousel
              title="Mis listas"
              data={carouselData}
              onItemPress={(id) => navigation.navigate('ListScreen', { itemListId: id })}
              onEmptyPress={handleCreateListPress}
              onSeeAllPress={() => setAllListsVisible(true)}
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
            {/* El email se verificó con un código al registrarse: no se puede cambiar desde aquí */}
            <ProfileInput
                label="Email"
                value={form.email}
                onChange={() => {}}
                readOnly
            />
            {/* Nivel musical autopercibido: se guarda con "Guardar ajustes" */}
            <DifficultyLevelSlider
                value={form.difficultyLevel}
                onChange={(level) => setForm({ ...form, difficultyLevel: level })}
                disabled={isLoading}
            />
          </View>

          <View className="h-[5px] bg-primary my-1 mx-4" />

          {/* VIEWERS DE ENTIDADES */}
          <View className="px-6 mt-6">
            <GenreViewer
                ids={user.favouriteGenresIds || []}
                label={"Mis géneros favoritos:"}
                onAddPress={() => setFavoritesKind('genres')}
            />

            <InstrumentViewer
                ids={user.favouriteInstrumentsIds || []}
                label="Instrumentos que toco:"
                onAddPress={() => setFavoritesKind('instruments')}
            />
          </View>

          <View className="h-[5px] bg-primary my-1 mx-4" />

          {/* BOTONES DE ACCIÓN */}
          <View className="px-10 mt-8 gap-y-2">
            <Touchable
                className={`px-7 py-4 rounded-full ${isLoading ? 'bg-gray-600' : 'bg-secondary'} self-center`}
                onPress={handleSaveProfile}
                disabled={isLoading}
            >
              <Text className="text-primary text-center font-bold text-lg self-center">
                {isLoading ? 'Guardando...' : 'Guardar ajustes'}
              </Text>
            </Touchable>

            <SpotifyButton />

            <Touchable className="rounded-full px-7 py-4 bg-red-600 self-center" onPress={handleLogout}>
              <Text className="text-white text-center font-semibold text-lg">Cerrar sesión</Text>
            </Touchable>
          </View>

        </FadeScrollView>

        {/* MODALES EXTRAÍDOS */}
        {favoritesKind && (
            <FavoritesPickerModal
                kind={favoritesKind}
                initialIds={favoritesKind === 'genres' ? user.favouriteGenresIds ?? [] : user.favouriteInstrumentsIds ?? []}
                saving={isLoading}
                onSave={handleSaveFavorites}
                onClose={() => setFavoritesKind(null)}
            />
        )}
        <PhotoOptionsModal
            visible={profileModalVisible}
            onClose={() => setProfileModalVisible(false)}
            onViewImage={() => {
              setViewImageModal(true);
              setProfileModalVisible(false);
            }}
            onPickImage={handlePickImage}
            isLoading={isLoading}
        />

        <Modal visible={viewImageModal} transparent animationType="fade" onRequestClose={() => setViewImageModal(false)}>
          <Pressable className="flex-1 bg-black justify-center items-center" onPress={() => setViewImageModal(false)}>
            {user.profileImageUrl ? (
              <Image
                  source={{ uri: user.profileImageUrl }}
                  className="w-full h-full"
                  resizeMode="contain"
              />
            ) : (
              <UserAvatar user={user} size={256} />
            )}
          </Pressable>
        </Modal>

        <ItemListPickerModal
            visible={allListsVisible}
            title="Mis listas"
            lists={sortedLists}
            emptyText="Aún no tienes listas."
            onSelect={handleOpenListFromModal}
            onClose={() => setAllListsVisible(false)}
            getAccessibilityLabel={(name) => `Abrir ${name}`}
            footer={
              <Touchable
                  onPress={handleCreateListPress}
                  style={{ paddingHorizontal: 16, paddingVertical: 8, borderRadius: 9999, backgroundColor: COLORS.primary }}
              >
                <Text style={{ color: COLORS.secondary, fontWeight: '600' }}>Añadir lista</Text>
              </Touchable>
            }
        />

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