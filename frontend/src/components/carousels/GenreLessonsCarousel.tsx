import { Image, View, Text, FlatList, useWindowDimensions } from 'react-native';
import Touchable from '../Touchable';
import React, { useState } from 'react';
import { useNavigation, NavigationProp } from "@react-navigation/native";
import {LessonDTO, RootStackParamList} from "../../types";
import {useRecommendedLessonsByGenre} from "../../hooks/useRecommendedLessonsByGenre";

/** Lecciones por página: el carrusel avanza de PAGE_SIZE en PAGE_SIZE. */
const PAGE_SIZE = 4;
const GAP = 18;
const SIDE_PADDING = 16;
/** Proporción alto/ancho de la portada (la de antes: 51x93). */
const IMAGE_RATIO = 93 / 51;

type CarouselProps = {
    title: string;
    genreId: string; // Recibe el ID, no los datos
};

export default function GenreLessonsCarousel({ title, genreId }: CarouselProps) {
    // Lecciones recomendadas del género (endpoint de recomendaciones)
    const { lessons } = useRecommendedLessonsByGenre(genreId);
    const navigation = useNavigation<NavigationProp<RootStackParamList>>();
    // Ancho del carrusel (hasta medirlo, el de la pantalla): reparte PAGE_SIZE lecciones en él
    const { width: windowWidth } = useWindowDimensions();
    const [width, setWidth] = useState(windowWidth);
    const itemWidth = (width - SIDE_PADDING * 2 - GAP * (PAGE_SIZE - 1)) / PAGE_SIZE;

    // Si no hay lecciones para este género, no renderizamos el carrusel
    if (lessons.length === 0) return null;

    const renderItem = ({ item }: { item: LessonDTO }) => (
        <View style={{ width: itemWidth }}>
            <Touchable
                onPress={() => navigation.navigate('LessonScreen', { lessonId: item.id })}
            >
                <Image
                    source={{ uri: item.imageUrl }} // Usamos imageUrl del DTO
                    className="mb-1"
                    style={{ width: itemWidth, height: itemWidth * IMAGE_RATIO, borderRadius: 6 }}
                    resizeMode="cover"
                />
            </Touchable>
            <Text
                className="text-white text-center text-[10px] leading-tight font-roboto"
                numberOfLines={2}
                ellipsizeMode="tail"
            >
                {item.title}
            </Text>
        </View>
    );

    return (
        <View className="my-2" onLayout={(e) => setWidth(e.nativeEvent.layout.width)}>
            <Text className="text-xl font-semibold text-secondary mb-4 text-center w-full">{title}</Text>
            <FlatList
                data={lessons}
                keyExtractor={(item) => item.id}
                renderItem={renderItem}
                horizontal
                showsHorizontalScrollIndicator={false}
                ItemSeparatorComponent={() => <View style={{ width: GAP }} />}
                contentContainerStyle={{ paddingHorizontal: SIDE_PADDING }}
                // Avanza una página entera: las siguientes PAGE_SIZE lecciones ocupan el sitio de las anteriores
                snapToInterval={(itemWidth + GAP) * PAGE_SIZE}
                decelerationRate="fast"
            />
        </View>
    );
}