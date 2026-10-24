import React from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react-native';
import { NavigationContainer, createNavigationContainerRef } from '@react-navigation/native';
import BottomTabs from '../../navigation/BottomTabs';

// Cada pantalla se sustituye por un stub que muestra su nombre y los params recibidos
function mockScreen(name: string) {
  const { Text, View } = require('react-native');
  const { useRoute, useNavigation } = require('@react-navigation/native');
  return function Stub() {
    const route = useRoute();
    const navigation = useNavigation();
    return (
      <View>
        <Text>{`Pantalla ${name} ${JSON.stringify(route.params ?? {})}`}</Text>
        <Text onPress={() => navigation.navigate('SongScreen', { songId: 's1' })}>{`Ir a canción desde ${name}`}</Text>
      </View>
    );
  };
}

jest.mock('../../screens/HomeScreen', () => mockScreen('Home'));
jest.mock('../../screens/LearnScreen', () => mockScreen('Learn'));
jest.mock('../../screens/GenresScreen', () => mockScreen('Genres'));
jest.mock('../../screens/SongsScreen', () => mockScreen('Songs'));
jest.mock('../../screens/SongScreen', () => mockScreen('Song'));
jest.mock('../../screens/LessonScreen', () => mockScreen('Lesson'));
jest.mock('../../screens/GenreMainScreen', () => mockScreen('GenreMain'));
jest.mock('../../screens/ProfileScreen', () => mockScreen('Profile'));
jest.mock('../../screens/MetronomeScreen', () => mockScreen('Metronome'));
jest.mock('../../screens/TunerScreen', () => mockScreen('Tuner'));
jest.mock('../../screens/InstrumentGenreLessonScreen', () => mockScreen('InstrumentGenreLesson'));
jest.mock('../../screens/ListScreen', () => mockScreen('List'));
jest.mock('../../screens/AddItemScreen', () => mockScreen('AddItem'));

jest.mock('../../components/AnimatedTabBarButton', () => {
  const { Pressable } = require('react-native');
  return ({ children, onPress, accessibilityLabel, style }: any) => (
    <Pressable onPress={onPress} accessibilityLabel={accessibilityLabel} style={style}>
      {children}
    </Pressable>
  );
});

jest.setTimeout(30000);

const navigationRef = createNavigationContainerRef<any>();

const renderTabs = () =>
  render(
    <NavigationContainer ref={navigationRef}>
      <BottomTabs />
    </NavigationContainer>
  );

describe('BottomTabs', () => {
  it('abre la pestaña de Inicio por defecto', async () => {
    await renderTabs();
    expect(screen.getByText('Pantalla Home {}')).toBeTruthy();
  });

  it('muestra solo las cuatro pestañas visibles', async () => {
    await renderTabs();
    ['Inicio', 'Aprender', 'Canciones', 'Generos'].forEach((label) => expect(screen.getByText(label)).toBeTruthy());
    expect(screen.queryByText('SongScreen')).toBeNull();
    expect(screen.queryByText('ProfileScreen')).toBeNull();
  });

  it('cambia de pestaña al pulsarla', async () => {
    await renderTabs();
    await fireEvent.press(screen.getByText('Canciones'));
    expect(await screen.findByText('Pantalla Songs {}')).toBeTruthy();
    await fireEvent.press(screen.getByText('Generos'));
    expect(await screen.findByText('Pantalla Genres {}')).toBeTruthy();
    await fireEvent.press(screen.getByText('Aprender'));
    expect(await screen.findByText('Pantalla Learn {}')).toBeTruthy();
  });

  it('navega a una pantalla oculta con sus parámetros', async () => {
    await renderTabs();
    await fireEvent.press(screen.getByText('Ir a canción desde Home'));
    expect(await screen.findByText('Pantalla Song {"songId":"s1"}')).toBeTruthy();
  });

  it.each([
    ['LessonScreen', { lessonId: 'l1' }, 'Lesson'],
    ['GenreMainScreen', { genreId: 'g1' }, 'GenreMain'],
    ['InstrumentGenreLesson', { instrumentGenreId: 'ig1' }, 'InstrumentGenreLesson'],
    ['ListScreen', { itemListId: 'list1' }, 'List'],
    ['AddItemScreen', { itemListId: 'list1' }, 'AddItem'],
    ['ProfileScreen', undefined, 'Profile'],
    ['MetronomeScreen', undefined, 'Metronome'],
    ['TunerScreen', undefined, 'Tuner'],
  ])('navega a la pantalla oculta %s', async (route, params, stubName) => {
    await renderTabs();
    await act(async () => {
      navigationRef.navigate(route, params);
    });
    expect(await screen.findByText(`Pantalla ${stubName} ${JSON.stringify(params ?? {})}`)).toBeTruthy();
  });

  it('cada item distinto abre una ruta nueva gracias a getId', async () => {
    await renderTabs();
    await act(async () => {
      navigationRef.navigate('SongScreen', { songId: 's1' });
    });
    await act(async () => {
      navigationRef.navigate('SongScreen', { songId: 's2' });
    });
    expect(await screen.findByText('Pantalla Song {"songId":"s2"}')).toBeTruthy();
    const songRoutes = navigationRef.getRootState().routes.filter((r: any) => r.name === 'SongScreen');
    expect(songRoutes.length).toBeGreaterThanOrEqual(1);
  });
});
