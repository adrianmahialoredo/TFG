import { StyleSheet, Text } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import BaseModal from '@/components/BaseModal';

jest.setTimeout(30000);

const findHost = (type: string) => screen.container.queryAll((n) => n.type === type);

describe('BaseModal', () => {
  it('muestra los hijos cuando es visible', async () => {
    await render(<BaseModal visible onClose={jest.fn()}><Text>Contenido</Text></BaseModal>);
    expect(screen.getByText('Contenido')).toBeTruthy();
  });

  it('oculta los hijos cuando no es visible', async () => {
    await render(<BaseModal visible={false} onClose={jest.fn()}><Text>Contenido</Text></BaseModal>);
    expect(screen.queryByText('Contenido')).toBeNull();
  });

  it('usa animación fade por defecto y onRequestClose llama a onClose', async () => {
    const onClose = jest.fn();
    await render(<BaseModal visible onClose={onClose}><Text>X</Text></BaseModal>);
    const [modal] = findHost('Modal');
    expect(modal.props.animationType).toBe('fade');
    expect(modal.props.transparent).toBe(true);
    modal.props.onRequestClose();
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('respeta animationType personalizado', async () => {
    await render(<BaseModal visible onClose={jest.fn()} animationType="slide"><Text>X</Text></BaseModal>);
    expect(findHost('Modal')[0].props.animationType).toBe('slide');
  });

  it('pulsar el contenido no cierra el modal', async () => {
    const onClose = jest.fn();
    await render(<BaseModal visible onClose={onClose}><Text>Contenido</Text></BaseModal>);
    await fireEvent.press(screen.getByText('Contenido'));
    expect(onClose).not.toHaveBeenCalled();
  });

  it('aplica maxWidth a la caja solo cuando se pasa', async () => {
    await render(<BaseModal visible onClose={jest.fn()} maxWidth={300}><Text>X</Text></BaseModal>);
    const withMax = findHost('View').filter((v) => StyleSheet.flatten(v.props.style)?.maxWidth === 300);
    expect(withMax.length).toBeGreaterThan(0);
  });
});
