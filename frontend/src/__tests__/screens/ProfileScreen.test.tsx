import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react-native';
import * as Clipboard from 'expo-clipboard';
import ProfileScreen from '../../screens/ProfileScreen';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

const mockNavigation = { navigate: jest.fn(), goBack: jest.fn() };
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => mockNavigation,
}));

jest.mock('lucide-react-native', () => {
  const { Text } = require('react-native');
  return new Proxy({}, {
    get: (_target, name) => (name === '__esModule' ? false : () => <Text>{`icon-${String(name)}`}</Text>),
  });
});

jest.mock('expo-clipboard', () => ({ setStringAsync: jest.fn() }));

let mockProfileState: any;
jest.mock('../../hooks/useProfile', () => ({
  useProfile: () => mockProfileState,
}));

const mockLogout = jest.fn();
jest.mock('../../context/AuthContext', () => ({
  useAuth: () => ({ logout: mockLogout }),
}));

let mockListsState: any;
const mockUseItemLists = jest.fn();
jest.mock('../../hooks/useItemLists', () => ({
  useItemLists: (userId: string) => {
    mockUseItemLists(userId);
    return mockListsState;
  },
}));

// Slider nativo sustituido por una View que conserva sus props (value, onValueChange…)
jest.mock('@react-native-community/slider', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  return { __esModule: true, default: (props: any) => mockJsx(View, { testID: 'slider', ...props }) };
});

const mockShowAlert = jest.fn();
jest.mock('../../context/AlertModalContext', () => ({
  useAppAlert: () => ({ showAlert: mockShowAlert, hideAlert: jest.fn() }),
}));

jest.mock('../../components/UserAvatar', () => {
  const { Text } = require('react-native');
  return ({ size }: any) => <Text>{`Avatar ${size}`}</Text>;
});
jest.mock('../../components/LoadingPlaceholder', () => {
  const { Text } = require('react-native');
  return { LoadingPlaceholder: () => <Text>Cargando perfil</Text> };
});
jest.mock('../../components/ProfileInput', () => {
  const { TextInput } = require('react-native');
  return {
    ProfileInput: ({ label, value, onChange, editable, readOnly }: any) => (
      <TextInput testID={`input-${label}`} value={value} onChangeText={onChange} editable={editable} readOnly={readOnly} />
    ),
  };
});
jest.mock('../../components/carousels/ListCarousel', () => {
  const { View, Text } = require('react-native');
  return ({ data, onItemPress, onEmptyPress, onSeeAllPress }: any) => (
    <View>
      {data.map((d: any) => (
        <Text key={d.id} onPress={() => onItemPress(d.id)}>{`Lista ${d.title} img=${d.imgUrl}`}</Text>
      ))}
      <Text onPress={onEmptyPress}>Crear lista</Text>
      <Text onPress={onSeeAllPress}>Ver todas</Text>
    </View>
  );
});
jest.mock('../../components/ItemListPickerModal', () => {
  const { View, Text } = require('react-native');
  return ({ visible, lists, onSelect, onClose, footer }: any) =>
    visible ? (
      <View>
        <Text>Mis listas</Text>
        {lists.map((l: any) => (
          <Text key={l.id} onPress={() => onSelect(l.id)}>{`Modal ${l.name}`}</Text>
        ))}
        <Text onPress={onClose}>Cerrar mis listas</Text>
        {footer}
      </View>
    ) : null;
});
jest.mock('../../components/GenreViewer', () => {
  const { Text } = require('react-native');
  return ({ ids, onAddPress }: any) => <Text onPress={onAddPress}>{`Géneros ${ids.join(',')}`}</Text>;
});
jest.mock('../../components/InstrumentViewer', () => {
  const { Text } = require('react-native');
  return ({ ids, onAddPress }: any) => <Text onPress={onAddPress}>{`Instrumentos ${ids.join(',')}`}</Text>;
});
jest.mock('../../components/FavoritesPickerModal', () => {
  const { View, Text } = require('react-native');
  return ({ kind, initialIds, onSave, onClose }: any) => (
    <View>
      <Text>{`Selector ${kind} ${initialIds.join(',')}`}</Text>
      <Text onPress={() => onSave(['x', 'y'])}>Guardar favoritos</Text>
      <Text onPress={onClose}>Cerrar selector</Text>
    </View>
  );
});
jest.mock('../../components/PhotoOptionsModal', () => {
  const { View, Text } = require('react-native');
  return {
    PhotoOptionsModal: ({ visible, onClose, onViewImage, onPickImage }: any) =>
      visible ? (
        <View>
          <Text>Opciones de foto</Text>
          <Text onPress={onViewImage}>Ver foto</Text>
          <Text onPress={onPickImage}>Elegir foto</Text>
          <Text onPress={onClose}>Cerrar opciones</Text>
        </View>
      ) : null,
  };
});
jest.mock('../../components/CreateListMiniModal', () => {
  const { View, Text, TextInput } = require('react-native');
  return ({ visible, name, error, onChangeName, onSubmit, onClose }: any) =>
    visible ? (
      <View>
        <Text>Nueva lista</Text>
        <TextInput testID="list-name" value={name} onChangeText={onChangeName} />
        {error ? <Text>{error}</Text> : null}
        <Text onPress={onSubmit}>Crear</Text>
        <Text onPress={onClose}>Cancelar</Text>
      </View>
    ) : null;
});
jest.mock('../../components/buttons/SpotifyButton', () => {
  const { Text } = require('react-native');
  return { SpotifyButton: () => <Text>Spotify</Text> };
});

