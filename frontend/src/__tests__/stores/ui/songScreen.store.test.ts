import { useSongScreenStore } from '../../../stores/ui/songScreen.store';
import { DifficultyLevel } from '../../../types';

const state = () => useSongScreenStore.getState();

describe('songScreen.store', () => {
  beforeEach(() => {
    state().reset();
  });

  it('empieza cargando y sin selección', () => {
    expect(state()).toMatchObject({
      isLoading: true,
      error: null,
      selectedInstrumentId: null,
      selectedDifficulty: null,
    });
  });

  it('setLoading y setError actualizan su campo', () => {
    state().setLoading(false);
    state().setError('fallo');
    expect(state()).toMatchObject({ isLoading: false, error: 'fallo' });
    state().setError(null);
    expect(state().error).toBeNull();
  });

  it('setInstrument guarda el instrumento elegido', () => {
    state().setInstrument('guitarra');
    expect(state().selectedInstrumentId).toBe('guitarra');
  });

  it('setDifficulty guarda la dificultad y termina la carga', () => {
    state().setDifficulty(DifficultyLevel.MEDIUM);
    expect(state()).toMatchObject({ selectedDifficulty: DifficultyLevel.MEDIUM, isLoading: false });
  });

  it('reset vuelve al estado inicial', () => {
    state().setInstrument('piano');
    state().setDifficulty(DifficultyLevel.ORIGINAL);
    state().setError('x');
    state().reset();
    expect(state()).toMatchObject({
      isLoading: true,
      error: null,
      selectedInstrumentId: null,
      selectedDifficulty: null,
    });
  });
});
