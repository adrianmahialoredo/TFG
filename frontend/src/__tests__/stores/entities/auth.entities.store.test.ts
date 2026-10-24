import AsyncStorage from '@react-native-async-storage/async-storage';
import { useAuthStore } from '../../../stores/entities/auth.entities.store';

const flush = () => new Promise((resolve) => setTimeout(resolve, 0));

const base64url = (obj: object) =>
  btoa(JSON.stringify(obj)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
const tokenExpiringIn = (seconds: number) =>
  `h.${base64url({ exp: Math.floor(Date.now() / 1000) + seconds })}.s`;

const persisted = (state: object) => JSON.stringify({ state, version: 0 });

describe('auth.entities.store', () => {
  beforeEach(async () => {
    useAuthStore.setState({ token: null, currentUserId: null, isAuthenticated: false, isHydrated: false });
    await AsyncStorage.clear();
  });

  it('setAuth guarda token, usuario y marca la sesión como autenticada', () => {
    useAuthStore.getState().setAuth('tok', 'u1');
    expect(useAuthStore.getState()).toMatchObject({ token: 'tok', currentUserId: 'u1', isAuthenticated: true });
  });

  it('clearAuth limpia la sesión', () => {
    useAuthStore.getState().setAuth('tok', 'u1');
    useAuthStore.getState().clearAuth();
    expect(useAuthStore.getState()).toMatchObject({ token: null, currentUserId: null, isAuthenticated: false });
  });

  it('setHydrated cambia el indicador de hidratación', () => {
    useAuthStore.getState().setHydrated(true);
    expect(useAuthStore.getState().isHydrated).toBe(true);
  });

  it('persiste la sesión en AsyncStorage bajo auth-store', async () => {
    useAuthStore.getState().setAuth('tok', 'u1');
    await flush();
    const stored = JSON.parse((await AsyncStorage.getItem('auth-store'))!);
    expect(stored.state).toMatchObject({ token: 'tok', currentUserId: 'u1', isAuthenticated: true });
  });

  describe('rehidratación', () => {
    it('restaura un token vigente y marca la store como hidratada', async () => {
      const token = tokenExpiringIn(3600);
      await AsyncStorage.setItem('auth-store', persisted({ token, currentUserId: 'u1', isAuthenticated: true }));

      await useAuthStore.persist.rehydrate();

      expect(useAuthStore.getState()).toMatchObject({ token, currentUserId: 'u1', isAuthenticated: true, isHydrated: true });
    });

    it('descarta un token caducado antes de marcar la store como hidratada', async () => {
      await AsyncStorage.setItem(
        'auth-store',
        persisted({ token: tokenExpiringIn(-60), currentUserId: 'u1', isAuthenticated: true })
      );

      await useAuthStore.persist.rehydrate();

      expect(useAuthStore.getState()).toMatchObject({
        token: null,
        currentUserId: null,
        isAuthenticated: false,
        isHydrated: true,
      });
    });

    it('sin sesión guardada solo marca la store como hidratada', async () => {
      await useAuthStore.persist.rehydrate();
      expect(useAuthStore.getState()).toMatchObject({ token: null, isAuthenticated: false, isHydrated: true });
    });
  });
});
