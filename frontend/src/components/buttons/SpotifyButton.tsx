import { Text, Image, ActivityIndicator } from "react-native";
import Touchable from "../Touchable";
import { useSpotifyConnect } from "../../hooks/useSpotifyConnect";
import { useAppAlert } from "../../context/AlertModalContext";

const SPOTIFY_WHITE_LOGO_URL = 'https://storage.googleapis.com/pr-newsroom-wp/1/2023/05/Spotify_Primary_Logo_RGB_White.png';

export const SpotifyButton = () => {
    const { showAlert } = useAppAlert();
    const { connect, connecting } = useSpotifyConnect();

    const handleSpotifyLogin = async () => {
        try {
          await connect();
        } catch (error) {
          showAlert({ title: 'Error', message: 'No se pudo iniciar sesion con Spotify.' });
          console.error(error);
        }
    };

    return (
        <Touchable
            className="bg-spotify px-7 py-4 rounded-full self-center flex-row items-center"
            onPress={handleSpotifyLogin}
            disabled={connecting}
            style={connecting ? { opacity: 0.7 } : undefined}
        >
            {connecting ? (
                <ActivityIndicator color="#ffffff" style={{ marginRight: 12 }} />
            ) : (
                <Image source={{ uri: SPOTIFY_WHITE_LOGO_URL }} className="h-6 w-6 mr-3" />
            )}
            <Text className="text-secondary text-lg font-bold">
                {connecting ? 'Conectando…' : 'Conectar con Spotify'}
            </Text>
        </Touchable>
    );
};
