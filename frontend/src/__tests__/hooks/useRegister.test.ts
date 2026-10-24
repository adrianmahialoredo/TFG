import { act, renderHook } from '@testing-library/react-native';
import { useRegister } from '../../hooks/useRegister';
import * as authRepository from '../../repositories/auth.repository';

jest.mock('../../repositories/auth.repository', () => ({
  requestVerificationCode: jest.fn(),
  register: jest.fn(),
}));
const mocked = authRepository as jest.Mocked<typeof authRepository>;

const validForm = {
  name: 'Ana López',
  email: '  ana@correo.com ',
  username: 'analopez',
  password: 'Secreta1!',
  confirmPassword: 'Secreta1!',
  favouriteGenresIds: ['1', '2'],
  favouriteInstrumentsIds: ['1', '2'],
};

describe('useRegister', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mocked.requestVerificationCode.mockResolvedValue(undefined as any);
    mocked.register.mockResolvedValue({ id: 'u1' } as any);
  });

  it('requestCode valida y pide el código con el email sin espacios', async () => {
    const { result } = await renderHook(() => useRegister());
    await act(async () => {
      await result.current.requestCode(validForm);
    });
    expect(mocked.requestVerificationCode).toHaveBeenCalledWith('ana@correo.com');
    expect(result.current.isLoading).toBe(false);
  });

  it('isLoading está activo mientras se pide el código', async () => {
    let resolve: (value?: unknown) => void = () => {};
    mocked.requestVerificationCode.mockReturnValue(new Promise<any>((r) => { resolve = r; }));
    const { result } = await renderHook(() => useRegister());

    let promise: Promise<void>;
    await act(async () => {
      promise = result.current.requestCode(validForm);
    });
    expect(result.current.isLoading).toBe(true);
    await act(async () => {
      resolve();
      await promise;
    });
    expect(result.current.isLoading).toBe(false);
  });

  it.each([
    [{ name: 'A' }, 'El nombre debe tener entre 2 y 100 caracteres'],
    [{ name: 'x'.repeat(101) }, 'El nombre debe tener entre 2 y 100 caracteres'],
    [{ name: undefined }, 'El nombre debe tener entre 2 y 100 caracteres'],
    [{ email: 'no-es-email' }, 'Email no válido'],
    [{ email: undefined }, 'Email no válido'],
    [{ username: 'ab' }, 'El usuario debe tener entre 3 y 20 caracteres'],
    [{ username: undefined }, 'El usuario debe tener entre 3 y 20 caracteres'],
    [{ username: 'a'.repeat(21) }, 'El usuario debe tener entre 3 y 20 caracteres'],
    [{ confirmPassword: 'Otra1!' }, 'Las contraseñas no coinciden'],
    [{ password: 'Ab1!', confirmPassword: 'Ab1!' }, 'La contraseña debe tener al menos 8 caracteres'],
    [{ password: undefined, confirmPassword: undefined }, 'La contraseña debe tener al menos 8 caracteres'],
    [{ password: 'Secre ta1!', confirmPassword: 'Secre ta1!' }, 'La contraseña no puede contener espacios'],
    [{ password: 'secreta1!', confirmPassword: 'secreta1!' }, 'La contraseña necesita al menos una mayúscula'],
    [{ password: 'Secretaa!', confirmPassword: 'Secretaa!' }, 'La contraseña necesita al menos un número'],
    [{ password: 'Secreta11', confirmPassword: 'Secreta11' }, 'La contraseña necesita al menos un símbolo'],
    [{ favouriteGenresIds: ['1'] }, 'Elige al menos 2 géneros favoritos'],
    [{ favouriteGenresIds: undefined }, 'Elige al menos 2 géneros favoritos'],
    [{ favouriteInstrumentsIds: [] }, 'Elige al menos 2 instrumentos'],
    [{ favouriteInstrumentsIds: undefined }, 'Elige al menos 2 instrumentos'],
  ])('rechaza el formulario inválido %o', async (override, message) => {
    const { result } = await renderHook(() => useRegister());
    await act(async () => {
      await expect(result.current.requestCode({ ...validForm, ...override })).rejects.toThrow(message);
    });
    expect(mocked.requestVerificationCode).not.toHaveBeenCalled();
    expect(result.current.isLoading).toBe(false);
  });

  it('resendCode reenvía el código con el email recortado', async () => {
    const { result } = await renderHook(() => useRegister());
    await result.current.resendCode('  ana@correo.com ');
    expect(mocked.requestVerificationCode).toHaveBeenCalledWith('ana@correo.com');
  });

  it('register valida y registra con el código', async () => {
    const { result } = await renderHook(() => useRegister());
    let response: any;
    await act(async () => {
      response = await result.current.register(validForm, '123456');
    });
    expect(response).toEqual({ id: 'u1' });
    expect(mocked.register).toHaveBeenCalledWith({ ...validForm, email: 'ana@correo.com' }, '123456');
    expect(result.current.isLoading).toBe(false);
  });

  it('register no llama al backend si el formulario es inválido', async () => {
    const { result } = await renderHook(() => useRegister());
    await act(async () => {
      await expect(result.current.register({ ...validForm, name: '' }, '1')).rejects.toThrow();
    });
    expect(mocked.register).not.toHaveBeenCalled();
  });

  it('register propaga el error del backend', async () => {
    mocked.register.mockRejectedValue(new Error('código incorrecto'));
    const { result } = await renderHook(() => useRegister());
    await act(async () => {
      await expect(result.current.register(validForm, '000000')).rejects.toThrow('código incorrecto');
    });
    expect(result.current.isLoading).toBe(false);
  });
});
