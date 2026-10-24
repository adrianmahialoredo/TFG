import { render, screen } from '@testing-library/react-native';
import DifficultyStars from '@/components/DifficultyStars';

jest.setTimeout(30000);

jest.mock('lucide-react-native', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  return { Star: (props: any) => mockJsx(View, { testID: 'icon-Star', ...props }) };
});

/** Estrellas rellenas (fill dorado) o vacías (fill "none"). */
const count = (filled: boolean) =>
  screen.queryAllByTestId('icon-Star').filter((star) => (star.props.fill !== 'none') === filled).length;

describe('DifficultyStars', () => {
  it.each([
    [undefined, 0],
    [0, 0],
    [3, 3],
    [2.6, 3],
    [2.4, 2],
    [5, 5],
    [8, 5],
    [-2, 0],
  ])('con nivel %p rellena %p de 5 estrellas', async (level, filled) => {
    await render(<DifficultyStars level={level as number | undefined} />);
    expect(count(true)).toBe(filled);
    expect(count(false)).toBe(5 - filled);
  });
});
