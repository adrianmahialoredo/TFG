import { render, screen, fireEvent, act, waitFor } from '@testing-library/react-native';
import EmailVerificationModal from '@/components/EmailVerificationModal';

jest.setTimeout(30000);

const makeProps = () => ({
  email: 'ana@test.com',
  submitting: false,
  error: null as string | null,
  onSubmit: jest.fn(),
  onResend: jest.fn().mockResolvedValue(undefined),
  onClose: jest.fn(),
});

/** Campo de texto oculto del CodeField (el único TextInput). */
const codeInput = () => screen.container.queryAll((n) => n.type === 'TextInput')[0];

/** Avanza la cuenta atrás segundo a segundo (cada tick programa el siguiente en un efecto). */
const advanceSeconds = async (seconds: number) => {
  for (let i = 0; i < seconds; i++) {
    await act(async () => {
      jest.advanceTimersByTime(1000);
    });
  }
};

describe('EmailVerificationModal', () => {
  afterEach(() => jest.useRealTimers());

  it('muestra el título, el email y la cuenta atrás del reenvío', async () => {
    await render(<EmailVerificationModal {...makeProps()} />);
    expect(screen.getByText('Verifica tu email')).toBeTruthy();
    expect(screen.getByText('ana@test.com')).toBeTruthy();
    expect(screen.getByText('Reenviar en 60s')).toBeTruthy();
  });

  it('se envía solo al completar las 6 cifras, ignorando lo que no son dígitos', async () => {
    const props = makeProps();
    await render(<EmailVerificationModal {...props} />);
    await fireEvent.changeText(codeInput(), '12a3');
    expect(props.onSubmit).not.toHaveBeenCalled();
    expect(codeInput().props.value).toBe('123');
    await fireEvent.changeText(codeInput(), '123456');
    expect(props.onSubmit).toHaveBeenCalledWith('123456');
  });

  it('muestra los dígitos escritos en las celdas', async () => {
    await render(<EmailVerificationModal {...makeProps()} />);
    await fireEvent.changeText(codeInput(), '42');
    expect(screen.getByText('4')).toBeTruthy();
    expect(screen.getByText('2')).toBeTruthy();
  });

  it('muestra el error y vacía el campo tras un intento fallido', async () => {
    const props = makeProps();
    const { rerender } = await render(<EmailVerificationModal {...props} />);
    await fireEvent.changeText(codeInput(), '111');
    await rerender(<EmailVerificationModal {...props} error="Código incorrecto" />);
    expect(screen.getByText('Código incorrecto')).toBeTruthy();
    expect(codeInput().props.value).toBe('');
  });

  it('mientras se envía muestra el spinner, bloquea el campo y no deja cancelar', async () => {
    const props = { ...makeProps(), submitting: true, error: 'antiguo' };
    await render(<EmailVerificationModal {...props} />);
    expect(screen.container.queryAll((n) => n.type === 'ActivityIndicator')).toHaveLength(1);
    expect(screen.queryByText('antiguo')).toBeNull();
    expect(codeInput().props.editable).toBe(false);
    await fireEvent.press(screen.getByText('Cancelar'));
    expect(props.onClose).not.toHaveBeenCalled();
    // Cerrar el modal (botón atrás) tampoco hace nada mientras se envía
    screen.container.queryAll((n) => n.type === 'Modal')[0].props.onRequestClose();
    expect(props.onClose).not.toHaveBeenCalled();
  });

  it('Cancelar llama a onClose', async () => {
    const props = makeProps();
    await render(<EmailVerificationModal {...props} />);
    await fireEvent.press(screen.getByText('Cancelar'));
    expect(props.onClose).toHaveBeenCalledTimes(1);
  });

  it('no deja reenviar durante la cuenta atrás', async () => {
    const props = makeProps();
    await render(<EmailVerificationModal {...props} />);
    await fireEvent.press(screen.getByText('Reenviar en 60s'));
    expect(props.onResend).not.toHaveBeenCalled();
  });

  it('al terminar la cuenta atrás permite reenviar y la reinicia', async () => {
    jest.useFakeTimers();
    const props = makeProps();
    await render(<EmailVerificationModal {...props} />);
    await advanceSeconds(1);
    expect(screen.getByText('Reenviar en 59s')).toBeTruthy();
    await advanceSeconds(59);
    expect(screen.getByText('Reenviar código')).toBeTruthy();

    await fireEvent.press(screen.getByText('Reenviar código'));
    expect(props.onResend).toHaveBeenCalledTimes(1);
    expect(await screen.findByText('Reenviar en 60s')).toBeTruthy();
  });

  it('si el reenvío falla muestra su error', async () => {
    jest.useFakeTimers();
    const props = makeProps();
    props.onResend.mockRejectedValue(new Error('Demasiados intentos'));
    await render(<EmailVerificationModal {...props} />);
    await advanceSeconds(60);
    await fireEvent.press(screen.getByText('Reenviar código'));
    expect(await screen.findByText('Demasiados intentos')).toBeTruthy();
    expect(screen.getByText('Reenviar código')).toBeTruthy();
  });

  it('si el reenvío falla sin mensaje usa uno genérico', async () => {
    jest.useFakeTimers();
    const props = makeProps();
    props.onResend.mockRejectedValue({});
    await render(<EmailVerificationModal {...props} />);
    await advanceSeconds(60);
    await fireEvent.press(screen.getByText('Reenviar código'));
    await waitFor(() => expect(screen.getByText('No se pudo reenviar el código')).toBeTruthy());
  });

  it('mientras reenvía muestra "Enviando..."', async () => {
    jest.useFakeTimers();
    const props = makeProps();
    let resolveResend: () => void = () => {};
    props.onResend.mockReturnValue(new Promise<void>((resolve) => { resolveResend = resolve; }));
    await render(<EmailVerificationModal {...props} />);
    await advanceSeconds(60);
    // Con el reenvío pendiente el act de fireEvent espera a los timers (falsos): se avanzan a mano
    const pressing = fireEvent.press(screen.getByText('Reenviar código'));
    await act(async () => {
      jest.advanceTimersByTime(10);
    });
    expect(screen.getByText('Enviando...')).toBeTruthy();
    expect(props.onResend).toHaveBeenCalledTimes(1);
    resolveResend();
    await pressing;
    await act(async () => {
      jest.advanceTimersByTime(10);
    });
    expect(screen.getByText('Reenviar en 60s')).toBeTruthy();
  });
});
