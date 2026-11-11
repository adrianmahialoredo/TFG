import { AxiosError } from 'axios';
import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import { MIN_FAVOURITE_GENRES, MIN_FAVOURITE_INSTRUMENTS } from '../../constants/Preferences';
import { useAuthStore } from '../../stores/entities/auth.entities.store';
import {
  AuthServiceError,
  login,
  logout,
  register,
  requestVerificationCode,
} from '../../services/auth.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;

/** Error de axios con respuesta HTTP (o sin ella, si `status` no se indica: error de red). */
const httpError = (status?: number, data?: unknown) =>
  new AxiosError(
    'http',
    status ? 'ERR_BAD_REQUEST' : 'ERR_NETWORK',
    undefined,
    null,
    status ? ({ status, data, statusText: '', headers: {}, config: {} } as any) : undefined
  );

const user = { id: 1, email: 'a@b.c', username: 'ana', name: 'Ana', password: 'P4ss!' } as any;

const catchError = async (p: Promise<unknown>): Promise<AuthServiceError> => {
  try {
    await p;
  } catch (e) {
    return e as AuthServiceError;
  }
  throw new Error('Se esperaba un error');
};

describe('auth.service', () => {
  beforeEach(() => jest.clearAllMocks());

  describe('AuthServiceError', () => {
    it('guarda mensaje, status y errores por campo', () => {
      const e = new AuthServiceError('msg', 400, { email: 'mal' });
      expect(e).toBeInstanceOf(Error);
      expect(e.name).toBe('AuthServiceError');
      expect(e.message).toBe('msg');
      expect(e.status).toBe(400);
      expect(e.fieldErrors).toEqual({ email: 'mal' });
    });
  });

  describe('login', () => {
    it('envía el identificador (email o usuario) y la contraseña y devuelve token y usuario', async () => {
      api.post.mockResolvedValue({ data: { token: 't', user, extra: 1 } } as any);
      await expect(login('a@b.c', 'pw')).resolves.toEqual({ token: 't', user });
      expect(api.post).toHaveBeenCalledWith(API_ENDPOINTS.AUTH.LOGIN, { identifier: 'a@b.c', password: 'pw' });
    });

    it('acepta un nombre de usuario y le quita los espacios de los extremos', async () => {
      api.post.mockResolvedValue({ data: { token: 't', user } } as any);
      await login('  ana ', 'pw');
      expect(api.post).toHaveBeenCalledWith(API_ENDPOINTS.AUTH.LOGIN, { identifier: 'ana', password: 'pw' });
    });

    it.each([
      ['sin token', { user }],
      ['sin usuario', { token: 't' }],
      ['sin datos', undefined],
    ])('rechaza una respuesta %s', async (_l, data) => {
      api.post.mockResolvedValue({ data } as any);
      const e = await catchError(login('a', 'b'));
      expect(e).toBeInstanceOf(AuthServiceError);
      expect(e.message).toBe('Respuesta inválida del servidor: falta token o usuario');
      expect(e.status).toBeUndefined();
    });

    it('un 401 sin detalle muestra "Usuario, email o contraseña incorrectos"', async () => {
      api.post.mockRejectedValue(httpError(401, {}));
      const e = await catchError(login('a', 'b'));
      expect(e.message).toBe('Usuario, email o contraseña incorrectos');
      expect(e.status).toBe(401);
    });

    it.each([['Invalid credentials'], ['User not found'], ['Password does not match']])(
      'un 401 con mensaje del backend (%s) muestra el genérico',
      async (message) => {
        api.post.mockRejectedValue(httpError(401, { message }));
        const e = await catchError(login('a', 'b'));
        expect(e.message).toBe('Usuario, email o contraseña incorrectos');
        expect(e.status).toBe(401);
      }
    );

    it('un 401 sin cuerpo usa el mensaje por defecto', async () => {
      api.post.mockRejectedValue(httpError(401, undefined));
      const e = await catchError(login('a', 'b'));
      expect(e.message).toBe('Usuario, email o contraseña incorrectos');
    });

    it('un 400 de validación oculta el detalle', async () => {
      api.post.mockRejectedValue(httpError(400, { message: 'Validation failed', errors: { email: 'formato' } }));
      const e = await catchError(login('a', 'b'));
      expect(e.message).toBe('Usuario, email o contraseña incorrectos');
      expect(e.status).toBe(400);
      expect(e.fieldErrors).toEqual({ email: 'formato' });
    });

    it('un 400 con error en la contraseña oculta el detalle', async () => {
      api.post.mockRejectedValue(httpError(400, { errors: { password: 'débil' } }));
      const e = await catchError(login('a', 'b'));
      expect(e.message).toBe('Usuario, email o contraseña incorrectos');
    });

    it('un 400 con error de otro campo muestra ese error', async () => {
      api.post.mockRejectedValue(httpError(400, { message: 'Bad', errors: { email: 'Email requerido' } }));
      const e = await catchError(login('a', 'b'));
      expect(e.message).toBe('Email requerido');
    });

    it('un 400 con errores vacíos usa el mensaje del backend', async () => {
      api.post.mockRejectedValue(httpError(400, { message: 'Cuenta bloqueada', errors: {} }));
      const e = await catchError(login('a', 'b'));
      expect(e.message).toBe('Cuenta bloqueada');
    });

    it('un error de red indica que no se pudo conectar', async () => {
      api.post.mockRejectedValue(httpError());
      const e = await catchError(login('a', 'b'));
      expect(e.message).toBe('No se pudo conectar con el servidor. Revisa tu red.');
      expect(e.status).toBeUndefined();
    });

    it('un 500 usa el mensaje del backend', async () => {
      api.post.mockRejectedValue(httpError(500, { message: 'Internal' }));
      const e = await catchError(login('a', 'b'));
      expect(e.message).toBe('Internal');
      expect(e.status).toBe(500);
    });

    it('un 500 sin mensaje usa el mensaje por defecto del login', async () => {
      api.post.mockRejectedValue(httpError(500, null));
      const e = await catchError(login('a', 'b'));
      expect(e.message).toBe('No se pudo iniciar sesión');
    });

    it('un Error sin mensaje usa el mensaje por defecto', async () => {
      api.post.mockRejectedValue(new Error(''));
      const e = await catchError(login('a', 'b'));
      expect(e.message).toBe('No se pudo iniciar sesión');
    });

    it('un valor lanzado que no es Error usa el mensaje por defecto', async () => {
      api.post.mockRejectedValue('cadena');
      const e = await catchError(login('a', 'b'));
      expect(e).toBeInstanceOf(AuthServiceError);
      expect(e.message).toBe('No se pudo iniciar sesión');
    });
  });

  describe('requestVerificationCode', () => {
    it('envía el email al endpoint de verificación', async () => {
      api.post.mockResolvedValue({ data: null } as any);
      await expect(requestVerificationCode('a@b.c')).resolves.toBeUndefined();
      expect(api.post).toHaveBeenCalledWith(API_ENDPOINTS.AUTH.VERIFICATION_CODE, { email: 'a@b.c' });
    });

    it('un 429 pide esperar un minuto', async () => {
      api.post.mockRejectedValue(httpError(429, {}));
      const e = await catchError(requestVerificationCode('a'));
      expect(e.message).toBe('Espera un minuto antes de pedir otro código');
      expect(e.status).toBe(429);
    });

    it('un 5xx indica que no se pudo enviar el email', async () => {
      api.post.mockRejectedValue(httpError(503, {}));
      const e = await catchError(requestVerificationCode('a'));
      expect(e.message).toBe('No se pudo enviar el email de verificación. Inténtalo más tarde');
      expect(e.status).toBe(503);
    });

    it('un 400 con error de email muestra el mensaje en español', async () => {
      api.post.mockRejectedValue(httpError(400, { errors: { email: 'must be valid' } }));
      const e = await catchError(requestVerificationCode('a'));
      expect(e.message).toBe('El email no es válido');
      expect(e.status).toBe(400);
    });

    it('un 400 de email ya registrado se traduce', async () => {
      api.post.mockRejectedValue(httpError(400, { message: 'Email already registered' }));
      const e = await catchError(requestVerificationCode('a'));
      expect(e.message).toBe('Ya existe una cuenta con ese email');
    });

    it('un error de red se informa como fallo de conexión', async () => {
      api.post.mockRejectedValue(httpError());
      const e = await catchError(requestVerificationCode('a'));
      expect(e.message).toBe('No se pudo conectar con el servidor. Revisa tu red.');
    });
  });

  describe('register', () => {
    it('envía el formulario con el código y devuelve token y usuario', async () => {
      api.post.mockResolvedValue({ data: { token: 't', user } } as any);
      await expect(register({ ...user, favouriteGenresIds: ['1'], favouriteInstrumentsIds: ['2'] }, '123456')).resolves.toEqual({
        token: 't',
        user,
      });
      expect(api.post).toHaveBeenCalledWith(API_ENDPOINTS.AUTH.REGISTER, {
        email: 'a@b.c',
        username: 'ana',
        name: 'Ana',
        password: 'P4ss!',
        favouriteGenresIds: ['1'],
        favouriteInstrumentsIds: ['2'],
        verificationCode: '123456',
      });
    });

    it('usa listas vacías si no hay favoritos', async () => {
      api.post.mockResolvedValue({ data: { token: 't', user } } as any);
      await register(user, '1');
      expect(api.post.mock.calls[0][1]).toMatchObject({ favouriteGenresIds: [], favouriteInstrumentsIds: [] });
    });

    it('si el backend no devuelve token, devuelve token vacío', async () => {
      api.post.mockResolvedValue({ data: { user } } as any);
      await expect(register(user, '1')).resolves.toEqual({ token: '', user });
    });

    it('una respuesta sin usuario es inválida', async () => {
      api.post.mockResolvedValue({ data: { token: 't' } } as any);
      const e = await catchError(register(user, '1'));
      expect(e).toBeInstanceOf(AuthServiceError);
      expect(e.message).toBe('Respuesta inválida del servidor');
    });

    it.each([
      ['email', `El email no es válido`],
      ['username', 'El usuario debe tener entre 3 y 20 caracteres'],
      ['name', 'El nombre debe tener entre 2 y 100 caracteres'],
      ['password', expect.stringContaining('La contraseña necesita al menos 8 caracteres')],
      ['favouriteGenresIds', `Elige al menos ${MIN_FAVOURITE_GENRES} géneros favoritos`],
      ['favouriteInstrumentsIds', `Elige al menos ${MIN_FAVOURITE_INSTRUMENTS} instrumentos`],
    ])('traduce el error de validación del campo %s', async (field, message) => {
      const errors = { [field]: 'backend msg' };
      api.post.mockRejectedValue(httpError(400, { message: 'Validation failed', errors }));
      const e = await catchError(register(user, '1'));
      expect(e.message).toEqual(message);
      expect(e.status).toBe(400);
      expect(e.fieldErrors).toEqual(errors);
    });

    it('un campo desconocido usa el mensaje del backend para ese campo', async () => {
      api.post.mockRejectedValue(httpError(400, { errors: { birthday: 'Fecha inválida' } }));
      const e = await catchError(register(user, '1'));
      expect(e.message).toBe('Fecha inválida');
    });

    it.each([
      ['Email already registered', 'Ya existe una cuenta con ese email'],
      ['Username already taken', 'Ese nombre de usuario ya está en uso'],
      ['Verification code is required', 'Introduce el código que te hemos enviado'],
      ['Verification code not requested', 'Pide un código de verificación primero'],
      ['Invalid verification code', 'El código no es correcto'],
      ['Verification code expired', 'El código ha caducado, pide uno nuevo'],
      ['Too many wrong verification codes', 'Demasiados intentos fallidos, pide un código nuevo'],
    ])('traduce el error de negocio "%s"', async (backend, message) => {
      api.post.mockRejectedValue(httpError(400, { message: backend }));
      const e = await catchError(register(user, '1'));
      expect(e.message).toBe(message);
    });

    it('un 400 con mensaje desconocido lo muestra tal cual', async () => {
      api.post.mockRejectedValue(httpError(400, { message: 'Otro problema' }));
      const e = await catchError(register(user, '1'));
      expect(e.message).toBe('Otro problema');
    });

    it('un 400 sin cuerpo usa el mensaje genérico del registro', async () => {
      api.post.mockRejectedValue(httpError(400, undefined));
      const e = await catchError(register(user, '1'));
      expect(e.message).toBe('No se pudo completar el registro');
      expect(e.fieldErrors).toBeUndefined();
    });

    it.each([401, 403])('un %i no usa el mensaje de credenciales del login', async (status) => {
      api.post.mockRejectedValue(httpError(status, { message: 'Unauthorized' }));
      const e = await catchError(register(user, '1'));
      expect(e.message).toBe('No se pudo completar el registro');
      expect(e.status).toBe(status);
    });

    it('un 500 sin mensaje usa el mensaje genérico del registro', async () => {
      api.post.mockRejectedValue(httpError(500, {}));
      const e = await catchError(register(user, '1'));
      expect(e.message).toBe('No se pudo completar el registro');
      expect(e.status).toBe(500);
    });
  });

  describe('logout', () => {
    it('limpia la sesión de la store', async () => {
      useAuthStore.setState({ token: 't', currentUserId: '1', isAuthenticated: true });
      await logout();
      expect(useAuthStore.getState()).toMatchObject({ token: null, currentUserId: null, isAuthenticated: false });
    });

    it('propaga el error si no se puede limpiar la sesión', async () => {
      const original = useAuthStore.getState().clearAuth;
      useAuthStore.setState({
        clearAuth: () => {
          throw new Error('boom');
        },
      });
      await expect(logout()).rejects.toThrow('boom');
      useAuthStore.setState({ clearAuth: original });
    });
  });
});
