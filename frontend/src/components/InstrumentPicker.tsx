import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, ActivityIndicator } from 'react-native';
import { CirclePlus } from 'lucide-react-native';
import { useInstruments } from '../hooks/useInstruments';
import { COLORS } from '../constants/Theme';

type Props = {
    onSelectionChange?: (selectedIds: string[]) => void;
};

export default function InstrumentPicker({ onSelectionChange }: Props) {
    const { instruments, loading } = useInstruments();
    const [selectedIds, setSelectedIds] = useState<string[]>([]);

    useEffect(() => {
        onSelectionChange?.(selectedIds);
    }, [selectedIds]);

    const toggleInstrument = (id: string) => {
        setSelectedIds(prev =>
            prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
        );
    };

    if (loading) return <ActivityIndicator color={COLORS.secondary} className="my-4" />;

    return (
        <View className="mb-8">
            <Text className="text-base text-secondary font-semibold mb-4">
                ¿Qué instrumentos te gustaría aprender?
            </Text>

            <View className="flex-row flex-wrap gap-2">
                {instruments.map((instrument) => {
                    const isSelected = selectedIds.includes(instrument.id.toString());
                    return (
                        <TouchableOpacity
                            key={instrument.id}
                            onPress={() => toggleInstrument(instrument.id.toString())}
                            className={`px-3 py-1 bg-secondary rounded-lg ${
                                isSelected ? "opacity-60" : ""
                            }`}
                        >
                            <Text className="text-primary text-base font-medium">
                                {instrument.name}
                            </Text>
                        </TouchableOpacity>
                    );
                })}
            </View>
        </View>
    );
}