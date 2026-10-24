import { render, screen, fireEvent } from '@testing-library/react-native';
import SearchBar from '@/components/SearchBar';

jest.setTimeout(30000);

describe('SearchBar', () => {
  it('usa el placeholder por defecto', async () => {
    await render(<SearchBar value="" onChangeText={jest.fn()} />);
    expect(screen.getByPlaceholderText('¿Que quieres tocar?')).toBeTruthy();
  });

  it('acepta un placeholder personalizado y muestra el valor', async () => {
    await render(<SearchBar placeholder="Buscar canciones" value="rock" onChangeText={jest.fn()} />);
    expect(screen.getByPlaceholderText('Buscar canciones')).toBeTruthy();
    expect(screen.getByDisplayValue('rock')).toBeTruthy();
  });

  it('llama a onChangeText al escribir', async () => {
    const onChangeText = jest.fn();
    await render(<SearchBar value="" onChangeText={onChangeText} />);
    await fireEvent.changeText(screen.getByPlaceholderText('¿Que quieres tocar?'), 'jazz');
    expect(onChangeText).toHaveBeenCalledWith('jazz');
  });
});
