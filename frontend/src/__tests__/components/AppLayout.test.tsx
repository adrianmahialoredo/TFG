import { Text } from 'react-native';
import { render, screen } from '@testing-library/react-native';
import AppLayout from '@/components/AppLayout';

jest.setTimeout(30000);

describe('AppLayout', () => {
  it('renderiza sus hijos', async () => {
    await render(<AppLayout><Text>Hola</Text></AppLayout>);
    expect(screen.getByText('Hola')).toBeTruthy();
  });

  it('acepta un color de fondo personalizado', async () => {
    await render(<AppLayout backgroundColor="bg-primary"><Text>Con fondo</Text></AppLayout>);
    expect(screen.getByText('Con fondo')).toBeTruthy();
  });
});
