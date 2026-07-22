import { View, Text, TouchableOpacity } from "react-native";
import { CirclePlus } from "lucide-react-native";
import { COLORS } from "../constants/Theme";
import { useGenreEntitiesStore } from "../stores/entities";

type Props = {
    ids: (string | number)[]; // Recibimos solo los IDs
    label: string;
};

export default function GenreViewer({ ids, label }: Props) {
    // Accedemos a la RAM directamente (O(1) access)
    const genresMap = useGenreEntitiesStore(state => state.genres);

    // Mapeamos los IDs a los objetos reales que ya están en la Store
    const genres = ids.map(id => genresMap[id]).filter(Boolean);

    if (genres.length === 0) return null;

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
                        <Text className="text-secondary text-base">{genre.name}</Text>
                    </View>
                ))}
                {/* Solo mostramos el Plus si es nuestro perfil (opcional) */}
            </View>
        </View>
    );
}