import { act, renderHook } from '@testing-library/react-native';
import { AppState } from 'react-native';
import * as WebBrowser from 'expo-web-browser';
import { useSpotifyConnect } from '../../hooks/useSpotifyConnect';
import { useAuthStore } from '../../stores/entities';
import * as spotifyRepository from '../../repositories/spotify.repository';

jest.mock('expo-web-browser', () => ({ openBrowserAsync: jest.fn() }));
jest.mock('../../repositories/spotify.repository', () => ({ getAuthUrl: jest.fn(), refreshLinkedUser: jest.fn() }));

const mockedBrowser = WebBrowser as jest.Mocked<typeof WebBrowser>;
const mockedSpotify = spotifyRepository as jest.Mocked<typeof spotifyRepository>;

describe('useSpotifyConnect', () => {
  let appStateListener: ((state: string) => void) | undefined;
  let removeSubscription: jest.Mock;

  beforeEach(() => {
    jest.clearAllMocks();
    useAuthStore.setState({ token: 'tk', currentUserId: 'u1', isAuthenticated: true });
    appStateListener = undefined;
    removeSubscription = jest.fn();
    jest.spyOn(AppState, 'addEventListener').mockImplementation((_type: any, listener: any) => {
      appStateListener = listener;
      return { remove: removeSubscription } as any;
    });
    mockedSpotify.getAuthUrl.mockResolvedValue('https://spotify/auth');
    mockedSpotify.refreshLinkedUser.mockResolvedValue(undefined as any);
    mockedBrowser.openBrowserAsync.mockResolvedValue({ type: 'cancel' } as any);
  });

  it('en iOS abre la autorización y al cerrar el navegador refresca el usuario', async () => {
    const { result } = await renderHook(() => useSpotifyConnect());
    expect(result.current.connecting).toBe(false);

    await act(async () => {
      await result.current.connect();
    });

    expect(mockedBrowser.openBrowserAsync).toHaveBeenCalledWith('https://spotify/auth');
    expect(mockedSpotify.refreshLinkedUser).toHaveBeenCalledWith('u1');
    expect(removeSubscription).toHaveBeenCalled();
    expect(result.current.connecting).toBe(false);
  });

  it('en Android espera a que la app vuelva a primer plano', async () => {
    mockedBrowser.openBrowserAsync.mockResolvedValue({ type: 'opened' } as any);
    const { result } = await renderHook(() => useSpotifyConnect());

    let promise: Promise<void>;
    await act(async () => {
      promise = result.current.connect();
    });
    expect(result.current.connecting).toBe(true);
    expect(mockedSpotify.refreshLinkedUser).not.toHaveBeenCalled();

    // Un cambio a segundo plano no cuenta
    await act(async () => appStateListener?.('background'));
    expect(mockedSpotify.refreshLinkedUser).not.toHaveBeenCalled();

    await act(async () => {
      appStateListener?.('active');
      await promise;
    });
    expect(mockedSpotify.refreshLinkedUser).toHaveBeenCalledWith('u1');
    expect(result.current.connecting).toBe(false);
  });

  it('sin usuario no refresca', async () => {
    useAuthStore.setState({ currentUserId: null });
    const { result } = await renderHook(() => useSpotifyConnect());
    await act(async () => {
      await result.current.connect();
    });
    expect(mockedSpotify.refreshLinkedUser).not.toHaveBeenCalled();
  });

  it('propaga el error y limpia el estado', async () => {
    mockedSpotify.getAuthUrl.mockRejectedValue(new Error('sin url'));
    const { result } = await renderHook(() => useSpotifyConnect());
    await act(async () => {
      await expect(result.current.connect()).rejects.toThrow('sin url');
    });
    expect(mockedBrowser.openBrowserAsync).not.toHaveBeenCalled();
    expect(removeSubscription).toHaveBeenCalled();
    expect(result.current.connecting).toBe(false);
  });
});
