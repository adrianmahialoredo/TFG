import { Text } from 'react-native';
import { act, fireEvent, render, screen } from '@testing-library/react-native';
import Touchable from '@/components/Touchable';
import { HELD_OPACITY, HOLD_DELAY_MS } from '@/hooks/useHeldPress';

const opacityOf = () => {
  const style = screen.getByTestId('boton').props.style;
  return [style].flat().reduce((opacity, s) => s?.opacity ?? opacity, 1);
};

describe('Touchable', () => {
  beforeEach(() => jest.useFakeTimers());
  afterEach(() => jest.useRealTimers());

  it('al tocarlo llama a onPress sin atenuarse', async () => {
    const onPress = jest.fn();
    await render(<Touchable testID="boton" onPress={onPress}><Text>Ir</Text></Touchable>);

    await fireEvent(screen.getByTestId('boton'), 'pressIn');
    expect(opacityOf()).toBe(1);
    await fireEvent(screen.getByTestId('boton'), 'pressOut');
    await fireEvent.press(screen.getByTestId('boton'));

    expect(onPress).toHaveBeenCalledTimes(1);
    expect(opacityOf()).toBe(1);
  });

  it('al mantenerlo pulsado se atenúa y al soltar vuelve', async () => {
    await render(<Touchable testID="boton" style={{ opacity: 0.5 }}><Text>Ir</Text></Touchable>);

    await fireEvent(screen.getByTestId('boton'), 'pressIn');
    await act(async () => { jest.advanceTimersByTime(HOLD_DELAY_MS); });
    // Se atenúa sobre la opacidad que ya tuviera
    expect(opacityOf()).toBeCloseTo(0.5 * HELD_OPACITY);

    await fireEvent(screen.getByTestId('boton'), 'pressOut');
    expect(opacityOf()).toBe(0.5);
  });

  it('con activeOpacity 1 no se atenúa nunca', async () => {
    await render(<Touchable testID="boton" activeOpacity={1}><Text>Ir</Text></Touchable>);

    await fireEvent(screen.getByTestId('boton'), 'pressIn');
    await act(async () => { jest.advanceTimersByTime(HOLD_DELAY_MS); });

    expect(opacityOf()).toBe(1);
  });

  it('encadena los onPressIn y onPressOut que se le pasen', async () => {
    const onPressIn = jest.fn();
    const onPressOut = jest.fn();
    await render(<Touchable testID="boton" onPressIn={onPressIn} onPressOut={onPressOut}><Text>Ir</Text></Touchable>);

    await fireEvent(screen.getByTestId('boton'), 'pressIn');
    await fireEvent(screen.getByTestId('boton'), 'pressOut');

    expect(onPressIn).toHaveBeenCalledTimes(1);
    expect(onPressOut).toHaveBeenCalledTimes(1);
  });
});
