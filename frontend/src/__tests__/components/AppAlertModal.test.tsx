import { render, screen, fireEvent } from '@testing-library/react-native';
import AppAlertModal, { AppAlertAction } from '@/components/AppAlertModal';

jest.setTimeout(30000);

describe('AppAlertModal', () => {
  const actions: AppAlertAction[] = [
    { text: 'Cancelar', style: 'cancel' },
    { text: 'Borrar', style: 'destructive' },
    { text: 'Aceptar' },
  ];

  it('muestra el título, el mensaje y todos los botones cuando es visible', async () => {
    await render(
      <AppAlertModal visible title="Atención" message="¿Seguro?" actions={actions} onClose={jest.fn()} onActionPress={jest.fn()} />
    );
    expect(screen.getByText('Atención')).toBeTruthy();
    expect(screen.getByText('¿Seguro?')).toBeTruthy();
    expect(screen.getByText('Cancelar')).toBeTruthy();
    expect(screen.getByText('Borrar')).toBeTruthy();
    expect(screen.getByText('Aceptar')).toBeTruthy();
  });

  it('no muestra el mensaje si no se pasa', async () => {
    await render(<AppAlertModal visible title="Solo título" actions={[]} onClose={jest.fn()} onActionPress={jest.fn()} />);
    expect(screen.getByText('Solo título')).toBeTruthy();
    expect(screen.queryByText('¿Seguro?')).toBeNull();
  });

  it('llama a onActionPress con la acción pulsada', async () => {
    const onActionPress = jest.fn();
    await render(<AppAlertModal visible title="T" actions={actions} onClose={jest.fn()} onActionPress={onActionPress} />);
    await fireEvent.press(screen.getByText('Borrar'));
    expect(onActionPress).toHaveBeenCalledWith(actions[1]);
  });

  it('no renderiza nada cuando no es visible', async () => {
    await render(<AppAlertModal visible={false} title="Oculto" actions={actions} onClose={jest.fn()} onActionPress={jest.fn()} />);
    expect(screen.queryByText('Oculto')).toBeNull();
  });
});
