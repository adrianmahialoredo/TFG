import { render, screen, fireEvent } from '@testing-library/react-native';
import ListItem from '@/components/ListItem';

jest.setTimeout(30000);

const item = { id: '1', title: 'Rock', description: 'Género', imageUrl: 'http://img/rock.png', info: 'Rock\nGénero' };

describe('ListItem', () => {
  it('muestra la información y la imagen del item', async () => {
    await render(<ListItem item={item} onPress={jest.fn()} />);
    expect(screen.getByText('Rock\nGénero')).toBeTruthy();
    const image = screen.container.queryAll((n) => n.type === 'Image')[0];
    expect(image.props.source).toEqual({ uri: 'http://img/rock.png' });
  });

  it('llama a onPress al pulsar el item', async () => {
    const onPress = jest.fn();
    await render(<ListItem item={item} onPress={onPress} />);
    await fireEvent.press(screen.getByText('Rock\nGénero'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('sin onRemove no muestra el botón de quitar', async () => {
    await render(<ListItem item={item} onPress={jest.fn()} />);
    expect(screen.queryByLabelText('Quitar Rock de la lista')).toBeNull();
  });

  it('con onRemove muestra el botón de quitar y lo llama sin abrir el item', async () => {
    const onPress = jest.fn();
    const onRemove = jest.fn();
    await render(<ListItem item={item} onPress={onPress} onRemove={onRemove} />);
    await fireEvent.press(screen.getByLabelText('Quitar Rock de la lista'));
    expect(onRemove).toHaveBeenCalledTimes(1);
    expect(onPress).not.toHaveBeenCalled();
  });

  it('el botón de quitar también funciona en modo invertido', async () => {
    const onRemove = jest.fn();
    await render(<ListItem item={item} onPress={jest.fn()} onRemove={onRemove} inverted />);
    await fireEvent.press(screen.getByLabelText('Quitar Rock de la lista'));
    expect(onRemove).toHaveBeenCalledTimes(1);
  });

  it('con onAdd muestra el botón + y lo llama sin abrir el item', async () => {
    const onPress = jest.fn();
    const onAdd = jest.fn();
    await render(<ListItem item={item} onPress={onPress} onAdd={onAdd} />);
    await fireEvent.press(screen.getByLabelText('Añadir Rock a una lista'));
    expect(onAdd).toHaveBeenCalledTimes(1);
    expect(onPress).not.toHaveBeenCalled();
  });

  it('sin onAdd no muestra el botón +', async () => {
    await render(<ListItem item={item} onPress={jest.fn()} />);
    expect(screen.queryByLabelText('Añadir Rock a una lista')).toBeNull();
  });

  it('funciona igual en modo invertido', async () => {
    const onPress = jest.fn();
    await render(<ListItem item={item} onPress={onPress} inverted />);
    await fireEvent.press(screen.getByText('Rock\nGénero'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });
});
