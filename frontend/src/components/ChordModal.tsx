import React from 'react';
import { View, Text, Image } from 'react-native';
import Touchable from './Touchable';
import BaseModal from './BaseModal';
import { ChordDTO } from '../types';
import { COLORS } from '../constants/Theme';

type ChordFlag = 'major' | 'minor' | 'seventh' | 'powerChord' | 'open' | 'barre';

/** Etiqueta de cada propiedad del acorde, en el orden en que se muestran. */
const FLAG_LABELS: [ChordFlag, string][] = [
  ['major', 'Mayor'],
  ['minor', 'Menor'],
  ['seventh', 'Séptima'],
  ['powerChord', 'Power chord'],
  ['open', 'Abierto'],
  ['barre', 'Cejilla'],
];

type Props = {
  /** Acorde a mostrar; con null el modal está cerrado */
  chord: ChordDTO | null;
  onClose: () => void;
};

/**
 * Detalle de un acorde: nombre, diagrama, sus propiedades como tags y el traste del capo.
 * Se abre al pulsar un acorde en la pantalla de la canción.
 */
export default function ChordModal({ chord, onClose }: Props) {
  const flags = chord ? FLAG_LABELS.filter(([flag]) => chord[flag]).map(([, label]) => label) : [];
  const capoFret = chord?.capoFret ?? 0;

  return (
    <BaseModal visible={!!chord} onClose={onClose} maxWidth={300}>
      {chord && (
        <View className="items-center">
          <Text className="text-primary text-2xl font-bold mb-3">{chord.name}</Text>

          {/* Los diagramas son líneas oscuras sobre fondo transparente: necesitan un fondo claro */}
          <View className="bg-secondary rounded-lg p-1 mb-3">
            <Image source={{ uri: chord.imageUrl }} className="w-24 h-32" resizeMode="contain" />
          </View>

          <View className="flex-row flex-wrap justify-center gap-1.5 mb-4">
            {flags.map((label) => (
              <View key={label} className="px-2 py-0.5 bg-primary rounded-md border border-secondary/20">
                <Text className="text-secondary text-sm">{label}</Text>
              </View>
            ))}
            <View className="px-2 py-0.5 bg-primary rounded-md border border-secondary/20">
              <Text className="text-secondary text-sm">{capoFret > 0 ? `Capo: ${capoFret}` : 'Sin capo'}</Text>
            </View>
          </View>

          <Touchable
            onPress={onClose}
            accessibilityRole="button"
            style={{ paddingHorizontal: 24, paddingVertical: 8, borderRadius: 9999, backgroundColor: COLORS.secondary }}
          >
            <Text style={{ color: COLORS.primary, fontWeight: '600', fontSize: 16 }}>Cerrar</Text>
          </Touchable>
        </View>
      )}
    </BaseModal>
  );
}
