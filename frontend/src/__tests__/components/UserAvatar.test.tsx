import { StyleSheet } from 'react-native';
import { render, screen } from '@testing-library/react-native';
import UserAvatar from '@/components/UserAvatar';

jest.setTimeout(30000);

const images = () => screen.container.queryAll((n) => n.type === 'Image');

describe('UserAvatar', () => {
  it('muestra la foto de perfil si el usuario la tiene', async () => {
    await render(<UserAvatar user={{ name: 'Ana', profileImageUrl: 'http://img/ana.png' }} size={48} />);
    const [image] = images();
    expect(image.props.source).toEqual({ uri: 'http://img/ana.png' });
    expect(StyleSheet.flatten(image.props.style)).toEqual(expect.objectContaining({ width: 48, height: 48 }));
    expect(screen.queryByText('A')).toBeNull();
  });

  it('sin foto muestra la inicial del nombre en mayúscula con tamaño proporcional', async () => {
    await render(<UserAvatar user={{ name: 'ana', username: 'zeta' }} size={40} />);
    expect(images()).toHaveLength(0);
    const initial = screen.getByText('A');
    expect(StyleSheet.flatten(initial.props.style).fontSize).toBe(18);
  });

  it('usa el nombre de usuario si no hay nombre', async () => {
    await render(<UserAvatar user={{ username: 'pepe' }} size={40} />);
    expect(screen.getByText('P')).toBeTruthy();
  });

  it('muestra "?" si no hay usuario', async () => {
    await render(<UserAvatar user={null} size={40} />);
    expect(screen.getByText('?')).toBeTruthy();
  });
});
