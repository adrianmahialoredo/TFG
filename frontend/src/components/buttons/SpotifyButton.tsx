import { TouchableOpacity, Text, Alert } from "react-native";
import * as WebBrowser from 'expo-web-browser';
import { spotifyLogin } from "../../services";

export const SpotifyButton = () => {

    const handleSpotifyLogin = async () => {
        try {
          const { authUrl } = await spotifyLogin();
          await WebBrowser.openBrowserAsync(authUrl);
        } catch (error) {
          Alert.alert("Error", "No se pudo iniciar sesión con Spotify.");
          console.error(error);
        }
    };

    return (
        <TouchableOpacity className="bg-spotify px-4 py-3 rounded-full self-center" onPress={handleSpotifyLogin}>
        <Text className="text-secondary text-lg self-center">
            Conectar con Spotify
        </Text>
      </TouchableOpacity>
    );
};