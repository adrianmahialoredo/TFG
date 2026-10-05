import { CLOUDINARY_CONFIG } from '../config/cloudinaryConfig';

type CloudinaryUploadResponse = {
  secure_url?: string;
  error?: { message?: string };
};

const inferMimeType = (uri: string): string => {
  const normalized = uri.toLowerCase();
  if (normalized.endsWith('.png')) return 'image/png';
  if (normalized.endsWith('.webp')) return 'image/webp';
  if (normalized.endsWith('.gif')) return 'image/gif';
  return 'image/jpeg';
};

const inferExtension = (mimeType: string): string => {
  if (mimeType === 'image/png') return 'png';
  if (mimeType === 'image/webp') return 'webp';
  if (mimeType === 'image/gif') return 'gif';
  return 'jpg';
};

export const uploadProfileImageToCloudinary = async (imageUri: string): Promise<string> => {
  const { cloudName, uploadPreset, folder } = CLOUDINARY_CONFIG;

  if (!cloudName || !uploadPreset) {
    throw new Error('Cloudinary no esta configurado: define cloudName y uploadPreset.');
  }

  const mimeType = inferMimeType(imageUri);
  const extension = inferExtension(mimeType);
  const fileName = `profile_${Date.now()}.${extension}`;

  const formData = new FormData();
  formData.append('file', {
    uri: imageUri,
    name: fileName,
    type: mimeType,
  } as any);
  formData.append('upload_preset', uploadPreset);
  if (folder) {
    formData.append('folder', folder);
  }

  const response = await fetch(`https://api.cloudinary.com/v1_1/${cloudName}/image/upload`, {
    method: 'POST',
    body: formData,
  });

  const data = (await response.json()) as CloudinaryUploadResponse;

  if (!response.ok) {
    const errorMessage = data.error?.message || 'Error subiendo imagen a Cloudinary.';
    throw new Error(errorMessage);
  }

  if (!data.secure_url || !data.secure_url.startsWith('https://')) {
    throw new Error('Cloudinary no devolvio una URL HTTPS valida.');
  }

  return data.secure_url;
};

