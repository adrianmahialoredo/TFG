import { PanResponder, PanResponderCallbacks, StyleSheet } from 'react-native';
import { render, screen, fireEvent, act } from '@testing-library/react-native';
import EdgePanel, { HANDLE_TOUCH_WIDTH, PANEL_WIDTH } from '@/components/EdgePanel';
import { LAYOUT_HORIZONTAL_PADDING } from '@/components/AppLayout';
import { LessonDTO } from '@/types';

jest.setTimeout(30000);

const lessons = [
  { id: 'l1', title: 'Introducción' },
  { id: 'l2', title: 'Acordes' },
  { id: 'l3', title: 'Ritmo' },
] as LessonDTO[];

const CLOSED_WIDTH = HANDLE_TOUCH_WIDTH;
const OPEN_WIDTH = PANEL_WIDTH + HANDLE_TOUCH_WIDTH;

/** Contenedor fijo que recibe los gestos (el que lleva los panHandlers). */
const gestureContainer = () => screen.container.queryAll((n) => n.type === 'View' && !!n.props.onResponderRelease)[0];
const containerWidth = () => StyleSheet.flatten(gestureContainer().props.style).width;
const containerLeft = () => StyleSheet.flatten(gestureContainer().props.style).left;

const gesture = (dx: number, dy = 0) => ({ dx, dy } as any);

describe('EdgePanel', () => {
  let config: PanResponderCallbacks;

  beforeEach(() => {
    jest.useFakeTimers();
    const original = PanResponder.create;
    jest.spyOn(PanResponder, 'create').mockImplementation((c) => {
      config = c;
      return original(c);
    });
  });

  afterEach(() => jest.useRealTimers());

  /** Ejecuta un callback del PanResponder y deja terminar la animación del muelle. */
  const runGesture = async (fn: () => void) => {
    await act(async () => {
      fn();
      jest.advanceTimersByTime(3000);
    });
  };

  it('muestra un botón por lección y marca la activa', async () => {
    await render(<EdgePanel lessons={lessons} activeLessonId="l2" onSelectLessonId={jest.fn()} />);
    lessons.forEach((l) => expect(screen.getByText(l.title)).toBeTruthy());
    expect(StyleSheet.flatten(screen.getByText('Acordes').props.style).backgroundColor).toBe('#C1853C');
    expect(StyleSheet.flatten(screen.getByText('Ritmo').props.style).backgroundColor).toBe('#000000');
  });

  it('al pulsar una lección avisa con su id', async () => {
    const onSelectLessonId = jest.fn();
    await render(<EdgePanel lessons={lessons} activeLessonId="l1" onSelectLessonId={onSelectLessonId} />);
    await fireEvent.press(screen.getByText('Ritmo'));
    expect(onSelectLessonId).toHaveBeenCalledWith('l3');
  });

  it('cerrado solo ocupa la franja de la pestaña', async () => {
    await render(<EdgePanel lessons={lessons} activeLessonId="l1" onSelectLessonId={jest.fn()} />);
    expect(containerWidth()).toBe(CLOSED_WIDTH);
  });

  it('compensa el margen lateral de AppLayout para quedar pegado al borde de la pantalla', async () => {
    await render(<EdgePanel lessons={lessons} activeLessonId="l1" onSelectLessonId={jest.fn()} />);
    expect(containerLeft()).toBe(-LAYOUT_HORIZONTAL_PADDING);
  });

  it('un toque en la pestaña lo abre y amplía el área táctil', async () => {
    await render(<EdgePanel lessons={lessons} activeLessonId="l1" onSelectLessonId={jest.fn()} />);
    expect(config.onStartShouldSetPanResponder!({} as any, gesture(0))).toBe(true);
    await runGesture(() => config.onPanResponderRelease!({} as any, gesture(0)));
    expect(containerWidth()).toBe(OPEN_WIDTH);
    // Abierto, un toque ya no lo captura el panel (va a los botones)
    expect(config.onStartShouldSetPanResponder!({} as any, gesture(0))).toBe(false);
  });

  it('se abre al deslizar a la derecha más del umbral y se cierra deslizando a la izquierda', async () => {
    await render(<EdgePanel lessons={lessons} activeLessonId="l1" onSelectLessonId={jest.fn()} />);
    await runGesture(() => {
      config.onPanResponderMove!({} as any, gesture(80));
      config.onPanResponderRelease!({} as any, gesture(80));
    });
    expect(containerWidth()).toBe(OPEN_WIDTH);

    await runGesture(() => {
      config.onPanResponderMove!({} as any, gesture(-80));
      config.onPanResponderRelease!({} as any, gesture(-80));
    });
    expect(containerWidth()).toBe(CLOSED_WIDTH);
  });

  it('un arrastre corto vuelve al estado anterior', async () => {
    await render(<EdgePanel lessons={lessons} activeLessonId="l1" onSelectLessonId={jest.fn()} />);
    await runGesture(() => config.onPanResponderRelease!({} as any, gesture(20)));
    expect(containerWidth()).toBe(CLOSED_WIDTH);

    await runGesture(() => config.onPanResponderRelease!({} as any, gesture(0)));
    expect(containerWidth()).toBe(OPEN_WIDTH);
    await runGesture(() => config.onPanResponderRelease!({} as any, gesture(-20)));
    expect(containerWidth()).toBe(OPEN_WIDTH);
  });

  it('si otro componente interrumpe el gesto vuelve al estado anterior', async () => {
    await render(<EdgePanel lessons={lessons} activeLessonId="l1" onSelectLessonId={jest.fn()} />);
    await runGesture(() => config.onPanResponderTerminate!({} as any, gesture(0)));
    expect(containerWidth()).toBe(CLOSED_WIDTH);
    expect(config.onPanResponderTerminationRequest!({} as any, gesture(0))).toBe(false);
  });

  it('solo captura movimientos horizontales mayores que el margen de toque', async () => {
    await render(<EdgePanel lessons={lessons} activeLessonId="l1" onSelectLessonId={jest.fn()} />);
    expect(config.onMoveShouldSetPanResponderCapture!({} as any, gesture(30, 5))).toBe(true);
    expect(config.onMoveShouldSetPanResponder!({} as any, gesture(-30, 5))).toBe(true);
    expect(config.onMoveShouldSetPanResponderCapture!({} as any, gesture(3, 0))).toBe(false);
    expect(config.onMoveShouldSetPanResponder!({} as any, gesture(10, 40))).toBe(false);
  });
});
