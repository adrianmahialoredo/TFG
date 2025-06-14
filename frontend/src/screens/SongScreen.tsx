import React, { useState } from 'react';
import { View, Text, Image, ScrollView, TouchableOpacity } from 'react-native';
import { Video, ResizeMode } from 'expo-av';
import { Ionicons } from '@expo/vector-icons';
import { useRoute } from '@react-navigation/native';
import AppLayout from '../components/AppLayout';
import { COLORS } from '../constants/Theme';
import YoutubePlayer from 'react-native-youtube-iframe';

type Lesson = {
  videoUrl: string;
  chordImages: { name: string; imageUrl: string }[];
  lyrics: string;
};

type SongLevel = 'Fácil' | 'Original';

type Song = {
  title: string;
  artist: string;
  imageUrl: string;
  difficulty: number;
  levels: SongLevel[];
  lessons: Record<SongLevel, Lesson>;
};

const sampleSong: Song = {
  title: 'bloody valentine',
  artist: 'mgk',
  imageUrl: 'https://is1-ssl.mzstatic.com/image/thumb/Music124/v4/30/0a/c2/300ac280-3160-6d89-19d5-3fc4a86f1112/20UMGIM85612.rgb.jpg/600x600bf-60.jpg',
  difficulty: 3,
  levels: ['Fácil', 'Original'],
  lessons: {
    'Fácil': {
      videoUrl: 'https://res.cloudinary.com/dvm8qkmuo/video/upload/v1749922993/f6uayh5nemucjfozkb9k.mp4',
      chordImages: [
        { name: 'D5', imageUrl: 'https://chordbank.com/cb4dg/bestial_pip_5_750.png' },
        { name: 'A5', imageUrl: 'https://chordbank.com/cb4dg/grumpy_ace_5_750.png' },
        { name: 'F#5', imageUrl: 'https://chordbank.com/cb4dg/grumpy_ace_2_750.png' },
      ],
      lyrics: `
[D5]The simulation just went bad
[A5]But you're the best I ever had
[F#5]Like hand prints in wet cement
She touched me, it's permanent

[D5]In my head, in my head

      `,
    },
    'Original': {
      videoUrl: 'wSdT-SArM2Q',
      chordImages: [],
      lyrics: '',
    },
  },
};

function parseLyrics(lyrics: string): { chord?: string; text: string }[] {
  const regex = /\[([^\]]+)\]|([^\[]+)/g;
  const result: { chord?: string; text: string }[] = [];

  let currentChord: string | null = null;
  let match: RegExpExecArray | null;

  while ((match = regex.exec(lyrics)) !== null) {
    if (match[1]) {
      currentChord = match[1];
    } else if (match[2]) {
      result.push({ chord: currentChord ?? undefined, text: match[2] });
      currentChord = null;
    }
  }

  return result;
}

export default function SongScreen() {
  const [selectedLevel, setSelectedLevel] = useState<SongLevel>(sampleSong.levels[0]);
  const lesson = sampleSong.lessons[selectedLevel];
  const parsedLyrics = parseLyrics(lesson.lyrics);

  return (
    <AppLayout>
      <View className = "bg-background flex-1 pt-2">
    
        {/* Icono de perfil y titulo */}
        <View className="flex-row justify-left items-center mt-6 mb-7">
          <TouchableOpacity>
            <Image
              source={{ uri: 'https://i.pravatar.cc/100' }}
              className="w-14 h-14 rounded-full border"
            />
          </TouchableOpacity>
          <Text className="text-[24px] font-roboto font-bold text-primary text-center ml-3">Canciones</Text>
        </View> 
    <ScrollView className="bg-background p-4 flex-1">

      <View className="items-center mb-1 mx-4">
        <Image
          source={{ uri: sampleSong.imageUrl }}
          className="w-24 h-24 rounded-lg mb-2"
          resizeMode="cover"
        />
        <Text className="text-songTitle text-base text-center">{sampleSong.title}</Text>
        <Text className="text-artistName text-base text-center mb-2">{sampleSong.artist}</Text>
      </View>

        {/* Estrellas de dificultad */}
      <View className="flex-row justify-center mb-7">
        {[...Array(5)].map((_, i) => (
          <Ionicons
            key={i}
            name={i < sampleSong.difficulty ? 'star' : 'star-outline'}
            size={18}
            color="#facc15"
          />
        ))}
      </View>

        {/* Video (opcional para más adelante) */}
      <Video
        source={{ uri: lesson.videoUrl }}
        useNativeControls
        resizeMode={ResizeMode.STRETCH}
        style={{ width: 180, height: 99, borderRadius: 12, marginBottom: 16, alignSelf: 'center' }}
      /> 

      {/* Selector de nivel */}
      <View className="flex-row justify-center space-x-2 mb-4 mt-2">
        {sampleSong.levels.map((level) => (
          <TouchableOpacity
            key={level}
            onPress={() => setSelectedLevel(level)}
            className={`px-4 py-1 rounded-full ${
              level === selectedLevel ? 'bg-yellow-600' : 'bg-gray-700'
            }`}
          >
            <Text className="text-white font-semibold">{level}</Text>
          </TouchableOpacity>
        ))}
      </View>



      {/* Acordes */}
<View className="flex-row flex-wrap gap-1 justify-center">
  {lesson.chordImages.map((chord, index) => (
    <View key={index} className="items-center">
      <Image
        source={{ uri: chord.imageUrl }}
        className="w-16 h-20 rounded-md"
        resizeMode="contain"
      />
      <Text className="text-secondary text-base">{chord.name}</Text>
    </View>
  ))}
</View>

      {/* Letra con acordes */}
      <View className="mb-10 justify-center items-center">
        {parsedLyrics.map((line, idx) => (
          <View key={idx} className="mb-1">
            {line.chord && (
              <Text className="text-primary font-bold text-center">{line.chord}</Text>
            )}
            <Text className="text-secondary text-center">{line.text.trim()}</Text>
          </View>
        ))}
      </View>
      
    </ScrollView>
    </View>
    </AppLayout>
  );
}
