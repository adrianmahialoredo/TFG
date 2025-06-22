// src/navigation/BottomTabs.tsx
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import HomeScreen from '../screens/HomeScreen';
import { Home, BookOpen, Music, Disc3 } from 'lucide-react-native';
import { View } from 'react-native';
import SongScreen from '../screens/SongScreen';
import LearnScreen from '../screens/LearnScreen';
import { COLORS } from '../constants/Theme';
import GenresScreen from '../screens/GenresScreen';
import SongsScreen from '../screens/SongsScreen';
import GenreMainScreen from '../screens/GenreMainScreen';
import RegisterScreen from '../screens/RegisterScreen';
import LoginScreen from '../screens/LoginScreen';
import ProfileScreen from '../screens/ProfileScreen';
import ListScreen from '../screens/ListScreen';
import AddItemScreen from '../screens/AddItemScreen';

const Tab = createBottomTabNavigator();

export default function BottomTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarShowLabel: true,
        tabBarStyle: {
          position: 'absolute',
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          elevation: 0,
          borderTopWidth: 0,
          height: 70,
        },
        tabBarActiveTintColor: '#C1853C',
        tabBarIcon: ({ focused, color, size }) => {
          const iconColor = focused ? '#C1853C' : '#888';

          switch (route.name) {
            case 'Inicio':
              return <Home color={iconColor} size={24} />;
            case 'Aprender':
              return <BookOpen color={iconColor} size={24} />;
            case 'Canciones':
              return <Music color={iconColor} size={24} />;
            case 'Generos':
              return <Disc3 color={iconColor} size={24} />;
            default:
              return <View />;
          }
        },
      })}
    >
      <Tab.Screen name="Inicio" component={HomeScreen} />
      <Tab.Screen name="Aprender" component={GenreMainScreen} />
      <Tab.Screen name="Canciones" component={AddItemScreen} />
      <Tab.Screen name="Generos" component={ProfileScreen} />
    </Tab.Navigator>
  );
}
