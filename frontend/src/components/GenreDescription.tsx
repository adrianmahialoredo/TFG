import React from 'react';
import { Image, View, ScrollView, Text } from 'react-native';
import YoutubePlayer from 'react-native-youtube-iframe';

type GenreDescriptionProps = {
  description: string;
  bannerUrl: string;
  welcomeVideoId: string;
  welcomeSongId: string;
};

export default function GenreDescription({ description, bannerUrl, welcomeVideoId, welcomeSongId }: GenreDescriptionProps) {
  return (
    <ScrollView className="bg-background py-4" contentContainerStyle={{ paddingBottom: 120 }}>
      <Image
        source={{ uri: bannerUrl }}
        className="h-24 rounded-lg mb-9"
      />
      <Text className="text-secondary text-base mb-10">
        {description}
      </Text>
      <View className="gap-10">
        <View className="aspect-video rounded-xl overflow-hidden">
          <YoutubePlayer
            height={230}
            play={false}
            videoId={welcomeSongId} // <- ID del video de YouTube
            webViewStyle={{ opacity: 0.99 }} // Para evitar el bug de la pantalla negra
          />
        </View>
        <View className="aspect-video rounded-xl overflow-hidden">
          <YoutubePlayer
            height={230}
            play={false}
            videoId={welcomeVideoId} // <- ID del video de YouTube
            webViewStyle={{ opacity: 0.99 }} // Para evitar el bug de la pantalla negra
          />
        </View>
      </View>
    </ScrollView>
  );
}