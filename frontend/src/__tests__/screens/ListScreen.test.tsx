import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react-native';
import * as ImagePicker from 'expo-image-picker';
import ListScreen from '../../screens/ListScreen';
import { ItemType } from '../../types';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

const mockNavigation = { navigate: jest.fn(), goBack: jest.fn() };
let mockFocusCallback: (() => void) | undefined;
jest.mock('@react-navigation/native', () => {
  const ReactActual = jest.requireActual('react');
  return {
    ...jest.requireActual('@react-navigation/native'),
    useNavigation: () => mockNavigation,
    useRoute: () => ({ params: { itemListId: 'list1' } }),
    useFocusEffect: (cb: () => void) => {
      mockFocusCallback = cb;
      ReactActual.useEffect(() => cb(), [cb]);
    },
  };
});

jest.mock('expo-image-picker', () => ({
  launchImageLibraryAsync: jest.fn(),
}));

let mockState: any;
const mockUseItemListDetail = jest.fn();
jest.mock('../../hooks/useItemListDetail', () => ({
  useItemListDetail: (id: string) => {
    mockUseItemListDetail(id);
    return mockState;
  },
}));

const mockShowAlert = jest.fn();
jest.mock('../../context/AlertModalContext', () => ({
  useAppAlert: () => ({ showAlert: mockShowAlert, hideAlert: jest.fn() }),
}));

jest.mock('../../components/ProfileHeader', () => {
  const { Text } = require('react-native');
  return ({ title }: any) => <Text>{title}</Text>;
});
jest.mock('../../components/ListItem', () => {
  const { View, Text } = require('react-native');
  return ({ item, onPress, onRemove }: any) => (
    <View>
      <Text onPress={onPress}>{`Item ${item.title}`}</Text>
      {onRemove ? <Text onPress={onRemove}>{`Quitar ${item.title}`}</Text> : null}
    </View>
  );
});
jest.mock('../../components/PhotoOptionsModal', () => {
  const { View, Text } = require('react-native');
  return {
    PhotoOptionsModal: ({ visible, onClose, onViewImage, onPickImage, isLoading }: any) =>
      visible ? (
        <View>
          <Text>{`Opciones de foto cargando=${isLoading}`}</Text>
          <Text onPress={onViewImage}>Ver foto</Text>
          <Text onPress={onPickImage}>Elegir foto</Text>
          <Text onPress={onClose}>Cerrar opciones</Text>
        </View>
      ) : null,
  };
});
jest.mock('../../components/CreateListMiniModal', () => {
  const { View, Text, TextInput } = require('react-native');
  return ({ visible, name, error, title, onChangeName, onSubmit, onClose }: any) =>
    visible ? (
      <View>
        <Text>{title}</Text>
        <TextInput testID="rename-input" value={name} onChangeText={onChangeName} />
        {error ? <Text>{error}</Text> : null}
        <Text onPress={onSubmit}>Guardar nombre</Text>
        <Text onPress={onClose}>Cancelar nombre</Text>
      </View>
    ) : null;
});

const launchImageLibraryAsync = ImagePicker.launchImageLibraryAsync as jest.Mock;

const song = { id: 's1', title: 'Wonderwall', imageUrl: '', type: ItemType.SONG, artist: 'Oasis' };
const genre = { id: 'g1', title: 'Rock', imageUrl: '', type: ItemType.GENRE };

