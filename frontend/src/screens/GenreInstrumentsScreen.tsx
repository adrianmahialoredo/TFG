import React from "react";
import { View, ScrollView, Text } from "react-native";
import { ToolButton } from "../components/buttons/ToolButton";
import { Guitar, Keyboard, Drum, Music } from 'lucide-react-native';
import InstrumentGenreScreen from "./InstrumentGenreScreen";
import { useGenreInstruments } from "../hooks/useGenreInstruments";
import { InstrumentGenreDTO } from "../types";
import GenreLessonsCarousel from "../components/carousels/GenreLessonsCarousel";

// Helper para rejilla de 2 columnas
function chunkArray<T>(arr: T[], size: number): T[][] {
  const chunks: T[][] = [];
  for (let i = 0; i < arr.length; i += size) {
    chunks.push(arr.slice(i, i + size));
  }
  return chunks;
}

const INSTRUMENT_ICONS: Record<string, React.ReactNode> = {
  'guitarra': <Guitar size={85} color="white" strokeWidth={1} />,
  'piano': <Keyboard size={85} color="white" strokeWidth={1} />,
  'bateria': <Drum size={85} color="white" strokeWidth={1} />,
  'bajo': <Guitar size={85} color="white" strokeWidth={1} />,
};

const getIconForName = (name?: string): React.ReactNode => {
  const normalized = name?.toLowerCase() || '';
  const key = Object.keys(INSTRUMENT_ICONS).find(k => normalized.includes(k));
  return key ? INSTRUMENT_ICONS[key] : <Music size={85} color="white" strokeWidth={1} />;
};

interface GenreInstrumentsProps {
  genreId: string;
}

export default function GenreInstrumentsScreen({ genreId }: GenreInstrumentsProps) {

  // Nivel 6: El hook se encarga de la lógica de selección y filtrado
  const {
    instrumentGenres,
    selectedInstrumentGenre,
    selectInstrument,
    unselectInstrument
  } = useGenreInstruments(genreId);

  // SI HAY UNO SELECCIONADO: Mostramos el detalle (Nivel de niveles, lecciones, etc.)
  if (selectedInstrumentGenre) {
    return (
        <InstrumentGenreScreen
            instrumentGenreId={selectedInstrumentGenre.id}
            instrumentName={selectedInstrumentGenre.title ?? "Instrumento"}
            onBack={unselectInstrument}
        />
    );
  }

  // SI NO HAY DATOS
  if (instrumentGenres.length === 0) {
    return (
        <View className="flex-1 justify-center items-center p-10">
          <Text className="text-secondary text-center font-roboto">
            No hay instrumentos configurados para este género.
          </Text>
        </View>
    );
  }

  return (
      <ScrollView
          className="bg-background"
          contentContainerStyle={{ paddingBottom: 100, paddingTop: 20 }}
      >
        {/* Listado de Instrumentos del Género */}
        <View className="px-4 mb-8">
          {chunkArray(instrumentGenres, 2).map((pair, rowIndex) => (
              <View key={rowIndex} className="flex-row justify-center gap-4 mb-4">
                {pair.map((ig: InstrumentGenreDTO) => (
                    <ToolButton
                        key={ig.id}
                        label={ig.title ?? "S/N"}
                        icon={getIconForName(ig.title)}
                        onPress={() => selectInstrument(ig.id)}
                    />
                ))}
              </View>
          ))}
        </View>

        {/* Carrusel de Lecciones: Ahora el carrusel es autónomo y usa el genreId */}
        <View className="mt-2">
          <GenreLessonsCarousel
              title="Conceptos de este Género"
              genreId={genreId}
          />
        </View>
      </ScrollView>
  );
}