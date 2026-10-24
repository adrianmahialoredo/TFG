import { render, screen, fireEvent } from '@testing-library/react-native';
import LessonSectionViewer from '@/components/LessonSectionViewer';
import { LessonSectionDTO } from '@/types';

jest.setTimeout(30000);

// Sustituto de RenderHtml: pinta el HTML recibido como texto
jest.mock('react-native-render-html', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { Text } = require('react-native');
  return { __esModule: true, default: (props: any) => mockJsx(Text, { testID: 'html', children: props.source.html }) };
});

jest.mock('lucide-react-native', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  const make = (name: string) => (props: any) => mockJsx(View, { testID: `icon-${name}`, ...props });
  return { ChevronLeft: make('ChevronLeft'), ChevronRight: make('ChevronRight') };
});

const sections: LessonSectionDTO[] = [
  { id: 's1', lessonId: 'l1', title: 'Intro', htmlContent: '<h1>Intro</h1>', orderIndex: 0 },
  { id: 's2', lessonId: 'l1', title: 'Práctica', htmlContent: '<p>Practica</p>', orderIndex: 1 },
  { id: 's3', lessonId: 'l1', title: 'Final', htmlContent: '<p>Fin</p>', orderIndex: 2 },
];

const makeProps = () => ({
  sections,
  currentSection: sections[1],
  sectionIndex: 1,
  hasPrevious: true,
  hasNext: true,
  goToPrevious: jest.fn(),
  goToNext: jest.fn(),
  loading: false,
});

describe('LessonSectionViewer', () => {
  it('muestra el vídeo de YouTube de la sección actual si tiene', async () => {
    const props = makeProps();
    props.currentSection = { ...sections[1], videoUrl: 'https://youtu.be/dQw4w9WgXcQ' };
    await render(<LessonSectionViewer {...props} />);
    expect(screen.getByText('YouTube dQw4w9WgXcQ')).toBeTruthy();
  });

  it('sin vídeo en la sección no muestra el reproductor', async () => {
    await render(<LessonSectionViewer {...makeProps()} />);
    expect(screen.queryByText(/YouTube/)).toBeNull();
  });

  it('muestra el HTML de la sección actual y la posición i/n', async () => {
    await render(<LessonSectionViewer {...makeProps()} />);
    expect(screen.getByTestId('html')).toHaveTextContent('<p>Practica</p>');
    expect(screen.getByText('2/3')).toBeTruthy();
  });

  it('las flechas llaman a goToPrevious y goToNext', async () => {
    const props = makeProps();
    await render(<LessonSectionViewer {...props} />);
    await fireEvent.press(screen.getByTestId('icon-ChevronLeft'));
    expect(props.goToPrevious).toHaveBeenCalledTimes(1);
    await fireEvent.press(screen.getByTestId('icon-ChevronRight'));
    expect(props.goToNext).toHaveBeenCalledTimes(1);
  });

  it('en la primera sección la flecha de atrás no hace nada', async () => {
    const props = { ...makeProps(), currentSection: sections[0], sectionIndex: 0, hasPrevious: false };
    await render(<LessonSectionViewer {...props} />);
    expect(screen.getByText('1/3')).toBeTruthy();
    await fireEvent.press(screen.getByTestId('icon-ChevronLeft'));
    expect(props.goToPrevious).not.toHaveBeenCalled();
  });

  it('en la última sección la flecha de siguiente no hace nada', async () => {
    const props = { ...makeProps(), currentSection: sections[2], sectionIndex: 2, hasNext: false };
    await render(<LessonSectionViewer {...props} />);
    await fireEvent.press(screen.getByTestId('icon-ChevronRight'));
    expect(props.goToNext).not.toHaveBeenCalled();
  });

  it('con una sola sección no muestra la navegación', async () => {
    await render(<LessonSectionViewer {...makeProps()} sections={[sections[0]]} currentSection={sections[0]} sectionIndex={0} />);
    expect(screen.queryByText('1/1')).toBeNull();
    expect(screen.queryByTestId('icon-ChevronLeft')).toBeNull();
  });

  it('sin contenido y cargando muestra un spinner', async () => {
    await render(<LessonSectionViewer {...makeProps()} sections={[]} currentSection={undefined} loading />);
    expect(screen.container.queryAll((n) => n.type === 'ActivityIndicator')).toHaveLength(1);
    expect(screen.queryByTestId('html')).toBeNull();
  });

  it('sin contenido y sin cargar muestra el error', async () => {
    await render(<LessonSectionViewer {...makeProps()} sections={[]} currentSection={undefined} />);
    expect(screen.getByText('No se pudo cargar el contenido.')).toBeTruthy();
  });
});
