import { PanResponder, PanResponderCallbacks, StyleSheet } from 'react-native';
import { render, screen, fireEvent, act } from '@testing-library/react-native';
import SlidePanel2 from '@/components/SlidePanel2';

jest.setTimeout(30000);

const lessons = [
  { id: 'l1', title: 'Pop Punk' },
  { id: 'l2', title: 'Grunge' },
];

const gesture = (dx: number, dy = 0) => ({ dx, dy } as any);

/** Altura de la pestaña: 100 cerrado, 120 abierto. */
const handleHeight = () => {
  const handle = screen.container.queryAll((n) => {
    const h = StyleSheet.flatten(n.props.style)?.height;
    return n.type === 'View' && (h === 100 || h === 120);
  })[0];
  return StyleSheet.flatten(handle.props.style).height;
};

describe('SlidePanel2', () => {
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

  const runGesture = async (fn: () => void) => {
    await act(async () => {
      fn();
      jest.advanceTimersByTime(1000);
    });
  };

  it('muestra la primera lección como seleccionada y el contenido de ejemplo', async () => {
    await render(<SlidePanel2 lessons={lessons} onSelectLesson={jest.fn()} />);
    expect(screen.getAllByText('Pop Punk')).toHaveLength(2); // cabecera + botón del panel
    expect(screen.getByText('Género Musical')).toBeTruthy();
    expect(screen.getByText('Canciones')).toBeTruthy();
  });

  it('al elegir otra lección cambia la cabecera y avisa al padre', async () => {
    const onSelectLesson = jest.fn();
    await render(<SlidePanel2 lessons={lessons} onSelectLesson={onSelectLesson} />);
    await fireEvent.press(screen.getByText('Grunge'));
    expect(onSelectLesson).toHaveBeenCalledWith(lessons[1]);
    expect(screen.getAllByText('Grunge')).toHaveLength(2);
    expect(screen.getAllByText('Pop Punk')).toHaveLength(1);
  });

  it('solo responde a movimientos horizontales de más de 5px', async () => {
    await render(<SlidePanel2 lessons={lessons} onSelectLesson={jest.fn()} />);
    expect(config.onMoveShouldSetPanResponder!({} as any, gesture(6))).toBe(true);
    expect(config.onMoveShouldSetPanResponder!({} as any, gesture(-3))).toBe(false);
  });

  it('se abre al deslizar más de 100px a la derecha', async () => {
    await render(<SlidePanel2 lessons={lessons} onSelectLesson={jest.fn()} />);
    expect(handleHeight()).toBe(100);
    await runGesture(() => {
      config.onPanResponderMove!({} as any, gesture(120));
      config.onPanResponderRelease!({} as any, gesture(120));
    });
    expect(handleHeight()).toBe(120);
  });

  it('un arrastre corto lo deja cerrado', async () => {
    await render(<SlidePanel2 lessons={lessons} onSelectLesson={jest.fn()} />);
    await runGesture(() => {
      config.onPanResponderMove!({} as any, gesture(50));
      config.onPanResponderRelease!({} as any, gesture(50));
    });
    expect(handleHeight()).toBe(100);
  });

  it('se cierra al deslizar a la izquierda estando abierto', async () => {
    await render(<SlidePanel2 lessons={lessons} onSelectLesson={jest.fn()} />);
    await runGesture(() => config.onPanResponderRelease!({} as any, gesture(150)));
    expect(handleHeight()).toBe(120);
    await runGesture(() => {
      config.onPanResponderMove!({} as any, gesture(-120));
      config.onPanResponderRelease!({} as any, gesture(-120));
    });
    expect(handleHeight()).toBe(100);
  });
});
