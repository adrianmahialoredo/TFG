import { Text } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import FadeScrollView from '@/components/FadeScrollView';
import { COLORS } from '@/constants/Theme';

jest.setTimeout(30000);

// Sustituto de react-native-svg: Views con las props visibles
jest.mock('react-native-svg', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  const make = (name: string) => (props: any) => mockJsx(View, { testID: `svg-${name}`, ...props });
  return {
    __esModule: true,
    default: make('Svg'),
    Defs: make('Defs'),
    LinearGradient: make('LinearGradient'),
    Rect: make('Rect'),
    Stop: make('Stop'),
  };
});

const scrollView = () => screen.container.queryAll((n) => n.type === 'RCTScrollView')[0];

describe('FadeScrollView', () => {
  it('renderiza sus hijos y pasa las props al ScrollView', async () => {
    await render(
      <FadeScrollView showsVerticalScrollIndicator={false}>
        <Text>Contenido</Text>
      </FadeScrollView>
    );
    expect(screen.getByText('Contenido')).toBeTruthy();
    expect(scrollView().props.showsVerticalScrollIndicator).toBe(false);
    expect(scrollView().props.scrollEventThrottle).toBe(16);
  });

  it('usa el color de fondo por defecto en el degradado y uno propio si se pasa', async () => {
    const { rerender } = await render(<FadeScrollView><Text>X</Text></FadeScrollView>);
    expect(screen.getAllByTestId('svg-Stop')[0].props.stopColor).toBe(COLORS.background);
    await rerender(<FadeScrollView fadeColor="#123456"><Text>X</Text></FadeScrollView>);
    screen.getAllByTestId('svg-Stop').forEach((stop) => expect(stop.props.stopColor).toBe('#123456'));
  });

  it('el rectángulo usa el degradado con su propio id', async () => {
    await render(<FadeScrollView><Text>X</Text></FadeScrollView>);
    const gradientId = screen.getByTestId('svg-LinearGradient').props.id;
    expect(gradientId).toMatch(/^scroll-fade-\d+$/);
    expect(screen.getByTestId('svg-Rect').props.fill).toBe(`url(#${gradientId})`);
  });

  it('cada instancia tiene un id de degradado distinto', async () => {
    await render(
      <>
        <FadeScrollView><Text>A</Text></FadeScrollView>
        <FadeScrollView><Text>B</Text></FadeScrollView>
      </>
    );
    const ids = screen.getAllByTestId('svg-LinearGradient').map((g) => g.props.id);
    expect(new Set(ids).size).toBe(2);
  });

  it('reenvía el evento de scroll al onScroll del padre', async () => {
    const onScroll = jest.fn();
    await render(<FadeScrollView onScroll={onScroll} scrollEventThrottle={32}><Text>X</Text></FadeScrollView>);
    expect(scrollView().props.scrollEventThrottle).toBe(32);
    const event = { nativeEvent: { contentOffset: { x: 0, y: 50 } } };
    await fireEvent.scroll(scrollView(), event);
    expect(onScroll).toHaveBeenCalledTimes(1);
    expect(onScroll.mock.calls[0][0].nativeEvent.contentOffset).toEqual({ x: 0, y: 50 });
  });

  it('hacer scroll sin onScroll no falla', async () => {
    await render(<FadeScrollView><Text>X</Text></FadeScrollView>);
    await fireEvent.scroll(scrollView(), { nativeEvent: { contentOffset: { x: 0, y: 10 } } });
    expect(screen.getByText('X')).toBeTruthy();
  });
});
