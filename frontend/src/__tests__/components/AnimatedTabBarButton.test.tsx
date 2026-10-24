import { Text } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import AnimatedTabBarButton from '@/components/AnimatedTabBarButton';

jest.setTimeout(30000);

// El mock global de reanimated 4 necesita también el de worklets (si no, carga el módulo nativo)
jest.mock('react-native-worklets', () => require('react-native-worklets/src/mock'));

describe('AnimatedTabBarButton', () => {
  it('renderiza sus hijos', async () => {
    await render(
      <AnimatedTabBarButton>
        <Text>Inicio</Text>
      </AnimatedTabBarButton>
    );
    expect(screen.getByText('Inicio')).toBeTruthy();
  });

  it('al pulsar llama a onPress con el evento', async () => {
    const onPress = jest.fn();
    await render(
      <AnimatedTabBarButton onPress={onPress}>
        <Text>Buscar</Text>
      </AnimatedTabBarButton>
    );
    await fireEvent.press(screen.getByText('Buscar'), { nativeEvent: {} });
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('no falla si no hay onPress', async () => {
    await render(
      <AnimatedTabBarButton>
        <Text>Perfil</Text>
      </AnimatedTabBarButton>
    );
    await fireEvent.press(screen.getByText('Perfil'));
    expect(screen.getByText('Perfil')).toBeTruthy();
  });

  it('pasa las props de accesibilidad y descarta las propias de React Navigation', async () => {
    await render(
      <AnimatedTabBarButton
        accessibilityRole="tab"
        accessibilityLabel="Pestaña inicio"
        accessibilityState={{ selected: true }}
        {...({ href: '/home', pressOpacity: 0.5, hoverEffect: {}, pressColor: 'red' } as any)}
      >
        <Text>Inicio</Text>
      </AnimatedTabBarButton>
    );
    const tab = screen.getByRole('tab', { name: 'Pestaña inicio' });
    expect(tab).toBeSelected();
    expect(tab.props.href).toBeUndefined();
    expect(tab.props.pressColor).toBeUndefined();
  });
});
