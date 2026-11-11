import { AxiosError } from 'axios';
import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import {
  AuthServiceError,
  requestPasswordResetCode,
  resetPassword,
  verifyPasswordResetCode,
} from '../../services/auth.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;

const httpError = (status?: number, data?: unknown) =>
  new AxiosError(
    'http',
    status ? 'ERR_BAD_REQUEST' : 'ERR_NETWORK',
    undefined,
    null,
    status ? ({ status, data, statusText: '', headers: {}, config: {} } as any) : undefined
  );

const catchError = async (p: Promise<unknown>): Promise<AuthServiceError> => {
  try {
    await p;
  } catch (e) {
    return e as AuthServiceError;
  }
  throw new Error('Se esperaba un error');
};

describe('auth.service: recuperación de contraseña', () => {
  beforeEach(() => jest.clearAllMocks());

  it('pide el código con el email', async () => {
    api.post.mockResolvedValue({ data: null } as any);
    await expect(requestPasswordResetCode('a@b.co')).resolves.toBeUndefined();
    expect(api.post).toHaveBeenCalledWith(API_ENDPOINTS.AUTH.PASSWORD_RESET_CODE, { email: 'a@b.co' });
  });

  it('comprueba el código con email y código', async () => {
    api.post.mockResolvedValue({ data: null } as any);
    await verifyPasswordResetCode('a@b.co', '123456');
    expect(api.post).toHaveBeenCalledWith(API_ENDPOINTS.AUTH.PASSWORD_RESET_VERIFY, { email: 'a@b.co', code: '123456' });
  });

  it('envía la contraseña nueva con el código', async () => {
    api.post.mockResolvedValue({ data: null } as any);
    await resetPassword('a@b.co', '123456', 'Nueva1!x');
    expect(api.post).toHaveBeenCalledWith(API_ENDPOINTS.AUTH.PASSWORD_RESET, {
      email: 'a@b.co',
      code: '123456',
      newPassword: 'Nueva1!x',
    });
  });

  it('un 429 pide esperar un minuto', async () => {
    api.post.mockRejectedValue(httpError(429, {}));
    const e = await catchError(requestPasswordResetCode('a@b.co'));
    expect(e.message).toBe('Espera un minuto antes de pedir otro código');
    expect(e.status).toBe(429);
  });

  it('traduce los errores del código', async () => {
    api.post.mockRejectedValue(httpError(400, { message: 'Invalid verification code' }));
    expect((await catchError(verifyPasswordResetCode('a@b.co', '000000'))).message).toBe('El código no es correcto');

    api.post.mockRejectedValue(httpError(400, { message: 'Verification code expired' }));
    expect((await catchError(resetPassword('a@b.co', '1', 'Nueva1!x'))).message).toBe('El código ha caducado, pide uno nuevo');
  });

  it('un 400 en la contraseña nueva explica la política', async () => {
    api.post.mockRejectedValue(httpError(400, { errors: { newPassword: 'weak' } }));
    const e = await catchError(resetPassword('a@b.co', '1', 'debil'));
    expect(e.message).toMatch(/al menos 8 caracteres/);
    expect(e.status).toBe(400);
  });

  it('un 400 desconocido usa un mensaje genérico', async () => {
    api.post.mockRejectedValue(httpError(400, { message: 'Something else' }));
    expect((await catchError(resetPassword('a@b.co', '1', 'Nueva1!x'))).message).toBe('No se pudo cambiar la contraseña');
  });

  it('un error de red se informa como fallo de conexión', async () => {
    api.post.mockRejectedValue(httpError());
    expect((await catchError(requestPasswordResetCode('a@b.co'))).message).toBe(
      'No se pudo conectar con el servidor. Revisa tu red.'
    );
  });

  it('un 500 pide intentarlo más tarde', async () => {
    api.post.mockRejectedValue(httpError(500, {}));
    const e = await catchError(resetPassword('a@b.co', '1', 'Nueva1!x'));
    expect(e.message).toBe('No se pudo cambiar la contraseña. Inténtalo más tarde');
    expect(e.status).toBe(500);
  });
});
