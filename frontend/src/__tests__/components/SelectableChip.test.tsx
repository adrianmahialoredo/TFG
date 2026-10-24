import { StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import SelectableChip from '@/components/SelectableChip';

jest.setTimeout(30000);

const chipOpacity = () => {
  const views = screen.container.queryAll(
    (n) => n.type === 'View' && StyleSheet.flatten(n.props.style)?.opacity !== undefined
  );
  return StyleSheet.flatten(views[0].props.style).opacity;
};

describe('SelectableChip', () => {
  it('muestra la etiqueta y se marca como checkbox seleccionado', async () => {
    await render(<SelectableChip label="Rock" selected onPress={jest.fn()} />);
    expect(screen.getByText('Rock')).toBeTruthy();
    expect(screen.getByRole('checkbox')).toBeChecked();
    expect(chipOpacity()).toBe(1);
  });

  it('sin seleccionar queda atenuado', async () => {
    await render(<SelectableChip label="Jazz" selected={false} onPress={jest.fn()} />);
    expect(screen.getByRole('checkbox')).not.toBeChecked();
    expect(chipOpacity()).toBe(0.4);
  });

  it('llama a onPress al pulsarlo', async () => {
    const onPress = jest.fn();
    await render(<SelectableChip label="Pop" selected={false} onPress={onPress} />);
    await fireEvent.press(screen.getByText('Pop'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });
});
