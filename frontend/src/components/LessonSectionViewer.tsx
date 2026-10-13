import React from "react";
import {View, Text, TouchableOpacity, ActivityIndicator, useWindowDimensions} from "react-native";
import {Ionicons} from "@expo/vector-icons";
import RenderHtml from "react-native-render-html";
import {LessonSectionDTO} from "../types";
import {COLORS} from "../constants/Theme";

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
 * Contenido de una lección: navegación ‹ i/n › entre secciones y el HTML de la sección actual
 * (el HTML ya trae su propio título). Recibe directamente lo que devuelve `useLesson`.
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
          <TouchableOpacity
              onPress={goToPrevious}
              disabled={!hasPrevious}
              style={{ padding: 4, opacity: hasPrevious ? 1 : 0 }}
          >
            <Ionicons name="chevron-back" size={18} color={COLORS.primary} />
          </TouchableOpacity>
          <Text style={{ color: COLORS.secondary, marginHorizontal: 8 }}>
            {sectionIndex + 1}/{sections.length}
          </Text>
          <TouchableOpacity
              onPress={goToNext}
              disabled={!hasNext}
              style={{ padding: 4, opacity: hasNext ? 1 : 0 }}
          >
            <Ionicons name="chevron-forward" size={18} color={COLORS.primary} />
          </TouchableOpacity>
        </View>
      )}

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
