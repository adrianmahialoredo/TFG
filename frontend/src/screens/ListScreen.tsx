import React from 'react';
import { View, Text, Image, ActivityIndicator, Modal, Pressable } from "react-native";
import Touchable from "../components/Touchable";
import FadeScrollView from "../components/FadeScrollView";
import {useRoute, RouteProp, useNavigation, NavigationProp, useFocusEffect} from '@react-navigation/native';
import * as ImagePicker from 'expo-image-picker';
import { Trash2 } from 'lucide-react-native';
import AppLayout from "../components/AppLayout";
import ProfileHeader from "../components/ProfileHeader";
import ListItem, { LIST_ITEM_SCALE } from "../components/ListItem";
import { COLORS } from "../constants/Theme";
import { RootStackParamList, ItemSummaryDTO } from '../types';
import { useItemListDetail } from '../hooks/useItemListDetail';
import { navigateToItem } from '../utils/navigation.utils';
import { useItemToListItem } from '../hooks/useItemToListItem';
import AddButton from '../components/iconbuttons/AddButton';
import { PhotoOptionsModal } from '../components/PhotoOptionsModal';
import EditButton from '../components/iconbuttons/EditButton';
import { useAppAlert } from '../context/AlertModalContext';
import CreateListMiniModal from '../components/CreateListMiniModal';
import GradientBackground from '../components/GradientBackground';
import { useImageGradient } from '../hooks/useImageGradient';

type ListScreenRouteProp = RouteProp<RootStackParamList, 'ListScreen'>;

