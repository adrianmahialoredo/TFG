/**
 * Color principal de una imagen, para el degradado de fondo de ListScreen.
 *
 * React Native no puede leer los píxeles de una imagen sin un módulo nativo (que no estaría en
 * Expo Go), así que se le pide a Cloudinary una miniatura de 8x8 en BMP: es un formato sin
 * comprimir que se lee a mano en unas pocas líneas. De sus 64 píxeles se elige el color más
 * representativo entre los que tienen color (ver `pickDominantColor`).
 */

export type Rgb = { r: number; g: number; b: number };

/** Lado de la miniatura que se pide a Cloudinary. */
export const SAMPLE_SIZE = 8;

const CLOUDINARY_UPLOAD = /^(https:\/\/res\.cloudinary\.com\/[^/]+\/image\/upload\/)(.+)$/;

/**
 * URL de la miniatura BMP de una imagen de Cloudinary, o null si la imagen no es de Cloudinary.
 * La transformación va delante de la versión (`v123/...`), que es como las devuelve la subida.
 */
export const toColorSampleUrl = (imageUrl?: string | null): string | null => {
  const match = imageUrl?.match(CLOUDINARY_UPLOAD);
  if (!match) return null;
  return `${match[1]}w_${SAMPLE_SIZE},h_${SAMPLE_SIZE},c_fill,f_bmp/${match[2]}`;
};

/** Desplazamiento y escala de una máscara de bits (formato BI_BITFIELDS). */
const maskReader = (mask: number) => {
  if (mask === 0) return () => 0;
  let shift = 0;
  while (((mask >>> shift) & 1) === 0) shift++;
  const max = mask >>> shift;
  return (pixel: number) => Math.round((((pixel & mask) >>> shift) / max) * 255);
};

/**
 * Píxeles de un BMP de 24 o 32 bits sin comprimir (o con máscaras, como los de Cloudinary).
 * Devuelve [] si el fichero no es un BMP que se sepa leer.
 */
export const parseBmp = (buffer: ArrayBuffer): Rgb[] => {
  const view = new DataView(buffer);
  if (buffer.byteLength < 54 || view.getUint8(0) !== 0x42 || view.getUint8(1) !== 0x4d) return [];

  const dataOffset = view.getUint32(10, true);
  const headerSize = view.getUint32(14, true);
  const width = view.getInt32(18, true);
  const rawHeight = view.getInt32(22, true);
  const height = Math.abs(rawHeight);
  const bitsPerPixel = view.getUint16(28, true);
  const compression = view.getUint32(30, true);

  if (width <= 0 || height === 0 || (bitsPerPixel !== 24 && bitsPerPixel !== 32)) return [];
  // 0 = BI_RGB (sin comprimir), 3 = BI_BITFIELDS (máscaras de color tras la cabecera)
  if (compression !== 0 && compression !== 3) return [];

  let readRgb: (offset: number) => Rgb;
  if (bitsPerPixel === 32 && compression === 3) {
    // Las máscaras van tras la cabecera de 40 bytes (o dentro, en las cabeceras V4/V5)
    const masksOffset = headerSize >= 52 ? 54 : 14 + headerSize;
    const red = maskReader(view.getUint32(masksOffset, true));
    const green = maskReader(view.getUint32(masksOffset + 4, true));
    const blue = maskReader(view.getUint32(masksOffset + 8, true));
    readRgb = (offset) => {
      const pixel = view.getUint32(offset, true);
      return { r: red(pixel), g: green(pixel), b: blue(pixel) };
    };
  } else {
    // Orden BGR(A)
    readRgb = (offset) => ({ r: view.getUint8(offset + 2), g: view.getUint8(offset + 1), b: view.getUint8(offset) });
  }

  const bytesPerPixel = bitsPerPixel / 8;
  const rowSize = Math.ceil((width * bytesPerPixel) / 4) * 4; // cada fila se rellena hasta múltiplo de 4
  if (dataOffset + rowSize * height > buffer.byteLength) return [];

  const pixels: Rgb[] = [];
  for (let row = 0; row < height; row++) {
    for (let col = 0; col < width; col++) {
      pixels.push(readRgb(dataOffset + row * rowSize + col * bytesPerPixel));
    }
  }
  return pixels;
};

type Hsl = { h: number; s: number; l: number };

const rgbToHsl = ({ r, g, b }: Rgb): Hsl => {
  const rn = r / 255, gn = g / 255, bn = b / 255;
  const max = Math.max(rn, gn, bn);
  const min = Math.min(rn, gn, bn);
  const l = (max + min) / 2;
  if (max === min) return { h: 0, s: 0, l };
  const d = max - min;
  const s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
  let h: number;
  if (max === rn) h = (gn - bn) / d + (gn < bn ? 6 : 0);
  else if (max === gn) h = (bn - rn) / d + 2;
  else h = (rn - gn) / d + 4;
  return { h: h * 60, s, l };
};

