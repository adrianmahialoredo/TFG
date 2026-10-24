import { render, screen, fireEvent } from '@testing-library/react-native';
import { ProfileInput } from '@/components/ProfileInput';

jest.setTimeout(30000);

describe('ProfileInput', () => {
  it('muestra la etiqueta con dos puntos, el valor y el placeholder', async () => {
    await render(<ProfileInput label="Nombre" value="Adrián" onChange={jest.fn()} placeholder="Tu nombre" />);
    expect(screen.getByText('Nombre:')).toBeTruthy();
    expect(screen.getByDisplayValue('Adrián')).toBeTruthy();
    expect(screen.getByPlaceholderText('Tu nombre')).toBeTruthy();
  });

  it('propaga los cambios de texto', async () => {
    const onChange = jest.fn();
    await render(<ProfileInput label="Nombre" value="" onChange={onChange} placeholder="Tu nombre" />);
    await fireEvent.changeText(screen.getByPlaceholderText('Tu nombre'), 'Nuevo');
    expect(onChange).toHaveBeenCalledWith('Nuevo');
  });

  it('es editable por defecto', async () => {
    await render(<ProfileInput label="Nombre" value="x" onChange={jest.fn()} />);
    expect(screen.getByDisplayValue('x').props.editable).toBe(true);
  });

  it('no es editable con editable=false', async () => {
    await render(<ProfileInput label="Nombre" value="x" onChange={jest.fn()} editable={false} />);
    expect(screen.getByDisplayValue('x').props.editable).toBe(false);
  });

  it('readOnly lo hace no editable aunque editable sea true', async () => {
    await render(<ProfileInput label="Email" value="a@b.com" onChange={jest.fn()} editable readOnly />);
    expect(screen.getByDisplayValue('a@b.com').props.editable).toBe(false);
  });
});