const baseUser = () => ({
  id: 'u1',
  name: 'Ana',
  username: 'ana',
  email: 'ana@mail.com',
  profileImageUrl: null as string | null,
  favouriteGenresIds: ['g1', 'g2'],
  favouriteInstrumentsIds: ['i1', 'i2'],
  difficultyLevel: 2,
});

describe('ProfileScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockProfileState = {
      user: baseUser(),
      isLoading: false,
      updateProfileData: jest.fn(),
      pickAndUploadImage: jest.fn(),
      updateFavoriteGenres: jest.fn(),
      updateFavoriteInstruments: jest.fn(),
    };
    mockListsState = {
      lists: [{ id: 'l1', name: 'Favoritas', imageUrl: null }],
      creating: false,
      createList: jest.fn(),
    };
  });

  it('muestra un placeholder si aún no hay usuario', async () => {
    mockProfileState.user = null;
    await render(<ProfileScreen />);
    expect(screen.getByText('Cargando perfil')).toBeTruthy();
  });

  it('muestra los datos del usuario, sus listas y favoritos', async () => {
    await render(<ProfileScreen />);
    expect(screen.getByText('@ana')).toBeTruthy();
    expect(screen.getByText('id: #u1')).toBeTruthy();
    expect(screen.getByTestId('input-Nombre').props.value).toBe('Ana');
    expect(screen.getByTestId('input-Usuario').props.value).toBe('ana');
    expect(screen.getByTestId('input-Email').props.value).toBe('ana@mail.com');
    expect(screen.getByTestId('input-Email').props.readOnly).toBe(true);
    expect(screen.getByText('Lista Favoritas img=')).toBeTruthy();
    expect(screen.getByText('Géneros g1,g2')).toBeTruthy();
    expect(screen.getByText('Instrumentos i1,i2')).toBeTruthy();
    expect(mockUseItemLists).toHaveBeenCalledWith('u1');
  });

  it('copia el id del usuario al portapapeles', async () => {
    await render(<ProfileScreen />);
    await fireEvent.press(screen.getByText('icon-Copy'));
    expect(Clipboard.setStringAsync).toHaveBeenCalledWith('u1');
  });

  it('navega a una lista al pulsarla', async () => {
    await render(<ProfileScreen />);
    await fireEvent.press(screen.getByText('Lista Favoritas img='));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('ListScreen', { itemListId: 'l1' });
  });

  describe('todas las listas (lupa)', () => {
    it('abre el modal y navega a la lista elegida cerrándolo', async () => {
      await render(<ProfileScreen />);
      expect(screen.queryByText('Mis listas')).toBeNull();
      await fireEvent.press(screen.getByText('Ver todas'));
      await fireEvent.press(screen.getByText('Modal Favoritas'));
      expect(mockNavigation.navigate).toHaveBeenCalledWith('ListScreen', { itemListId: 'l1' });
      expect(screen.queryByText('Mis listas')).toBeNull();
    });

    it('el botón de añadir del modal abre el de crear lista', async () => {
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Ver todas'));
      await fireEvent.press(screen.getByText('Añadir lista'));
      expect(screen.queryByText('Mis listas')).toBeNull();
      expect(screen.getByText('Nueva lista')).toBeTruthy();
    });
  });

  describe('crear lista', () => {
    it('crea la lista y navega a ella', async () => {
      mockListsState.createList.mockResolvedValue({ id: 'nueva' });
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Crear lista'));
      await fireEvent.changeText(screen.getByTestId('list-name'), '  Rock  ');
      await fireEvent.press(screen.getByText('Crear'));
      await waitFor(() => expect(mockNavigation.navigate).toHaveBeenCalledWith('ListScreen', { itemListId: 'nueva' }));
      expect(mockListsState.createList).toHaveBeenCalledWith({ name: 'Rock' });
      expect(screen.queryByText('Nueva lista')).toBeNull();
    });

    it('valida la longitud mínima del nombre y limpia el error al escribir', async () => {
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Crear lista'));
      await fireEvent.press(screen.getByText('Crear'));
      expect(screen.getByText('El nombre debe tener al menos 2 caracteres')).toBeTruthy();
      expect(mockListsState.createList).not.toHaveBeenCalled();
      await fireEvent.changeText(screen.getByTestId('list-name'), 'Ro');
      expect(screen.queryByText('El nombre debe tener al menos 2 caracteres')).toBeNull();
    });

    it('muestra un error si no se pudo crear', async () => {
      mockListsState.createList.mockResolvedValue(null);
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Crear lista'));
      await fireEvent.changeText(screen.getByTestId('list-name'), 'Rock');
      await fireEvent.press(screen.getByText('Crear'));
      await waitFor(() => expect(screen.getByText('No se pudo crear la lista')).toBeTruthy());
      expect(mockNavigation.navigate).not.toHaveBeenCalled();
    });

    it('cierra el modal al cancelar', async () => {
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Crear lista'));
      await fireEvent.press(screen.getByText('Cancelar'));
      expect(screen.queryByText('Nueva lista')).toBeNull();
    });
  });

  describe('guardar perfil', () => {
    it('guarda los cambios del formulario', async () => {
      mockProfileState.updateProfileData.mockResolvedValue(undefined);
      await render(<ProfileScreen />);
      await fireEvent.changeText(screen.getByTestId('input-Nombre'), 'Ana María');
      await fireEvent.changeText(screen.getByTestId('input-Usuario'), 'anam');
      await fireEvent.press(screen.getByText('Guardar ajustes'));
      await waitFor(() =>
        expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Exito', message: 'Perfil actualizado correctamente' })
      );
      expect(mockProfileState.updateProfileData).toHaveBeenCalledWith({
        name: 'Ana María',
        username: 'anam',
        email: 'ana@mail.com',
        difficultyLevel: 2,
      });
    });

    it('guarda el nivel elegido en el slider', async () => {
      mockProfileState.updateProfileData.mockResolvedValue(undefined);
      await render(<ProfileScreen />);
      const slider = screen.getByLabelText('Nivel musical');
      expect(slider.props.value).toBe(2);

      await act(async () => slider.props.onValueChange(4));
      expect(screen.getByLabelText('Nivel musical').props.value).toBe(4);
      await fireEvent.press(screen.getByText('Guardar ajustes'));
      await waitFor(() =>
        expect(mockProfileState.updateProfileData).toHaveBeenCalledWith(expect.objectContaining({ difficultyLevel: 4 }))
      );
    });

    it('un usuario sin nivel empieza en 0', async () => {
      mockProfileState.user = { ...baseUser(), difficultyLevel: undefined };
      await render(<ProfileScreen />);
      expect(screen.getByLabelText('Nivel musical').props.value).toBe(0);
    });

    it('no guarda si faltan favoritos', async () => {
      mockProfileState.user = { ...baseUser(), favouriteInstrumentsIds: ['i1'] };
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Guardar ajustes'));
      expect(mockShowAlert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Faltan favoritos' }));
      expect(mockProfileState.updateProfileData).not.toHaveBeenCalled();
    });

    it('avisa si el guardado falla', async () => {
      mockProfileState.updateProfileData.mockRejectedValue(new Error('500'));
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Guardar ajustes'));
      await waitFor(() =>
        expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Error', message: 'No se pudieron guardar los cambios' })
      );
    });

    it('muestra "Guardando..." mientras carga', async () => {
      mockProfileState.isLoading = true;
      await render(<ProfileScreen />);
      expect(screen.getByText('Guardando...')).toBeTruthy();
      expect(screen.getByTestId('input-Nombre').props.editable).toBe(false);
    });
  });

  it('sincroniza el formulario cuando cambia el usuario de la store', async () => {
    const { rerender } = await render(<ProfileScreen />);
    mockProfileState = { ...mockProfileState, user: { ...baseUser(), name: 'Ana Nueva' } };
    await rerender(<ProfileScreen />);
    expect(screen.getByTestId('input-Nombre').props.value).toBe('Ana Nueva');
  });

  it('tolera un usuario con campos y favoritos vacíos', async () => {
    mockProfileState.user = {
      id: 'u2', name: undefined, username: undefined, email: undefined,
      favouriteGenresIds: undefined, favouriteInstrumentsIds: undefined,
    };
    await render(<ProfileScreen />);
    expect(screen.getByTestId('input-Nombre').props.value).toBe('');
    expect(screen.getByText('Géneros ')).toBeTruthy();
    await fireEvent.press(screen.getByText('Instrumentos '));
    expect(screen.getByText('Selector instruments ')).toBeTruthy();
    // Sin favoritos no se permite guardar
    await fireEvent.press(screen.getByText('Guardar ajustes'));
    expect(mockShowAlert).toHaveBeenCalledWith(expect.objectContaining({ title: 'Faltan favoritos' }));
  });

  it('cierra sesión tras confirmar', async () => {
    await render(<ProfileScreen />);
    await fireEvent.press(screen.getByText('Cerrar sesión'));
    const alert = mockShowAlert.mock.calls[0][0];
    expect(alert.title).toBe('Cerrar sesión');
    const confirm = alert.actions.find((a: any) => a.style === 'destructive');
    expect(confirm.onPress).toBe(mockLogout);
  });

  describe('favoritos', () => {
    it('edita los géneros favoritos', async () => {
      mockProfileState.updateFavoriteGenres.mockResolvedValue(undefined);
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Géneros g1,g2'));
      expect(screen.getByText('Selector genres g1,g2')).toBeTruthy();
      await fireEvent.press(screen.getByText('Guardar favoritos'));
      await waitFor(() => expect(screen.queryByText(/Selector/)).toBeNull());
      expect(mockProfileState.updateFavoriteGenres).toHaveBeenCalledWith(['x', 'y']);
      expect(mockProfileState.updateFavoriteInstruments).not.toHaveBeenCalled();
    });

    it('edita los instrumentos favoritos', async () => {
      mockProfileState.updateFavoriteInstruments.mockResolvedValue(undefined);
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Instrumentos i1,i2'));
      expect(screen.getByText('Selector instruments i1,i2')).toBeTruthy();
      await fireEvent.press(screen.getByText('Guardar favoritos'));
      await waitFor(() => expect(mockProfileState.updateFavoriteInstruments).toHaveBeenCalledWith(['x', 'y']));
    });

    it('avisa si no se pudieron guardar y mantiene el selector abierto', async () => {
      mockProfileState.updateFavoriteGenres.mockRejectedValue(new Error('x'));
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Géneros g1,g2'));
      await fireEvent.press(screen.getByText('Guardar favoritos'));
      await waitFor(() =>
        expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Error', message: 'No se pudieron guardar los favoritos' })
      );
      expect(screen.getByText(/Selector genres/)).toBeTruthy();
    });

    it('cierra el selector', async () => {
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Géneros g1,g2'));
      await fireEvent.press(screen.getByText('Cerrar selector'));
      expect(screen.queryByText(/Selector/)).toBeNull();
    });
  });

  describe('foto de perfil', () => {
    it('sube la foto y cierra las opciones', async () => {
      mockProfileState.pickAndUploadImage.mockResolvedValue(true);
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Avatar 128'));
      expect(screen.getByText('Opciones de foto')).toBeTruthy();
      await fireEvent.press(screen.getByText('Elegir foto'));
      await waitFor(() => expect(screen.queryByText('Opciones de foto')).toBeNull());
    });

    it('mantiene las opciones abiertas si no se eligió foto', async () => {
      mockProfileState.pickAndUploadImage.mockResolvedValue(false);
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Avatar 128'));
      await act(async () => {
        await fireEvent.press(screen.getByText('Elegir foto'));
      });
      expect(screen.getByText('Opciones de foto')).toBeTruthy();
    });

    it('avisa si la subida falla', async () => {
      mockProfileState.pickAndUploadImage.mockRejectedValue(new Error('x'));
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Avatar 128'));
      await fireEvent.press(screen.getByText('Elegir foto'));
      await waitFor(() =>
        expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Error', message: 'No se pudo actualizar la foto de perfil' })
      );
    });

    it('sin foto, "Ver foto" muestra el avatar ampliado', async () => {
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Avatar 128'));
      await fireEvent.press(screen.getByText('Ver foto'));
      expect(screen.queryByText('Opciones de foto')).toBeNull();
      expect(screen.getByText('Avatar 256')).toBeTruthy();
      await fireEvent.press(screen.getByText('Avatar 256'));
      expect(screen.queryByText('Avatar 256')).toBeNull();
    });

    it('el botón atrás de Android cierra la foto ampliada', async () => {
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Avatar 128'));
      await fireEvent.press(screen.getByText('Ver foto'));
      expect(screen.getByText('Avatar 256')).toBeTruthy();

      const modal = screen.container.queryAll((n) => n.type === 'Modal' && n.props.visible === true)[0];
      await act(async () => modal.props.onRequestClose());
      expect(screen.queryByText('Avatar 256')).toBeNull();
    });

    it('con foto, "Ver foto" muestra la imagen ampliada', async () => {
      mockProfileState.user = { ...baseUser(), profileImageUrl: 'https://img/yo.png' };
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Avatar 128'));
      await fireEvent.press(screen.getByText('Ver foto'));
      const imgs = screen.root!.queryAll((n) => n.type === 'Image' && n.props.source?.uri === 'https://img/yo.png');
      expect(imgs.length).toBeGreaterThan(0);
    });

    it('cierra las opciones de foto', async () => {
      await render(<ProfileScreen />);
      await fireEvent.press(screen.getByText('Avatar 128'));
      await fireEvent.press(screen.getByText('Cerrar opciones'));
      expect(screen.queryByText('Opciones de foto')).toBeNull();
    });
  });
});
