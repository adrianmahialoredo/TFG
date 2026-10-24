import React from 'react';
import { View, Text, Image, ActivityIndicator, TouchableOpacity } from 'react-native';
import FadeScrollView from '../components/FadeScrollView';
import { useRoute, RouteProp } from '@react-navigation/native';
import AppLayout from '../components/AppLayout';
import ProfileHeader from '../components/ProfileHeader';
import DifficultyStars from '../components/DifficultyStars';
import OptionSelector from '../components/OptionSelector';
import YoutubeVideo from '../components/YoutubeVideo';
import ChordModal from '../components/ChordModal';
import {ChordDTO, RootStackParamList, TabSectionDTO} from '../types';
import { useSong } from '../hooks/useSong';
import { DifficultyLevel } from '../types';
import { LyricLine } from '../utils/song.utils';

type SongScreenRouteProp = RouteProp<RootStackParamList, 'SongScreen'>;

const DIFFICULTY_LABELS: Record<DifficultyLevel, string> = {
  [DifficultyLevel.EASY]: 'Fácil',
  [DifficultyLevel.MEDIUM]: 'Intermedio',
  [DifficultyLevel.ORIGINAL]: 'Original',
};

export default function SongScreen() {
  const route = useRoute<SongScreenRouteProp>();
  const { songId } = route.params;

  const {
    song,
    loading,
    selectedLesson,
    chords,
    tabSections,
    availableInstruments,
    selectedInstrumentId,
    changeInstrument,
    availableDifficulties,
    changeDifficulty,
    parsedLyrics,
  } = useSong(songId);

  // Acorde abierto en el modal de detalle (null = cerrado)
  const [selectedChord, setSelectedChord] = React.useState<ChordDTO | null>(null);

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
        
        <FadeScrollView className="bg-background p-4 flex-1">
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

          {/* Vídeo de la lección (YouTube) */}
          <YoutubeVideo url={selectedLesson.videoUrl} />

          {/* Selector de instrumento (solo si hay lecciones para más de uno) */}
          {availableInstruments.length > 1 && (
            <OptionSelector
              options={availableInstruments.map((instrument) => ({ value: instrument.id, label: instrument.name }))}
              selected={selectedInstrumentId}
              onSelect={changeInstrument}
            />
          )}

          {/* Selector de dificultad */}
          <View className="mb-4 mt-2">
            <OptionSelector
              options={availableDifficulties.map((difficulty) => ({ value: difficulty, label: DIFFICULTY_LABELS[difficulty] }))}
              selected={selectedLesson.difficulty}
              onSelect={changeDifficulty}
            />
          </View>

          {/* Acordes */}
          {chords.length > 0 && (
            <View className="mb-6">
              <Text className="text-primary text-lg font-bold mb-3 text-center">Acordes</Text>
              <View className="flex-row flex-wrap gap-1 justify-center">
                {chords.map((chord: ChordDTO) => (
                  // Al pulsar un acorde se abre su detalle. Estilos en línea: con className
                  // NativeWind pisa la opacidad animada del TouchableOpacity
                  <TouchableOpacity
                    key={chord.id}
                    onPress={() => setSelectedChord(chord)}
                    activeOpacity={0.7}
                    accessibilityRole="button"
                    accessibilityLabel={`Ver acorde ${chord.name}`}
                    style={{ alignItems: 'center', marginBottom: 12 }}
                  >
                    {/* Los diagramas son líneas oscuras sobre fondo transparente: sin un fondo
                        claro detrás no se ven sobre el fondo negro de la app */}
                    <View className="bg-secondary rounded-md p-1">
                      <Image
                        source={{ uri: chord.imageUrl }}
                        className="w-16 h-20"
                        resizeMode="contain"
                      />
                    </View>
                    <Text className="text-secondary text-base mt-1">{chord.name}</Text>
                  </TouchableOpacity>
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
        </FadeScrollView>
      </View>

      <ChordModal chord={selectedChord} onClose={() => setSelectedChord(null)} />
    </AppLayout>
  );
}