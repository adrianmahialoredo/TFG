import React from 'react';
import { View } from 'react-native';
import YoutubePlayer from 'react-native-youtube-iframe';
import { getYoutubeVideoId } from '../utils/youtube.utils';

type Props = {
  /** URL de YouTube (watch, youtu.be, embed, shorts) o directamente el ID del vídeo */
  url?: string | null;
  height?: number;
};

/**
 * Reproductor de un vídeo de YouTube (vídeo de una lección de canción o de una sección de lección).
 * No pinta nada si la URL no es de un vídeo de YouTube reconocible.
 */
export default function YoutubeVideo({ url, height = 200 }: Props) {
  const videoId = getYoutubeVideoId(url);
  if (!videoId) return null;

  return (
    <View className="rounded-2xl overflow-hidden mb-4">
      <YoutubePlayer height={height} play={false} videoId={videoId} />
    </View>
  );
}
