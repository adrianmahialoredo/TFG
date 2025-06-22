import React from 'react';
import { View, Text, ScrollView } from 'react-native';
import HorizontalSongsCarousel from './carousels/SongsCarousel';

const recommendations = [
  { id: '1', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '2', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '3', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '4', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '5', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '6', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
];

export default function GenreSongs({ description }: { description: string }) {
  return (   
    <ScrollView
    className='mt-6'
      showsVerticalScrollIndicator={false}
      contentContainerStyle={{ paddingBottom: 120 }}
    >
      {/* Carrusel de canciones populares */}
      <HorizontalSongsCarousel title="Populares" data={recommendations} />

      {/* Carrusel de canciones populares */}
      <HorizontalSongsCarousel title="Clasicas" data={recommendations} />

      {/* Carrusel de canciones populares */}
      <HorizontalSongsCarousel title="Nuevas" data={recommendations} />

      {/* Carrusel de canciones populares */}
      <HorizontalSongsCarousel title="Populares" data={recommendations} />

      {/* Carrusel de canciones populares */}
      <HorizontalSongsCarousel title="Nuevas" data={recommendations} />

      {/* Carrusel de canciones populares */}
      <HorizontalSongsCarousel title="Para ti" data={recommendations} />
    </ScrollView>
  );
}