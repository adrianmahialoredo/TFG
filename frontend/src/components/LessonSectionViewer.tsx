import React from "react";
import {View, Text, ActivityIndicator, useWindowDimensions} from "react-native";
import Touchable from "./Touchable";
import {ChevronLeft, ChevronRight} from "lucide-react-native";
import RenderHtml from "react-native-render-html";
import {LessonSectionDTO} from "../types";
import {COLORS} from "../constants/Theme";
import YoutubeVideo from "./YoutubeVideo";

const HTML_BASE_STYLE = { color: COLORS.secondary, fontSize: 15, lineHeight: 22 };
const HTML_TAGS_STYLES = {
  h1: { color: COLORS.primary },
  h2: { color: COLORS.primary },
  h3: { color: COLORS.primary },
  b: { color: COLORS.songTitle },
  strong: { color: COLORS.songTitle },
};

type Props = {
  sections: LessonSectionDTO[];
  currentSection?: LessonSectionDTO;
  sectionIndex: number;
  hasPrevious: boolean;
  hasNext: boolean;
  goToPrevious: () => void;
  goToNext: () => void;
  loading: boolean;
};

/**
 * Contenido de una lección: navegación ‹ i/n › entre secciones, el vídeo de apoyo de la sección
 * actual (si tiene) y su HTML (el HTML ya trae su propio título). Recibe directamente lo que
 * devuelve `useLesson`.
 */
export default function LessonSectionViewer({
  sections,
  currentSection,
  sectionIndex,
  hasPrevious,
  hasNext,
  goToPrevious,
  goToNext,
  loading,
}: Props) {
  const {width} = useWindowDimensions();

  return (
    <>
      {/* Navegación entre secciones: ‹ i/n › */}
      {sections.length > 1 && (
        <View style={{ flexDirection: 'row', justifyContent: 'center', alignItems: 'center', marginBottom: 12 }}>
          <Touchable
              onPress={goToPrevious}
              disabled={!hasPrevious}
              style={{ padding: 4, opacity: hasPrevious ? 1 : 0 }}
          >
            <ChevronLeft size={18} color={COLORS.primary} />
          </Touchable>
          <Text style={{ color: COLORS.secondary, marginHorizontal: 8 }}>
            {sectionIndex + 1}/{sections.length}
          </Text>
          <Touchable
              onPress={goToNext}
              disabled={!hasNext}
              style={{ padding: 4, opacity: hasNext ? 1 : 0 }}
          >
            <ChevronRight size={18} color={COLORS.primary} />
          </Touchable>
        </View>
      )}

      {/* Vídeo de apoyo de la sección (YouTube) */}
      <YoutubeVideo url={currentSection?.videoUrl} />

      {/* Contenido de la sección actual */}
      {currentSection?.htmlContent ? (
          <View className="mb-10">
            <RenderHtml
                contentWidth={width}
                source={{ html: currentSection.htmlContent }}
                baseStyle={HTML_BASE_STYLE}
                tagsStyles={HTML_TAGS_STYLES}
            />
          </View>
      ) : loading ? (
          <ActivityIndicator size="small" color="#ffffff" />
      ) : (
          <Text className="text-secondary text-center">No se pudo cargar el contenido.</Text>
      )}
    </>
  );
}
