import { StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import OptionSelector from '@/components/OptionSelector';

jest.setTimeout(30000);

const options = [
  { value: 'guitar', label: 'Guitarra' },
  { value: 'piano', label: 'Piano' },
  { value: 'drums', label: 'Batería' },
];

describe('OptionSelector', () => {
  it('muestra un botón por cada opción', async () => {
    await render(<OptionSelector options={options} selected={undefined} onSelect={jest.fn()} />);
    expect(screen.getAllByRole('button')).toHaveLength(3);
    expect(screen.getByText('Guitarra')).toBeTruthy();
    expect(screen.getByText('Batería')).toBeTruthy();
  });

  it('marca como seleccionada solo la opción elegida', async () => {
    await render(<OptionSelector options={options} selected="piano" onSelect={jest.fn()} />);
    const piano = screen.getByRole('button', { name: 'Piano' });
    const guitar = screen.getByRole('button', { name: 'Guitarra' });
    expect(piano).toBeSelected();
    expect(guitar).not.toBeSelected();
    expect(StyleSheet.flatten(piano.props.style).backgroundColor).toBe('#ca8a04');
    expect(StyleSheet.flatten(guitar.props.style).backgroundColor).toBe('#374151');
  });

  it('llama a onSelect con el valor de la opción pulsada', async () => {
    const onSelect = jest.fn();
    await render(<OptionSelector options={options} selected="piano" onSelect={onSelect} />);
    await fireEvent.press(screen.getByText('Batería'));
    expect(onSelect).toHaveBeenCalledWith('drums');
  });
});
