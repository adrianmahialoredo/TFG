import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import AddItemScreen from '../../screens/AddItemScreen';
import { ItemType } from '../../types';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useRoute: () => ({ params: { itemListId: 'list1' } }),
}));

// El mock oficial de reanimated falla al cargar react-native-worklets en Jest: stub mínimo
jest.mock('react-native-reanimated', () => {
  const { View } = require('react-native');
  const chain: any = { duration: () => chain };
  return {
    __esModule: true,
    default: { View },
    FadeOut: chain,
    LinearTransition: {},
  };
});

let mockState: any;
const mockUseAddItemScreen = jest.fn();
jest.mock('../../hooks/useAddItemScreen', () => ({
  useAddItemScreen: (id: string) => {
    mockUseAddItemScreen(id);
    return mockState;
  },
}));

jest.mock('../../components/ProfileHeader', () => {
  const { Text } = require('react-native');
  return ({ title }: any) => <Text>{title}</Text>;
});
jest.mock('../../components/AddListItem', () => {
  const { Text } = require('react-native');
  return ({ item, added, inverted, onPress }: any) => (
    <Text onPress={onPress}>{`${item.title} añadido=${added} invertido=${inverted}`}</Text>
  );
});

const items = [
  { id: 'a', title: 'Wonderwall', imageUrl: '', type: ItemType.SONG, artist: 'Oasis' },
  { id: 'b', title: 'Rock', imageUrl: '', type: ItemType.GENRE },
];

describe('AddItemScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      items,
      query: '',
      setQuery: jest.fn(),
      loading: false,
      searching: false,
      error: null,
      addItem: jest.fn(),
      isAdded: (id: string) => id === 'a',
    };
  });

  it('muestra las recomendaciones de la lista de la ruta', async () => {
    await render(<AddItemScreen />);
    expect(mockUseAddItemScreen).toHaveBeenCalledWith('list1');
    expect(screen.getByText('Añadir a lista')).toBeTruthy();
    expect(screen.getByText('Para ti')).toBeTruthy();
    expect(screen.getByText('Wonderwall añadido=true invertido=false')).toBeTruthy();
    expect(screen.getByText('Rock añadido=false invertido=true')).toBeTruthy();
  });

  it('añade el item pulsado', async () => {
    await render(<AddItemScreen />);
    await fireEvent.press(screen.getByText(/^Rock/));
    expect(mockState.addItem).toHaveBeenCalledWith('b');
  });

  it('pasa el texto del buscador al hook', async () => {
    await render(<AddItemScreen />);
    await fireEvent.changeText(screen.getByPlaceholderText('¿Que quieres tocar?'), 'oasis');
    expect(mockState.setQuery).toHaveBeenCalledWith('oasis');
  });

  it('con búsqueda muestra "Resultados"', async () => {
    mockState.query = 'won';
    await render(<AddItemScreen />);
    expect(screen.getByText('Resultados')).toBeTruthy();
  });

  it('no muestra items mientras carga o busca', async () => {
    mockState.searching = true;
    await render(<AddItemScreen />);
    expect(screen.queryByText(/Wonderwall/)).toBeNull();
  });

  it('muestra el error', async () => {
    mockState.error = 'No se pudo cargar';
    await render(<AddItemScreen />);
    expect(screen.getByText('No se pudo cargar')).toBeTruthy();
    expect(screen.queryByText(/Wonderwall/)).toBeNull();
  });

  it('sin recomendaciones muestra su mensaje', async () => {
    mockState.items = [];
    await render(<AddItemScreen />);
    expect(screen.getByText('No hay recomendaciones disponibles')).toBeTruthy();
  });

  it('sin resultados de búsqueda muestra su mensaje', async () => {
    mockState.items = [];
    mockState.query = 'zzz';
    await render(<AddItemScreen />);
    expect(screen.getByText('No se encontraron items para tu búsqueda')).toBeTruthy();
  });
});
