import { render, screen, fireEvent } from '@testing-library/react-native';
import { PhotoOptionsModal } from '@/components/PhotoOptionsModal';

jest.setTimeout(30000);

const makeProps = () => ({ visible: true, onClose: jest.fn(), onViewImage: jest.fn(), onPickImage: jest.fn() });

describe('PhotoOptionsModal', () => {
  it('muestra la cabecera y las opciones', async () => {
    await render(<PhotoOptionsModal {...makeProps()} />);
    expect(screen.getByText('Foto')).toBeTruthy();
    expect(screen.getByText('¿Qué quieres hacer con la imagen?')).toBeTruthy();
    expect(screen.getByText('Ver foto ampliada')).toBeTruthy();
    expect(screen.getByText('Subir nueva foto')).toBeTruthy();
    expect(screen.getByText('Cancelar')).toBeTruthy();
  });

  it('cada opción llama a su callback', async () => {
    const p = makeProps();
    await render(<PhotoOptionsModal {...p} />);
    await fireEvent.press(screen.getByText('Ver foto ampliada'));
    expect(p.onViewImage).toHaveBeenCalledTimes(1);
    await fireEvent.press(screen.getByText('Subir nueva foto'));
    expect(p.onPickImage).toHaveBeenCalledTimes(1);
    await fireEvent.press(screen.getByText('Cancelar'));
    expect(p.onClose).toHaveBeenCalledTimes(1);
  });

  it('mientras sube muestra el spinner y desactiva la subida', async () => {
    const p = makeProps();
    await render(<PhotoOptionsModal {...p} isLoading />);
    expect(screen.getByText('Subiendo foto...')).toBeTruthy();
    expect(screen.queryByText('Subir nueva foto')).toBeNull();
    expect(screen.container.queryAll((n) => n.type === 'ActivityIndicator')).toHaveLength(1);
    await fireEvent.press(screen.getByText('Subiendo foto...'));
    expect(p.onPickImage).not.toHaveBeenCalled();
  });

  it('onRequestClose del modal llama a onClose', async () => {
    const p = makeProps();
    await render(<PhotoOptionsModal {...p} />);
    screen.container.queryAll((n) => n.type === 'Modal')[0].props.onRequestClose();
    expect(p.onClose).toHaveBeenCalledTimes(1);
  });

  it('no muestra nada si no es visible', async () => {
    await render(<PhotoOptionsModal {...makeProps()} visible={false} />);
    expect(screen.queryByText('Foto')).toBeNull();
  });
});
