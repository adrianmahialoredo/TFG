import React from 'react';
import { View, Text, Image, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useVideoPlayer, VideoView } from 'expo-video';
import { useRoute, RouteProp } from '@react-navigation/native';
import AppLayout from '../components/AppLayout';
import ProfileHeader from '../components/ProfileHeader';
import DifficultyStars from '../components/DifficultyStars';
import {ChordDTO, RootStackParamList, TabSectionDTO} from '../types';
import { useSong } from '../hooks/useSong';
import { DifficultyLevel } from '../types';
import { LyricLine } from '../utils/song.utils';

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

  // expo-video: el player se crea siempre (las reglas de hooks no permiten hacerlo tras los return)
  // y cambia de fuente cuando cambia la lección seleccionada. Sin vídeo, la fuente es null.
  const player = useVideoPlayer(selectedLesson?.videoUrl ?? null);

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
        <ProfileHeader title="Canciones" />
        
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
          <View className="mb-7">
            <DifficultyStars level={song.level} />
          </View>

          {/* Video */}
          {selectedLesson.videoUrl && (
            <VideoView
              player={player}
              nativeControls
              contentFit="fill"
              style={{ width: 180, height: 99, borderRadius: 12, marginBottom: 16, alignSelf: 'center' }}
            />
          )}

          {/* Selector de dificultad */}
          <View className="flex-row justify-center space-x-2 mb-4 mt-2 flex-wrap">
            {availableDifficulties.map((difficulty: DifficultyLevel) => (
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
                {chords.map((chord: ChordDTO) => (
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
              <View>
                {parsedLyrics.map((line: LyricLine, lineIdx) => {
                  if (line.length === 0) return <View key={lineIdx} className="h-4" />;

                  // Las líneas sin acordes no reservan la fila superior
                  const hasChords = line.some((segment) => segment.chord);

                  return (
                    <View key={lineIdx} className="flex-row flex-wrap justify-center mb-2">
                      {line.map((segment, segmentIdx) => (
                        // Cada fragmento es una columna: acorde arriba y letra debajo, así el acorde
                        // queda alineado con el inicio del fragmento aunque la línea haga wrap
                        <View key={segmentIdx}>
                          {hasChords && (
                            <Text className="text-primary font-bold pr-1">{segment.chord ?? ' '}</Text>
                          )}
                          <Text className="text-secondary">{segment.text}</Text>
                        </View>
                      ))}
                    </View>
                  );
                })}
              </View>
            </View>
          )}

          {/* Tab Sections */}
          {tabSections.length > 0 && (
            <View className="mb-10">
              <Text className="text-primary text-lg font-bold mb-3 text-center">Tabs</Text>
              {tabSections.map((section: TabSectionDTO) => (
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