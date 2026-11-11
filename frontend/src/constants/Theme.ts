export const COLORS = {
    primary: "#C1853C", // Gold
    secondary: "#ECECEC", // Light Gray
    songTitle: "#FFFFFF", // White
    artistName: "#A3A3A3", // Light Gray
    background: "#000000", // Black
    text: "#111827", // Dark Gray
    placeholder: "#e7e7e7", // Medium Gray
};

/** Opacidad del fondo de las cards de color (la de GOLD_CARD_STYLE). */
export const CARD_FILL_OPACITY = 0.4;

/**
 * Card dorada semitransparente (carruseles vacíos y tarjetas de sugerencia del Home):
 * se ve el fondo a través del dorado, con el borde más intenso.
 */
export const GOLD_CARD_STYLE = {
    backgroundColor: "rgba(193, 133, 60, 0.4)", // primary al 40 % (CARD_FILL_OPACITY)
    borderColor: "rgba(193, 133, 60, 0.8)", // primary al 80 %
    borderWidth: 1,
    borderRadius: 24,
} as const;

/** URL del icono de la app, para poner en el Login */
export const ICON_URL = 'https://res.cloudinary.com/dvm8qkmuo/image/upload/v1793908797/image_17_hdf0o6.png';