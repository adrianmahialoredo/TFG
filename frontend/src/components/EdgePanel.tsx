import React, { useRef, useState } from 'react';
import { View, Animated, PanResponder, Dimensions } from 'react-native';
import EdgeButton from './buttons/EdgeButton';
import { LessonDTO } from '../types';

const { height } = Dimensions.get('window');
const PANEL_WIDTH = 152;
const PANEL_OFFSET = -28;
const CLOSED_POSITION = -PANEL_WIDTH + PANEL_OFFSET;

type Props = {
    lessons: LessonDTO[];
    activeLessonId: string;
    onSelectLessonId: (id: string) => void;
};

export default function EdgePanel({ lessons, activeLessonId, onSelectLessonId }: Props) {
    const translateX = useRef(new Animated.Value(CLOSED_POSITION)).current;
    const [isOpen, setIsOpen] = useState(false);

    /**
     * CONFIGURACIÓN DE GESTOS TÁCTILES
     *
     * PanResponder maneja los gestos de deslizar para abrir/cerrar el panel
     */
    const panResponder = useRef(
        PanResponder.create({
            // Determina cuándo debe activarse el gesto (movimiento horizontal > 10px)
            onMoveShouldSetPanResponder: (_, gestureState) => Math.abs(gestureState.dx) > 5,

            // Maneja el movimiento mientras se arrastra
            onPanResponderMove: (_, gesture) => {
                let dx = gesture.dx; // Distancia horizontal del movimiento

                if (isOpen) {
                    // Si el panel está abierto, permite deslizar hacia la izquierda para cerrarlo
                    if (dx < 0 && dx >= -PANEL_WIDTH) {
                        translateX.setValue(dx);
                    }
                } else {
                    // Si el panel está cerrado, permite deslizar hacia la derecha para abrirlo
                    if (dx > 0 && dx <= PANEL_WIDTH) {
                        translateX.setValue(CLOSED_POSITION + dx);
                    }
                }
            },

            // Maneja el final del gesto (cuando se suelta el dedo)
            onPanResponderRelease: (_, gesture) => {
                // Si está cerrado y se desliza más de 100px a la derecha, abrir
                if (!isOpen && gesture.dx > 100) {
                    openPanel();
                }
                // Si está abierto y se desliza más de 100px a la izquierda, cerrar
                else if (isOpen && gesture.dx < -100) {
                    closePanel();
                }
                // Si no se cumple el umbral, volver al estado anterior
                else {
                    isOpen ? openPanel() : closePanel();
                }
            },
        })
    ).current;

    /**
     * FUNCIONES DE ANIMACIÓN
     */

        // Abre el panel con animación suave
    const openPanel = () => {
            Animated.timing(translateX, {
                toValue: 0,          // Posición final (visible)
                duration: 300,       // Duración de la animación en ms
                useNativeDriver: true, // Usa el driver nativo para mejor rendimiento
            }).start(() => setIsOpen(true)); // Actualiza el estado cuando termina
        };

    // Cierra el panel con animación suave
    const closePanel = () => {
        Animated.timing(translateX, {
            toValue: CLOSED_POSITION, // Usa la constante para mantener la pestaña visible
            duration: 300,
            useNativeDriver: true,
        }).start(() => setIsOpen(false));
    };

    return (
        <View
            className="flex-row absolute top-0 left-0 bottom-0 z-50"
            style={{ height: height * 0.7, alignSelf: 'center' }}
            {...panResponder.panHandlers}
        >
            <Animated.View
                className="bg-secondary rounded-xl self-center"
                style={{ width: PANEL_WIDTH, transform: [{ translateX }] }}
            >
                <View className="flex-col">
                    {lessons.map((lesson, index) => (
                        <EdgeButton
                            key={lesson.id}
                            name={lesson.title} // O "Paso " + (index + 1)
                            isSelected={activeLessonId === lesson.id}
                            isTopEdge={index === 0}
                            isBottomEdge={index === lessons.length - 1}
                            onPress={() => {
                                onSelectLessonId(lesson.id);
                                closePanel();
                            }}
                        />
                    ))}
                </View>
            </Animated.View>

            <Animated.View
                className="w-1.5 bg-primary rounded-r-xl self-center"
                style={{ height: 100, transform: [{ translateX }] }}
            />
        </View>
    );
}