import React from 'react';
import { View, Text, Image, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import { Video, ResizeMode } from 'expo-av';
import { Ionicons } from '@expo/vector-icons';
import { useRoute, RouteProp } from '@react-navigation/native';
import AppLayout from '../components/AppLayout';
import { RootStackParamList } from '../types';
import { useSong } from '../hooks/useSong';
import { DifficultyLevel } from '../types';

type SongScreenRouteProp = RouteProp<RootStackParamList, 'SongScreen'>;

export default function SongScreen() {
  const route = useRoute<SongScreenRouteProp>();
  const { songId } = route.params;

  const {
    song,
    loading,
    selectedLesson,
    chords,
    tabSections,
    availableDifficulties,
    changeDifficulty,
    parsedLyrics,
  } = useSong(songId);

  if (loading) {
    return (
      <AppLayout>
        <View className="bg-background flex-1 justify-center items-center">
          <ActivityIndicator size="large" color="#ffffff" />
        </View>
      </AppLayout>
    );
  }

  if (!song) {
    return (
      <AppLayout>
        <View className="bg-background flex-1 justify-center items-center">
          <Text className="text-secondary text-lg">Canción no encontrada</Text>
        </View>
      </AppLayout>
    );
  }

  if (!selectedLesson) {
    return (
      <AppLayout>
        <View className="bg-background flex-1 justify-center items-center">
          <Text className="text-secondary text-lg">No hay lecciones disponibles para esta canción</Text>
        </View>
      </AppLayout>
    );
  }

  return (
    <AppLayout>
      <View className="bg-background flex-1 pt-2">
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
          {/* Información de la canción */}
          <View className="items-center mb-1 mx-4">
            <Image
              source={{ uri: song.imageUrl }}
              className="w-24 h-24 rounded-lg mb-2"
              resizeMode="cover"
            />
            <Text className="text-songTitle text-base text-center">{song.title}</Text>
            <Text className="text-artistName text-base text-center mb-2">{song.artist}</Text>
          </View>

          {/* Estrellas de dificultad */}
          <View className="flex-row justify-center mb-7">
            {[...Array(5)].map((_, i) => (
              <Ionicons
                key={i}
                name={i < 3 ? 'star' : 'star-outline'}
                size={18}
                color="#facc15"
              />
            ))}
          </View>

          {/* Video */}
          {selectedLesson.videoUrl && (
            <Video
              source={{ uri: selectedLesson.videoUrl }}
              useNativeControls
              resizeMode={ResizeMode.STRETCH}
              style={{ width: 180, height: 99, borderRadius: 12, marginBottom: 16, alignSelf: 'center' }}
            />
          )}

          {/* Selector de dificultad */}
          <View className="flex-row justify-center space-x-2 mb-4 mt-2 flex-wrap">
            {availableDifficulties.map((difficulty) => (
              <TouchableOpacity
                key={difficulty}
                onPress={() => changeDifficulty(difficulty)}
                className={`px-4 py-1 rounded-full mb-2 ${
                  difficulty === selectedLesson?.difficulty
                    ? 'bg-yellow-600'
                    : 'bg-gray-700'
                }`}
              >
                <Text className="text-white font-semibold">
                  {difficulty === DifficultyLevel.EASY
                    ? 'Fácil'
                    : difficulty === DifficultyLevel.MEDIUM
                    ? 'Intermedio'
                    : 'Original'}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          {/* Acordes */}
          {chords.length > 0 && (
            <View className="mb-6">
              <Text className="text-primary text-lg font-bold mb-3 text-center">Acordes</Text>
              <View className="flex-row flex-wrap gap-1 justify-center">
                {chords.map((chord) => (
                  <View key={chord.id} className="items-center mb-3">
                    <Image
                      source={{ uri: chord.imageUrl }}
                      className="w-16 h-20 rounded-md"
                      resizeMode="contain"
                    />
                    <Text className="text-secondary text-base mt-1">{chord.name}</Text>
                  </View>
                ))}
              </View>
            </View>
          )}

          {/* Letra con acordes */}
          {parsedLyrics.length > 0 && (
            <View className="mb-6">
              <Text className="text-primary text-lg font-bold mb-3 text-center">Letra</Text>
              <View className="justify-center items-center">
                {parsedLyrics.map((line, idx) => (
                  <View key={idx} className="mb-1">
                    {line.chord && (
                      <Text className="text-primary font-bold text-center">[{line.chord}]</Text>
                    )}
                    <Text className="text-secondary text-center">{line.text.trim()}</Text>
                  </View>
                ))}
              </View>
            </View>
          )}

          {/* Tab Sections */}
          {tabSections.length > 0 && (
            <View className="mb-10">
              <Text className="text-primary text-lg font-bold mb-3 text-center">Tabs</Text>
              {tabSections.map((section) => (
                <View key={section.id} className="mb-4 bg-gray-800 p-4 rounded-lg">
                  <Text className="text-primary font-bold mb-2">{section.identifier.toUpperCase()}</Text>
                  <Text className="text-secondary font-mono text-xs">{section.content}</Text>
                </View>
              ))}
            </View>
          )}
        </ScrollView>
      </View>
    </AppLayout>
  );
}