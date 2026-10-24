import { render, screen, fireEvent } from '@testing-library/react-native';
import FavoritesPickerModal from '@/components/FavoritesPickerModal';
import { useGenres } from '@/hooks/useGenres';
import { useInstruments } from '@/hooks/useInstruments';

jest.setTimeout(30000);

jest.mock('@/hooks/useGenres', () => ({ useGenres: jest.fn() }));
jest.mock('@/hooks/useInstruments', () => ({ useInstruments: jest.fn() }));

const genres = [
  { id: 1, title: 'Rock' },
  { id: 2, title: 'Jazz' },
  { id: 3, title: 'Pop' },
];
const instruments = [
  { id: 10, name: 'Guitarra' },
  { id: 20, name: 'Piano' },
];

const makeProps = () => ({ saving: false, onSave: jest.fn(), onClose: jest.fn() });

describe('FavoritesPickerModal', () => {
  beforeEach(() => {
    (useGenres as jest.Mock).mockReturnValue({ genres, loading: false });
    (useInstruments as jest.Mock).mockReturnValue({ instruments, loading: false });
  });

  it('en modo géneros muestra el selector de géneros con la selección inicial', async () => {
    await render(<FavoritesPickerModal kind="genres" initialIds={[1, 2]} {...makeProps()} />);
    expect(screen.getByText('¿Cuáles son tus géneros favoritos?')).toBeTruthy();
    expect(screen.getByRole('checkbox', { name: 'Rock' })).toBeChecked();
    expect(screen.getByRole('checkbox', { name: 'Jazz' })).toBeChecked();
    expect(screen.queryByText(/Elige al menos/)).toBeNull();
  });

  it('guarda los ids seleccionados como strings', async () => {
    const props = makeProps();
    await render(<FavoritesPickerModal kind="genres" initialIds={[1, 2]} {...props} />);
    await fireEvent.press(screen.getByText('Pop'));
    await fireEvent.press(screen.getByText('Guardar'));
    expect(props.onSave).toHaveBeenCalledWith(['1', '2', '3']);
  });

  it('por debajo del mínimo avisa y no deja guardar', async () => {
    const props = makeProps();
    await render(<FavoritesPickerModal kind="genres" initialIds={[1, 2]} {...props} />);
    await fireEvent.press(screen.getByText('Rock'));
    expect(screen.getByText('Elige al menos 2 géneros')).toBeTruthy();
    await fireEvent.press(screen.getByText('Guardar'));
    expect(props.onSave).not.toHaveBeenCalled();
  });

  it('en modo instrumentos usa el selector de instrumentos y su mensaje', async () => {
    await render(<FavoritesPickerModal kind="instruments" initialIds={['10']} {...makeProps()} />);
    expect(screen.getByText('¿Qué instrumentos te gustaría aprender?')).toBeTruthy();
    expect(screen.getByText('Elige al menos 2 instrumentos')).toBeTruthy();
  });

  it('mientras guarda muestra "Guardando..." y desactiva los botones', async () => {
    const props = { ...makeProps(), saving: true };
    await render(<FavoritesPickerModal kind="genres" initialIds={[1, 2]} {...props} />);
    expect(screen.getByText('Guardando...')).toBeTruthy();
    await fireEvent.press(screen.getByText('Guardando...'));
    await fireEvent.press(screen.getByText('Cancelar'));
    expect(props.onSave).not.toHaveBeenCalled();
    expect(props.onClose).not.toHaveBeenCalled();
  });

  it('Cancelar llama a onClose', async () => {
    const props = makeProps();
    await render(<FavoritesPickerModal kind="genres" initialIds={[1, 2]} {...props} />);
    await fireEvent.press(screen.getByText('Cancelar'));
    expect(props.onClose).toHaveBeenCalledTimes(1);
  });
});
