import axios from 'axios';
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

  // Con axios (XMLHttpRequest) y no con fetch: el fetch global de Expo no admite la parte
  // `{ uri, name, type }` de React Native en el FormData ("Unsupported FormDataPart implementation").
  // Sin cliente de la app: Cloudinary no debe recibir el token ni la baseURL del backend.
  let data: CloudinaryUploadResponse;
  try {
    const response = await axios.post<CloudinaryUploadResponse>(
      `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`,
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
        // El FormData se envía tal cual (axios no debe intentar serializarlo)
        transformRequest: (body) => body,
      },
    );
    data = response.data;
  } catch (error) {
    const cloudinaryMessage = axios.isAxiosError<CloudinaryUploadResponse>(error)
      ? error.response?.data?.error?.message
      : undefined;
    throw new Error(cloudinaryMessage || 'Error subiendo imagen a Cloudinary.');
  }

  if (!data.secure_url || !data.secure_url.startsWith('https://')) {
    throw new Error('Cloudinary no devolvio una URL HTTPS valida.');
  }

  return data.secure_url;
};

