import { useNavigation } from "@react-navigation/native";
import { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { View, Text } from "react-native";
import Touchable from "./Touchable";
import { RootStackParamList } from "../types";
import { useAuth } from "../context/AuthContext";
import UserAvatar from "./UserAvatar";

type ProfileHeaderProps = {
    title: string;
    compact?: boolean;
};

export default function ProfileHeader({ title, compact = false }: ProfileHeaderProps) {
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

    // Como ya hemos arreglado el Provider, useAuth() ya devuelve el usuario real
    const { user } = useAuth();

    if (compact) {
        return (
            <View className="justify-center mr-4">
                <Touchable onPress={() => navigation.navigate('ProfileScreen')}>
                    <UserAvatar user={user} size={48} className="border border-primary/30" />
                </Touchable>
            </View>
        );
    }

    return (
        <View className="flex-row items-center mt-2 mb-7">
            <Touchable onPress={() => navigation.navigate('ProfileScreen')}>
                <UserAvatar user={user} size={56} className="border-2 border-primary/20" />
            </Touchable>
            <Text className="text-[24px] font-bold text-primary ml-3 flex-1">{title}</Text>
        </View>
    );
}