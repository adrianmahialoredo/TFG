import { StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import InstrumentCarousel, { CAROUSEL_ITEM_WIDTH } from '@/components/InstrumentCarousel';

jest.setTimeout(30000);

const OPTIONS = [
  { id: 'i1', name: 'Guitarra' },
  { id: 'i2', name: 'Piano' },
  { id: 'i3', name: 'Batería' },
];

const opacity = (name: string) => StyleSheet.flatten(screen.getByText(name).props.style).opacity;

/** Evento de scroll del carrusel parado en `index`. */
const scrollEvent = (index: number) => ({
  nativeEvent: { contentOffset: { x: index * CAROUSEL_ITEM_WIDTH, y: 0 }, velocity: { x: 0, y: 0 } },
});

describe('InstrumentCarousel', () => {
  it('resalta el instrumento seleccionado y atenúa los de los lados', async () => {
    await render(<InstrumentCarousel options={OPTIONS} selectedId="i2" onSelect={jest.fn()} />);
    expect(opacity('Piano')).toBe(1);
    expect(opacity('Guitarra')).toBe(0.3);
    expect(opacity('Batería')).toBe(0.3);
    expect(screen.getByRole('button', { name: 'Piano' })).toBeSelected();
  });

  it('en medio muestra las dos flechas, y cada una elige el instrumento de ese lado', async () => {
    const onSelect = jest.fn();
    await render(<InstrumentCarousel options={OPTIONS} selectedId="i2" onSelect={onSelect} />);
    await fireEvent.press(screen.getByLabelText('Instrumento anterior'));
    expect(onSelect).toHaveBeenLastCalledWith('i1');
    await fireEvent.press(screen.getByLabelText('Instrumento siguiente'));
    expect(onSelect).toHaveBeenLastCalledWith('i2');
  });

  it('en el primero no hay flecha hacia la izquierda, y en el último no la hay hacia la derecha', async () => {
    const { rerender } = await render(<InstrumentCarousel options={OPTIONS} selectedId="i1" onSelect={jest.fn()} />);
    expect(screen.queryByLabelText('Instrumento anterior')).toBeNull();
    expect(screen.getByLabelText('Instrumento siguiente')).toBeTruthy();

    await rerender(<InstrumentCarousel options={OPTIONS} selectedId="i3" onSelect={jest.fn()} />);
    expect(screen.getByLabelText('Instrumento anterior')).toBeTruthy();
    expect(screen.queryByLabelText('Instrumento siguiente')).toBeNull();
  });

  it('pulsar un instrumento lo elige', async () => {
    const onSelect = jest.fn();
    await render(<InstrumentCarousel options={OPTIONS} selectedId="i1" onSelect={onSelect} />);
    await fireEvent.press(screen.getByText('Batería'));
    expect(onSelect).toHaveBeenCalledWith('i3');
    expect(opacity('Batería')).toBe(1);
  });

  it('al deslizar elige el instrumento en el que se para', async () => {
    const onSelect = jest.fn();
    await render(<InstrumentCarousel options={OPTIONS} selectedId="i1" onSelect={onSelect} />);
    const carousel = screen.getByTestId('instrument-carousel');
    await fireEvent.scroll(carousel, scrollEvent(1));
    expect(opacity('Piano')).toBe(1);
    await fireEvent(carousel, 'momentumScrollEnd', scrollEvent(1));
    expect(onSelect).toHaveBeenCalledWith('i2');
  });
});
