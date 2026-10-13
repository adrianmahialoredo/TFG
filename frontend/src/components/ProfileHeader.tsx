import { useNavigation } from "@react-navigation/native";
import { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { TouchableOpacity, View, Text } from "react-native";
import { RootStackParamList } from "../types";
import {useAuth} from "../hooks/useAuth";
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
                <TouchableOpacity onPress={() => navigation.navigate('ProfileScreen')}>
                    <UserAvatar user={user} size={48} className="border border-primary/30" />
                </TouchableOpacity>
            </View>
        );
    }

    return (
        <View className="flex-row items-center mt-6 mb-7">
            <TouchableOpacity onPress={() => navigation.navigate('ProfileScreen')}>
                <UserAvatar user={user} size={56} className="border-2 border-primary/20" />
            </TouchableOpacity>
            <Text className="text-[24px] font-bold text-primary ml-3 flex-1">{title}</Text>
        </View>
    );
}