export default function ListScreen() {
  const navigation = useNavigation<NavigationProp<RootStackParamList>>();
  const toListItem = useItemToListItem();
  const route = useRoute<ListScreenRouteProp>();
  const { itemListId } = route.params;

  const { list, items, loading, error, refresh, updateList, updateImage, removeItem, deleteList } = useItemListDetail(itemListId);
  const { showAlert } = useAppAlert();

  // Fondo: degradado del color principal de la imagen de la lista al negro (dorado si no tiene)
  const gradient = useImageGradient(list?.imageUrl);

  const [editingPhoto, setEditingPhoto] = React.useState(false);
  const [viewImageModal, setViewImageModal] = React.useState(false);
  const [savingImage, setSavingImage] = React.useState(false);

  const [renameVisible, setRenameVisible] = React.useState(false);
  const [newName, setNewName] = React.useState('');
  const [renameError, setRenameError] = React.useState<string | null>(null);
  const [renaming, setRenaming] = React.useState(false);

  const [deleting, setDeleting] = React.useState(false);

  /** Pide confirmación y, si se elimina, vuelve a la pantalla anterior. */
  const handleDelete = () => {
    showAlert({
      title: 'Eliminar lista',
      message: `¿Seguro que quieres eliminar "${list?.name ?? 'esta lista'}"? No se puede deshacer.`,
      actions: [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Eliminar',
          style: 'destructive',
          onPress: async () => {
            setDeleting(true);
            if (await deleteList()) {
              navigation.goBack();
            } else {
              setDeleting(false);
              showAlert({ title: 'Error', message: 'No se pudo eliminar la lista' });
            }
          },
        },
      ],
    });
  };

  /** Pide confirmación y quita el item de la lista. */
  const handleRemoveItem = (item: ItemSummaryDTO) => {
    showAlert({
      title: 'Quitar de la lista',
      message: `¿Quitar "${item.title}" de "${list?.name ?? 'la lista'}"?`,
      actions: [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Quitar',
          style: 'destructive',
          onPress: async () => {
            try {
              await removeItem(item.id);
            } catch {
              showAlert({ title: 'Error', message: 'No se pudo quitar el item de la lista' });
            }
          },
        },
      ],
    });
  };

  // Refresca la lista al volver a esta pantalla (p. ej. tras añadir un item),
  // omitiendo el primer foco porque el hook ya carga en el montaje.
  const didMountRef = React.useRef(false);
  useFocusEffect(
    React.useCallback(() => {
      if (!didMountRef.current) {
        didMountRef.current = true;
        return;
      }
      refresh();
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [itemListId])
  );

  /**
   * Maneja la navegación según el tipo de item pulsado
   */
  const handlePress = (item: ItemSummaryDTO) => {
    navigateToItem(navigation, item);
  };

  const handleOpenRename = () => {
    setNewName(list?.name || '');
    setRenameError(null);
    setRenameVisible(true);
  };

  const handleRename = async () => {
    const trimmed = newName.trim();
    if (trimmed.length < 2) {
      setRenameError('El nombre debe tener al menos 2 caracteres');
      return;
    }

    try {
      setRenaming(true);
      const updated = await updateList({ name: trimmed });
      if (!updated) {
        setRenameError('No se pudo actualizar el nombre');
        return;
      }
      setRenameVisible(false);
      setRenameError(null);
    } catch (e) {
      setRenameError('No se pudo actualizar el nombre');
    } finally {
      setRenaming(false);
    }
  };

  const handlePickImage = async () => {
    try {
      const result = await ImagePicker.launchImageLibraryAsync({
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.6,
      });

      if (result.canceled || !result.assets[0]?.uri) {
        return;
      }

      setSavingImage(true);
      await updateImage(result.assets[0].uri);
      setEditingPhoto(false);
    } catch (e) {
      showAlert({ title: 'Error', message: 'No se pudo actualizar la foto de la lista' });
    } finally {
      setSavingImage(false);
    }
  };

  // Estado de Carga
  if (loading) {
    return (
        <AppLayout>
          <View className="bg-background flex-1 justify-center items-center">
            <ActivityIndicator size="large" color={COLORS.primary} />
          </View>
        </AppLayout>
    );
  }

  // Eliminando (o ya eliminada, mientras se vuelve atrás): sin el aviso de "lista no encontrada"
  if (deleting) {
    return (
        <AppLayout>
          <View className="bg-background flex-1 justify-center items-center">
            <ActivityIndicator size="large" color={COLORS.primary} />
          </View>
        </AppLayout>
    );
  }

  // Estado de Error o Lista No Encontrada
  if (error || !list) {
    return (
        <AppLayout>
          <View className="bg-background flex-1 justify-center items-center px-4">
            <Text className="text-secondary text-lg text-center">
              {error || 'No se pudo encontrar la lista seleccionada'}
            </Text>
          </View>
        </AppLayout>
    );
  }

  return (
      <AppLayout>
        <GradientBackground color={gradient.color} height={gradient.height} />
        {/* Sin fondo propio: deja ver el degradado */}
        <View className="flex-1" onLayout={gradient.onContainerLayout}>
          {/* Header con botón de volver atrás */}
          <ProfileHeader title="Listas" />

          <View style={{ flex: 1 }} onLayout={gradient.onScrollLayout}>
            <FadeScrollView
                showsVerticalScrollIndicator={false}
                contentContainerStyle={{ paddingBottom: 120 }}
                fadeColor={gradient.fadeColor}
            >
              {/* Cabecera con Info de la Lista */}
              <View className="items-center mt-2 mb-8 px-4">
                {list.imageUrl ? (
                    <Touchable onPress={() => setEditingPhoto(true)}>
                      <Image
                          source={{ uri: list.imageUrl }}
                          className="w-40 h-40 rounded-3xl mb-4 shadow-lg"
                          resizeMode="cover"
                      />
                    </Touchable>
                ) : (
                    <Touchable
                      className="w-40 h-40 bg-secondary rounded-3xl mb-4 items-center justify-center"
                      onPress={() => setEditingPhoto(true)}
                    >
                      <Text className="text-primary text-4xl font-bold">
                        {list.name.charAt(0).toUpperCase()}
                      </Text>
                    </Touchable>
                )}
                <View className="flex-row items-center gap-2">
                  <Text className="text-secondary text-xl font-bold text-center">
                    {list.name}
                  </Text>
                  <EditButton onPress={handleOpenRename} size={12} color={COLORS.secondary} strokeWidth={2} accessibilityLabel="Renombrar lista" />
                </View>
                {list.description && (
                    <Text className="text-secondary opacity-60 text-sm text-center mt-1">
                      {list.description}
                    </Text>
                )}
              </View>

              {/* Listado de Items Polimórficos */}
              <View className="flex-column gap-3 mb-7 px-4">
                {items.length > 0 ? (
                    items.map((item, index) => (
                        <ListItem
                            key={item.id}
                            item={toListItem(item)}
                            inverted={index % 2 !== 0}
                            scale={LIST_ITEM_SCALE}
                            onPress={() => handlePress(item)}
                            onRemove={() => handleRemoveItem(item)}
                        />
                    ))
                ) : (
                    <View className="items-center py-12">
                      <Text className="text-secondary opacity-50 text-base">
                        Esta lista aún no tiene contenido
                      </Text>
                    </View>
                )}

                {/* "Añadir a la lista" y "Eliminar lista": el contenedor toma el ancho del más ancho
                    y los dos se estiran a él, así miden lo mismo */}
                <View style={{ alignSelf: 'center', marginTop: 16, gap: 6 }}>
                  {/* Botón de Acción para añadir contenido */}
                  <Touchable
                      onPress={() => navigation.navigate('AddItemScreen', { itemListId })}
                      accessibilityRole="button"
                      style={{
                        flexDirection: 'row',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: 8,
                        paddingHorizontal: 28,
                        paddingVertical: 14,
                        borderRadius: 9999,
                        backgroundColor: COLORS.secondary,
                      }}
                  >
                    <AddButton size={20} color={COLORS.primary} />
                    <Text className="text-primary text-base font-semibold">Añadir a la lista</Text>
                  </Touchable>

                  {/* Eliminar la lista (con confirmación) */}
                  <Touchable
                      onPress={handleDelete}
                      accessibilityRole="button"
                      style={{
                        flexDirection: 'row',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: 8,
                        paddingHorizontal: 28,
                        paddingVertical: 14,
                        borderRadius: 9999,
                        backgroundColor: '#dc2626', // red-600, como "Cerrar sesión"
                      }}
                  >
                    <Trash2 size={20} color="white" />
                    <Text className="text-white text-base font-semibold">Eliminar lista</Text>
                  </Touchable>
                </View>
              </View>
            </FadeScrollView>
          </View>
        </View>

        <PhotoOptionsModal
          visible={editingPhoto}
          onClose={() => setEditingPhoto(false)}
          onViewImage={() => {
            if (!list.imageUrl) {
              showAlert({ title: 'Info', message: 'Esta lista aún no tiene foto' });
              return;
            }
            setEditingPhoto(false);
            setViewImageModal(true);
          }}
          onPickImage={handlePickImage}
          isLoading={savingImage}
        />

        <Modal visible={viewImageModal} transparent animationType="fade" onRequestClose={() => setViewImageModal(false)}>
          <Pressable className="flex-1 bg-black justify-center items-center" onPress={() => setViewImageModal(false)}>
            {list.imageUrl ? (
              <Image
                source={{ uri: list.imageUrl }}
                className="w-full h-full"
                resizeMode="contain"
              />
            ) : null}
          </Pressable>
        </Modal>

        <CreateListMiniModal
          visible={renameVisible}
          name={newName}
          error={renameError}
          creating={renaming}
          title="Editar nombre"
          placeholder="Nuevo nombre de la lista"
          submitLabel="Guardar"
          onChangeName={(value) => {
            setNewName(value);
            if (renameError) setRenameError(null);
          }}
          onSubmit={handleRename}
          onClose={() => {
            setRenameVisible(false);
            setRenameError(null);
          }}
        />
      </AppLayout>
  );
}