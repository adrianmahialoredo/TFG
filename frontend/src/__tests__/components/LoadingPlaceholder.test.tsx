import { render, screen } from '@testing-library/react-native';
import { LoadingPlaceholder } from '@/components/LoadingPlaceholder';

jest.setTimeout(30000);

describe('LoadingPlaceholder', () => {
  it('muestra el spinner y el texto de carga', async () => {
    await render(<LoadingPlaceholder />);
    expect(screen.getByText('Cargando perfil musical...')).toBeTruthy();
    expect(screen.container.queryAll((n) => n.type === 'ActivityIndicator')).toHaveLength(1);
  });
});
