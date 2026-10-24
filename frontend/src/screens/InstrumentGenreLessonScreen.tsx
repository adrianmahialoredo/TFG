import React from 'react';
import { View, Text, Image, ActivityIndicator } from 'react-native';
import FadeScrollView from '../components/FadeScrollView';
import BackButton from "../components/iconbuttons/BackButton";
import { useRoute, useNavigation, RouteProp } from "@react-navigation/native";
import EdgePanel from '../components/EdgePanel';
import DifficultyStars from '../components/DifficultyStars';
import LessonSectionViewer from '../components/LessonSectionViewer';
import { useInstrumentGenreLesson } from '../hooks/useInstrumentGenreLesson';
import { useLesson } from '../hooks/useLesson';
import { RootStackParamList } from '../types';
import AppLayout from '../components/AppLayout';
import { COLORS } from '../constants/Theme';

/**
 * Vista de un InstrumentGenre (itinerario de un instrumento dentro de un género): la lección
 * activa con sus secciones y el panel lateral con todas las lecciones. Es la ÚNICA vista de un
 * InstrumentGenre, se llegue desde donde se llegue: embebida en la pestaña "Instrumentos" del
 * género (GenreInstrumentsScreen) o como ruta al abrirlo desde un carrusel, una lista o el Home.
 */

// Props opcionales: se usa embebida (GenreInstrumentsScreen) o como ruta de navegación
interface InstrumentGenreLessonProps {
    instrumentGenreId?: string;
    instrumentName?: string;
    onBack?: () => void;
}

type InstrumentGenreLessonRouteProp = RouteProp<RootStackParamList, 'InstrumentGenreLesson'>;

export default function InstrumentGenreLessonScreen(props: InstrumentGenreLessonProps) {
    const navigation = useNavigation<any>();
    const route = useRoute<InstrumentGenreLessonRouteProp>();

    // Lógica Híbrida: Prioridad a Props, si no, a Route
    const instrumentGenreId = props.instrumentGenreId || route.params?.instrumentGenreId || '';
    const routeName = props.instrumentName || route.params?.instrumentName;

    const handleBack = () => {
        if (props.onBack) {
            props.onBack();
        } else if (navigation.canGoBack()) {
            navigation.goBack();
        }
    };

    const {
        instrumentGenre,
        lessons,
        activeLesson,
        setActiveLessonId,
        loading
    } = useInstrumentGenreLesson(instrumentGenreId);

    // Dos niveles: el EdgePanel elige la lección y las flechas, la sección mostrada.
    // No se marca en "Recientes": el item accedido es el InstrumentGenre, no cada lección.
    const lessonState = useLesson(activeLesson?.id ?? '', { markAccessed: false });

    // Sin nombre en la navegación se usa el título del InstrumentGenre
    const instrumentName = routeName || instrumentGenre?.title;

    // Embebida, la pantalla del género ya pone el layout; como ruta, lo pone ella
    const withLayout = (content: React.ReactElement) =>
        props.instrumentGenreId ? content : <AppLayout>{content}</AppLayout>;

    // Mientras carga, si no existe o si aún no tiene lecciones se pinta un aviso: un `null`
    // dejaba la pantalla en blanco (p.ej. al abrirlo desde una card, solo con su resumen)
    if (!instrumentGenre || !activeLesson) {
        return withLayout(
            <View className="flex-1 bg-background">
                <View className="flex-row items-center px-4 mt-4 mb-6">
                    <BackButton onPress={handleBack} />
                    <Text className="text-primary text-lg font-bold ml-4 uppercase tracking-widest">
                        {instrumentName}
                    </Text>
                </View>
                <View className="flex-1 justify-center items-center px-4">
                    {loading ? (
                        <ActivityIndicator size="large" color={COLORS.primary} />
                    ) : (
                        <Text className="text-secondary text-lg text-center">
                            {instrumentGenre ? 'Este itinerario aún no tiene lecciones' : 'No se pudo cargar el contenido'}
                        </Text>
                    )}
                </View>
            </View>
        );
    }

    return withLayout(
        <View className="flex-1 bg-black">
            <FadeScrollView className="flex-1" contentContainerStyle={{ paddingBottom: 100 }}>
                {/* Header con botón atrás */}
                <View className="flex-row items-center px-4 mt-4 mb-6">
                    <BackButton onPress={handleBack} />
                    <Text className="text-primary text-lg font-bold ml-4 uppercase tracking-widest">
                        {instrumentName}
                    </Text>
                </View>

                {/* Información de la lección */}
                <View className="items-center mb-1 mx-4">
                    {activeLesson.imageUrl && (
                        <Image
                            source={{ uri: activeLesson.imageUrl }}
                            className="w-24 h-24 rounded-lg mb-2"
                            resizeMode="cover"
                        />
                    )}
                    <Text className="text-songTitle text-base text-center">{activeLesson.title}</Text>
                    <Text className="text-artistName text-base text-center mb-2">{instrumentGenre.title}</Text>
                </View>

                {/* Estrellas de dificultad */}
                <View className="mb-5">
                    <DifficultyStars level={activeLesson.level} />
                </View>

                {/* Navegación entre secciones y contenido HTML de la lección activa */}
                <View className="px-4">
                    <LessonSectionViewer {...lessonState} />
                </View>
            </FadeScrollView>

            {/* EL PANEL LATERAL: Solo se encarga de cambiar el ID */}
            <EdgePanel
                lessons={lessons}
                activeLessonId={activeLesson.id}
                onSelectLessonId={(id) => setActiveLessonId(id)}
            />
        </View>
    );
}