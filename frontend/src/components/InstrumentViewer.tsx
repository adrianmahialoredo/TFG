// components/InstrumentViewer.tsx
import { View, Text, TouchableOpacity } from "react-native";
import { CirclePlus } from "lucide-react-native";
import { COLORS } from "../constants/Theme";
import { useInstrumentsByIds } from "../hooks/useInstruments";

type Props = {
    ids: (string | number)[]; // Recibe la lista de IDs de cualquier usuario
    label?: string;           // Opcional para cambiar "Toco:" por "Quiere aprender:"
};

export default function InstrumentViewer({ ids, label = "Toco:" }: Props) {
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
                {/* El Plus solo debería estar aquí si es modo edición, pero lo mantenemos por estética */}
                <TouchableOpacity className="py-1">
                    <CirclePlus size={20} color={COLORS.primary} strokeWidth={1} />
                </TouchableOpacity>
            </View>
        </View>
    );
}