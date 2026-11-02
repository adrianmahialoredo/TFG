import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import LoginScreen from '../../screens/LoginScreen';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

const mockNavigation = { navigate: jest.fn(), reset: jest.fn(), goBack: jest.fn() };
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => mockNavigation,
}));

const mockLogin = jest.fn();
jest.mock('../../context/AuthContext', () => ({
  useAuth: () => ({ login: mockLogin }),
}));

const mockShowAlert = jest.fn();
jest.mock('../../context/AlertModalContext', () => ({
  useAppAlert: () => ({ showAlert: mockShowAlert, hideAlert: jest.fn() }),
}));

const fillForm = async (email: string, password: string) => {
  await fireEvent.changeText(screen.getByPlaceholderText('Email o Usuario'), email);
  await fireEvent.changeText(screen.getByPlaceholderText('Contraseña'), password);
};

describe('LoginScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('muestra el formulario de inicio de sesión', async () => {
    await render(<LoginScreen />);
    expect(screen.getByText('Bienvenido a Toco')).toBeTruthy();
    expect(screen.getByPlaceholderText('Email o Usuario')).toBeTruthy();
    expect(screen.getByPlaceholderText('Contraseña')).toBeTruthy();
    expect(screen.getByText('Iniciar Sesión')).toBeTruthy();
  });

  it('avisa si falta el email o el usuario', async () => {
    await render(<LoginScreen />);
    await fireEvent.press(screen.getByText('Iniciar Sesión'));
    expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Error', message: 'Por favor ingresa tu email o tu usuario' });
    expect(mockLogin).not.toHaveBeenCalled();
  });

  it('avisa si falta la contraseña', async () => {
    await render(<LoginScreen />);
    await fireEvent.changeText(screen.getByPlaceholderText('Email o Usuario'), 'a@b.com');
    await fireEvent.press(screen.getByText('Iniciar Sesión'));
    expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Error', message: 'Por favor ingresa tu contrasena' });
  });

  it('permite iniciar sesión con el nombre de usuario (sin @)', async () => {
    mockLogin.mockResolvedValue(undefined);
    await render(<LoginScreen />);
    await fillForm('usuario', '1234');
    await fireEvent.press(screen.getByText('Iniciar Sesión'));
    expect(mockShowAlert).not.toHaveBeenCalled();
    expect(mockLogin).toHaveBeenCalledWith('usuario', '1234');
  });

  it('el campo no pone mayúsculas ni autocorrige (sirve para email y usuario)', async () => {
    await render(<LoginScreen />);
    const input = screen.getByPlaceholderText('Email o Usuario');
    expect(input.props.autoCapitalize).toBe('none');
    expect(input.props.autoCorrect).toBe(false);
    expect(input.props.keyboardType).toBeUndefined();
  });

  it('inicia sesión y resetea la navegación a las tabs', async () => {
    mockLogin.mockResolvedValue(undefined);
    await render(<LoginScreen />);
    await fillForm('a@b.com', 'secreto');
    await fireEvent.press(screen.getByText('Iniciar Sesión'));
    await waitFor(() => expect(mockNavigation.reset).toHaveBeenCalledWith({ index: 0, routes: [{ name: 'BottomTab' }] }));
    expect(mockLogin).toHaveBeenCalledWith('a@b.com', 'secreto');
    expect(mockShowAlert).not.toHaveBeenCalled();
  });

  it('muestra el mensaje del error si el login falla', async () => {
    mockLogin.mockRejectedValue(new Error('Credenciales inválidas'));
    await render(<LoginScreen />);
    await fillForm('a@b.com', 'mal');
    await fireEvent.press(screen.getByText('Iniciar Sesión'));
    await waitFor(() =>
      expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Error de Inicio de Sesion', message: 'Credenciales inválidas' })
    );
    expect(mockNavigation.reset).not.toHaveBeenCalled();
    expect(screen.getByText('Iniciar Sesión')).toBeTruthy();
  });

  it('usa un mensaje genérico si el error no es un Error', async () => {
    mockLogin.mockRejectedValue('boom');
    await render(<LoginScreen />);
    await fillForm('a@b.com', 'mal');
    await fireEvent.press(screen.getByText('Iniciar Sesión'));
    await waitFor(() =>
      expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Error de Inicio de Sesion', message: 'Usuario, email o contraseña incorrectos' })
    );
  });

  it('navega al registro', async () => {
    await render(<LoginScreen />);
    await fireEvent.press(screen.getByText('Registrarse'));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('RegisterScreen');
  });

  it('alterna la visibilidad de la contraseña', async () => {
    await render(<LoginScreen />);
    expect(screen.getByPlaceholderText('Contraseña').props.secureTextEntry).toBe(true);
    await fireEvent.press(screen.getByLabelText('Mostrar contraseña'));
    expect(screen.getByPlaceholderText('Contraseña').props.secureTextEntry).toBe(false);
    expect(screen.getByLabelText('Ocultar contraseña')).toBeTruthy();
  });
});
