import ProfileHeader from "../components/ProfileHeader";
import AppLayout from "../components/AppLayout";
import {Text, View, ScrollView} from "react-native";
import {COLORS} from "../constants/Theme";
import React, {useCallback, useRef, useState} from "react";
import {useFocusEffect} from "@react-navigation/native";
import MetronomeSlider, {clampTempo, DEFAULT_TEMPO} from "../components/metronome/MetronomeSlider";
import MetronomePulse, {MetronomePulseHandle} from "../components/metronome/MetronomePulse";
import MetronomeBeatIndicator, {MetronomeBeatIndicatorHandle} from "../components/metronome/MetronomeBeatIndicator";
import MetronomeActionButton from "../components/metronome/MetronomeActionButton";
import {MetronomeSound, useMetronome, useTapTempo} from "../hooks/useMetronome";

/**
 * Alto fijo de la zona del pulso: cabe la onda más grande (100 px × 2.3) y los puntos del compás.
 * No puede ser `flex: 1` dentro de un ScrollView: se quedaría a 0 y taparía el botón de play.
 */
const PULSE_AREA_HEIGHT = 300;

export default function MetronomeScreen() {
    const [tempo, setTempo] = useState(DEFAULT_TEMPO);
    const [sound, setSound] = useState<MetronomeSound>('click');

    // La onda y los puntos se actualizan por ref: en cada golpe no se re-renderiza la pantalla
    const pulseRef = useRef<MetronomePulseHandle>(null);
    const beatIndicatorRef = useRef<MetronomeBeatIndicatorHandle>(null);

    const handleBeat = useCallback((beatInBar: number) => {
        pulseRef.current?.pulse(beatInBar);
        beatIndicatorRef.current?.setBeat(beatInBar);
    }, []);

    const {isPlaying, toggle, stop} = useMetronome(tempo, sound, handleBeat);
    const {tap} = useTapTempo();

    // Las pantallas del tab no se desmontan: al salir de la pantalla se para el metrónomo
    useFocusEffect(useCallback(() => () => {
        stop();
        beatIndicatorRef.current?.setBeat(null);
    }, [stop]));

    const handleToggle = () => {
        if (isPlaying) beatIndicatorRef.current?.setBeat(null);
        toggle();
    };

    const handleTap = () => {
        const bpm = tap();
        if (bpm !== null) setTempo(clampTempo(bpm));
    };

    const isHiHat = sound === 'hihat';

    return (
        <AppLayout>
            <View className = "bg-background flex-1 pt-2">
                <ProfileHeader title={"Metrónomo"} />

                {/* Scroll con hueco abajo para la tab bar (absoluta), como el resto de pantallas */}
                <ScrollView
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={{paddingBottom: 120}}
                >
                    <MetronomeSlider tempo={tempo} onTempoChange={setTempo} />

                    {/** PULSO DEL METRÓNOMO: el círculo central es el botón de play/stop */}
                    <View style={{height: PULSE_AREA_HEIGHT, justifyContent: 'center', alignItems: 'center'}}>
                        <MetronomePulse ref={pulseRef} tempo={tempo} isPlaying={isPlaying} onToggle={handleToggle} />

                        <Text style={{color: COLORS.secondary, opacity: 0.6, marginTop: 72}}>
                            {isPlaying ? 'Toca para parar' : 'Toca para empezar'}
                        </Text>

                        <MetronomeBeatIndicator ref={beatIndicatorRef} />
                    </View>

                    <View style={{gap: 4}}>
                        <MetronomeActionButton label="TAP TEMPO" onPress={handleTap} large />
                        {/** Alterna el sonido entre click y hi-hat (se aplica al siguiente golpe si está sonando) */}
                        <MetronomeActionButton
                            label={isHiHat ? 'Hi-Hat ✓' : 'Hi-Hat'}
                            onPress={() => setSound(isHiHat ? 'click' : 'hihat')}
                            selected={isHiHat}
                        />
                    </View>
                </ScrollView>
            </View>
        </AppLayout>
    );
}
