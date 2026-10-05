import { View, Text, TouchableOpacity } from "react-native";
import { CirclePlus } from "lucide-react-native";
import { COLORS } from "../constants/Theme";
import { useGenresByIds } from "../hooks/useGenres";

type Props = {
    ids: (string | number)[]; // Recibimos solo los IDs
    label: string;
};

export default function GenreViewer({ ids, label }: Props) {
    // IDs -> Objetos (el hook trae de Disco/API los que no estén en RAM)
    const genres = useGenresByIds(ids);

    return (
        <View className="mb-5">
            <Text className="text-lg text-secondary font-semibold mb-4">
                {label}
            </Text>
            <View className="flex-row flex-wrap gap-2">
                {genres.map((genre) => (
                    <View
                        key={genre.id}
                        className="px-3 py-1 bg-primary rounded-lg border border-secondary/20"
                    >
                        <Text className="text-secondary text-base">{genre.title}</Text>
                    </View>
                ))}
                <TouchableOpacity className="py-1">
                    <CirclePlus size={20} color={COLORS.primary} strokeWidth={1} />
                </TouchableOpacity>
            </View>
        </View>
    );
}