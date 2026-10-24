import { StyleSheet } from 'react-native';
import { render, screen } from '@testing-library/react-native';
import EmptyCarouselCard from '@/components/carousels/EmptyCarouselCard';

jest.setTimeout(30000);

describe('EmptyCarouselCard', () => {
  it('muestra el mensaje y respeta el alto mínimo', async () => {
    await render(<EmptyCarouselCard message="Nada por aquí" minHeight={93} />);
    expect(screen.getByText('Nada por aquí')).toBeTruthy();
    const withMinHeight = screen.container.queryAll(
      (n) => n.type === 'View' && StyleSheet.flatten(n.props.style)?.minHeight === 93
    );
    expect(withMinHeight).toHaveLength(1);
  });
});