describe('ListScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockFocusCallback = undefined;
    mockState = {
      list: { id: 'list1', name: 'favoritas', description: 'Mis canciones', imageUrl: null },
      items: [song, genre],
      loading: false,
      error: null,
      refresh: jest.fn(),
      updateList: jest.fn(),
      updateImage: jest.fn(),
      removeItem: jest.fn(),
      deleteList: jest.fn(),
    };
  });

  it('muestra la lista con su inicial, descripción e items', async () => {
    await render(<ListScreen />);
    expect(mockUseItemListDetail).toHaveBeenCalledWith('list1');
    expect(screen.getByText('favoritas')).toBeTruthy();
    expect(screen.getByText('F')).toBeTruthy();
    expect(screen.getByText('Mis canciones')).toBeTruthy();
    expect(screen.getByText('Item Wonderwall')).toBeTruthy();
    expect(screen.getByText('Item Rock')).toBeTruthy();
  });

  it('muestra un aviso si la lista está vacía', async () => {
    mockState.items = [];
    await render(<ListScreen />);
    expect(screen.getByText('Esta lista aún no tiene contenido')).toBeTruthy();
  });

  it('muestra la carga', async () => {
    mockState.loading = true;
    await render(<ListScreen />);
    expect(screen.queryByText('favoritas')).toBeNull();
  });

  it('muestra el error o el aviso de lista no encontrada', async () => {
    mockState.error = 'Fallo de red';
    const { rerender } = await render(<ListScreen />);
    expect(screen.getByText('Fallo de red')).toBeTruthy();

    mockState = { ...mockState, error: null, list: null };
    await rerender(<ListScreen />);
    expect(screen.getByText('No se pudo encontrar la lista seleccionada')).toBeTruthy();
  });

  it('navega al detalle del item pulsado', async () => {
    await render(<ListScreen />);
    await fireEvent.press(screen.getByText('Item Wonderwall'));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('SongScreen', { songId: 's1' });
    await fireEvent.press(screen.getByText('Item Rock'));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('GenreMainScreen', { genreId: 'g1' });
  });

  it('navega a añadir items a la lista', async () => {
    await render(<ListScreen />);
    await fireEvent.press(screen.getByText('Añadir a la lista'));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('AddItemScreen', { itemListId: 'list1' });
  });

  it('no refresca en el primer foco pero sí al volver a la pantalla', async () => {
    await render(<ListScreen />);
    expect(mockState.refresh).not.toHaveBeenCalled();
    await act(async () => mockFocusCallback?.());
    expect(mockState.refresh).toHaveBeenCalledTimes(1);
  });

  describe('quitar un item', () => {
    const pressRemove = async (action: 'Quitar' | 'Cancelar') => {
      await fireEvent.press(screen.getByText('Quitar Wonderwall'));
      const alert = mockShowAlert.mock.calls[0][0];
      expect(alert.title).toBe('Quitar de la lista');
      expect(alert.message).toBe('¿Quitar "Wonderwall" de "favoritas"?');
      const chosen = alert.actions.find((a: any) => a.text === action);
      await act(async () => {
        await chosen.onPress?.();
      });
    };

    it('cada item tiene su botón de quitar', async () => {
      await render(<ListScreen />);
      expect(screen.getByText('Quitar Wonderwall')).toBeTruthy();
      expect(screen.getByText('Quitar Rock')).toBeTruthy();
    });

    it('quita el item tras confirmar', async () => {
      mockState.removeItem.mockResolvedValue({ id: 'list1' });
      await render(<ListScreen />);
      await pressRemove('Quitar');
      expect(mockState.removeItem).toHaveBeenCalledWith('s1');
      expect(mockShowAlert).toHaveBeenCalledTimes(1);
    });

    it('no quita nada si se cancela', async () => {
      await render(<ListScreen />);
      await pressRemove('Cancelar');
      expect(mockState.removeItem).not.toHaveBeenCalled();
    });

    it('avisa si no se pudo quitar', async () => {
      mockState.removeItem.mockRejectedValue(new Error('red'));
      await render(<ListScreen />);
      await pressRemove('Quitar');
      expect(mockShowAlert).toHaveBeenLastCalledWith({ title: 'Error', message: 'No se pudo quitar el item de la lista' });
    });
  });

  describe('eliminar lista', () => {
    const confirmDelete = async () => {
      await fireEvent.press(screen.getByText('Eliminar lista'));
      const alert = mockShowAlert.mock.calls[0][0];
      expect(alert.title).toBe('Eliminar lista');
      expect(alert.message).toContain('"favoritas"');
      const destructive = alert.actions.find((a: any) => a.style === 'destructive');
      await act(async () => {
        await destructive.onPress();
      });
    };

    it('elimina y vuelve atrás tras confirmar', async () => {
      mockState.deleteList.mockResolvedValue(true);
      await render(<ListScreen />);
      await confirmDelete();
      expect(mockState.deleteList).toHaveBeenCalled();
      expect(mockNavigation.goBack).toHaveBeenCalled();
    });

    it('avisa si no se pudo eliminar', async () => {
      mockState.deleteList.mockResolvedValue(false);
      await render(<ListScreen />);
      await confirmDelete();
      expect(mockNavigation.goBack).not.toHaveBeenCalled();
      expect(mockShowAlert).toHaveBeenLastCalledWith({ title: 'Error', message: 'No se pudo eliminar la lista' });
      expect(screen.getByText('favoritas')).toBeTruthy();
    });
  });

  describe('renombrar', () => {
    it('abre el modal con el nombre actual y guarda el nuevo nombre recortado', async () => {
      mockState.updateList.mockResolvedValue({ id: 'list1' });
      await render(<ListScreen />);
      await fireEvent.press(screen.getByLabelText('Renombrar lista'));
      expect(screen.getByText('Editar nombre')).toBeTruthy();
      expect(screen.getByTestId('rename-input').props.value).toBe('favoritas');
      await fireEvent.changeText(screen.getByTestId('rename-input'), '  Rock clásico  ');
      await fireEvent.press(screen.getByText('Guardar nombre'));
      await waitFor(() => expect(screen.queryByText('Editar nombre')).toBeNull());
      expect(mockState.updateList).toHaveBeenCalledWith({ name: 'Rock clásico' });
    });

    it('valida que el nombre tenga al menos 2 caracteres y limpia el error al escribir', async () => {
      await render(<ListScreen />);
      await fireEvent.press(screen.getByLabelText('Renombrar lista'));
      await fireEvent.changeText(screen.getByTestId('rename-input'), ' a ');
      await fireEvent.press(screen.getByText('Guardar nombre'));
      expect(screen.getByText('El nombre debe tener al menos 2 caracteres')).toBeTruthy();
      expect(mockState.updateList).not.toHaveBeenCalled();
      await fireEvent.changeText(screen.getByTestId('rename-input'), 'ab');
      expect(screen.queryByText('El nombre debe tener al menos 2 caracteres')).toBeNull();
    });

    it('muestra un error si la actualización no devuelve la lista', async () => {
      mockState.updateList.mockResolvedValue(null);
      await render(<ListScreen />);
      await fireEvent.press(screen.getByLabelText('Renombrar lista'));
      await fireEvent.press(screen.getByText('Guardar nombre'));
      await waitFor(() => expect(screen.getByText('No se pudo actualizar el nombre')).toBeTruthy());
    });

    it('muestra un error si la actualización lanza', async () => {
      mockState.updateList.mockRejectedValue(new Error('500'));
      await render(<ListScreen />);
      await fireEvent.press(screen.getByLabelText('Renombrar lista'));
      await fireEvent.press(screen.getByText('Guardar nombre'));
      await waitFor(() => expect(screen.getByText('No se pudo actualizar el nombre')).toBeTruthy());
    });

    it('cierra el modal al cancelar', async () => {
      await render(<ListScreen />);
      await fireEvent.press(screen.getByLabelText('Renombrar lista'));
      await fireEvent.press(screen.getByText('Cancelar nombre'));
      expect(screen.queryByText('Editar nombre')).toBeNull();
    });
  });

  describe('foto de la lista', () => {
    it('abre y cierra las opciones de foto', async () => {
      await render(<ListScreen />);
      await fireEvent.press(screen.getByText('F'));
      expect(screen.getByText('Opciones de foto cargando=false')).toBeTruthy();
      await fireEvent.press(screen.getByText('Cerrar opciones'));
      expect(screen.queryByText(/Opciones de foto/)).toBeNull();
    });

    it('avisa si se quiere ver la foto y la lista no tiene', async () => {
      await render(<ListScreen />);
      await fireEvent.press(screen.getByText('F'));
      await fireEvent.press(screen.getByText('Ver foto'));
      expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Info', message: 'Esta lista aún no tiene foto' });
    });

    it('con foto, "Ver foto" cierra las opciones y abre la imagen a pantalla completa', async () => {
      mockState.list.imageUrl = 'https://img/lista.png';
      await render(<ListScreen />);
      expect(screen.queryByText('F')).toBeNull();
      const listImages = () =>
        screen.root!.queryAll((n) => n.type === 'Image' && n.props.source?.uri === 'https://img/lista.png');
      expect(listImages()).toHaveLength(1);

      await fireEvent.press(listImages()[0]);
      await fireEvent.press(screen.getByText('Ver foto'));
      expect(screen.queryByText(/Opciones de foto/)).toBeNull();
      const fullScreen = listImages().find((n) => n.props.resizeMode === 'contain');
      expect(fullScreen).toBeTruthy();

      // Pulsar sobre la imagen ampliada la cierra
      await fireEvent.press(fullScreen!);
      expect(listImages().find((n) => n.props.resizeMode === 'contain')).toBeUndefined();
    });

    it('sube la foto elegida y cierra las opciones', async () => {
      launchImageLibraryAsync.mockResolvedValue({ canceled: false, assets: [{ uri: 'file://foto.jpg' }] });
      mockState.updateImage.mockResolvedValue(undefined);
      await render(<ListScreen />);
      await fireEvent.press(screen.getByText('F'));
      await fireEvent.press(screen.getByText('Elegir foto'));
      await waitFor(() => expect(screen.queryByText(/Opciones de foto/)).toBeNull());
      expect(launchImageLibraryAsync).toHaveBeenCalledWith({ allowsEditing: true, aspect: [1, 1], quality: 0.6 });
      expect(mockState.updateImage).toHaveBeenCalledWith('file://foto.jpg');
    });

    it('no hace nada si se cancela el selector', async () => {
      launchImageLibraryAsync.mockResolvedValue({ canceled: true, assets: [] });
      await render(<ListScreen />);
      await fireEvent.press(screen.getByText('F'));
      await fireEvent.press(screen.getByText('Elegir foto'));
      await waitFor(() => expect(launchImageLibraryAsync).toHaveBeenCalled());
      expect(mockState.updateImage).not.toHaveBeenCalled();
      expect(screen.getByText('Opciones de foto cargando=false')).toBeTruthy();
    });

    it('avisa si la subida falla', async () => {
      launchImageLibraryAsync.mockResolvedValue({ canceled: false, assets: [{ uri: 'file://foto.jpg' }] });
      mockState.updateImage.mockRejectedValue(new Error('upload'));
      await render(<ListScreen />);
      await fireEvent.press(screen.getByText('F'));
      await fireEvent.press(screen.getByText('Elegir foto'));
      await waitFor(() =>
        expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Error', message: 'No se pudo actualizar la foto de la lista' })
      );
    });
  });
});
