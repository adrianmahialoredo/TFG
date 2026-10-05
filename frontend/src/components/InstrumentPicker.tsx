import React, { useState } from 'react';
import { View, Text, ActivityIndicator } from 'react-native';
import { useInstruments } from '../hooks/useInstruments';
import { COLORS } from '../constants/Theme';
import SelectableChip from './SelectableChip';

type Props = {
    onSelectionChange?: (selectedIds: string[]) => void;
    initialSelection?: string[];
};

export default function InstrumentPicker({ onSelectionChange, initialSelection = [] }: Props) {
    const { instruments, loading } = useInstruments();
    const [selectedIds, setSelectedIds] = useState<string[]>(initialSelection);

    // Se avisa al padre en el mismo toque (no en un useEffect, que llegaría un render tarde)
    const toggleInstrument = (id: string) => {
        const next = selectedIds.includes(id) ? selectedIds.filter(i => i !== id) : [...selectedIds, id];
        setSelectedIds(next);
        onSelectionChange?.(next);
    };

    if (loading) return <ActivityIndicator color={COLORS.secondary} className="my-4" />;

    return (
        <View className="mb-8">
            <Text className="text-base text-secondary font-semibold mb-4">
                ¿Qué instrumentos te gustaría aprender?
            </Text>

            <View className="flex-row flex-wrap gap-2">
                {instruments.map((instrument) => (
                    <SelectableChip
                        key={instrument.id}
                        label={instrument.name}
                        selected={selectedIds.includes(instrument.id.toString())}
                        onPress={() => toggleInstrument(instrument.id.toString())}
                    />
                ))}
            </View>
        </View>
    );
}
