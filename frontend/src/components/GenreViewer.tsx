import { CirclePlus } from "lucide-react-native";
import { TouchableOpacity, View, Text } from "react-native";
import { COLORS } from "../constants/Theme";
import { UserDTO } from "../types/UserDTO";
import { useEffect, useState } from "react";
import { getStoredGenresByIdList, getStoredUser } from "../services/storageService";
import { GenreDTO } from "../types/GenreDTO";

export default function GenreViewer() {

  const [user, setUser] = useState<UserDTO | null>(null);
  const [genres, setGenres] = useState<GenreDTO[]>([]);

  /* Load user data from storage when the component mounts */
  useEffect(() => {
      const loadUser = async () => {
        const data = await getStoredUser();
        if (data) {
          setUser(data.user);
        }
      };
  
      loadUser();
    }, []);

  /* Load genres based on user's favourite genres when the user data changes */
  useEffect(() => {
    const loadGenres = async () => {
      if (user?.favouriteGenresIds) {
        const fetchedGenres = await getStoredGenresByIdList(user.favouriteGenresIds);
        setGenres(fetchedGenres);
      } else {
        setGenres([]);
      }
    };
    loadGenres();
  }, [user]);

  return (
    <View className="mb-5">
      <View className="flex-row flex-wrap justify-left gap-2 mb-4">
        <Text className="text-lg text-secondary font-semibold">
          Me gustan:
        </Text>
      </View>
      <View className="flex-row flex-wrap justify-left gap-2">
        {genres.map((genre) => {
          return (
            <TouchableOpacity
              key={genre.id}
              className= "px-3 py-1 bg-primary rounded-lg"
              disabled= {true} // Disable interaction for viewing purposes
            >
              <Text className="text-secondary text-base">{genre.name}</Text>
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