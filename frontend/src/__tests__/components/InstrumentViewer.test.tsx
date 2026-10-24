import { render, screen, fireEvent } from '@testing-library/react-native';
import InstrumentViewer from '@/components/InstrumentViewer';
import { useInstrumentsByIds } from '@/hooks/useInstruments';

jest.setTimeout(30000);

jest.mock('@/hooks/useInstruments', () => ({ useInstrumentsByIds: jest.fn() }));
const mockedUseInstrumentsByIds = useInstrumentsByIds as jest.Mock;

describe('InstrumentViewer', () => {
  beforeEach(() =>
    mockedUseInstrumentsByIds.mockReturnValue([
      { id: '1', name: 'Guitarra' },
      { id: '2', name: 'Batería' },
    ])
  );

  it('usa la etiqueta "Toco:" por defecto y muestra los instrumentos', async () => {
    await render(<InstrumentViewer ids={['1', '2']} />);
    expect(mockedUseInstrumentsByIds).toHaveBeenCalledWith(['1', '2']);
    expect(screen.getByText('Toco:')).toBeTruthy();
    expect(screen.getByText('Guitarra')).toBeTruthy();
    expect(screen.getByText('Batería')).toBeTruthy();
  });

  it('acepta una etiqueta personalizada', async () => {
    await render(<InstrumentViewer ids={[]} label="Quiere aprender:" />);
    expect(screen.getByText('Quiere aprender:')).toBeTruthy();
  });

  it('muestra el botón de editar solo con onAddPress', async () => {
    const onAddPress = jest.fn();
    const { rerender } = await render(<InstrumentViewer ids={[]} />);
    expect(screen.queryByRole('button', { name: 'Editar' })).toBeNull();
    await rerender(<InstrumentViewer ids={[]} onAddPress={onAddPress} />);
    await fireEvent.press(screen.getByRole('button', { name: 'Editar' }));
    expect(onAddPress).toHaveBeenCalledTimes(1);
  });
});
