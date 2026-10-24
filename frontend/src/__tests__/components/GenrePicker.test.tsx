import { render, screen, fireEvent } from '@testing-library/react-native';
import GenrePicker from '@/components/GenrePicker';
import { useGenres } from '@/hooks/useGenres';

jest.setTimeout(30000);

jest.mock('@/hooks/useGenres', () => ({ useGenres: jest.fn() }));
const mockedUseGenres = useGenres as jest.Mock;

const genres = [
  { id: 1, title: 'Rock' },
  { id: 2, title: 'Jazz' },
  { id: 3, title: 'Pop' },
];

describe('GenrePicker', () => {
  beforeEach(() => mockedUseGenres.mockReturnValue({ genres, loading: false }));

  it('mientras carga muestra un spinner', async () => {
    mockedUseGenres.mockReturnValue({ genres: [], loading: true });
    await render(<GenrePicker />);
    expect(screen.container.queryAll((n) => n.type === 'ActivityIndicator')).toHaveLength(1);
    expect(screen.queryByText('¿Cuáles son tus géneros favoritos?')).toBeNull();
  });

  it('muestra la pregunta y un chip por género', async () => {
    await render(<GenrePicker />);
    expect(screen.getByText('¿Cuáles son tus géneros favoritos?')).toBeTruthy();
    expect(screen.getAllByRole('checkbox')).toHaveLength(3);
  });

  it('marca la selección inicial', async () => {
    await render(<GenrePicker initialSelection={['2']} />);
    expect(screen.getByRole('checkbox', { name: 'Jazz' })).toBeChecked();
    expect(screen.getByRole('checkbox', { name: 'Rock' })).not.toBeChecked();
  });

  it('añade y quita géneros avisando al padre en cada toque', async () => {
    const onSelectionChange = jest.fn();
    await render(<GenrePicker initialSelection={['2']} onSelectionChange={onSelectionChange} />);
    await fireEvent.press(screen.getByText('Rock'));
    expect(onSelectionChange).toHaveBeenLastCalledWith(['2', '1']);
    expect(screen.getByRole('checkbox', { name: 'Rock' })).toBeChecked();
    await fireEvent.press(screen.getByText('Jazz'));
    expect(onSelectionChange).toHaveBeenLastCalledWith(['1']);
    expect(screen.getByRole('checkbox', { name: 'Jazz' })).not.toBeChecked();
  });

  it('funciona sin onSelectionChange', async () => {
    await render(<GenrePicker />);
    await fireEvent.press(screen.getByText('Pop'));
    expect(screen.getByRole('checkbox', { name: 'Pop' })).toBeChecked();
  });
});
