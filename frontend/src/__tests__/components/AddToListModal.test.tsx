import { render, screen, fireEvent, act } from '@testing-library/react-native';
import AddToListModal from '@/components/AddToListModal';

jest.setTimeout(30000);

jest.mock('lucide-react-native', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  return { Check: (props: any) => mockJsx(View, { testID: 'icon-Check', ...props }) };
});

let mockState: any;
jest.mock('@/hooks/useAddToList', () => ({ useAddToList: () => mockState }));

const item = { id: 'i1', title: 'Wonderwall' };

describe('AddToListModal', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    mockState = {
      lists: [
        { id: 'L1', name: 'Rock', imageUrl: 'http://img/rock.png', itemIds: ['i1'] },
        { id: 'L2', name: 'blues', itemIds: [] },
      ],
      loading: false,
      addingListId: null,
      error: null,
      contains: (id: string) => id === 'L1',
      addToList: jest.fn().mockResolvedValue(true),
    };
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('muestra el item y cada lista con su imagen o su inicial', async () => {
    await render(<AddToListModal item={item} onClose={jest.fn()} />);
    expect(screen.getByText('Wonderwall')).toBeTruthy();
    expect(screen.getByText('Rock')).toBeTruthy();
    expect(screen.getByText('blues')).toBeTruthy();
    expect(screen.getByText('B')).toBeTruthy();
    const image = screen.container.queryAll((n) => n.type === 'Image')[0];
    expect(image.props.source).toEqual({ uri: 'http://img/rock.png' });
  });

  it('las listas que ya tienen el item salen con el tic y no se pueden pulsar', async () => {
    await render(<AddToListModal item={item} onClose={jest.fn()} />);
    expect(screen.getAllByTestId('icon-Check')).toHaveLength(1);
    await fireEvent.press(screen.getByLabelText('Añadir a Rock'));
    expect(mockState.addToList).not.toHaveBeenCalled();
  });

  it('al elegir una lista añade el item y cierra el modal tras el tic', async () => {
    const onClose = jest.fn();
    await render(<AddToListModal item={item} onClose={onClose} />);
    await fireEvent.press(screen.getByLabelText('Añadir a blues'));
    expect(mockState.addToList).toHaveBeenCalledWith('L2');
    expect(onClose).not.toHaveBeenCalled();
    await act(async () => {
      jest.advanceTimersByTime(500);
    });
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('si no se pudo añadir no se cierra y muestra el error', async () => {
    mockState.addToList = jest.fn().mockResolvedValue(false);
    mockState.error = 'No se pudo añadir a la lista';
    const onClose = jest.fn();
    await render(<AddToListModal item={item} onClose={onClose} />);
    await fireEvent.press(screen.getByLabelText('Añadir a blues'));
    await act(async () => {
      jest.advanceTimersByTime(1000);
    });
    expect(onClose).not.toHaveBeenCalled();
    expect(screen.getByText('No se pudo añadir a la lista')).toBeTruthy();
  });

  it('sin listas avisa de que se crean desde el perfil', async () => {
    mockState.lists = [];
    await render(<AddToListModal item={item} onClose={jest.fn()} />);
    expect(screen.getByText('Aún no tienes listas. Puedes crearlas desde tu perfil.')).toBeTruthy();
  });

  it('el botón Cerrar llama a onClose', async () => {
    const onClose = jest.fn();
    await render(<AddToListModal item={item} onClose={onClose} />);
    await fireEvent.press(screen.getByText('Cerrar'));
    expect(onClose).toHaveBeenCalled();
  });
});
