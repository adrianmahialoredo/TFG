import React from 'react';
import { act, renderHook, waitFor } from '@testing-library/react-native';
import { AuthProvider, useAuth } from '../../context/AuthContext';
import { useAuthStore, useUserEntitiesStore } from '../../stores/entities';
import * as authRepository from '../../repositories/auth.repository';
import * as userRepository from '../../repositories/user.repository';
import * as genreRepository from '../../repositories/genre.repository';
import * as itemRepository from '../../repositories/item.repository';

jest.mock('../../repositories/auth.repository', () => ({ login: jest.fn(), logout: jest.fn() }));
jest.mock('../../repositories/user.repository', () => ({ getById: jest.fn() }));
jest.mock('../../repositories/genre.repository', () => ({ getAll: jest.fn() }));
jest.mock('../../repositories/item.repository', () => ({ getCatalog: jest.fn() }));

const mockedAuth = authRepository as jest.Mocked<typeof authRepository>;
const mockedUser = userRepository as jest.Mocked<typeof userRepository>;
const mockedGenre = genreRepository as jest.Mocked<typeof genreRepository>;
const mockedItem = itemRepository as jest.Mocked<typeof itemRepository>;

const wrapper = ({ children }: { children: React.ReactNode }) => <AuthProvider>{children}</AuthProvider>;

const user = { id: '7', name: 'Ana', username: 'ana' } as any;

