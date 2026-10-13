import { Image, View, Text, FlatList, TouchableOpacity } from 'react-native';
import React from 'react';
import { useNavigation, NavigationProp } from "@react-navigation/native";
import {LessonDTO, RootStackParamList} from "../../types";
import {useRecommendedLessonsByGenre} from "../../hooks/useRecommendedLessonsByGenre";

type CarouselProps = {
    title: string;
    genreId: string; // Recibe el ID, no los datos
};

export default function GenreLessonsCarousel({ title, genreId }: CarouselProps) {
    // Lecciones recomendadas del género (endpoint de recomendaciones)
    const { lessons } = useRecommendedLessonsByGenre(genreId);
    const navigation = useNavigation<NavigationProp<RootStackParamList>>();

    // Si no hay lecciones para este género, no renderizamos el carrusel
    if (lessons.length === 0) return null;

    const renderItem = ({ item }: { item: LessonDTO }) => (
        <View className="w-[51px]">
            <TouchableOpacity
                activeOpacity={0.7}
                onPress={() => navigation.navigate('LessonScreen', { lessonId: item.id })}
            >
                <Image
                    source={{ uri: item.imageUrl }} // Usamos imageUrl del DTO
                    className="w-[51px] h-[93px] mb-1"
                    style={{ borderRadius: 6 }}
                    resizeMode="cover"
                />
            </TouchableOpacity>
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
        <View className="my-2">
            <Text className="text-xl font-semibold text-secondary mb-4 text-center w-full">{title}</Text>
            <FlatList
                data={lessons}
                keyExtractor={(item) => item.id}
                renderItem={renderItem}
                horizontal
                showsHorizontalScrollIndicator={false}
                ItemSeparatorComponent={() => <View style={{ width: 18 }} />}
                contentContainerStyle={{ paddingHorizontal: 16 }} // Mejor espaciado lateral
                snapToAlignment="start"
                decelerationRate="fast"
            />
        </View>
    );
}