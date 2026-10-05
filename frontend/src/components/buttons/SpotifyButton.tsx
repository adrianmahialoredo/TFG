import { TouchableOpacity, Text } from "react-native";
import * as WebBrowser from 'expo-web-browser';
import { spotifyLogin } from "../../services";
import { useAppAlert } from "../../context/AlertModalContext";

export const SpotifyButton = () => {
    const { showAlert } = useAppAlert();

    const handleSpotifyLogin = async () => {
        try {
          const { authUrl } = await spotifyLogin();

          if (!authUrl) {
            throw new Error('Spotify auth URL no disponible');
          }

          await WebBrowser.openBrowserAsync(authUrl);
        } catch (error) {
          showAlert({ title: 'Error', message: 'No se pudo iniciar sesion con Spotify.' });
          console.error(error);
        }
    };

    return (
        <TouchableOpacity className="bg-spotify px-7 py-4 rounded-full self-center" onPress={handleSpotifyLogin}>
            <Text className="text-secondary text-lg self-center  font-bold">
                Conectar con Spotify
            </Text>
        </TouchableOpacity>
    );
};