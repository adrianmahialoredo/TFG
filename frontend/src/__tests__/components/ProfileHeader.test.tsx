import { render, screen, fireEvent } from '@testing-library/react-native';
import ProfileHeader from '@/components/ProfileHeader';
import { useAuth } from '@/context/AuthContext';

jest.setTimeout(30000);

const mockNavigate = jest.fn();
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => ({ navigate: mockNavigate }),
}));
jest.mock('@/context/AuthContext', () => ({ useAuth: jest.fn() }));

describe('ProfileHeader', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
    (useAuth as jest.Mock).mockReturnValue({ user: { name: 'lucía', username: 'lu' } });
  });

  it('muestra el título y el avatar con la inicial del usuario', async () => {
    await render(<ProfileHeader title="Hola, Lucía" />);
    expect(screen.getByText('Hola, Lucía')).toBeTruthy();
    expect(screen.getByText('L')).toBeTruthy();
  });

  it('al pulsar el avatar navega al perfil', async () => {
    await render(<ProfileHeader title="Inicio" />);
    await fireEvent.press(screen.getByText('L'));
    expect(mockNavigate).toHaveBeenCalledWith('ProfileScreen');
  });

  it('en modo compacto no muestra el título pero sí navega al perfil', async () => {
    await render(<ProfileHeader title="Inicio" compact />);
    expect(screen.queryByText('Inicio')).toBeNull();
    await fireEvent.press(screen.getByText('L'));
    expect(mockNavigate).toHaveBeenCalledWith('ProfileScreen');
  });

  it('usa la foto de perfil si el usuario la tiene', async () => {
    (useAuth as jest.Mock).mockReturnValue({ user: { name: 'Lucía', profileImageUrl: 'http://img/lu.png' } });
    await render(<ProfileHeader title="Inicio" />);
    const [image] = screen.container.queryAll((n) => n.type === 'Image');
    expect(image.props.source).toEqual({ uri: 'http://img/lu.png' });
  });
});
