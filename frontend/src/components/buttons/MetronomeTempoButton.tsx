import {TouchableOpacity, Text} from "react-native";

export default function MetronomeTempoButton({ title, onPress }: { title: string, onPress: () => void }) {
    return (
        <TouchableOpacity
            className="border-primary border rounded-2xl flex-1 justify-center h-7"
            onPress={onPress}
            activeOpacity={1}
            delayPressIn={0.5}
        >
            <Text className="text-sm text-center text-primary">{title}</Text>
        </TouchableOpacity>
    );
}