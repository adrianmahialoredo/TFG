import { render, screen, fireEvent } from '@testing-library/react-native';
import ChordModal from '@/components/ChordModal';
import { ChordDTO } from '@/types';

jest.setTimeout(30000);

const chord: ChordDTO = {
  id: 'c1',
  name: 'Gmaj7',
  notes: ['G', 'B', 'D', 'F#'],
  imageUrl: 'https://img/gmaj7.png',
  open: true,
  barre: false,
  powerChord: false,
  minor: false,
  major: true,
  seventh: true,
  capo: false,
  capoFret: 0,
};

describe('ChordModal', () => {
  it('sin acorde está cerrado', async () => {
    await render(<ChordModal chord={null} onClose={jest.fn()} />);
    expect(screen.queryByText('Cerrar')).toBeNull();
  });

  it('muestra el nombre y el diagrama del acorde', async () => {
    await render(<ChordModal chord={chord} onClose={jest.fn()} />);
    expect(screen.getByText('Gmaj7')).toBeTruthy();
    const image = screen.container.queryAll((n) => n.type === 'Image')[0];
    expect(image.props.source).toEqual({ uri: 'https://img/gmaj7.png' });
  });

  it('muestra un tag por cada propiedad activa, en orden, y ninguna de las inactivas', async () => {
    await render(<ChordModal chord={chord} onClose={jest.fn()} />);
    const texts = screen.container.queryAll((n) => n.type === 'Text').map((n) => n.props.children);
    expect(texts).toEqual(['Gmaj7', 'Mayor', 'Séptima', 'Abierto', 'Sin capo', 'Cerrar']);
  });

  it('muestra todas las etiquetas cuando todas las propiedades están activas', async () => {
    const full = { ...chord, major: true, minor: true, seventh: true, powerChord: true, open: true, barre: true };
    await render(<ChordModal chord={full} onClose={jest.fn()} />);
    for (const label of ['Mayor', 'Menor', 'Séptima', 'Power chord', 'Abierto', 'Cejilla']) {
      expect(screen.getByText(label)).toBeTruthy();
    }
  });

  it('muestra el traste del capo si es mayor que 0', async () => {
    await render(<ChordModal chord={{ ...chord, capo: true, capoFret: 3 }} onClose={jest.fn()} />);
    expect(screen.getByText('Capo: 3')).toBeTruthy();
    expect(screen.queryByText('Sin capo')).toBeNull();
  });

  it('sin capoFret se trata como sin capo', async () => {
    await render(<ChordModal chord={{ ...chord, capoFret: undefined }} onClose={jest.fn()} />);
    expect(screen.getByText('Sin capo')).toBeTruthy();
  });

  it('el botón "Cerrar" llama a onClose', async () => {
    const onClose = jest.fn();
    await render(<ChordModal chord={chord} onClose={onClose} />);
    await fireEvent.press(screen.getByText('Cerrar'));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('el botón atrás de Android cierra el modal', async () => {
    const onClose = jest.fn();
    await render(<ChordModal chord={chord} onClose={onClose} />);
    const modal = screen.container.queryAll((n) => n.type === 'Modal')[0];
    modal.props.onRequestClose();
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
