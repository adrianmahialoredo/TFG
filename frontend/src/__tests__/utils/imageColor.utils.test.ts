import {
  hexToRgb,
  parseBmp,
  pickDominantColor,
  rgbToHex,
  Rgb,
  toColorSampleUrl,
  withOpacity,
} from '@/utils/imageColor.utils';

/** BMP de 24 bits (BI_RGB), filas de abajo arriba y rellenas a múltiplo de 4. */
const makeBmp24 = (width: number, height: number, pixels: Rgb[]): ArrayBuffer => {
  const rowSize = Math.ceil((width * 3) / 4) * 4;
  const buffer = new ArrayBuffer(54 + rowSize * height);
  const view = new DataView(buffer);
  view.setUint8(0, 0x42);
  view.setUint8(1, 0x4d);
  view.setUint32(10, 54, true);
  view.setUint32(14, 40, true);
  view.setInt32(18, width, true);
  view.setInt32(22, height, true);
  view.setUint16(28, 24, true);
  view.setUint32(30, 0, true);
  pixels.forEach((p, i) => {
    const offset = 54 + Math.floor(i / width) * rowSize + (i % width) * 3;
    view.setUint8(offset, p.b);
    view.setUint8(offset + 1, p.g);
    view.setUint8(offset + 2, p.r);
  });
  return buffer;
};

/** BMP de 32 bits con máscaras (BI_BITFIELDS) y cabecera V5, como los que devuelve Cloudinary. */
const makeBmp32Bitfields = (pixels: Rgb[]): ArrayBuffer => {
  const headerSize = 124;
  const dataOffset = 14 + headerSize;
  const buffer = new ArrayBuffer(dataOffset + pixels.length * 4);
  const view = new DataView(buffer);
  view.setUint8(0, 0x42);
  view.setUint8(1, 0x4d);
  view.setUint32(10, dataOffset, true);
  view.setUint32(14, headerSize, true);
  view.setInt32(18, pixels.length, true);
  view.setInt32(22, 1, true);
  view.setUint16(28, 32, true);
  view.setUint32(30, 3, true);
  view.setUint32(54, 0x00ff0000, true);
  view.setUint32(58, 0x0000ff00, true);
  view.setUint32(62, 0x000000ff, true);
  pixels.forEach((p, i) => view.setUint32(dataOffset + i * 4, (0xff << 24) | (p.r << 16) | (p.g << 8) | p.b, true));
  return buffer;
};

describe('toColorSampleUrl', () => {
  it('pide a Cloudinary una miniatura BMP delante de la versión', () => {
    expect(toColorSampleUrl('https://res.cloudinary.com/demo/image/upload/v1750532214/C_vkidop.png'))
      .toBe('https://res.cloudinary.com/demo/image/upload/w_8,h_8,c_fill,f_bmp/v1750532214/C_vkidop.png');
  });

  it('devuelve null si la imagen no es de Cloudinary o no hay imagen', () => {
    expect(toColorSampleUrl('https://example.com/a.png')).toBeNull();
    expect(toColorSampleUrl(undefined)).toBeNull();
    expect(toColorSampleUrl(null)).toBeNull();
  });
});

describe('parseBmp', () => {
  it('lee un BMP de 24 bits con relleno de filas', () => {
    const pixels = [
      { r: 255, g: 0, b: 0 }, { r: 0, g: 255, b: 0 }, { r: 0, g: 0, b: 255 },
      { r: 10, g: 20, b: 30 }, { r: 40, g: 50, b: 60 }, { r: 70, g: 80, b: 90 },
    ];
    expect(parseBmp(makeBmp24(3, 2, pixels))).toEqual(pixels);
  });

  it('lee un BMP de 32 bits con máscaras de color', () => {
    const pixels = [{ r: 200, g: 100, b: 50 }, { r: 1, g: 2, b: 3 }];
    expect(parseBmp(makeBmp32Bitfields(pixels))).toEqual(pixels);
  });

  it('devuelve [] si no es un BMP o está cortado', () => {
    expect(parseBmp(new ArrayBuffer(10))).toEqual([]);
    expect(parseBmp(new TextEncoder().encode('no es un bmp, pero es bastante largo para la cabecera......').buffer as ArrayBuffer)).toEqual([]);
    const cut = makeBmp24(4, 4, []).slice(0, 60);
    expect(parseBmp(cut)).toEqual([]);
  });
});

describe('pickDominantColor', () => {
  it('devuelve null sin píxeles', () => {
    expect(pickDominantColor([])).toBeNull();
  });

  it('prefiere el color frente a un fondo blanco o negro', () => {
    const pixels: Rgb[] = [
      ...Array(40).fill({ r: 255, g: 255, b: 255 }),
      ...Array(14).fill({ r: 0, g: 0, b: 0 }),
      ...Array(10).fill({ r: 200, g: 30, b: 30 }),
    ];
    const { r, g, b } = pickDominantColor(pixels)!;
    expect(r).toBeGreaterThan(g * 2);
    expect(r).toBeGreaterThan(b * 2);
  });

  it('gana el tono con más presencia', () => {
    const pixels: Rgb[] = [
      ...Array(40).fill({ r: 30, g: 60, b: 200 }),
      ...Array(24).fill({ r: 200, g: 30, b: 30 }),
    ];
    const { r, b } = pickDominantColor(pixels)!;
    expect(b).toBeGreaterThan(r);
  });

  it('en una imagen gris devuelve un gris', () => {
    const { r, g, b } = pickDominantColor(Array(64).fill({ r: 128, g: 128, b: 128 }))!;
    expect(r).toBe(g);
    expect(g).toBe(b);
  });

  it('limita la luminosidad para que se lea el texto blanco encima', () => {
    const veryLight = pickDominantColor(Array(64).fill({ r: 255, g: 200, b: 200 }))!;
    const veryDark = pickDominantColor(Array(64).fill({ r: 20, g: 0, b: 0 }))!;
    const lightness = ({ r, g, b }: Rgb) => (Math.max(r, g, b) + Math.min(r, g, b)) / 2 / 255;
    expect(lightness(veryLight)).toBeLessThanOrEqual(0.51);
    expect(lightness(veryDark)).toBeGreaterThanOrEqual(0.31);
  });
});

describe('rgbToHex / hexToRgb', () => {
  it('convierte en los dos sentidos', () => {
    expect(rgbToHex({ r: 193, g: 133, b: 60 })).toBe('#c1853c');
    expect(hexToRgb('#C1853C')).toEqual({ r: 193, g: 133, b: 60 });
    expect(hexToRgb('#fff')).toEqual({ r: 255, g: 255, b: 255 });
  });

  it('redondea y limita los valores, y rechaza lo que no es un color', () => {
    expect(rgbToHex({ r: 300, g: -5, b: 10.6 })).toBe('#ff000b');
    expect(hexToRgb('dorado')).toBeNull();
  });
});

describe('withOpacity', () => {
  it('convierte un color hexadecimal a rgba con la opacidad', () => {
    expect(withOpacity('#336699', 0.4)).toBe('rgba(51, 102, 153, 0.4)');
    expect(withOpacity('#fff', 1)).toBe('rgba(255, 255, 255, 1)');
  });

  it('si no es hexadecimal devuelve el color tal cual', () => {
    expect(withOpacity('red', 0.5)).toBe('red');
  });
});
