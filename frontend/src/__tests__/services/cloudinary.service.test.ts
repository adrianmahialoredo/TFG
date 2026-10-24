import axios, { AxiosError } from 'axios';
import { uploadProfileImageToCloudinary } from '../../services/cloudinary.service';
import { CLOUDINARY_CONFIG } from '../../config/cloudinaryConfig';

jest.mock('../../config/cloudinaryConfig', () => ({
  CLOUDINARY_CONFIG: { cloudName: 'demo', uploadPreset: 'preset', folder: 'profile-images' },
}));

const config = CLOUDINARY_CONFIG as { cloudName: string; uploadPreset: string; folder: string };

describe('uploadProfileImageToCloudinary', () => {
  let appendSpy: jest.SpyInstance;
  let postSpy: jest.SpyInstance;

  beforeEach(() => {
    config.cloudName = 'demo';
    config.uploadPreset = 'preset';
    config.folder = 'profile-images';
    appendSpy = jest.spyOn(FormData.prototype, 'append');
    postSpy = jest.spyOn(axios, 'post');
  });

  const appended = (): Record<string, any> =>
    Object.fromEntries(appendSpy.mock.calls.map(([k, v]) => [k, v]));

  it('sube la imagen y devuelve la URL HTTPS', async () => {
    postSpy.mockResolvedValue({ data: { secure_url: 'https://res.cloudinary.com/x.jpg' } });
    jest.spyOn(Date, 'now').mockReturnValue(1234);

    await expect(uploadProfileImageToCloudinary('file:///foto.JPG')).resolves.toBe('https://res.cloudinary.com/x.jpg');

    const [url, body, options] = postSpy.mock.calls[0];
    expect(url).toBe('https://api.cloudinary.com/v1_1/demo/image/upload');
    expect(body).toBeInstanceOf(FormData);
    expect(options.headers).toEqual({ 'Content-Type': 'multipart/form-data' });
    // transformRequest no debe tocar el FormData
    expect(options.transformRequest(body)).toBe(body);

    expect(appended()).toEqual({
      file: { uri: 'file:///foto.JPG', name: 'profile_1234.jpg', type: 'image/jpeg' },
      upload_preset: 'preset',
      folder: 'profile-images',
    });
  });

  it.each([
    ['a.png', 'image/png', 'png'],
    ['a.WEBP', 'image/webp', 'webp'],
    ['a.gif', 'image/gif', 'gif'],
    ['a.jpeg', 'image/jpeg', 'jpg'],
    ['sin-extension', 'image/jpeg', 'jpg'],
  ])('infiere el tipo MIME de %s', async (uri, mime, ext) => {
    postSpy.mockResolvedValue({ data: { secure_url: 'https://ok' } });
    await uploadProfileImageToCloudinary(uri);
    const file = appended().file;
    expect(file.type).toBe(mime);
    expect(file.name).toMatch(new RegExp(`^profile_\\d+\\.${ext}$`));
  });

  it('no añade la carpeta si no está configurada', async () => {
    config.folder = '';
    postSpy.mockResolvedValue({ data: { secure_url: 'https://ok' } });
    await uploadProfileImageToCloudinary('a.png');
    expect(appended()).not.toHaveProperty('folder');
  });

  it.each([
    ['cloudName', { cloudName: '' }],
    ['uploadPreset', { uploadPreset: '' }],
  ])('lanza error si falta %s', async (_label, override) => {
    Object.assign(config, override);
    await expect(uploadProfileImageToCloudinary('a.png')).rejects.toThrow('Cloudinary no esta configurado');
    expect(postSpy).not.toHaveBeenCalled();
  });

  it('propaga el mensaje de error de Cloudinary', async () => {
    const error = new AxiosError('400', 'ERR_BAD_REQUEST', undefined, null, {
      data: { error: { message: 'Invalid upload preset' } },
      status: 400,
    } as any);
    postSpy.mockRejectedValue(error);
    await expect(uploadProfileImageToCloudinary('a.png')).rejects.toThrow('Invalid upload preset');
  });

  it('usa un mensaje genérico si el error de axios no trae mensaje de Cloudinary', async () => {
    postSpy.mockRejectedValue(new AxiosError('net', 'ERR_NETWORK'));
    await expect(uploadProfileImageToCloudinary('a.png')).rejects.toThrow('Error subiendo imagen a Cloudinary.');
  });

  it('usa un mensaje genérico ante errores que no son de axios', async () => {
    postSpy.mockRejectedValue(new Error('boom'));
    await expect(uploadProfileImageToCloudinary('a.png')).rejects.toThrow('Error subiendo imagen a Cloudinary.');
  });

  it.each([
    ['sin secure_url', {}],
    ['con URL http', { secure_url: 'http://inseguro' }],
  ])('rechaza una respuesta %s', async (_label, data) => {
    postSpy.mockResolvedValue({ data });
    await expect(uploadProfileImageToCloudinary('a.png')).rejects.toThrow('Cloudinary no devolvio una URL HTTPS valida.');
  });
});
