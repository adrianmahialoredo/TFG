import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import RegisterScreen from '../../screens/RegisterScreen';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

const mockNavigation = { navigate: jest.fn(), goBack: jest.fn() };
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => mockNavigation,
}));

const mockRequestCode = jest.fn();
const mockResendCode = jest.fn();
const mockRegister = jest.fn();
let mockIsLoading = false;
jest.mock('../../hooks/useRegister', () => ({
  useRegister: () => ({
    requestCode: mockRequestCode,
    resendCode: mockResendCode,
    register: mockRegister,
    isLoading: mockIsLoading,
  }),
}));

const mockShowAlert = jest.fn();
jest.mock('../../context/AlertModalContext', () => ({
  useAppAlert: () => ({ showAlert: mockShowAlert, hideAlert: jest.fn() }),
}));

jest.mock('../../components/GenrePicker', () => {
  const { Text } = require('react-native');
  return ({ onSelectionChange }: any) => (
    <Text onPress={() => onSelectionChange(['g1', 'g2'])}>GenrePicker</Text>
  );
});
jest.mock('../../components/InstrumentPicker', () => {
  const { Text } = require('react-native');
  return ({ onSelectionChange }: any) => (
    <Text onPress={() => onSelectionChange(['i1'])}>InstrumentPicker</Text>
  );
});
jest.mock('../../components/EmailVerificationModal', () => {
  const { View, Text } = require('react-native');
  return ({ email, error, onSubmit, onResend, onClose }: any) => (
    <View>
      <Text>Verificar {email}</Text>
      {error ? <Text>{error}</Text> : null}
      <Text onPress={() => onSubmit('123456')}>Enviar código</Text>
      <Text onPress={onResend}>Reenviar código</Text>
      <Text onPress={onClose}>Cerrar modal</Text>
    </View>
  );
});

const fillForm = async () => {
  await fireEvent.changeText(screen.getByPlaceholderText('Nombre'), 'Ana');
  await fireEvent.changeText(screen.getByPlaceholderText('Email'), ' ana@mail.com ');
  await fireEvent.changeText(screen.getByPlaceholderText('Usuario'), 'ana');
  await fireEvent.changeText(screen.getByPlaceholderText('Contraseña'), 'Secreta1');
  await fireEvent.changeText(screen.getByPlaceholderText('Repetir contraseña'), 'Secreta1');
  await fireEvent.press(screen.getByText('GenrePicker'));
  await fireEvent.press(screen.getByText('InstrumentPicker'));
};

describe('RegisterScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockIsLoading = false;
  });

  it('muestra todos los campos del formulario', async () => {
    await render(<RegisterScreen />);
    ['Nombre', 'Email', 'Usuario', 'Contraseña', 'Repetir contraseña'].forEach((p) =>
      expect(screen.getByPlaceholderText(p)).toBeTruthy()
    );
    expect(screen.getByText('Crear cuenta')).toBeTruthy();
    expect(screen.queryByText(/Verificar/)).toBeNull();
  });

  it('vuelve atrás con el botón de volver', async () => {
    await render(<RegisterScreen />);
    await fireEvent.press(screen.getByLabelText('Volver'));
    expect(mockNavigation.goBack).toHaveBeenCalled();
  });

  it('pide el código con los datos del formulario y abre la verificación', async () => {
    mockRequestCode.mockResolvedValue(undefined);
    await render(<RegisterScreen />);
    await fillForm();
    await fireEvent.press(screen.getByText('Crear cuenta'));
    await waitFor(() => expect(screen.getByText('Verificar ana@mail.com')).toBeTruthy());
    expect(mockRequestCode).toHaveBeenCalledWith({
      name: 'Ana',
      email: ' ana@mail.com ',
      username: 'ana',
      password: 'Secreta1',
      confirmPassword: 'Secreta1',
      favouriteGenresIds: ['g1', 'g2'],
      favouriteInstrumentsIds: ['i1'],
    });
  });

  it('muestra una alerta si la petición del código falla', async () => {
    mockRequestCode.mockRejectedValue(new Error('Las contraseñas no coinciden'));
    await render(<RegisterScreen />);
    await fireEvent.press(screen.getByText('Crear cuenta'));
    await waitFor(() =>
      expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Error', message: 'Las contraseñas no coinciden' })
    );
    expect(screen.queryByText(/Verificar/)).toBeNull();
  });

  it('con un 429 vuelve al paso del código avisando de que ya se envió', async () => {
    mockRequestCode.mockRejectedValue({ status: 429, message: 'Too many' });
    await render(<RegisterScreen />);
    await fireEvent.press(screen.getByText('Crear cuenta'));
    await waitFor(() =>
      expect(screen.getByText('Ya te enviamos un código hace poco, revisa tu email')).toBeTruthy()
    );
    expect(mockShowAlert).not.toHaveBeenCalled();
  });

  it('registra con el código y cierra la verificación', async () => {
    mockRequestCode.mockResolvedValue(undefined);
    mockRegister.mockResolvedValue(undefined);
    await render(<RegisterScreen />);
    await fillForm();
    await fireEvent.press(screen.getByText('Crear cuenta'));
    await waitFor(() => screen.getByText('Enviar código'));
    await fireEvent.press(screen.getByText('Enviar código'));
    await waitFor(() =>
      expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Exito', message: 'Creando tu perfil musical...' })
    );
    expect(mockRegister).toHaveBeenCalledWith(expect.objectContaining({ username: 'ana' }), '123456');
    expect(screen.queryByText(/Verificar/)).toBeNull();
  });

  it('muestra el error del código si el registro falla', async () => {
    mockRequestCode.mockResolvedValue(undefined);
    mockRegister.mockRejectedValue(new Error('Código incorrecto'));
    await render(<RegisterScreen />);
    await fireEvent.press(screen.getByText('Crear cuenta'));
    await waitFor(() => screen.getByText('Enviar código'));
    await fireEvent.press(screen.getByText('Enviar código'));
    await waitFor(() => expect(screen.getByText('Código incorrecto')).toBeTruthy());
  });

  it('reenvía el código al email y permite cerrar la verificación', async () => {
    mockRequestCode.mockResolvedValue(undefined);
    await render(<RegisterScreen />);
    await fireEvent.changeText(screen.getByPlaceholderText('Email'), 'ana@mail.com');
    await fireEvent.press(screen.getByText('Crear cuenta'));
    await waitFor(() => screen.getByText('Reenviar código'));
    await fireEvent.press(screen.getByText('Reenviar código'));
    expect(mockResendCode).toHaveBeenCalledWith('ana@mail.com');
    await fireEvent.press(screen.getByText('Cerrar modal'));
    expect(screen.queryByText(/Verificar/)).toBeNull();
  });

  it('alterna la visibilidad de ambas contraseñas', async () => {
    await render(<RegisterScreen />);
    const toggles = screen.getAllByLabelText('Mostrar contraseña');
    expect(toggles).toHaveLength(2);
    await fireEvent.press(toggles[0]);
    expect(screen.getByPlaceholderText('Contraseña').props.secureTextEntry).toBe(false);
    expect(screen.getByPlaceholderText('Repetir contraseña').props.secureTextEntry).toBe(true);
    await fireEvent.press(screen.getByLabelText('Mostrar contraseña'));
    expect(screen.getByPlaceholderText('Repetir contraseña').props.secureTextEntry).toBe(false);
  });

  it('oculta el texto del botón mientras se está cargando', async () => {
    mockIsLoading = true;
    await render(<RegisterScreen />);
    expect(screen.queryByText('Crear cuenta')).toBeNull();
  });
});
