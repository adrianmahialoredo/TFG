import {useEffect} from "react";
import {TouchableOpacity, View} from "react-native";
import Animated, {useAnimatedStyle, useSharedValue, withSpring} from "react-native-reanimated";
import Svg, {Circle, Line, Path, Polygon} from "react-native-svg";
import {COLORS} from "../../constants/Theme";
import {MAX_GAUGE_CENTS} from "../../utils/tuner.utils";

type TunerGaugeProps = {
    /** Desviación en cents (null: no hay lectura, la aguja descansa en el centro). */
    cents: number | null;
    /** Dentro del margen de afinado: la aguja se pone verde. */
    inTune: boolean;
    /** Pulsar el medidor empieza o deja de escuchar. */
    onPress: () => void;
    accessibilityLabel: string;
};

/** Medidas del semicírculo: radio exterior e interior (el hueco donde gira la aguja). */
const OUTER_RADIUS = 150;
const INNER_RADIUS = 72;
const WIDTH = OUTER_RADIUS * 2;
const CENTER_X = OUTER_RADIUS;
const CENTER_Y = OUTER_RADIUS;

const NEEDLE_LENGTH = 110;
const NEEDLE_WIDTH = 14;
const HUB_RADIUS = 13;
const HEIGHT = CENTER_Y + HUB_RADIUS + 2;

/** Verde de "afinado" (el dorado del tema se mantiene para el resto del medidor). */
export const IN_TUNE_COLOR = '#4CAF50';

/** Separadores del semicírculo: a 45º, 90º y 135º quedan 4 sectores, como en el diseño. */
const SEPARATOR_ANGLES = [45, 90, 135];

const pointAt = (radius: number, degrees: number) => {
    const radians = (degrees * Math.PI) / 180;
    return {x: CENTER_X + radius * Math.cos(radians), y: CENTER_Y - radius * Math.sin(radians)};
};

/** Semianillo superior: arco exterior de izquierda a derecha y vuelta por el interior. */
const ARC_PATH = [
    `M ${CENTER_X - OUTER_RADIUS} ${CENTER_Y}`,
    `A ${OUTER_RADIUS} ${OUTER_RADIUS} 0 0 1 ${CENTER_X + OUTER_RADIUS} ${CENTER_Y}`,
    `L ${CENTER_X + INNER_RADIUS} ${CENTER_Y}`,
    `A ${INNER_RADIUS} ${INNER_RADIUS} 0 0 0 ${CENTER_X - INNER_RADIUS} ${CENTER_Y}`,
    'Z',
].join(' ');

/** Ángulo de la aguja: 0º en el centro, ±90º en los extremos (±50 cents). */
export const centsToAngle = (cents: number | null): number => {
    if (cents === null || Number.isNaN(cents)) return 0;
    const clamped = Math.max(-MAX_GAUGE_CENTS, Math.min(MAX_GAUGE_CENTS, cents));
    return (clamped / MAX_GAUGE_CENTS) * 90;
};

/**
 * Medidor del afinador: semicírculo dorado con una aguja que marca la desviación.
 * Aguja vertical = afinada; a la izquierda, la nota está baja; a la derecha, alta.
 *
 * La aguja gira con Reanimated (en el hilo de UI, con muelle) para que los saltos entre
 * bloques de audio se vean suaves; gira sobre su base (`transformOrigin: 'bottom'`).
 */
export default function TunerGauge({cents, inTune, onPress, accessibilityLabel}: TunerGaugeProps) {
    const rotation = useSharedValue(0);
    const angle = centsToAngle(cents);

    useEffect(() => {
        rotation.value = withSpring(angle, {damping: 14, stiffness: 120});
    }, [angle, rotation]);

    const needleStyle = useAnimatedStyle(() => ({transform: [{rotate: `${rotation.value}deg`}]}));
    const needleColor = cents !== null && inTune ? IN_TUNE_COLOR : COLORS.primary;

    return (
        <TouchableOpacity
            onPress={onPress}
            activeOpacity={0.85}
            accessibilityRole="button"
            accessibilityLabel={accessibilityLabel}
            style={{alignSelf: 'center', width: WIDTH, height: HEIGHT}}
        >
            <Svg width={WIDTH} height={HEIGHT}>
                <Path d={ARC_PATH} fill={COLORS.primary} />
                {SEPARATOR_ANGLES.map((degrees) => {
                    const from = pointAt(INNER_RADIUS, degrees);
                    const to = pointAt(OUTER_RADIUS, degrees);
                    return (
                        <Line key={degrees} x1={from.x} y1={from.y} x2={to.x} y2={to.y}
                              stroke={COLORS.background} strokeWidth={2} />
                    );
                })}
            </Svg>

            <Animated.View
                testID="tuner-needle"
                pointerEvents="none"
                style={[{
                    position: 'absolute',
                    left: CENTER_X - NEEDLE_WIDTH / 2,
                    top: CENTER_Y - NEEDLE_LENGTH,
                    width: NEEDLE_WIDTH,
                    height: NEEDLE_LENGTH,
                    transformOrigin: 'bottom',
                    opacity: cents === null ? 0.5 : 1,
                }, needleStyle]}
            >
                <Svg width={NEEDLE_WIDTH} height={NEEDLE_LENGTH}>
                    <Polygon
                        testID="tuner-needle-shape"
                        points={`${NEEDLE_WIDTH / 2 - 2},0 ${NEEDLE_WIDTH / 2 + 2},0 ${NEEDLE_WIDTH},${NEEDLE_LENGTH} 0,${NEEDLE_LENGTH}`}
                        fill={needleColor}
                        stroke={COLORS.background}
                        strokeWidth={1.5}
                    />
                </Svg>
            </Animated.View>

            {/* Eje de la aguja (encima de ella) */}
            <View pointerEvents="none" style={{position: 'absolute', left: 0, top: 0}}>
                <Svg width={WIDTH} height={HEIGHT}>
                    <Circle cx={CENTER_X} cy={CENTER_Y} r={HUB_RADIUS} fill={needleColor}
                            stroke={COLORS.background} strokeWidth={1.5} />
                    <Circle cx={CENTER_X} cy={CENTER_Y} r={HUB_RADIUS / 2.5} fill={COLORS.background} />
                </Svg>
            </View>
        </TouchableOpacity>
    );
}
