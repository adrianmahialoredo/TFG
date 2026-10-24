import { render, screen, fireEvent } from '@testing-library/react-native';
import CreateListMiniModal from '@/components/CreateListMiniModal';

jest.setTimeout(30000);

const makeProps = () => ({
  visible: true,
  name: '',
  error: null as string | null,
  creating: false,
  onChangeName: jest.fn(),
  onSubmit: jest.fn(),
  onClose: jest.fn(),
});

describe('CreateListMiniModal', () => {
  it('muestra los textos por defecto', async () => {
    await render(<CreateListMiniModal {...makeProps()} />);
    expect(screen.getByText('Nueva lista')).toBeTruthy();
    expect(screen.getByPlaceholderText('Nombre de la lista')).toBeTruthy();
    expect(screen.getByText('Crear')).toBeTruthy();
    expect(screen.getByText('Cancelar')).toBeTruthy();
  });

  it('acepta título, placeholder y etiqueta personalizados', async () => {
    await render(
      <CreateListMiniModal {...makeProps()} title="Renombrar" placeholder="Nuevo nombre" submitLabel="Guardar" name="Mi lista" />
    );
    expect(screen.getByText('Renombrar')).toBeTruthy();
    expect(screen.getByPlaceholderText('Nuevo nombre')).toBeTruthy();
    expect(screen.getByDisplayValue('Mi lista')).toBeTruthy();
    expect(screen.getByText('Guardar')).toBeTruthy();
  });

  it('propaga los cambios del nombre', async () => {
    const props = makeProps();
    await render(<CreateListMiniModal {...props} />);
    await fireEvent.changeText(screen.getByPlaceholderText('Nombre de la lista'), 'Favoritas');
    expect(props.onChangeName).toHaveBeenCalledWith('Favoritas');
  });

  it('llama a onSubmit y a onClose desde sus botones', async () => {
    const props = makeProps();
    await render(<CreateListMiniModal {...props} />);
    await fireEvent.press(screen.getByText('Crear'));
    expect(props.onSubmit).toHaveBeenCalledTimes(1);
    await fireEvent.press(screen.getByText('Cancelar'));
    expect(props.onClose).toHaveBeenCalledTimes(1);
  });

  it('muestra el error si lo hay', async () => {
    await render(<CreateListMiniModal {...makeProps()} error="El nombre es obligatorio" />);
    expect(screen.getByText('El nombre es obligatorio')).toBeTruthy();
  });

  it('mientras crea muestra "Guardando..." y desactiva los botones', async () => {
    const props = makeProps();
    await render(<CreateListMiniModal {...props} creating />);
    expect(screen.getByText('Guardando...')).toBeTruthy();
    expect(screen.queryByText('Crear')).toBeNull();
    await fireEvent.press(screen.getByText('Guardando...'));
    await fireEvent.press(screen.getByText('Cancelar'));
    expect(props.onSubmit).not.toHaveBeenCalled();
    expect(props.onClose).not.toHaveBeenCalled();
  });

  it('no muestra nada si no es visible', async () => {
    await render(<CreateListMiniModal {...makeProps()} visible={false} />);
    expect(screen.queryByText('Nueva lista')).toBeNull();
  });
});
