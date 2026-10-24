import { AxiosError, AxiosHeaders, InternalAxiosRequestConfig } from 'axios';
import { apiClient } from '../../services/apiClient';
import { useAuthStore } from '../../stores/entities/auth.entities.store';

/** Adaptador falso: evita la red y deja ver la config final de la petición. */
const okAdapter = () =>
  jest.fn(async (config: InternalAxiosRequestConfig) => ({
    data: { ok: true },
    status: 200,
    statusText: 'OK',
    headers: {},
    config,
  }));

const failingAdapter = (status?: number) =>
  jest.fn(async (config: InternalAxiosRequestConfig) => {
    const response = status ? { data: {}, status, statusText: 'ERR', headers: {}, config } : undefined;
    throw new AxiosError('fallo', status ? 'ERR_BAD_REQUEST' : 'ERR_NETWORK', config, null, response as any);
  });

const authHeader = (adapter: jest.Mock, call = 0) =>
  AxiosHeaders.from(adapter.mock.calls[call][0].headers).get('Authorization');

describe('apiClient', () => {
  beforeEach(() => {
    useAuthStore.setState({ token: null, currentUserId: null, isAuthenticated: false });
  });

  describe('interceptor de petición', () => {
    it('añade la cabecera Authorization cuando hay token', async () => {
      useAuthStore.setState({ token: 'abc', currentUserId: '1', isAuthenticated: true });
      const adapter = okAdapter();
      const res = await apiClient.get('http://x/test', { adapter });
      expect(res.data).toEqual({ ok: true });
      expect(authHeader(adapter)).toBe('Bearer abc');
    });

    it('no añade Authorization si no hay token', async () => {
      const adapter = okAdapter();
      await apiClient.get('http://x/test', { adapter });
      expect(AxiosHeaders.from(adapter.mock.calls[0][0].headers).has('Authorization')).toBe(false);
    });

    it('lee el token en cada petición (no lo cachea)', async () => {
      const adapter = okAdapter();
      useAuthStore.setState({ token: 't1' });
      await apiClient.get('http://x/a', { adapter });
      useAuthStore.setState({ token: 't2' });
      await apiClient.post('http://x/b', {}, { adapter });
      expect(authHeader(adapter, 0)).toBe('Bearer t1');
      expect(authHeader(adapter, 1)).toBe('Bearer t2');
    });
  });

  describe('interceptor de respuesta', () => {
    it('deja pasar las respuestas correctas', async () => {
      const res = await apiClient.get('http://x/ok', { adapter: okAdapter() });
      expect(res.status).toBe(200);
    });

    it('un 401 con sesión abierta cierra la sesión y rechaza', async () => {
      useAuthStore.setState({ token: 'abc', currentUserId: '1', isAuthenticated: true });
      await expect(apiClient.get('http://x/priv', { adapter: failingAdapter(401) })).rejects.toMatchObject({
        response: { status: 401 },
      });
      const state = useAuthStore.getState();
      expect(state.token).toBeNull();
      expect(state.currentUserId).toBeNull();
      expect(state.isAuthenticated).toBe(false);
    });

    it('un 401 sin token no llama a clearAuth', async () => {
      const clearAuth = jest.fn();
      useAuthStore.setState({ clearAuth });
      await expect(apiClient.get('http://x/priv', { adapter: failingAdapter(401) })).rejects.toBeInstanceOf(AxiosError);
      expect(clearAuth).not.toHaveBeenCalled();
    });

    it('otros errores (500) no cierran la sesión', async () => {
      useAuthStore.setState({ token: 'abc', currentUserId: '1', isAuthenticated: true });
      await expect(apiClient.get('http://x/err', { adapter: failingAdapter(500) })).rejects.toMatchObject({
        response: { status: 500 },
      });
      expect(useAuthStore.getState().token).toBe('abc');
    });

    it('un error de red (sin respuesta) se propaga sin cerrar la sesión', async () => {
      useAuthStore.setState({ token: 'abc', currentUserId: '1', isAuthenticated: true });
      await expect(apiClient.get('http://x/err', { adapter: failingAdapter() })).rejects.toMatchObject({
        code: 'ERR_NETWORK',
      });
      expect(useAuthStore.getState().token).toBe('abc');
    });
  });
});
