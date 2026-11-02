import React, {forwardRef, useImperativeHandle, useRef} from "react";
import {StyleSheet, View} from "react-native";
import Touchable from "../Touchable";
import Animated, {Easing, useAnimatedStyle, useSharedValue, withSequence, withTiming} from "react-native-reanimated";
import {Play, Square} from "lucide-react-native";
import {COLORS} from "../../constants/Theme";

export type MetronomePulseHandle = {
    /** Lanza una onda para un golpe (0 = acento: onda más grande y más opaca). */
    pulse: (beatInBar: number) => void;
};

type MetronomePulseProps = {
    tempo: number;
    isPlaying: boolean;
    onToggle: () => void;
};

/** Ondas que pueden estar animándose a la vez (a tempos altos se solapan). */
const RIPPLE_COUNT = 3;
/** Duración máxima de una onda: a tempos lentos no se alarga indefinidamente. */
const MAX_RIPPLE_MS = 700;

/** Escala final de la onda: 1.8 normal, 2.3 en el acento. */
const RIPPLE_GROWTH = 0.8;
const RIPPLE_ACCENT_EXTRA_GROWTH = 0.5;
/** Opacidad inicial de la onda: 0.6 normal, 0.9 en el acento. */
const RIPPLE_OPACITY = 0.6;
const RIPPLE_ACCENT_EXTRA_OPACITY = 0.3;

const DOT_SIZE = 100;

const styles = StyleSheet.create({
    dot: {
        width: DOT_SIZE,
        height: DOT_SIZE,
        borderRadius: DOT_SIZE / 2,
        backgroundColor: COLORS.primary,
    },
    center: {
        justifyContent: 'center',
        alignItems: 'center',
    },
});

type RippleHandle = {
    /** Lanza la onda desde el círculo hacia fuera. */
    fire: (isAccent: boolean, duration: number) => void;
};

/**
 * Una onda del pulso, con Reanimated: sus valores viven en el hilo de UI, así que se anima
 * aunque la pantalla no se re-renderice (con `Animated` de React Native y la nueva arquitectura
 * la onda no llegaba a moverse, ni con el driver nativo ni con el de JS).
 *  - progress: 0 al empezar el golpe -> 1 al terminar (en reposo vale 1: invisible).
 *  - accent: 1 si el golpe es el acento (onda más grande y más opaca), 0 si no.
 */
const Ripple = forwardRef<RippleHandle>(function Ripple(_, ref) {
    const progress = useSharedValue(1);
    const accent = useSharedValue(0);

    useImperativeHandle(ref, () => ({
        fire: (isAccent: boolean, duration: number) => {
            accent.value = isAccent ? 1 : 0;
            // Reinicio a 0 y crecimiento hasta 1, en el mismo hilo y en orden
            progress.value = withSequence(
                withTiming(0, {duration: 0}),
                withTiming(1, {duration, easing: Easing.out(Easing.ease)}),
            );
        },
    }), [accent, progress]);

    const animatedStyle = useAnimatedStyle(() => ({
        opacity: (1 - progress.value) * (RIPPLE_OPACITY + RIPPLE_ACCENT_EXTRA_OPACITY * accent.value),
        transform: [{scale: 1 + progress.value * (RIPPLE_GROWTH + RIPPLE_ACCENT_EXTRA_GROWTH * accent.value)}],
    }));

    return (
        <Animated.View
            pointerEvents="none"
            style={[StyleSheet.absoluteFill, styles.dot, animatedStyle]}
        />
    );
});

/**
 * Círculo central del metrónomo: es el botón de play/stop y emite una onda en cada golpe.
 *
 * Las ondas se lanzan por ref (`pulse`) en vez de por props, para no re-renderizar la pantalla
 * en cada golpe; las animaciones van con Reanimated en el hilo de UI.
 */
const MetronomePulse = forwardRef<MetronomePulseHandle, MetronomePulseProps>(function MetronomePulse({tempo, isPlaying, onToggle}, ref) {
    // Ondas: se reutilizan por turnos, una por golpe
    const rippleRefs = useRef<(RippleHandle | null)[]>([]);
    const nextRippleRef = useRef(0);
    const tempoRef = useRef(tempo);
    tempoRef.current = tempo;

    useImperativeHandle(ref, () => ({
        pulse: (beatInBar: number) => {
            const ripple = rippleRefs.current[nextRippleRef.current];
            nextRippleRef.current = (nextRippleRef.current + 1) % RIPPLE_COUNT;

            const duration = Math.min(MAX_RIPPLE_MS, (60000 / tempoRef.current) * RIPPLE_COUNT * 0.8);
            ripple?.fire(beatInBar === 0, duration);
        },
    }), []);

    return (
        <View style={styles.center}>
            {[...Array(RIPPLE_COUNT).keys()].map((index) => (
                <Ripple key={index} ref={(ripple) => { rippleRefs.current[index] = ripple; }} />
            ))}
            <Touchable
                onPress={onToggle}
                accessibilityRole="button"
                accessibilityLabel={isPlaying ? 'Parar metrónomo' : 'Iniciar metrónomo'}
                style={[styles.dot, styles.center]}
            >
                {isPlaying
                    ? <Square size={34} color={COLORS.background} fill={COLORS.background} />
                    : <Play size={40} color={COLORS.background} fill={COLORS.background} style={{marginLeft: 6}} />}
            </Touchable>
        </View>
    );
});

export default MetronomePulse;
