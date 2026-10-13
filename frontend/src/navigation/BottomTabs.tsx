 // src/navigation/BottomTabs.tsx
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import HomeScreen from '../screens/HomeScreen';
import { Home, BookOpen, Music, Disc3 } from 'lucide-react-native';
import { View } from 'react-native';
import LearnScreen from '../screens/LearnScreen';
import GenresScreen from '../screens/GenresScreen';
import SongsScreen from '../screens/SongsScreen';
import SongScreen from '../screens/SongScreen';
import LessonScreen from '../screens/LessonScreen';
import GenreMainScreen from '../screens/GenreMainScreen';
import ProfileScreen from '../screens/ProfileScreen';
import MetronomeScreen from '../screens/MetronomeScreen';
import InstrumentGenreScreen from '../screens/InstrumentGenreScreen';
import InstrumentGenreLessonScreen from '../screens/InstrumentGenreLessonScreen';
import ListScreen from '../screens/ListScreen';
import AddItemScreen from '../screens/AddItemScreen';
import AnimatedTabBarButton from '../components/AnimatedTabBarButton';


const Tab = createBottomTabNavigator();

const HIDDEN_TAB_OPTIONS = {
  tabBarButton: () => null,
  tabBarItemStyle: { display: 'none' as const },
};

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
        // Pequeño "pop" del icono y el texto al pulsar (las pantallas ocultas lo sustituyen por null)
        tabBarButton: (props) => <AnimatedTabBarButton {...props} />,
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
      <Tab.Screen name="Aprender" component={LearnScreen} />
      <Tab.Screen name="Canciones" component={SongsScreen} />
      <Tab.Screen name="Generos" component={GenresScreen} />

      {/* Pantallas secundarias ocultas del menú; mantienen visible la tab bar */}
      {/* Pantallas de detalle: los tabs no se desmontan, así que sin `getId` la misma instancia
          se reutiliza para otro item y arrastra su estado (pestaña, selección…) del anterior.
          Con `getId`, cambiar de item crea una ruta nueva y la pantalla se monta desde cero. */}
      <Tab.Screen name="SongScreen" component={SongScreen} options={HIDDEN_TAB_OPTIONS}
        getId={({ params }) => (params as { songId?: string } | undefined)?.songId?.toString()} />
      <Tab.Screen name="LessonScreen" component={LessonScreen} options={HIDDEN_TAB_OPTIONS}
        getId={({ params }) => (params as { lessonId?: string } | undefined)?.lessonId?.toString()} />
      <Tab.Screen name="GenreMainScreen" component={GenreMainScreen} options={HIDDEN_TAB_OPTIONS}
        getId={({ params }) => (params as { genreId?: string } | undefined)?.genreId?.toString()} />
      <Tab.Screen name="ProfileScreen" component={ProfileScreen} options={HIDDEN_TAB_OPTIONS} />
      <Tab.Screen name="MetronomeScreen" component={MetronomeScreen} options={HIDDEN_TAB_OPTIONS} />
      <Tab.Screen name="InstrumentGenreScreen" component={InstrumentGenreScreen} options={HIDDEN_TAB_OPTIONS}
        getId={({ params }) => (params as { instrumentGenreId?: string } | undefined)?.instrumentGenreId?.toString()} />
      <Tab.Screen name="InstrumentGenreLesson" component={InstrumentGenreLessonScreen} options={HIDDEN_TAB_OPTIONS}
        getId={({ params }) => (params as { instrumentGenreId?: string } | undefined)?.instrumentGenreId?.toString()} />
      <Tab.Screen name="ListScreen" component={ListScreen} options={HIDDEN_TAB_OPTIONS}
        getId={({ params }) => (params as { itemListId?: string } | undefined)?.itemListId?.toString()} />
      <Tab.Screen name="AddItemScreen" component={AddItemScreen} options={HIDDEN_TAB_OPTIONS}
        getId={({ params }) => (params as { itemListId?: string } | undefined)?.itemListId?.toString()} />
    </Tab.Navigator>
  );
}
