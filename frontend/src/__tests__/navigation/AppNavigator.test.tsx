import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import { NavigationContainer } from '@react-navigation/native';
import AppNavigator from '../../navigation/AppNavigator';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

let mockAuthState: any;
jest.mock('../../context/AuthContext', () => ({
  useAuth: () => mockAuthState,
}));

jest.mock('../../screens/LoginScreen', () => {
  const { Text } = require('react-native');
  const { useNavigation: useNav } = require('@react-navigation/native');
  return () => {
    const navigation = useNav();
    return <Text onPress={() => navigation.navigate('RegisterScreen')}>Pantalla Login</Text>;
  };
});
jest.mock('../../screens/RegisterScreen', () => {
  const { Text } = require('react-native');
  return () => <Text>Pantalla Registro</Text>;
});
jest.mock('../../navigation/BottomTabs', () => {
  const { Text } = require('react-native');
  return () => <Text>Pestañas</Text>;
});

const renderNavigator = () =>
  render(
    <NavigationContainer>
      <AppNavigator />
    </NavigationContainer>
  );

describe('AppNavigator', () => {
  beforeEach(() => {
    mockAuthState = { isAuthenticated: false, isReady: true };
  });

  it('muestra una pantalla de carga mientras la store no está hidratada', async () => {
    mockAuthState = { isAuthenticated: false, isReady: false };
    await renderNavigator();
    expect(screen.queryByText('Pantalla Login')).toBeNull();
    expect(screen.queryByText('Pestañas')).toBeNull();
  });

  it('sin sesión muestra el login', async () => {
    await renderNavigator();
    expect(screen.getByText('Pantalla Login')).toBeTruthy();
    expect(screen.queryByText('Pestañas')).toBeNull();
  });

  it('sin sesión permite ir al registro', async () => {
    await renderNavigator();
    await fireEvent.press(screen.getByText('Pantalla Login'));
    expect(await screen.findByText('Pantalla Registro')).toBeTruthy();
  });

  it('con sesión muestra las pestañas', async () => {
    mockAuthState = { isAuthenticated: true, isReady: true };
    await renderNavigator();
    expect(screen.getByText('Pestañas')).toBeTruthy();
    expect(screen.queryByText('Pantalla Login')).toBeNull();
  });
});
