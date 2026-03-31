import ProfileHeader from "../components/ProfileHeader";
import AppLayout from "../components/AppLayout";
import {Animated, Text, TouchableOpacity, View, StyleSheet, Easing} from "react-native";
import {COLORS} from "../constants/Theme";
import React, {useEffect, useRef, useState} from "react";
import MetronomeSlider from "../components/MetronomeSlider";

type RippleCircleProps = {
    delay: number;
    duration: number;
};

const styles = StyleSheet.create({
    dot: {
        width: 100,
        height: 100,
        borderRadius: 50,
        backgroundColor: COLORS.primary,
    },
    baseDot: {
        opacity: 0.5,
    },
    center: {
        justifyContent: 'center',
        alignItems: 'center',
    },
});

function RippleCircle({delay, duration}: RippleCircleProps) {
    const scale = useRef(new Animated.Value(1)).current;
    const opacity = useRef(new Animated.Value(1)).current;

    useEffect(() => {
        scale.setValue(1);
        opacity.setValue(1);

        const animation = Animated.loop(
            Animated.sequence([
                Animated.delay(delay),
                Animated.parallel([
                    Animated.timing(scale, {
                        toValue: 2,
                        duration,
                        easing: Easing.out(Easing.ease),
                        useNativeDriver: true,
                    }),
                    Animated.timing(opacity, {
                        toValue: 0,
                        duration,
                        easing: Easing.out(Easing.ease),
                        useNativeDriver: true,
                    }),
                ]),
            ]),
            {resetBeforeIteration: true}
        );

        animation.start();

        return () => {
            animation.stop();
        };
    }, [delay, duration, opacity, scale]);

    return (
        <Animated.View
            style={[
                StyleSheet.absoluteFillObject,
                styles.dot,
                {
                    opacity,
                    transform: [{scale}],
                },
            ]}
        />
    );
}

export default function MetronomeScreen() {
    const [tempo, setTempo] = useState(120);
    const beatDuration = 60000 / tempo;

    return (
        <AppLayout>
            <View className = "bg-background flex-1 pt-2">
                <ProfileHeader title={"Metrónomo"} />
                <MetronomeSlider onTempoChange={setTempo} />

                {/** ANIMACIONES DE LOS GOLPES DEL METRÓNOMO */}
                <View style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
                    <View style={[styles.dot, styles.baseDot, styles.center]}>
                        {[...Array(3).keys()].map((index) => {
                            return (
                                <RippleCircle
                                    key={index}
                                    delay={index * beatDuration}
                                    duration={beatDuration}
                                />
                            );
                        })}
                    </View>
                </View>

                <View className="flex-column mt-10 gap-1">
                    <TouchableOpacity className="bg-secondary w-80 py-4 rounded-md self-center">
                        <Text className="text-primary text-3xl self-center">TAP TEMPO</Text>
                    </TouchableOpacity>
                    <TouchableOpacity className="bg-secondary w-80 py-5 rounded-md self-center">
                        <Text className="text-primary text-lg text-bold self-center">Hi-Hat</Text>
                    </TouchableOpacity>
                </View>
            </View>
        </AppLayout>
    );
}