describe('AuthContext', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useAuthStore.setState({ token: null, currentUserId: null, isAuthenticated: false, isHydrated: true });
    useUserEntitiesStore.setState({ users: {} });
    mockedAuth.login.mockResolvedValue(undefined as any);
    mockedAuth.logout.mockResolvedValue(undefined as any);
    mockedUser.getById.mockResolvedValue(user);
    mockedGenre.getAll.mockResolvedValue([] as any);
    mockedItem.getCatalog.mockResolvedValue([] as any);
  });

  it('lanza un error si se usa fuera del AuthProvider', async () => {
    await expect(renderHook(() => useAuth())).rejects.toThrow('useAuth debe usarse dentro de AuthProvider');
  });

  it('se queda cargando mientras la AuthStore no está hidratada', async () => {
    useAuthStore.setState({ isHydrated: false, token: 'tk', currentUserId: '7' });
    const { result } = await renderHook(() => useAuth(), { wrapper });

    expect(result.current.loading).toBe(true);
    expect(mockedUser.getById).not.toHaveBeenCalled();
    expect(mockedGenre.getAll).not.toHaveBeenCalled();
  });

  it('sin sesión termina de cargar sin pedir datos', async () => {
    const { result } = await renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.user).toBeNull();
    expect(result.current.token).toBeNull();
    expect(mockedUser.getById).not.toHaveBeenCalled();
    expect(mockedGenre.getAll).not.toHaveBeenCalled();
    expect(mockedItem.getCatalog).not.toHaveBeenCalled();
  });

  it('restaura el usuario de una sesión persistida y carga los datos globales', async () => {
    useAuthStore.setState({ token: 'tk', currentUserId: '7', isAuthenticated: true });
    mockedUser.getById.mockImplementation(async () => {
      useUserEntitiesStore.setState({ users: { '7': user } });
      return user;
    });

    const { result } = await renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedUser.getById).toHaveBeenCalledWith('7');
    expect(result.current.user).toEqual(user);
    expect(result.current.token).toBe('tk');
    expect(mockedItem.getCatalog).toHaveBeenCalledTimes(1);
    expect(mockedGenre.getAll).toHaveBeenCalledTimes(1);
  });

  it('no pide el usuario si ya está en la Store', async () => {
    useAuthStore.setState({ token: 'tk', currentUserId: '7', isAuthenticated: true });
    useUserEntitiesStore.setState({ users: { '7': user } });

    const { result } = await renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedUser.getById).not.toHaveBeenCalled();
    expect(result.current.user).toEqual(user);
    expect(mockedGenre.getAll).toHaveBeenCalledTimes(1);
  });

  it('si falla la restauración del usuario registra el error y termina de cargar', async () => {
    useAuthStore.setState({ token: 'tk', currentUserId: '7', isAuthenticated: true });
    mockedUser.getById.mockRejectedValue(new Error('red'));

    const { result } = await renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(console.error).toHaveBeenCalledWith('[AuthContext] Error restaurando usuario:', expect.any(Error));
    expect(result.current.user).toBeNull();
  });

  it('los errores del catálogo y de los géneros no rompen el arranque', async () => {
    useAuthStore.setState({ token: 'tk', currentUserId: '7', isAuthenticated: true });
    useUserEntitiesStore.setState({ users: { '7': user } });
    mockedItem.getCatalog.mockRejectedValue(new Error('catálogo'));
    mockedGenre.getAll.mockRejectedValue(new Error('géneros'));

    const { result } = await renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));
    await waitFor(() =>
      expect(console.error).toHaveBeenCalledWith('[AuthContext] Error cargando el catálogo de items:', expect.any(Error))
    );
    expect(console.error).toHaveBeenCalledWith('[AuthContext] Error cargando géneros:', expect.any(Error));
  });

  it('carga los datos globales una sola vez aunque cambie el token', async () => {
    useAuthStore.setState({ token: 'tk', currentUserId: '7', isAuthenticated: true });
    useUserEntitiesStore.setState({ users: { '7': user } });

    const { result } = await renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    await act(async () => {
      useAuthStore.setState({ token: 'tk2' });
    });

    expect(result.current.token).toBe('tk2');
    expect(mockedGenre.getAll).toHaveBeenCalledTimes(1);
  });

  /** El repositorio real guarda la sesión en la AuthStore al hacer login. */
  const loginSetsSession = () =>
    mockedAuth.login.mockImplementation(async () => {
      useAuthStore.setState({ token: 'tk', currentUserId: '7', isAuthenticated: true });
      useUserEntitiesStore.setState({ users: { '7': user } });
      return { token: 'tk', user };
    });

  it('login llama al repositorio y carga los datos globales una sola vez', async () => {
    loginSetsSession();
    const { result } = await renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.loading).toBe(false));

    await act(async () => {
      await result.current.login('a@b.com', 'Secreta1!');
    });

    expect(mockedAuth.login).toHaveBeenCalledWith('a@b.com', 'Secreta1!');
    await waitFor(() => expect(mockedGenre.getAll).toHaveBeenCalledTimes(1));
    expect(mockedItem.getCatalog).toHaveBeenCalledTimes(1);
    expect(result.current.isAuthenticated).toBe(true);
  });

  it('isLoading está activo mientras el login está en curso', async () => {
    let resolveLogin: () => void = () => {};
    mockedAuth.login.mockReturnValue(new Promise<void>((r) => { resolveLogin = r; }) as any);
    const { result } = await renderHook(() => useAuth(), { wrapper });
    expect(result.current.isLoading).toBe(false);

    let pending: Promise<void>;
    await act(async () => { pending = result.current.login('a@b.com', 'Secreta1!'); });
    expect(result.current.isLoading).toBe(true);

    await act(async () => { resolveLogin(); await pending; });
    expect(result.current.isLoading).toBe(false);
  });

  it('expone isReady según la hidratación de la AuthStore', async () => {
    useAuthStore.setState({ isHydrated: false });
    const { result } = await renderHook(() => useAuth(), { wrapper });
    expect(result.current.isReady).toBe(false);

    await act(async () => { useAuthStore.setState({ isHydrated: true }); });
    expect(result.current.isReady).toBe(true);
  });

  it('tras un logout, el siguiente login vuelve a cargar los datos globales', async () => {
    loginSetsSession();
    mockedAuth.logout.mockImplementation(async () => {
      useAuthStore.setState({ token: null, currentUserId: null, isAuthenticated: false });
    });
    const { result } = await renderHook(() => useAuth(), { wrapper });

    await act(async () => { await result.current.login('a@b.com', 'Secreta1!'); });
    await waitFor(() => expect(mockedGenre.getAll).toHaveBeenCalledTimes(1));

    await act(async () => { await result.current.logout(); });
    await act(async () => { await result.current.login('a@b.com', 'Secreta1!'); });
    await waitFor(() => expect(mockedGenre.getAll).toHaveBeenCalledTimes(2));
  });

  it('login relanza el error del backend', async () => {
    mockedAuth.login.mockRejectedValue(new Error('401'));
    const { result } = await renderHook(() => useAuth(), { wrapper });

    await expect(result.current.login('a@b.com', 'mala')).rejects.toThrow('401');
    expect(console.warn).toHaveBeenCalledWith('[AuthContext] Login rejected by backend');
    expect(mockedGenre.getAll).not.toHaveBeenCalled();
  });

  it('logout llama al repositorio', async () => {
    const { result } = await renderHook(() => useAuth(), { wrapper });

    await act(async () => {
      await result.current.logout();
    });
    expect(mockedAuth.logout).toHaveBeenCalledTimes(1);
  });

  it('logout relanza el error si falla', async () => {
    mockedAuth.logout.mockRejectedValue(new Error('fallo'));
    const { result } = await renderHook(() => useAuth(), { wrapper });

    await expect(result.current.logout()).rejects.toThrow('fallo');
    expect(console.error).toHaveBeenCalledWith('[AuthContext] Logout failed:', expect.any(Error));
  });
});
