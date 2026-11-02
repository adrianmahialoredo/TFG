import ProfileHeader from "../components/ProfileHeader";
import AppLayout from "../components/AppLayout";
import {ActivityIndicator, Linking, Text, View} from "react-native";
import Touchable from "../components/Touchable";
import FadeScrollView from "../components/FadeScrollView";
import {COLORS} from "../constants/Theme";
import React, {useCallback, useState} from "react";
import {useFocusEffect} from "@react-navigation/native";
import TunerStringSelector from "../components/tuner/TunerStringSelector";
import TunerGauge from "../components/tuner/TunerGauge";
import TuneWheel from "../components/tuner/TuneWheel";
import {TunerStatus, useTuner} from "../hooks/useTuner";
import {useTunes} from "../hooks/useTunes";
import {IN_TUNE_CENTS, tuningAdvice} from "../utils/tuner.utils";

/** Texto bajo la desviación cuando aún no hay ninguna nota. */
const STATUS_HINTS: Record<TunerStatus, string> = {
    idle: 'Toca el medidor para escuchar',
    starting: 'Preparando el micrófono…',
    listening: 'Toca una cuerda',
    denied: 'Sin acceso al micrófono',
    error: 'No se pudo usar el micrófono',
};

/** Desviación en cents con signo: "+3", "-12", "0". */
const formatCents = (cents: number) => {
    const rounded = Math.round(cents);
    return rounded > 0 ? `+${rounded}` : `${rounded}`;
};

/**
 * Afinador cromático (CU-11): escucha el micrófono y marca cuánto se desvía la cuerda
 * de la nota de la afinación elegida.
 *
 * - Empieza a escuchar al entrar en la pantalla y para al salir (los tabs no se desmontan).
 * - Elegir otra afinación en la rueda deja de escuchar; se vuelve a escuchar tocando el
 *   medidor o una cuerda.
 * - Las cuerdas se detectan solas (la más cercana a lo que suena); tocar una la fija.
 * - En Expo Go no hay micrófono: `useTuner` simula una cuerda que se va afinando y se avisa aquí.
 */
export default function TunerScreen() {
    const {tunes, defaultTuneId, loading} = useTunes();
    const [selectedTuneId, setSelectedTuneId] = useState<string | null>(null);

    const selectedIndex = Math.max(0, tunes.findIndex((tune) => tune.id === (selectedTuneId ?? defaultTuneId)));
    const tune = tunes[selectedIndex];

    const {status, isListening, simulated, reading, lockedString, start, stop, selectString} = useTuner(tune?.notes ?? []);

    useFocusEffect(useCallback(() => {
        start();
        return () => { stop(); };
    }, [start, stop]));

    // Callbacks estables: la rueda y las cuerdas van con memo y no se repintan con cada lectura
    const handleTuneSelect = useCallback((index: number) => {
        const next = tunes[index];
        if (!next) return;
        stop();
        setSelectedTuneId(next.id);
    }, [tunes, stop]);

    const handleStringPress = useCallback((index: number) => {
        selectString(lockedString === index ? null : index);
        if (!isListening) start();
    }, [selectString, lockedString, isListening, start]);

    const handleGaugePress = () => (isListening ? stop() : start());

    const cents = reading?.cents ?? null;
    const inTune = cents !== null && Math.abs(cents) <= IN_TUNE_CENTS;

    return (
        <AppLayout>
            <View className = "bg-background flex-1 pt-2">
                <ProfileHeader title={"Afinador"} />

                {/* Scroll con hueco abajo para la tab bar (absoluta), como el resto de pantallas */}
                <FadeScrollView
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={{paddingBottom: 120}}
                >
                    {loading && tunes.length === 0 ? (
                        <ActivityIndicator color={COLORS.primary} style={{marginTop: 48}} />
                    ) : !tune ? (
                        <Text style={{color: COLORS.secondary, textAlign: 'center', marginTop: 48}}>
                            No hay afinaciones disponibles. Comprueba tu conexión.
                        </Text>
                    ) : (
                        <>
                            {/** Expo Go no incluye el módulo nativo del micrófono: suena una cuerda simulada */}
                            {simulated && (
                                <Text style={{color: COLORS.artistName, fontSize: 13, textAlign: 'center', paddingHorizontal: 16}}>
                                    Modo demostración: en Expo Go no hay micrófono y suena una cuerda simulada.
                                    El micrófono real necesita la development build.
                                </Text>
                            )}

                            {/** CUERDAS DE LA AFINACIÓN */}
                            <View className="pt-6">
                                <TunerStringSelector
                                    notes={tune.notes}
                                    activeIndex={reading?.stringIndex ?? lockedString}
                                    lockedIndex={lockedString}
                                    onSelect={handleStringPress}
                                />
                            </View>

                            {/** DESVIACIÓN Y QUÉ HACER CON LA CLAVIJA */}
                            <View className="items-center pt-6">
                                <Text style={{color: COLORS.songTitle, fontSize: 24, fontWeight: '600'}}>
                                    {cents === null ? '--' : formatCents(cents)}
                                </Text>
                                <Text style={{color: COLORS.songTitle, fontSize: 18}}>
                                    {cents === null ? STATUS_HINTS[status] : tuningAdvice(cents)}
                                </Text>
                            </View>

                            {/** MEDIDOR: la aguja vertical es afinado; tocarlo empieza o deja de escuchar */}
                            <View className="pt-4">
                                <TunerGauge
                                    cents={cents}
                                    inTune={inTune}
                                    onPress={handleGaugePress}
                                    accessibilityLabel={isListening ? 'Dejar de escuchar' : 'Escuchar'}
                                />
                            </View>

                            {/** FRECUENCIA DETECTADA */}
                            <View className="items-center pt-3">
                                <Text style={{color: COLORS.songTitle, fontSize: 28, fontWeight: '500'}}>
                                    {reading ? `${Math.round(reading.frequency)} hz` : '-- hz'}
                                </Text>
                            </View>

                            {status === 'denied' && (
                                <View className="items-center pt-4 px-6">
                                    <Text style={{color: COLORS.secondary, textAlign: 'center', marginBottom: 12}}>
                                        El afinador necesita el micrófono para escuchar tu instrumento.
                                    </Text>
                                    <Touchable
                                        onPress={() => Linking.openSettings()}
                                        style={{paddingVertical: 10, paddingHorizontal: 20, borderRadius: 6, backgroundColor: COLORS.primary}}
                                    >
                                        <Text style={{color: COLORS.secondary, fontWeight: '600'}}>Abrir ajustes</Text>
                                    </Touchable>
                                </View>
                            )}

                            {/** RUEDA DE AFINACIONES (cambiar de afinación deja de escuchar) */}
                            <View className="pt-10">
                                <TuneWheel options={tunes} selectedIndex={selectedIndex} onSelect={handleTuneSelect} />
                            </View>
                        </>
                    )}
                </FadeScrollView>
            </View>
        </AppLayout>
    );
}
