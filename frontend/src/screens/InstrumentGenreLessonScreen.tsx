import React from 'react';
import { View, Text, ScrollView, Image, TouchableOpacity } from 'react-native';
import { ArrowLeft } from "lucide-react-native";
import EdgePanel from '../components/EdgePanel';
import { useInstrumentGenreLesson } from '../hooks/useInstrumentGenreLesson';

export default function InstrumentGenreLessonScreen({ route, navigation }: any) {
    const { instrumentGenreId, instrumentName } = route.params;

    const {
        instrumentGenre,
        lessons,
        activeLesson,
        setActiveLessonId
    } = useInstrumentGenreLesson(instrumentGenreId);

    if (!instrumentGenre || !activeLesson) return null;

    return (
        <View className="flex-1 bg-black">
            <ScrollView className="flex-1" contentContainerStyle={{ paddingBottom: 100 }}>
                {/* Header con botón atrás */}
                <View className="flex-row items-center px-4 mt-12 mb-6">
                    <TouchableOpacity onPress={() => navigation.goBack()}>
                        <ArrowLeft size={24} color="white" />
                    </TouchableOpacity>
                    <Text className="text-primary text-lg font-bold ml-4 uppercase tracking-widest">
                        {instrumentName}
                    </Text>
                </View>

                {/* Contenido Dinámico de la Lección */}
                <View className="px-6">
                    <Text className="text-3xl font-bold text-white mb-2">
                        {activeLesson.title}
                    </Text>
                    <Text className="text-secondary text-sm mb-6 uppercase">
                        {instrumentGenre.title}
                    </Text>

                    {activeLesson.imageUrl && (
                        <Image
                            source={{ uri: activeLesson.imageUrl }}
                            className="w-full h-48 rounded-2xl mb-6"
                            resizeMode="cover"
                        />
                    )}

                    <Text className="text-gray-300 text-base leading-7">
                        {"Contenido de la lección en preparación..."}
                    </Text>

                    {/* Botón de acción */}
                    <TouchableOpacity className="bg-primary h-14 rounded-xl items-center justify-center mt-8">
                        <Text className="text-black font-bold text-lg">EMPEZAR VIDEO</Text>
                    </TouchableOpacity>
                </View>
            </ScrollView>

            {/* EL PANEL LATERAL: Solo se encarga de cambiar el ID */}
            <EdgePanel
                lessons={lessons}
                activeLessonId={activeLesson.id}
                onSelectLessonId={(id) => setActiveLessonId(id)}
            />
        </View>
    );
}