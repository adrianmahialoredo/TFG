import { render, screen, fireEvent } from '@testing-library/react-native';
import InstrumentPicker from '@/components/InstrumentPicker';
import { useInstruments } from '@/hooks/useInstruments';

jest.setTimeout(30000);

jest.mock('@/hooks/useInstruments', () => ({ useInstruments: jest.fn() }));
const mockedUseInstruments = useInstruments as jest.Mock;

const instruments = [
  { id: 10, name: 'Guitarra' },
  { id: 20, name: 'Piano' },
];

describe('InstrumentPicker', () => {
  beforeEach(() => mockedUseInstruments.mockReturnValue({ instruments, loading: false }));

  it('mientras carga muestra un spinner', async () => {
    mockedUseInstruments.mockReturnValue({ instruments: [], loading: true });
    await render(<InstrumentPicker />);
    expect(screen.container.queryAll((n) => n.type === 'ActivityIndicator')).toHaveLength(1);
  });

  it('muestra la pregunta y un chip por instrumento', async () => {
    await render(<InstrumentPicker />);
    expect(screen.getByText('¿Qué instrumentos te gustaría aprender?')).toBeTruthy();
    expect(screen.getByText('Guitarra')).toBeTruthy();
    expect(screen.getByText('Piano')).toBeTruthy();
  });

  it('alterna la selección y avisa al padre', async () => {
    const onSelectionChange = jest.fn();
    await render(<InstrumentPicker initialSelection={['10']} onSelectionChange={onSelectionChange} />);
    expect(screen.getByRole('checkbox', { name: 'Guitarra' })).toBeChecked();
    await fireEvent.press(screen.getByText('Piano'));
    expect(onSelectionChange).toHaveBeenLastCalledWith(['10', '20']);
    await fireEvent.press(screen.getByText('Guitarra'));
    expect(onSelectionChange).toHaveBeenLastCalledWith(['20']);
  });

  it('funciona sin onSelectionChange', async () => {
    await render(<InstrumentPicker />);
    await fireEvent.press(screen.getByText('Piano'));
    expect(screen.getByRole('checkbox', { name: 'Piano' })).toBeChecked();
  });
});
