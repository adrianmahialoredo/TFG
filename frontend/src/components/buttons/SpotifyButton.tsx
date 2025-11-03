import { TouchableOpacity, Text, Alert } from "react-native";
import * as WebBrowser from 'expo-web-browser';

const API_URL = "http://127.0.0.1:8080"; 

export const SpotifyButton = () => {

    const handleSpotifyLogin = async () => {
    try {
      // Abre el endpoint del backend que inicia el flujo OAuth
      await WebBrowser.openBrowserAsync(`${API_URL}/api/spotify/login`);
    } catch (error) {
      Alert.alert("Error", "No se pudo iniciar sesión con Spotify.");
      console.error(error);
    }
  };

    return (
      <TouchableOpacity className="bg-spotify px-4 py-3 rounded-full self-center" onPress={handleSpotifyLogin}>
      <Text className="text-secondary text-lg self-center">Conectar con Spotify</Text>
      </TouchableOpacity>
    );
};