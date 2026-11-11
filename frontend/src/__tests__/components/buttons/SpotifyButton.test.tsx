import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import { SpotifyButton } from '@/components/buttons/SpotifyButton';
import { useSpotifyConnect } from '@/hooks/useSpotifyConnect';
import { useAppAlert } from '@/context/AlertModalContext';

jest.setTimeout(30000);

jest.mock('@/hooks/useSpotifyConnect', () => ({ useSpotifyConnect: jest.fn() }));
jest.mock('@/context/AlertModalContext', () => ({ useAppAlert: jest.fn() }));

const mockedUseSpotifyConnect = useSpotifyConnect as jest.Mock;
const mockedUseAppAlert = useAppAlert as jest.Mock;

describe('SpotifyButton', () => {
  let showAlert: jest.Mock;
  let connect: jest.Mock;

  beforeEach(() => {
    showAlert = jest.fn();
    connect = jest.fn().mockResolvedValue(undefined);
    mockedUseAppAlert.mockReturnValue({ showAlert });
    mockedUseSpotifyConnect.mockReturnValue({ connect, connecting: false });
  });

  it('muestra el texto del botón', async () => {
    await render(<SpotifyButton />);
    expect(screen.getByText('Conectar con Spotify')).toBeTruthy();
  });

  it('al pulsar inicia la conexión con Spotify', async () => {
    await render(<SpotifyButton />);
    await fireEvent.press(screen.getByText('Conectar con Spotify'));
    expect(connect).toHaveBeenCalledTimes(1);
    expect(showAlert).not.toHaveBeenCalled();
  });

  it('si la conexión falla muestra una alerta de error', async () => {
    connect.mockRejectedValue(new Error('fallo'));
    await render(<SpotifyButton />);
    await fireEvent.press(screen.getByText('Conectar con Spotify'));
    await waitFor(() =>
      expect(showAlert).toHaveBeenCalledWith({ title: 'Error', message: 'No se pudo iniciar sesion con Spotify.' })
    );
  });

  it('mientras conecta el botón está desactivado', async () => {
    mockedUseSpotifyConnect.mockReturnValue({ connect, connecting: true });
    await render(<SpotifyButton />);
    await fireEvent.press(screen.getByText('Conectando…'));
    expect(connect).not.toHaveBeenCalled();
  });
});
