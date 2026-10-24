import React from 'react';
import { Image, View, Text } from 'react-native';
import FadeScrollView from '../components/FadeScrollView';
import YoutubePlayer from 'react-native-youtube-iframe';
import { useGenreDescription } from '../hooks/useGenreDescription';

interface GenreDescriptionProps {
    genreId: string;
}

export default function GenreDescriptionScreen({ genreId }: GenreDescriptionProps) {
    // NIVEL 6: Obtenemos los datos limpios
    const {
        description,
        bannerUrl,
        welcomeVideoId,
        welcomeSongId,
        isValid
    } = useGenreDescription(genreId);

    if (!isValid) {
        return (
            <View className="flex-1 justify-center items-center p-10">
                <Text className="text-secondary text-center font-roboto">
                    La historia de este género se está escribiendo...
                </Text>
            </View>
        );
    }

    return (
        <FadeScrollView
            className="bg-background px-4 pt-2"
            contentContainerStyle={{ paddingBottom: 120 }}
            showsVerticalScrollIndicator={false}
        >
            {/* Banner de Género */}
            {bannerUrl && (
                <Image
                    source={{ uri: bannerUrl }}
                    className="h-32 rounded-2xl mb-8 w-full"
                    resizeMode="cover"
                />
            )}

            {/* Texto Descriptivo */}
            <Text className="text-gray-300 text-base leading-6 mb-10 font-roboto">
                {description}
            </Text>

            {/* Videos Destacados */}
            <View className="gap-y-8">
                <View>
                    <Text className="text-primary font-bold mb-4 uppercase text-xs tracking-widest">
                        Canción Emblemática
                    </Text>
                    <View className="rounded-2xl overflow-hidden bg-surface">
                        <YoutubePlayer
                            height={200}
                            play={false}
                            videoId={welcomeSongId}
                        />
                    </View>
                </View>

                <View>
                    <Text className="text-primary font-bold mb-4 uppercase text-xs tracking-widest">
                        Introducción al Estilo
                    </Text>
                    <View className="rounded-2xl overflow-hidden bg-surface">
                        <YoutubePlayer
                            height={200}
                            play={false}
                            videoId={welcomeVideoId}
                        />
                    </View>
                </View>
            </View>
        </FadeScrollView>
    );
}