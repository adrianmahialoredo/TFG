import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import ForgotPasswordScreen from '../../screens/ForgotPasswordScreen';

jest.setTimeout(30000);

const mockNavigation = { navigate: jest.fn(), goBack: jest.fn() };
let mockRouteParams: any = undefined;
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => mockNavigation,
  useRoute: () => ({ params: mockRouteParams }),
}));

const mockShowAlert = jest.fn();
jest.mock('../../context/AlertModalContext', () => ({
  useAppAlert: () => ({ showAlert: mockShowAlert, hideAlert: jest.fn() }),
}));

let mockState: any;
jest.mock('../../hooks/useForgotPassword', () => ({
  useForgotPassword: (initialEmail: string) => {
    mockState.initialEmail = initialEmail;
    return mockState;
  },
}));

jest.mock('../../components/EmailVerificationModal', () => {
  const { Text } = require('react-native');
  return ({ title, email, error, onSubmit }: any) => (
    <Text onPress={() => onSubmit('123456')}>{`Modal ${title} ${email} ${error ?? ''}`}</Text>
  );
});

const baseState = () => ({
  step: 'email',
  email: 'ana@correo.com',
  setEmail: jest.fn(),
  codeError: null,
  isLoading: false,
  requestCode: jest.fn().mockResolvedValue(undefined),
  resendCode: jest.fn(),
  submitCode: jest.fn(),
  resetPassword: jest.fn().mockResolvedValue(undefined),
  cancelCode: jest.fn(),
});

describe('ForgotPasswordScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockRouteParams = undefined;
    mockState = baseState();
  });

  it('empieza pidiendo el email, con el que llegue del login', async () => {
    mockRouteParams = { email: 'ana@correo.com' };
    await render(<ForgotPasswordScreen />);
    expect(screen.getByText('Recuperar contraseña')).toBeTruthy();
    expect(screen.getByPlaceholderText('Email')).toBeTruthy();
    expect(screen.getByText('Enviar código')).toBeTruthy();
    expect(mockState.initialEmail).toBe('ana@correo.com');
  });

  it('envía el código al pulsar el botón', async () => {
    await render(<ForgotPasswordScreen />);
    await fireEvent.press(screen.getByText('Enviar código'));
    expect(mockState.requestCode).toHaveBeenCalled();
    expect(mockShowAlert).not.toHaveBeenCalled();
  });

  it('muestra el error si no se puede pedir el código', async () => {
    mockState.requestCode.mockRejectedValue(new Error('Email no válido'));
    await render(<ForgotPasswordScreen />);
    await fireEvent.press(screen.getByText('Enviar código'));
    await waitFor(() => expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Error', message: 'Email no válido' }));
  });

  it('en el paso del código muestra el modal con su título', async () => {
    mockState.step = 'code';
    await render(<ForgotPasswordScreen />);
    await fireEvent.press(screen.getByText(/Modal Recupera tu contraseña ana@correo.com/));
    expect(mockState.submitCode).toHaveBeenCalledWith('123456');
  });

  it('con el código bueno pide la contraseña nueva y vuelve al login al guardarla', async () => {
    mockState.step = 'password';
    await render(<ForgotPasswordScreen />);
    await fireEvent.changeText(screen.getByPlaceholderText('Nueva contraseña'), 'Nueva1!x');
    await fireEvent.changeText(screen.getByPlaceholderText('Repetir contraseña'), 'Nueva1!x');
    await fireEvent.press(screen.getByText('Cambiar contraseña'));
    await waitFor(() => expect(mockNavigation.goBack).toHaveBeenCalled());
    expect(mockState.resetPassword).toHaveBeenCalledWith('Nueva1!x', 'Nueva1!x');
    expect(mockShowAlert).toHaveBeenCalledWith({
      title: 'Contraseña cambiada',
      message: 'Ya puedes iniciar sesión con tu nueva contraseña',
    });
  });

  it('si la contraseña no es válida avisa y no vuelve al login', async () => {
    mockState.step = 'password';
    mockState.resetPassword.mockRejectedValue(new Error('Las contraseñas no coinciden'));
    await render(<ForgotPasswordScreen />);
    await fireEvent.press(screen.getByText('Cambiar contraseña'));
    await waitFor(() =>
      expect(mockShowAlert).toHaveBeenCalledWith({ title: 'Error', message: 'Las contraseñas no coinciden' })
    );
    expect(mockNavigation.goBack).not.toHaveBeenCalled();
  });

  it('"Volver a iniciar sesión" vuelve atrás', async () => {
    await render(<ForgotPasswordScreen />);
    await fireEvent.press(screen.getByText('Volver a iniciar sesión'));
    expect(mockNavigation.goBack).toHaveBeenCalled();
  });
});
