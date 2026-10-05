import { Image, View, Text, FlatList, TouchableOpacity } from 'react-native';
import React from 'react';
import {LessonDTO} from "../../types";
import {useLessonsByGenre} from "../../hooks/useLessonsByGenre";

type CarouselProps = {
    title: string;
    genreId: string; // Recibe el ID, no los datos
};

export default function GenreLessonsCarousel({ title, genreId }: CarouselProps) {
    // Conexión a la RAM
    const { lessons } = useLessonsByGenre(genreId);

    // Si no hay lecciones para este género, no renderizamos el carrusel
    if (lessons.length === 0) return null;

    const renderItem = ({ item }: { item: LessonDTO }) => (
        <View className="w-[51px]">
            <TouchableOpacity activeOpacity={0.7}>
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