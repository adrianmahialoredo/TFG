import { render, screen, fireEvent } from '@testing-library/react-native';
import MetronomeTempoButton from '@/components/metronome/MetronomeTempoButton';

jest.setTimeout(30000);

describe('MetronomeTempoButton', () => {
  it('muestra el título y llama a onPress', async () => {
    const onPress = jest.fn();
    await render(<MetronomeTempoButton title="+10" onPress={onPress} />);
    await fireEvent.press(screen.getByText('+10'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });
});