const hslToRgb = ({ h, s, l }: Hsl): Rgb => {
  const c = (1 - Math.abs(2 * l - 1)) * s;
  const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
  const m = l - c / 2;
  const [r, g, b] =
    h < 60 ? [c, x, 0] : h < 120 ? [x, c, 0] : h < 180 ? [0, c, x] :
    h < 240 ? [0, x, c] : h < 300 ? [x, 0, c] : [c, 0, x];
  return { r: Math.round((r + m) * 255), g: Math.round((g + m) * 255), b: Math.round((b + m) * 255) };
};

/** Tramos del círculo cromático en los que se agrupan los píxeles. */
const HUE_BUCKETS = 12;

/**
 * Color principal de unos píxeles, ajustado para servir de fondo:
 *  - Se agrupan por tono y gana el grupo con más "color" (más píxeles, más saturados y ni casi
 *    negros ni casi blancos). Así una portada con fondo blanco y un dibujo rojo da rojo.
 *  - Si la imagen es prácticamente gris, se usa su gris medio.
 *  - La luminosidad se limita para que el texto blanco se lea encima y el degradado al negro
 *    no salga ni apagado ni chillón.
 * Devuelve null si no hay píxeles.
 */
export const pickDominantColor = (pixels: Rgb[]): Rgb | null => {
  if (pixels.length === 0) return null;

  const buckets = Array.from({ length: HUE_BUCKETS }, () => ({ weight: 0, r: 0, g: 0, b: 0 }));
  for (const pixel of pixels) {
    const { h, s, l } = rgbToHsl(pixel);
    // Peso: saturación, penalizando los extremos de luminosidad (casi negro / casi blanco)
    const weight = s * (1 - Math.abs(2 * l - 1));
    if (weight < 0.08) continue;
    const bucket = buckets[Math.floor(h / (360 / HUE_BUCKETS)) % HUE_BUCKETS];
    bucket.weight += weight;
    bucket.r += pixel.r * weight;
    bucket.g += pixel.g * weight;
    bucket.b += pixel.b * weight;
  }

  const best = buckets.reduce((a, b) => (b.weight > a.weight ? b : a));
  const base: Rgb = best.weight > 0
    ? { r: best.r / best.weight, g: best.g / best.weight, b: best.b / best.weight }
    : {
        r: pixels.reduce((sum, p) => sum + p.r, 0) / pixels.length,
        g: pixels.reduce((sum, p) => sum + p.g, 0) / pixels.length,
        b: pixels.reduce((sum, p) => sum + p.b, 0) / pixels.length,
      };

  const hsl = rgbToHsl(base);
  return hslToRgb({
    h: hsl.h,
    s: best.weight > 0 ? Math.max(hsl.s, 0.45) : hsl.s,
    l: Math.min(Math.max(hsl.l, 0.32), 0.5),
  });
};

/** `#rrggbb` de un color. */
export const rgbToHex = ({ r, g, b }: Rgb): string =>
  `#${[r, g, b].map((v) => Math.round(Math.min(Math.max(v, 0), 255)).toString(16).padStart(2, '0')).join('')}`;

/** `#rrggbb` (o `#rgb`) a RGB; null si no es un color hexadecimal. */
export const hexToRgb = (hex: string): Rgb | null => {
  const match = hex.trim().match(/^#?([0-9a-f]{3}|[0-9a-f]{6})$/i);
  if (!match) return null;
  const full = match[1].length === 3 ? match[1].split('').map((c) => c + c).join('') : match[1];
  return {
    r: parseInt(full.slice(0, 2), 16),
    g: parseInt(full.slice(2, 4), 16),
    b: parseInt(full.slice(4, 6), 16),
  };
};

/** `rgba(...)` de un color `#rrggbb` con la opacidad dada; el color tal cual si no es hexadecimal. */
export const withOpacity = (hex: string, opacity: number): string => {
  const rgb = hexToRgb(hex);
  return rgb ? `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${opacity})` : hex;
};

/** Opacidad del color arriba del todo del degradado de fondo: por debajo de 1 para que no compita con la imagen. */
export const GRADIENT_TOP_OPACITY = 0.85;

/**
 * Color que se ve a una altura `y` del degradado de fondo (`GradientBackground`, sobre negro).
 * Sirve para que el borde de un FadeScrollView que esté encima se funda con ese fondo.
 */
export const gradientColorAt = (color: string, y: number, height: number): string => {
  const rgb = hexToRgb(color);
  if (!rgb) return color;
  const opacity = GRADIENT_TOP_OPACITY * Math.min(Math.max(1 - y / height, 0), 1);
  return rgbToHex({ r: rgb.r * opacity, g: rgb.g * opacity, b: rgb.b * opacity });
};
