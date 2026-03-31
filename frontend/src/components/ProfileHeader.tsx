import { useNavigation } from "@react-navigation/native";
import { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { TouchableOpacity, View, Image, Text } from "react-native";
import { RootStackParamList } from "../types/RootStackParamList";
import { useEffect, useState } from "react";
import { getStoredUser } from "../local/user.local";
import { UserDTO } from "../types/UserDTO";

export default function ProfileHeader({title} : {title: string}) {

    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

    const [user, setUser] = useState<UserDTO | null>(null);
    
    useEffect(() => {
      const loadUser = async () => {
        const data = await getStoredUser();
        if (data) {
          setUser(data.user);
        }
      };

      loadUser();
    }, []);

    return (
      <View className="flex-row justify-left items-center mt-6 mb-7">
        <TouchableOpacity
          onPress={() => navigation.navigate('ProfileScreen')}
        >
          <Image
            source={{ uri: user?.profileImageUrl }}
            className="w-14 h-14 rounded-full border"
          />
        </TouchableOpacity>
        <Text className="text-[24px] font-roboto font-bold text-primary text-center ml-3">{title}</Text>
      </View> 
    )
}