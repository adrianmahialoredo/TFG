import React from 'react';
import { render, screen } from '@testing-library/react-native';
import App from '../../App';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

jest.mock('../../global.css', () => ({}));

let mockLoading = false;
jest.mock('../context/AuthContext', () => {
  const { Fragment } = require('react');
  return {
    AuthProvider: ({ children }: any) => <Fragment>{children}</Fragment>,
    useAuth: () => ({ loading: mockLoading }),
  };
});

jest.mock('../context/AlertModalContext', () => {
  const { Fragment } = require('react');
  return { AlertModalProvider: ({ children }: any) => <Fragment>{children}</Fragment> };
});

jest.mock('../navigation/AppNavigator', () => {
  const { Text } = require('react-native');
  return () => <Text>Navegador</Text>;
});

describe('App', () => {
  beforeEach(() => {
    mockLoading = false;
  });

  it('muestra un indicador de carga mientras se inicializa la sesión', async () => {
    mockLoading = true;
    await render(<App />);
    expect(screen.queryByText('Navegador')).toBeNull();
  });

  it('monta el navegador cuando termina de cargar', async () => {
    await render(<App />);
    expect(screen.getByText('Navegador')).toBeTruthy();
  });
});
