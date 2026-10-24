import {RouteProp, useRoute} from "@react-navigation/native";
import {RootStackParamList} from "../types";
import { View, Text, Image, ActivityIndicator } from "react-native";
import FadeScrollView from "../components/FadeScrollView";
import ProfileHeader from "../components/ProfileHeader";
import React from "react";
import AppLayout from "../components/AppLayout";
import DifficultyStars from "../components/DifficultyStars";
import LessonSectionViewer from "../components/LessonSectionViewer";
import {useLesson} from "../hooks/useLesson";

type LessonScreenRouteProp = RouteProp<RootStackParamList, 'LessonScreen'>;

export default function LessonScreen() {
  const route = useRoute<LessonScreenRouteProp>();
  const {lessonId} = route.params;

  const {lesson, genre, ...sectionState} = useLesson(lessonId);

  if (sectionState.loading && !lesson) {
    return (
      <AppLayout>
        <View className="bg-background flex-1 justify-center items-center">
          <ActivityIndicator size="large" color="#ffffff" />
        </View>
      </AppLayout>
    );
  }

  if (!lesson) {
    return (
      <AppLayout>
        <View className="bg-background flex-1 justify-center items-center">
          <Text className="text-secondary text-lg">Lección no encontrada</Text>
        </View>
      </AppLayout>
    );
  }

  return (
      <AppLayout>
        <View className="bg-background flex-1 pt-2">
          {/* Icono de perfil y titulo */}
          <ProfileHeader title="Aprender" />

          <FadeScrollView className="bg-background p-4 flex-1">
            {/* Información de la lección */}
            <View className="items-center mb-1 mx-4">
              <Image
                  source={{ uri: lesson.imageUrl }}
                  className="w-24 h-24 rounded-lg mb-2"
                  resizeMode="cover"
              />
              <Text className="text-songTitle text-base text-center">{lesson.title}</Text>
              {genre && (
                <Text className="text-artistName text-base text-center mb-2">{genre.title}</Text>
              )}
            </View>

            {/* Estrellas de dificultad */}
            <View className="mb-5">
              <DifficultyStars level={lesson.level} />
            </View>

            {/* Navegación entre secciones y contenido HTML */}
            <LessonSectionViewer {...sectionState} />
          </FadeScrollView>
        </View>
      </AppLayout>
  );
}
