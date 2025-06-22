import { CirclePlus } from "lucide-react-native";
import { TouchableOpacity, View, Text } from "react-native";
import { COLORS } from "../constants/Theme";

const instruments = [
  "Guitarra",
  "Piano",
  "Batería",
];

export default function InstrumentViewer() {
  return (
    <View className="mb-5">
      <View className="flex-row flex-wrap justify-left gap-2 mb-4">
        <Text className="text-lg text-secondary font-semibold">
          Quiero aprender:
        </Text>
      </View>
      <View className="flex-row flex-wrap justify-left gap-2">
        {instruments.map((instrument) => {
          return (
            <TouchableOpacity
              key={instrument}
              className= "px-3 py-1 bg-primary rounded-lg"
              disabled= {true} // Disable interaction for viewing purposes
            >
              <Text className="text-secondary text-base">{instrument}</Text>
            </TouchableOpacity>
          );
        })}
        <TouchableOpacity className="py-1">
          <CirclePlus size={20} color={COLORS.primary} strokeWidth={1} />
        </TouchableOpacity>
      </View>
    </View>
  );
}