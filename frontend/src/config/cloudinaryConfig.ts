export const CLOUDINARY_CONFIG = {
  // Expo solo expone variables EXPO_PUBLIC_* al codigo cliente.
  cloudName: process.env.EXPO_PUBLIC_CLOUDINARY_CLOUD_NAME ?? '',
  uploadPreset: process.env.EXPO_PUBLIC_CLOUDINARY_UPLOAD_PRESET ?? '',
  folder: 'profile-images',
};


