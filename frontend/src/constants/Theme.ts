export const COLORS = {
    primary: "#C1853C", // Gold
    secondary: "#ECECEC", // Light Gray
    songTitle: "#FFFFFF", // White
    artistName: "#A3A3A3", // Light Gray
    background: "#000000", // Black
    text: "#111827", // Dark Gray
    placeholder: "#e7e7e7", // Medium Gray
};
/**
 * Card dorada semitransparente (carruseles vacíos y tarjetas de sugerencia del Home):
 * se ve el fondo a través del dorado, con el borde más intenso.
 */
export const GOLD_CARD_STYLE = {
    backgroundColor: "rgba(193, 133, 60, 0.24)", // primary al 24 %
    borderColor: "rgba(193, 133, 60, 0.8)", // primary al 80 %
    borderWidth: 1,
    borderRadius: 12,
} as const;
