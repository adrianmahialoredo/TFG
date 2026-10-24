import { render, screen, act } from '@testing-library/react-native';
import DifficultyLevelSlider from '@/components/DifficultyLevelSlider';
import { COLORS } from '@/constants/Theme';

jest.setTimeout(30000);

jest.mock('@react-native-community/slider', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  return { __esModule: true, default: (props: any) => mockJsx(View, { testID: 'slider', ...props }) };
});

const slider = () => screen.getByTestId('slider');

describe('DifficultyLevelSlider', () => {
  it('muestra el nivel actual y los extremos 0 y 5', async () => {
    await render(<DifficultyLevelSlider value={3} onChange={jest.fn()} />);
    expect(screen.getByText('Mi nivel:')).toBeTruthy();
    expect(screen.getByText('3')).toBeTruthy();
    expect(screen.getByText('0')).toBeTruthy();
    expect(screen.getByText('5')).toBeTruthy();
  });

  it('es un slider dorado de 0 a 5 por pasos enteros', async () => {
    await render(<DifficultyLevelSlider value={3} onChange={jest.fn()} />);
    expect(slider().props).toEqual(expect.objectContaining({
      minimumValue: 0,
      maximumValue: 5,
      step: 1,
      value: 3,
      minimumTrackTintColor: COLORS.primary,
      thumbTintColor: COLORS.primary,
    }));
  });

  it('avisa del nuevo nivel redondeado al moverlo', async () => {
    const onChange = jest.fn();
    await render(<DifficultyLevelSlider value={3} onChange={onChange} />);
    await act(async () => slider().props.onValueChange(4.4));
    expect(onChange).toHaveBeenCalledWith(4);
  });

  it('se puede deshabilitar (p.ej. mientras se guarda)', async () => {
    const { rerender } = await render(<DifficultyLevelSlider value={1} onChange={jest.fn()} />);
    expect(slider().props.disabled).toBe(false);
    await rerender(<DifficultyLevelSlider value={1} onChange={jest.fn()} disabled />);
    expect(slider().props.disabled).toBe(true);
  });
});
