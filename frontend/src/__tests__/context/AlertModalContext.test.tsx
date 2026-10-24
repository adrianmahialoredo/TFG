import React from 'react';
import { act, renderHook } from '@testing-library/react-native';
import { AlertModalProvider, useAppAlert } from '../../context/AlertModalContext';

// Captura las props que el provider pasa al modal (se renderiza el último estado)
const mockModalProps: { current: any } = { current: null };

jest.mock('../../components/AppAlertModal', () => ({
  __esModule: true,
  default: (props: any) => {
    mockModalProps.current = props;
    return null;
  },
}));

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <AlertModalProvider>{children}</AlertModalProvider>
);

describe('AlertModalContext', () => {
  beforeEach(() => {
    mockModalProps.current = null;
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('lanza un error si se usa fuera del AlertModalProvider', async () => {
    await expect(renderHook(() => useAppAlert())).rejects.toThrow(
      'useAppAlert debe usarse dentro de AlertModalProvider'
    );
  });

  it('empieza con el modal oculto y la acción por defecto', async () => {
    await renderHook(() => useAppAlert(), { wrapper });
    expect(mockModalProps.current.visible).toBe(false);
    expect(mockModalProps.current.title).toBe('');
    expect(mockModalProps.current.message).toBe('');
    expect(mockModalProps.current.actions).toEqual([{ text: 'Aceptar' }]);
  });

  it('showAlert muestra el modal con título, mensaje y acciones', async () => {
    const { result } = await renderHook(() => useAppAlert(), { wrapper });
    const actions = [{ text: 'Cancelar', style: 'cancel' as const }, { text: 'Borrar' }];

    await act(async () => {
      result.current.showAlert({ title: 'Título', message: 'Mensaje', actions });
    });

    expect(mockModalProps.current.visible).toBe(true);
    expect(mockModalProps.current.title).toBe('Título');
    expect(mockModalProps.current.message).toBe('Mensaje');
    expect(mockModalProps.current.actions).toEqual(actions);
  });

  it('showAlert usa mensaje vacío y la acción por defecto si no se indican', async () => {
    const { result } = await renderHook(() => useAppAlert(), { wrapper });

    await act(async () => {
      result.current.showAlert({ title: 'Solo título', actions: [] });
    });

    expect(mockModalProps.current.message).toBe('');
    expect(mockModalProps.current.actions).toEqual([{ text: 'Aceptar' }]);
  });

  it('hideAlert oculta el modal', async () => {
    const { result } = await renderHook(() => useAppAlert(), { wrapper });

    await act(async () => {
      result.current.showAlert({ title: 'Hola' });
    });
    expect(mockModalProps.current.visible).toBe(true);

    await act(async () => {
      result.current.hideAlert();
    });
    expect(mockModalProps.current.visible).toBe(false);
  });

  it('onClose del modal oculta la alerta', async () => {
    const { result } = await renderHook(() => useAppAlert(), { wrapper });

    await act(async () => {
      result.current.showAlert({ title: 'Hola' });
    });
    await act(async () => {
      mockModalProps.current.onClose();
    });
    expect(mockModalProps.current.visible).toBe(false);
  });

  it('al pulsar una acción oculta el modal y ejecuta su onPress tras el cierre', async () => {
    jest.useFakeTimers();
    const onPress = jest.fn();
    const { result } = await renderHook(() => useAppAlert(), { wrapper });

    await act(async () => {
      result.current.showAlert({ title: 'Confirmar', actions: [{ text: 'Sí', onPress }] });
    });
    await act(async () => {
      mockModalProps.current.onActionPress({ text: 'Sí', onPress });
    });

    expect(mockModalProps.current.visible).toBe(false);
    expect(onPress).not.toHaveBeenCalled();

    await act(async () => {
      jest.runAllTimers();
    });
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('al pulsar una acción sin onPress solo oculta el modal', async () => {
    jest.useFakeTimers();
    const { result } = await renderHook(() => useAppAlert(), { wrapper });

    await act(async () => {
      result.current.showAlert({ title: 'Info' });
    });
    await act(async () => {
      mockModalProps.current.onActionPress({ text: 'Aceptar' });
    });

    expect(mockModalProps.current.visible).toBe(false);
    await act(async () => {
      jest.runAllTimers();
    });
    expect(mockModalProps.current.visible).toBe(false);
  });
});
