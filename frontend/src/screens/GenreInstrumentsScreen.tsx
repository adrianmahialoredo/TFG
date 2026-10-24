import React from "react";
import { View, Text } from "react-native";
import FadeScrollView from "../components/FadeScrollView";
import { ToolButton } from "../components/buttons/ToolButton";
import InstrumentIcon from "../components/InstrumentIcon";
import InstrumentGenreLessonScreen from "./InstrumentGenreLessonScreen";
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

interface GenreInstrumentsProps {
  genreId: string;
}

// Ajustar este valor para controlar el gap horizontal entre los 2 botones por fila.
const TOOL_BUTTON_GAP = 24;

export default function GenreInstrumentsScreen({ genreId }: GenreInstrumentsProps) {

  // Nivel 6: El hook se encarga de la lógica de selección y filtrado
  const {
    instrumentGenres,
    selectedInstrumentGenre,
    selectInstrument,
    unselectInstrument
  } = useGenreInstruments(genreId);

  // SI HAY UNO SELECCIONADO: Mostramos sus lecciones con el panel lateral (EdgePanel)
  if (selectedInstrumentGenre) {
    return (
        <InstrumentGenreLessonScreen
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
      <FadeScrollView
          className="bg-background"
          contentContainerStyle={{ paddingBottom: 100, paddingTop: 8 }}
      >
        {/* Listado de Instrumentos del Género */}
        <View className="px-4 mb-8">
          {chunkArray(instrumentGenres, 2).map((pair, rowIndex) => (
              <View key={rowIndex} className="flex-row justify-center mb-4">
                {pair.map((ig: InstrumentGenreDTO, index: number) => (
                    <View
                        key={ig.id}
                        style={{ marginRight: index < pair.length - 1 ? TOOL_BUTTON_GAP : 0 }}
                    >
                      <ToolButton
                          label={ig.title ?? "S/N"}
                          icon={<InstrumentIcon name={ig.title} size={85} color="white" />}
                          buttonMarginRight={0}
                          labelMarginRight={0}
                          onPress={() => selectInstrument(ig.id)}
                      />
                    </View>
                ))}
              </View>
          ))}
        </View>

        {/* Carrusel de Lecciones: Ahora el carrusel es autónomo y usa el genreId */}
        <View className="mt-2">
          <GenreLessonsCarousel
              title="Recomendaciones"
              genreId={genreId}
          />
        </View>
      </FadeScrollView>
  );
}