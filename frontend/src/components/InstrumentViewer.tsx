// components/InstrumentViewer.tsx
import { View, Text } from "react-native";
import EditButton from "./iconbuttons/EditButton";
import { useInstrumentsByIds } from "../hooks/useInstruments";

type Props = {
    ids: (string | number)[]; // Recibe la lista de IDs de cualquier usuario
    label?: string;           // Opcional para cambiar "Toco:" por "Quiere aprender:"
    onAddPress?: () => void;  // Sin él no se muestra el botón de añadir
};

export default function InstrumentViewer({ ids, label = "Toco:", onAddPress }: Props) {
    // IDs -> Objetos (el hook trae de Disco/API los que no estén en RAM)
    const instruments = useInstrumentsByIds(ids);

    return (
        <View className="mb-5">
            <View className="flex-row flex-wrap justify-left gap-2 mb-4">
                <Text className="text-lg text-secondary font-semibold">
                    {label}
                </Text>
            </View>
            <View className="flex-row flex-wrap justify-left gap-2">
                {instruments.map((instrument) => (
                    <View
                        key={instrument.id}
                        className="px-3 py-1 bg-primary rounded-lg border border-secondary/10"
                    >
                        <Text className="text-secondary text-base">{instrument.name}</Text>
                    </View>
                ))}
                {onAddPress && (
                    <EditButton onPress={onAddPress} />
                )}
            </View>
        </View>
    );
}