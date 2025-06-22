import { CirclePlus } from "lucide-react-native";
import { useState } from "react";
import { TouchableOpacity, View, Text } from "react-native";

const instruments = [
  "Guitarra",
  "Piano",
  "Batería",
  "Bajo",
  "Voz",
  "Saxofón",
  "Violín",
  "Trompeta",
  "Flauta",
  "Acordeón",
];

export default function InstrumentPicker() {
    const [selectedInstruments, setSelectedInstruments] = useState<string[]>([]);
    const toggleInstrument = (instrument: string) => {
    setSelectedInstruments((prev) =>
      prev.includes(instrument)
        ? prev.filter((i) => i !== instrument)
        : [...prev, instrument]
    );
  };
  return (
    <View className="mb-8">
      <View className="flex-row flex-wrap justify-left gap-2 mb-4">
        <Text className="text-base text-secondary font-semibold">
          Escoge al menos un instrumento
        </Text>
      </View>
      <View className="flex-row flex-wrap justify-left gap-2">
        {instruments.map((instrument) => {
          const isSelected = selectedInstruments.includes(instrument);
          return (
            <TouchableOpacity
              key={instrument}
              className={`px-3 py-1 bg-secondary rounded-lg ${
                  isSelected ? "opacity-60" : ""
                }`}
              onPress={() => toggleInstrument(instrument)}
            >
              <Text className="text-primary text-base">{instrument}</Text>
            </TouchableOpacity>
          );
        })}
        <TouchableOpacity className="py-1">
          <CirclePlus size={20} color="white" strokeWidth={1} />
        </TouchableOpacity>
      </View>
    </View>
  );
}