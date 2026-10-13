import React, {forwardRef, useImperativeHandle, useState} from "react";
import {View} from "react-native";
import {COLORS} from "../../constants/Theme";
import {BEATS_PER_BAR} from "../../hooks/useMetronome";

export type MetronomeBeatIndicatorHandle = {
    /** Marca el golpe actual del compás (0 = acento), o ninguno con null. */
    setBeat: (beat: number | null) => void;
};

/** Tamaño de los puntos: todos iguales; el del golpe actual crece (más el acento). */
const BEAT_DOT_SIZE = 10;
const ACTIVE_BEAT_DOT_SIZE = 14;
const ACTIVE_ACCENT_DOT_SIZE = 18;

/**
 * Puntos del compás del metrónomo: se ilumina y crece el golpe actual.
 *
 * Lleva su propio estado y se actualiza por ref (`setBeat`), para que en cada golpe solo se
 * re-rendericen estos puntos y no toda la pantalla (slider y botones incluidos).
 */
const MetronomeBeatIndicator = forwardRef<MetronomeBeatIndicatorHandle>(function MetronomeBeatIndicator(_, ref) {
    const [currentBeat, setCurrentBeat] = useState<number | null>(null);
    useImperativeHandle(ref, () => ({setBeat: setCurrentBeat}), []);

    return (
        // Alto fijo: que el punto activo crezca no mueva el resto de la pantalla
        <View style={{flexDirection: 'row', alignItems: 'center', gap: 12, marginTop: 16, height: ACTIVE_ACCENT_DOT_SIZE}}>
            {[...Array(BEATS_PER_BAR).keys()].map((beat) => {
                const active = currentBeat === beat;
                const size = !active ? BEAT_DOT_SIZE : beat === 0 ? ACTIVE_ACCENT_DOT_SIZE : ACTIVE_BEAT_DOT_SIZE;
                return (
                    <View
                        key={beat}
                        style={{
                            width: size,
                            height: size,
                            borderRadius: size / 2,
                            backgroundColor: active ? COLORS.primary : COLORS.secondary,
                            opacity: active ? 1 : 0.25,
                        }}
                    />
                );
            })}
        </View>
    );
});

export default MetronomeBeatIndicator;
