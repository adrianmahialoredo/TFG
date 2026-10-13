import { View, Text } from "react-native";
import EditButton from "./iconbuttons/EditButton";
import { useGenresByIds } from "../hooks/useGenres";

type Props = {
    ids: (string | number)[]; // Recibimos solo los IDs
    label: string;
    onAddPress?: () => void; // Sin él no se muestra el botón de añadir
};

export default function GenreViewer({ ids, label, onAddPress }: Props) {
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
                {onAddPress && (
                    <EditButton onPress={onAddPress} />
                )}
            </View>
        </View>
    );
}