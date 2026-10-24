import React from 'react';
import { View, Text, Image, TouchableOpacity } from 'react-native';
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
    <BaseModal visible={!!chord} onClose={onClose}>
      {chord && (
        <View className="items-center">
          <Text className="text-primary text-2xl font-bold mb-4">{chord.name}</Text>

          {/* Los diagramas son líneas oscuras sobre fondo transparente: necesitan un fondo claro */}
          <View className="bg-secondary rounded-lg p-2 mb-4">
            <Image source={{ uri: chord.imageUrl }} className="w-32 h-40" resizeMode="contain" />
          </View>

          <View className="flex-row flex-wrap justify-center gap-2 mb-5">
            {flags.map((label) => (
              <View key={label} className="px-3 py-1 bg-primary rounded-lg border border-secondary/20">
                <Text className="text-secondary text-base">{label}</Text>
              </View>
            ))}
            <View className="px-3 py-1 bg-primary rounded-lg border border-secondary/20">
              <Text className="text-secondary text-base">{capoFret > 0 ? `Capo: ${capoFret}` : 'Sin capo'}</Text>
            </View>
          </View>

          {/* Estilos en línea: con className NativeWind pisa la opacidad animada del TouchableOpacity */}
          <TouchableOpacity
            onPress={onClose}
            activeOpacity={0.8}
            accessibilityRole="button"
            style={{ paddingHorizontal: 28, paddingVertical: 10, borderRadius: 9999, backgroundColor: COLORS.secondary }}
          >
            <Text style={{ color: COLORS.primary, fontWeight: '600', fontSize: 16 }}>Cerrar</Text>
          </TouchableOpacity>
        </View>
      )}
    </BaseModal>
  );
}
