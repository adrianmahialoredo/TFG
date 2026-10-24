import { render, screen, fireEvent } from '@testing-library/react-native';
import GenreViewer from '@/components/GenreViewer';
import { useGenresByIds } from '@/hooks/useGenres';

jest.setTimeout(30000);

jest.mock('@/hooks/useGenres', () => ({ useGenresByIds: jest.fn() }));
const mockedUseGenresByIds = useGenresByIds as jest.Mock;

describe('GenreViewer', () => {
  beforeEach(() =>
    mockedUseGenresByIds.mockReturnValue([
      { id: '1', title: 'Rock' },
      { id: '2', title: 'Blues' },
    ])
  );

  it('resuelve los ids con el hook y muestra la etiqueta y los géneros', async () => {
    await render(<GenreViewer ids={[1, '2']} label="Géneros favoritos" />);
    expect(mockedUseGenresByIds).toHaveBeenCalledWith([1, '2']);
    expect(screen.getByText('Géneros favoritos')).toBeTruthy();
    expect(screen.getByText('Rock')).toBeTruthy();
    expect(screen.getByText('Blues')).toBeTruthy();
  });

  it('sin onAddPress no muestra el botón de editar', async () => {
    await render(<GenreViewer ids={[]} label="Géneros" />);
    expect(screen.queryByRole('button', { name: 'Editar' })).toBeNull();
  });

  it('con onAddPress muestra el botón de editar y lo llama', async () => {
    const onAddPress = jest.fn();
    await render(<GenreViewer ids={['1']} label="Géneros" onAddPress={onAddPress} />);
    await fireEvent.press(screen.getByRole('button', { name: 'Editar' }));
    expect(onAddPress).toHaveBeenCalledTimes(1);
  });
});
