
/** @type {import('tailwindcss').Config} */
module.exports = {
  presets: [require("nativewind/preset")], // 👉 Añade esta línea
  content: [
    "./App.{js,jsx,ts,tsx}",
    "./src/**/*.{js,jsx,ts,tsx}",
    "./components/**/*.{js,jsx,ts,tsx}",
    "./screens/**/*.{js,jsx,ts,tsx}"
  ],
  theme: {
    extend: {
      colors: {
        primary: "#C1853C", // Gold
        secondary: "#ECECEC", // Light Gray
        songTitle: "#FFFFFF", // White
        artistName: "#A3A3A3", // Light Gray
        background: "#000000", // Black
        text: "#111827", // Dark Gray
      },
      fontFamily: {
        sans: ["Inter", "sans-serif"],
        serif: ["Merriweather", "serif"],
        roboto: ["Roboto", "sans-serif"],
      },
    },
  },
  plugins: [],
}