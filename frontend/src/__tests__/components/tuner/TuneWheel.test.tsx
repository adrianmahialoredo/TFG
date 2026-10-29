import { StyleSheet } from 'react-native';
import { render, screen, fireEvent, act } from '@testing-library/react-native';
import TuneWheel, { SETTLE_MS, WHEEL_ITEM_HEIGHT } from '@/components/tuner/TuneWheel';

jest.setTimeout(30000);

const OPTIONS = [
  { id: '6', label: 'Bajo (Estándar)' },
  { id: '1', label: 'Guitarra (Estándar)' },
  { id: '2', label: 'Guitarra (Drop D)' },
];

const opacity = (label: string) => StyleSheet.flatten(screen.getByText(label).props.style).opacity;

/** Evento de scroll de la rueda parada en `index` (con la velocidad indicada). */
const scrollEvent = (index: number, velocity = 0) => ({
  nativeEvent: { contentOffset: { x: 0, y: index * WHEEL_ITEM_HEIGHT }, velocity: { x: 0, y: velocity } },
});

describe('TuneWheel', () => {
  it('resalta la afinación seleccionada y atenúa las demás', async () => {
    await render(<TuneWheel options={OPTIONS} selectedIndex={1} onSelect={jest.fn()} />);
    expect(opacity('Guitarra (Estándar)')).toBe(1);
    expect(opacity('Bajo (Estándar)')).toBe(0.3);
    expect(opacity('Guitarra (Drop D)')).toBe(0.3);
  });

  it('al deslizar resalta la fila del centro, pero no elige hasta que se para', async () => {
    const onSelect = jest.fn();
    await render(<TuneWheel options={OPTIONS} selectedIndex={1} onSelect={onSelect} />);
    const wheel = screen.getByTestId('tune-wheel');

    await fireEvent.scroll(wheel, scrollEvent(2));
    expect(opacity('Guitarra (Drop D)')).toBe(1);
    expect(onSelect).not.toHaveBeenCalled();

    await fireEvent(wheel, 'momentumScrollEnd', scrollEvent(2));
    expect(onSelect).toHaveBeenCalledWith(2);
  });

  it('si se suelta sin inercia elige al soltar', async () => {
    const onSelect = jest.fn();
    await render(<TuneWheel options={OPTIONS} selectedIndex={1} onSelect={onSelect} />);
    await fireEvent(screen.getByTestId('tune-wheel'), 'scrollEndDrag', scrollEvent(0));
    expect(onSelect).toHaveBeenCalledWith(0);
  });

  it('si se suelta con inercia espera al final del movimiento', async () => {
    const onSelect = jest.fn();
    await render(<TuneWheel options={OPTIONS} selectedIndex={1} onSelect={onSelect} />);
    await fireEvent(screen.getByTestId('tune-wheel'), 'scrollEndDrag', scrollEvent(0, 2));
    expect(onSelect).not.toHaveBeenCalled();
  });

  it('pararse en la fila ya seleccionada no vuelve a elegirla', async () => {
    const onSelect = jest.fn();
    await render(<TuneWheel options={OPTIONS} selectedIndex={1} onSelect={onSelect} />);
    await fireEvent(screen.getByTestId('tune-wheel'), 'momentumScrollEnd', scrollEvent(1));
    expect(onSelect).not.toHaveBeenCalled();
  });

  it('no se sale de la lista al pasarse de rosca', async () => {
    const onSelect = jest.fn();
    await render(<TuneWheel options={OPTIONS} selectedIndex={1} onSelect={onSelect} />);
    await fireEvent(screen.getByTestId('tune-wheel'), 'momentumScrollEnd', scrollEvent(9));
    expect(onSelect).toHaveBeenCalledWith(2);
  });

  it('pulsar una fila la elige', async () => {
    const onSelect = jest.fn();
    await render(<TuneWheel options={OPTIONS} selectedIndex={1} onSelect={onSelect} />);
    await fireEvent.press(screen.getByText('Bajo (Estándar)'));
    expect(onSelect).toHaveBeenCalledWith(0);
    expect(opacity('Bajo (Estándar)')).toBe(1);
  });

  describe('si no llega el evento de fin de deslizamiento', () => {
    beforeEach(() => jest.useFakeTimers());
    afterEach(() => jest.useRealTimers());

    it('elige la fila del centro cuando la rueda lleva un rato quieta', async () => {
      const onSelect = jest.fn();
      await render(<TuneWheel options={OPTIONS} selectedIndex={1} onSelect={onSelect} />);
      await fireEvent.scroll(screen.getByTestId('tune-wheel'), scrollEvent(0));
      expect(onSelect).not.toHaveBeenCalled();
      await act(async () => { jest.advanceTimersByTime(SETTLE_MS); });
      expect(onSelect).toHaveBeenCalledWith(0);
    });

    it('no elige mientras el dedo sigue encima, y no repite lo ya elegido', async () => {
      const onSelect = jest.fn();
      await render(<TuneWheel options={OPTIONS} selectedIndex={1} onSelect={onSelect} />);
      const wheel = screen.getByTestId('tune-wheel');
      await fireEvent(wheel, 'scrollBeginDrag', scrollEvent(1));
      await fireEvent.scroll(wheel, scrollEvent(2));
      await act(async () => { jest.advanceTimersByTime(SETTLE_MS * 3); });
      expect(onSelect).not.toHaveBeenCalled();

      await fireEvent(wheel, 'scrollEndDrag', scrollEvent(2));
      expect(onSelect).toHaveBeenCalledWith(2);
      await fireEvent.scroll(wheel, scrollEvent(2));
      await fireEvent(wheel, 'momentumScrollEnd', scrollEvent(2));
      await act(async () => { jest.advanceTimersByTime(SETTLE_MS); });
      expect(onSelect).toHaveBeenCalledTimes(1);
    });
  });

  it('sigue a la selección que llega de fuera', async () => {
    const { rerender } = await render(<TuneWheel options={OPTIONS} selectedIndex={0} onSelect={jest.fn()} />);
    await rerender(<TuneWheel options={OPTIONS} selectedIndex={2} onSelect={jest.fn()} />);
    expect(opacity('Guitarra (Drop D)')).toBe(1);
    expect(opacity('Bajo (Estándar)')).toBe(0.3);
  });
});
