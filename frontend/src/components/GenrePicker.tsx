import { CirclePlus } from "lucide-react-native";
import { useState } from "react";
import { TouchableOpacity, View, Text } from "react-native";

const genres = [
  "Rock",
  "Pop",
  "Jazz",
  "Blues",
  "Reggae",
  "Metal",
  "Hip-Hop",
  "Country",
  "Folk",
  "Classical",
  "Electronic",
  "Latin",
];

export default function GenrePicker() {
    const [selectedGenres, setSelectedGenres] = useState<string[]>([]);
    const toggleInstrument = (instrument: string) => {
    setSelectedGenres((prev) =>
      prev.includes(instrument)
        ? prev.filter((i) => i !== instrument)
        : [...prev, instrument]
    );
  };
  return (
    <View className="mb-5">
      <View className="flex-row flex-wrap justify-left gap-2 mb-4">
        <Text className="text-base text-secondary font-semibold">
          Escoge al menos un género
        </Text>
      </View>
      <View className="flex-row flex-wrap justify-left gap-2">
        {genres.map((genre) => {
          const isSelected = selectedGenres.includes(genre);
          return (
            <TouchableOpacity
              key={genre}
              className={`px-3 py-1 bg-secondary rounded-lg ${
                  isSelected ? "opacity-60" : ""
              }`}
              onPress={() => toggleInstrument(genre)}
            >
              <Text className="text-primary text-base">{genre}</Text>
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