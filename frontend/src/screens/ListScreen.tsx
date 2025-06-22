import { ScrollView, TouchableOpacity, View, Text, Image } from "react-native";
import AppLayout from "../components/AppLayout";
import ListItem from "../components/ListItem";
import { Circle } from "react-native-svg";
import { CirclePlus } from "lucide-react-native";
import { COLORS } from "../constants/Theme";

type List = {
  id: string;
  title: string;
  items: Item[];
  imageUrl: string; // Added imageUrl property
};

type Item = {
  id: string;
  title: string;
  description: string;
  imageUrl: string;
  info: string; // Additional information to display
};

const list: List = {
  id: '1',
    title: 'Lista de Ejemplo',
    items: [
        {
        id: '1',
        title: 'Lección de guitarra',
        description: 'Aprende los acordes básicos de guitarra.',
        imageUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png',
        info: `American Idiot
        Cancion - Green Day`,
        },
        {
        id: '2',
        title: 'Canción favorita',
        description: 'Disfruta de tu canción favorita.',
        imageUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png',
        info: `Shape of You
        Cancion - Ed Sheeran`,
        },
        {
        id: '3',
        title: 'Última práctica',
        description: 'Revisa tu última práctica de guitarra.',
        imageUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png',
        info: `Stairway to Heaven
        Cancion - Led Zeppelin`,
        },
        {
        id: '4',
        title: 'Nuevo acorde aprendido',
        description: 'Descubre un nuevo acorde de guitarra.',
        imageUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png',
        info: `Wonderwall
        Cancion - Oasis`,
        },
        {
        id: '5',
        title: 'Técnica de pua',
        description: 'Mejora tu técnica de pua.',
        imageUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png',
        info: `Smoke on the Water
        Cancion - Deep Purple`,
        },
    ],
    imageUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png',
}

export default function ListScreen() {
  return (
    <AppLayout>
      <View className = "bg-background flex-1 pt-2">
        <View className="flex-row justify-left items-center mt-6 mb-7">
          <TouchableOpacity>
            <Image
                source={{ uri: 'https://i.pravatar.cc/100' }}
                className="w-14 h-14 rounded-full border"
            />
          </TouchableOpacity>
          <Text className="text-[24px] font-roboto font-bold text-primary text-center ml-3">Listas</Text>
        </View> 
        <ScrollView
            showsVerticalScrollIndicator={false}
            contentContainerStyle={{ paddingBottom: 120 }}
        >
          <View className="items-center mt-5 mb-10">
            <Image
                source={{ uri: list.imageUrl }}
                className="w-24 h-24 rounded-lg mb-2"
                resizeMode="cover"
            />
            <Text className="text-secondary text-base text-center">{list.title}</Text>
          </View>
          <View className="flex-column gap-3 mb-7">
            {list.items.map((item) => {
            return (
              <ListItem
                key={item.id}
                item={item}
                onPress={() => console.log(`Selected item: ${item.title}`)}
              />
            );
            })}
            <TouchableOpacity className="flex-row h-24 bg-secondary rounded-2xl shadow-md p-4 items-center justify-center gap-2">
              <Text className="text-2xl font-semibold text-primary text-center">Añadir a la lista</Text>
              <CirclePlus size={28} color={COLORS.primary} />
            </TouchableOpacity>
          </View>
        </ScrollView>
      </View>
    </AppLayout>
  );
}