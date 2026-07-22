import { useNavigation } from "@react-navigation/native";
import { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { TouchableOpacity, View, Image, Text } from "react-native";
import { RootStackParamList } from "../types";
import {useAuth} from "../hooks/useAuth";

export default function ProfileHeader({ title }: { title: string }) {
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

    // Como ya hemos arreglado el Provider, useAuth() ya devuelve el usuario real
    const { user } = useAuth();

    return (
        <View className="flex-row items-center mt-6 mb-7">
            <TouchableOpacity onPress={() => navigation.navigate('ProfileScreen')}>
                <Image
                    source={{ uri: user?.profileImageUrl || 'https://via.placeholder.com/150' }}
                    className="w-14 h-14 rounded-full border-2 border-primary/20"
                />
            </TouchableOpacity>
            <Text className="text-[24px] font-bold text-primary ml-3 flex-1">{title}</Text>
        </View>
    );
}