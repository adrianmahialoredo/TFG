import { act, renderHook } from '@testing-library/react-native';
import { useForgotPassword } from '../../hooks/useForgotPassword';
import * as authRepository from '../../repositories/auth.repository';

jest.mock('../../repositories/auth.repository', () => ({
  requestPasswordResetCode: jest.fn(),
  verifyPasswordResetCode: jest.fn(),
  resetPassword: jest.fn(),
}));
const mocked = authRepository as jest.Mocked<typeof authRepository>;

const statusError = (status: number, message: string) => Object.assign(new Error(message), { status });

describe('useForgotPassword', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mocked.requestPasswordResetCode.mockResolvedValue(undefined);
    mocked.verifyPasswordResetCode.mockResolvedValue(undefined);
    mocked.resetPassword.mockResolvedValue(undefined);
  });

  it('empieza en el paso del email, con el email que llegue del login', async () => {
    const { result } = await renderHook(() => useForgotPassword('ana@correo.com'));
    expect(result.current.step).toBe('email');
    expect(result.current.email).toBe('ana@correo.com');
  });

  it('rechaza un email mal escrito sin llamar al backend', async () => {
    const { result } = await renderHook(() => useForgotPassword('no-es-email'));
    await expect(result.current.requestCode()).rejects.toThrow('Email no válido');
    expect(mocked.requestPasswordResetCode).not.toHaveBeenCalled();
  });

  it('pide el código (email sin espacios) y abre el modal', async () => {
    const { result } = await renderHook(() => useForgotPassword('  ana@correo.com '));
    await act(async () => {
      await result.current.requestCode();
    });
    expect(mocked.requestPasswordResetCode).toHaveBeenCalledWith('ana@correo.com');
    expect(result.current.step).toBe('code');
    expect(result.current.codeError).toBeNull();
  });

  it('un 429 abre igualmente el modal con un aviso', async () => {
    mocked.requestPasswordResetCode.mockRejectedValue(statusError(429, 'Espera'));
    const { result } = await renderHook(() => useForgotPassword('ana@correo.com'));
    await act(async () => {
      await result.current.requestCode();
    });
    expect(result.current.step).toBe('code');
    expect(result.current.codeError).toBe('Ya te enviamos un código hace poco, revisa tu email');
  });

  it('otro error al pedir el código se propaga y se queda en el email', async () => {
    mocked.requestPasswordResetCode.mockRejectedValue(statusError(500, 'Fallo'));
    const { result } = await renderHook(() => useForgotPassword('ana@correo.com'));
    await act(async () => {
      await expect(result.current.requestCode()).rejects.toThrow('Fallo');
    });
    expect(result.current.step).toBe('email');
  });

  it('un código correcto lleva al paso de la contraseña', async () => {
    const { result } = await renderHook(() => useForgotPassword('ana@correo.com'));
    await act(async () => {
      await result.current.requestCode();
    });
    await act(async () => {
      await result.current.submitCode('123456');
    });
    expect(mocked.verifyPasswordResetCode).toHaveBeenCalledWith('ana@correo.com', '123456');
    expect(result.current.step).toBe('password');
  });

  it('un código incorrecto se queda en el modal con el error', async () => {
    mocked.verifyPasswordResetCode.mockRejectedValue(statusError(400, 'El código no es correcto'));
    const { result } = await renderHook(() => useForgotPassword('ana@correo.com'));
    await act(async () => {
      await result.current.requestCode();
    });
    await act(async () => {
      await result.current.submitCode('000000');
    });
    expect(result.current.step).toBe('code');
    expect(result.current.codeError).toBe('El código no es correcto');
  });

  it('valida la contraseña nueva antes de enviarla', async () => {
    const { result } = await renderHook(() => useForgotPassword('ana@correo.com'));
    await expect(result.current.resetPassword('Nueva1!x', 'Otra1!xx')).rejects.toThrow('Las contraseñas no coinciden');
    await expect(result.current.resetPassword('corta', 'corta')).rejects.toThrow(/al menos 8 caracteres/);
    await expect(result.current.resetPassword('sinmayus1!', 'sinmayus1!')).rejects.toThrow(/mayúscula/);
    expect(mocked.resetPassword).not.toHaveBeenCalled();
  });

  it('guarda la contraseña con el email y el código comprobado', async () => {
    const { result } = await renderHook(() => useForgotPassword('ana@correo.com'));
    await act(async () => {
      await result.current.requestCode();
    });
    await act(async () => {
      await result.current.submitCode('123456');
    });
    await act(async () => {
      await result.current.resetPassword('Nueva1!x', 'Nueva1!x');
    });
    expect(mocked.resetPassword).toHaveBeenCalledWith('ana@correo.com', '123456', 'Nueva1!x');
  });

  it('si el código caducó al guardar, vuelve al paso del email', async () => {
    mocked.resetPassword.mockRejectedValue(statusError(400, 'El código ha caducado, pide uno nuevo'));
    const { result } = await renderHook(() => useForgotPassword('ana@correo.com'));
    await act(async () => {
      await result.current.requestCode();
    });
    await act(async () => {
      await result.current.submitCode('123456');
    });
    await act(async () => {
      await expect(result.current.resetPassword('Nueva1!x', 'Nueva1!x')).rejects.toThrow('caducado');
    });
    expect(result.current.step).toBe('email');
  });

  it('cancelar el modal vuelve al email sin borrarlo', async () => {
    const { result } = await renderHook(() => useForgotPassword('ana@correo.com'));
    await act(async () => {
      await result.current.requestCode();
    });
    await act(async () => {
      result.current.cancelCode();
    });
    expect(result.current.step).toBe('email');
    expect(result.current.email).toBe('ana@correo.com');
  });

  it('reenviar pide otro código para el mismo email', async () => {
    const { result } = await renderHook(() => useForgotPassword(' ana@correo.com'));
    await result.current.resendCode();
    expect(mocked.requestPasswordResetCode).toHaveBeenCalledWith('ana@correo.com');
  });
});
