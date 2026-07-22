import React from "react";
import { View, Image, ScrollView, TouchableOpacity, Text } from "react-native";
import { ArrowLeft, Play } from "lucide-react-native";
import { useRoute, useNavigation, RouteProp } from "@react-navigation/native";
import { useInstrumentGenre } from "../hooks/useInstrumentGenre";
import { RootStackParamList } from "../types";
import AppLayout from "../components/AppLayout";

// Definimos que los props son opcionales
interface InstrumentGenreProps {
    instrumentGenreId?: string;
    instrumentName?: string;
    onBack?: () => void;
}

type InstrumentGenreRouteProp = RouteProp<RootStackParamList, 'InstrumentGenreScreen'>;

export default function InstrumentGenreScreen(props: InstrumentGenreProps) {
    const navigation = useNavigation<any>();
    const route = useRoute<InstrumentGenreRouteProp>();

    // Lógica Híbrida: Prioridad a Props, si no, a Route
    const instrumentGenreId = props.instrumentGenreId || route.params?.instrumentGenreId;
    const instrumentName = props.instrumentName || route.params?.instrumentName;

    // Si hay onBack en props lo usamos, si no, usamos el goBack de la navegación
    const handleBack = () => {
        if (props.onBack) {
            props.onBack();
        } else if (navigation.canGoBack()) {
            navigation.goBack();
        }
    };

    const { instrumentGenre, lessons } = useInstrumentGenre(instrumentGenreId || '');

    if (!instrumentGenre) return null;

    return (
        <AppLayout>
            <ScrollView className="bg-background flex-1">
                {/* Header Adaptable */}
                <View className="flex-row items-center px-4 mt-4 mb-6">
                    <TouchableOpacity onPress={handleBack}>
                        <ArrowLeft size={24} color="white" />
                    </TouchableOpacity>
                    <Text className="text-primary text-xl font-bold ml-4">
                        {instrumentName || instrumentGenre.title}
                    </Text>
                </View>

                {/* Hero Card */}
                <View className="px-4 mb-8">
                    <View className="relative h-48 w-full overflow-hidden rounded-3xl bg-secondary">
                        {instrumentGenre.imageUrl && (
                            <Image
                                source={{ uri: instrumentGenre.imageUrl }}
                                className="absolute inset-0 h-full w-full opacity-60"
                                resizeMode="cover"
                            />
                        )}
                        <View className="flex-1 justify-end p-6">
                            <Text className="text-secondary text-xs font-bold uppercase tracking-widest">
                                Tu Progreso
                            </Text>
                            <Text className="text-white text-3xl font-bold">
                                Nivel {instrumentGenre.level || 1}
                            </Text>
                        </View>
                    </View>
                </View>

                {/* Botón Explorar */}
                <View className="px-4 mb-10">
                    <TouchableOpacity
                        onPress={() => navigation.navigate('InstrumentGenreLesson', {
                            instrumentGenreId,
                            instrumentName
                        })}
                        className="bg-primary flex-row h-16 items-center justify-center rounded-2xl shadow-lg"
                    >
                        <Play size={20} color="black" fill="black" />
                        <Text className="text-black font-bold text-lg ml-2">Explorar Lecciones</Text>
                    </TouchableOpacity>
                </View>

                {/* Lista de Lecciones */}
                <View className="px-6 pb-20">
                    <Text className="text-white font-semibold mb-4 text-lg">¿Qué aprenderás?</Text>
                    {lessons.map((lesson, index) => (
                        <View key={lesson.id} className="flex-row items-center mb-4">
                            <View className="w-8 h-8 rounded-full bg-white/10 items-center justify-center mr-4">
                                <Text className="text-primary font-bold">{index + 1}</Text>
                            </View>
                            <Text className="text-gray-300 flex-1">{lesson.title}</Text>
                        </View>
                    ))}
                </View>
            </ScrollView>
        </AppLayout>
    );
}