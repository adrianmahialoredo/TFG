describe('CLOUDINARY_CONFIG', () => {
  const ORIGINAL_ENV = process.env;

  afterEach(() => {
    process.env = ORIGINAL_ENV;
  });

  const load = () => {
    let config: typeof import('../../config/cloudinaryConfig').CLOUDINARY_CONFIG;
    jest.isolateModules(() => {
      config = require('../../config/cloudinaryConfig').CLOUDINARY_CONFIG;
    });
    return config!;
  };

  it('lee cloudName y uploadPreset de las variables EXPO_PUBLIC_*', () => {
    process.env = {
      ...ORIGINAL_ENV,
      EXPO_PUBLIC_CLOUDINARY_CLOUD_NAME: 'mi-nube',
      EXPO_PUBLIC_CLOUDINARY_UPLOAD_PRESET: 'mi-preset',
    };
    expect(load()).toEqual({ cloudName: 'mi-nube', uploadPreset: 'mi-preset', folder: 'profile-images' });
  });

  it('usa cadenas vacías si las variables no están definidas', () => {
    const env = { ...ORIGINAL_ENV };
    delete env.EXPO_PUBLIC_CLOUDINARY_CLOUD_NAME;
    delete env.EXPO_PUBLIC_CLOUDINARY_UPLOAD_PRESET;
    process.env = env;
    expect(load()).toEqual({ cloudName: '', uploadPreset: '', folder: 'profile-images' });
  });
});
