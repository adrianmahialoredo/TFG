import { render, screen, fireEvent } from '@testing-library/react-native';
import SearchResults from '@/components/SearchResults';
import { ItemType } from '@/types';
import type { SearchSection } from '@/hooks/useCatalogSearch';

jest.setTimeout(30000);

const mockNavigate = jest.fn();
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => ({ navigate: mockNavigate }),
}));

// El modal real necesita las Stores y el repositorio de listas: aquí basta con saber qué item recibe
jest.mock('@/components/AddToListModal', () => {
  const { Text } = require('react-native');
  return ({ item, onClose }: any) =>
    item ? <Text onPress={onClose}>{`Modal: ${item.title}`}</Text> : null;
});

const sections: SearchSection[] = [
  {
    field: 'title',
    items: [
      { id: 's1', title: 'Wonderwall', imageUrl: 'http://img/s1.png', type: ItemType.SONG, artist: 'Oasis' },
      { id: 'l1', title: 'Acordes', imageUrl: 'http://img/l1.png', type: ItemType.LESSON },
    ],
  },
  {
    field: 'genre',
    items: [{ id: 'g1', title: 'Rock', imageUrl: 'http://img/g1.png', type: ItemType.GENRE }],
  },
] as SearchSection[];

describe('SearchResults', () => {
  beforeEach(() => mockNavigate.mockClear());

  it('sin secciones muestra el texto de vacío', async () => {
    await render(<SearchResults sections={[]} emptyText="No hay resultados" />);
    expect(screen.getByText('No hay resultados')).toBeTruthy();
  });

  it('muestra un título por sección y sus items', async () => {
    await render(<SearchResults sections={sections} emptyText="No hay resultados" />);
    expect(screen.queryByText('No hay resultados')).toBeNull();
    expect(screen.getByText('Por título')).toBeTruthy();
    expect(screen.getByText('Por género')).toBeTruthy();
    expect(screen.getByText('Wonderwall\nOasis · Canción')).toBeTruthy();
    expect(screen.getByText('Acordes\nLección')).toBeTruthy();
    expect(screen.getByText('Rock · Género')).toBeTruthy();
  });

  it('al pulsar un item navega a su pantalla de detalle', async () => {
    await render(<SearchResults sections={sections} emptyText="" />);
    await fireEvent.press(screen.getByText('Wonderwall\nOasis · Canción'));
    expect(mockNavigate).toHaveBeenCalledWith('SongScreen', { songId: 's1' });
    await fireEvent.press(screen.getByText('Rock · Género'));
    expect(mockNavigate).toHaveBeenCalledWith('GenreMainScreen', { genreId: 'g1' });
  });

  it('el botón + abre el modal de listas con ese item, sin navegar', async () => {
    await render(<SearchResults sections={sections} emptyText="" />);
    expect(screen.queryByText(/^Modal:/)).toBeNull();
    await fireEvent.press(screen.getByLabelText('Añadir Wonderwall a una lista'));
    expect(screen.getByText('Modal: Wonderwall')).toBeTruthy();
    expect(mockNavigate).not.toHaveBeenCalled();

    await fireEvent.press(screen.getByText('Modal: Wonderwall'));
    expect(screen.queryByText(/^Modal:/)).toBeNull();
  });
});
