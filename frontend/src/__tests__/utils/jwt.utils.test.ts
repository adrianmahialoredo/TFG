import { isTokenExpired } from '../../utils/jwt.utils';

const base64url = (obj: object) =>
  btoa(JSON.stringify(obj)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');

const makeToken = (payload: object) => `header.${base64url(payload)}.signature`;

describe('isTokenExpired', () => {
  it('devuelve false para un token que caduca en el futuro', () => {
    const exp = Math.floor(Date.now() / 1000) + 3600;
    expect(isTokenExpired(makeToken({ exp }))).toBe(false);
  });

  it('devuelve true para un token ya caducado', () => {
    const exp = Math.floor(Date.now() / 1000) - 10;
    expect(isTokenExpired(makeToken({ exp }))).toBe(true);
  });

  it('aplica un margen de 30 segundos antes de la caducidad', () => {
    const exp = Math.floor(Date.now() / 1000) + 20;
    expect(isTokenExpired(makeToken({ exp }))).toBe(true);
  });

  it('trata como caducado un token sin payload', () => {
    expect(isTokenExpired('solo-una-parte')).toBe(true);
  });

  it('trata como caducado un payload que no es JSON', () => {
    expect(isTokenExpired('a.###.c')).toBe(true);
  });

  it('trata como caducado un payload sin exp numérico', () => {
    expect(isTokenExpired(makeToken({ exp: 'mañana' }))).toBe(true);
    expect(isTokenExpired(makeToken({ sub: 'user' }))).toBe(true);
  });
});
