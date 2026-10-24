import { act, renderHook, waitFor } from '@testing-library/react-native';
import * as ImagePicker from 'expo-image-picker';
import { useProfile } from '../../hooks/useProfile';
import { useAuthStore, useUserEntitiesStore } from '../../stores/entities';
import * as userRepository from '../../repositories/user.repository';

jest.mock('expo-image-picker', () => ({ launchImageLibraryAsync: jest.fn() }));
jest.mock('../../repositories/user.repository', () => ({
  getById: jest.fn(),
  updateProfile: jest.fn(),
  updateProfileImage: jest.fn(),
  updateFavorites: jest.fn(),
}));

const mockedPicker = ImagePicker as jest.Mocked<typeof ImagePicker>;
const mockedUser = userRepository as jest.Mocked<typeof userRepository>;

const user = { id: 'u1', name: 'Ana' } as any;

describe('useProfile', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useAuthStore.setState({ token: 'tk', currentUserId: 'u1', isAuthenticated: true });
    useUserEntitiesStore.setState({ users: { u1: user } });
    mockedUser.getById.mockResolvedValue(user);
    mockedUser.updateProfile.mockResolvedValue(user);
    mockedUser.updateProfileImage.mockResolvedValue(user);
    mockedUser.updateFavorites.mockResolvedValue(user);
    mockedPicker.launchImageLibraryAsync.mockResolvedValue({ canceled: false, assets: [{ uri: 'file://foto.jpg' }] } as any);
  });

  it('devuelve el usuario actual sin volver a pedirlo', async () => {
    const { result } = await renderHook(() => useProfile());
    expect(result.current.user).toBe(user);
    expect(result.current.isLoading).toBe(false);
    expect(result.current.error).toBeNull();
    expect(mockedUser.getById).not.toHaveBeenCalled();
  });

  it('hidrata el usuario si no está en RAM', async () => {
    useUserEntitiesStore.setState({ users: {} });
    mockedUser.getById.mockImplementation(async () => {
      useUserEntitiesStore.setState({ users: { u1: user } });
      return user;
    });
    const { result } = await renderHook(() => useProfile());
    await waitFor(() => expect(result.current.user).toBe(user));
    expect(mockedUser.getById).toHaveBeenCalledTimes(1);
  });

  it('registra el error si falla la hidratación', async () => {
    useUserEntitiesStore.setState({ users: {} });
    mockedUser.getById.mockRejectedValue(new Error('red'));
    await renderHook(() => useProfile());
    await waitFor(() =>
      expect(console.error).toHaveBeenCalledWith('[useProfile] Error hidratando usuario:', expect.any(Error))
    );
  });

  it('sin sesión no hidrata ni ejecuta acciones', async () => {
    useAuthStore.setState({ currentUserId: null });
    const { result } = await renderHook(() => useProfile());
    expect(result.current.user).toBeUndefined();

    await act(async () => {
      await result.current.updateProfileData({ name: 'x' });
      await result.current.updateFavoriteGenres(['1']);
      await expect(result.current.pickAndUploadImage()).resolves.toBe(false);
    });
    expect(mockedUser.getById).not.toHaveBeenCalled();
    expect(mockedUser.updateProfile).not.toHaveBeenCalled();
    expect(mockedUser.updateFavorites).not.toHaveBeenCalled();
    expect(mockedUser.updateProfileImage).not.toHaveBeenCalled();
  });

  it('updateProfileData actualiza el perfil', async () => {
    const { result } = await renderHook(() => useProfile());
    await act(async () => {
      await result.current.updateProfileData({ name: 'Nuevo' });
    });
    expect(mockedUser.updateProfile).toHaveBeenCalledWith('u1', { name: 'Nuevo' });
    expect(result.current.isLoading).toBe(false);
  });

  it('updateProfileData guarda el error y relanza', async () => {
    mockedUser.updateProfile.mockRejectedValue(new Error('x'));
    const { result } = await renderHook(() => useProfile());
    await act(async () => {
      await expect(result.current.updateProfileData({})).rejects.toThrow('x');
    });
    expect(result.current.error).toBe('Error al actualizar el perfil');
    expect(result.current.isLoading).toBe(false);
  });

  it('pickAndUploadImage sube la foto elegida', async () => {
    const { result } = await renderHook(() => useProfile());
    let ok: boolean | undefined;
    await act(async () => {
      ok = await result.current.pickAndUploadImage();
    });
    expect(ok).toBe(true);
    expect(mockedPicker.launchImageLibraryAsync).toHaveBeenCalledWith({ allowsEditing: true, aspect: [1, 1], quality: 0.5 });
    expect(mockedUser.updateProfileImage).toHaveBeenCalledWith('u1', 'file://foto.jpg');
  });

  it('pickAndUploadImage devuelve false si el usuario cancela', async () => {
    mockedPicker.launchImageLibraryAsync.mockResolvedValue({ canceled: true, assets: null } as any);
    const { result } = await renderHook(() => useProfile());
    await expect(result.current.pickAndUploadImage()).resolves.toBe(false);
    expect(mockedUser.updateProfileImage).not.toHaveBeenCalled();
  });

  it('pickAndUploadImage guarda el error y relanza si falla la subida', async () => {
    mockedUser.updateProfileImage.mockRejectedValue(new Error('subida'));
    const { result } = await renderHook(() => useProfile());
    await act(async () => {
      await expect(result.current.pickAndUploadImage()).rejects.toThrow('subida');
    });
    expect(result.current.error).toBe('No se pudo actualizar la foto de perfil');
    expect(result.current.isLoading).toBe(false);
  });

  it('actualiza géneros e instrumentos favoritos', async () => {
    const { result } = await renderHook(() => useProfile());
    await act(async () => {
      await result.current.updateFavoriteGenres(['g1']);
      await result.current.updateFavoriteInstruments(['i1']);
    });
    expect(mockedUser.updateFavorites).toHaveBeenCalledWith('u1', 'genres', ['g1']);
    expect(mockedUser.updateFavorites).toHaveBeenCalledWith('u1', 'instruments', ['i1']);
  });

  it('los favoritos guardan el error y relanzan', async () => {
    mockedUser.updateFavorites.mockRejectedValue(new Error('fav'));
    const { result } = await renderHook(() => useProfile());
    await act(async () => {
      await expect(result.current.updateFavoriteInstruments(['i1'])).rejects.toThrow('fav');
    });
    expect(result.current.error).toBe('Error al actualizar los favoritos');
  });
});
