import React, { useEffect, useRef, useState } from 'react';
import { View, Animated, PanResponder, Dimensions, ScrollView } from 'react-native';
import EdgeButton from './buttons/EdgeButton';
import { LessonDTO } from '../types';
import { LAYOUT_HORIZONTAL_PADDING } from './AppLayout';

const { height } = Dimensions.get('window');

export const PANEL_WIDTH = 150;
const HANDLE_WIDTH = 9;          // Ancho de la pestaña naranja que queda siempre visible
export const HANDLE_TOUCH_WIDTH = 50;   // Zona táctil de la pestaña (más ancha que la propia pestaña para poder arrastrarla)
const PANEL_MAX_HEIGHT = height * 0.6;
const CLOSED_POSITION = -PANEL_WIDTH; // Cerrado: el panel queda fuera y solo asoma la pestaña
const SWIPE_THRESHOLD = PANEL_WIDTH / 3;
const TAP_SLOP = 5;
// El muelle pasa un poco de largo al abrir: el cajón se prolonga por la izquierda (fuera de
// pantalla) para que en ese rebote no asome un hueco entre el borde y el panel.
const BOUNCE_OVERFLOW = 24;

type Props = {
    lessons: LessonDTO[];
    activeLessonId: string;
    onSelectLessonId: (id: string) => void;
};

/**
 * Panel lateral tipo "edge panel": una pestaña siempre visible en el borde izquierdo
 * que al deslizar hacia la derecha se abre como un cajón con todas las lecciones.
 * Al pulsar una lección se cambia el contenido de la pantalla y el panel sigue abierto;
 * se oculta deslizando hacia la izquierda. Con el panel cerrado, tocar la pestaña lo abre.
 *
 * Como en SlidePanel2, los gestos los recibe un contenedor FIJO (sin transform) y lo que
 * se anima es su contenido: el área táctil no se mueve bajo el dedo mientras se arrastra.
 */
export default function EdgePanel({ lessons, activeLessonId, onSelectLessonId }: Props) {
    const translateX = useRef(new Animated.Value(CLOSED_POSITION)).current;
    // El estado dimensiona el área táctil; el ref es lo que lee el PanResponder (creado una
    // sola vez), que con el estado vería siempre el valor inicial.
    const [isOpen, setIsOpen] = useState(false);
    const isOpenRef = useRef(false);

    // Fast Refresh conserva los refs: al cambiar PANEL_WIDTH, translateX seguía con el
    // CLOSED_POSITION antiguo y asomaba parte del cajón. Fast Refresh vuelve a ejecutar los
    // efectos, así que aquí se recoloca con el valor nuevo (al montar no cambia nada).
    useEffect(() => {
        if (!isOpenRef.current) translateX.setValue(CLOSED_POSITION);
    }, [translateX]);

    const animateTo = (open: boolean) => {
        isOpenRef.current = open;
        if (open) setIsOpen(true); // Se amplía el área táctil antes de que el panel llegue
        // Muelle con algo de rebote al final, en vez de una transición lineal
        Animated.spring(translateX, {
            toValue: open ? 0 : CLOSED_POSITION,
            tension: 70,
            friction: 8,
            useNativeDriver: true,
        }).start(({ finished }) => { if (finished && !open) setIsOpen(false); });
    };

    const isHorizontal = (dx: number, dy: number) => Math.abs(dx) > TAP_SLOP && Math.abs(dx) > Math.abs(dy);

    /**
     * CONFIGURACIÓN DE GESTOS TÁCTILES
     *
     * Se usa la fase de captura para que los botones de lección no se queden con el arrastre,
     * y solo ante movimientos horizontales, para no robar el scroll vertical de la lista.
     */
    const panResponder = useRef(
        PanResponder.create({
            // Cerrado solo es tocable la pestaña: un toque en ella abre el panel
            onStartShouldSetPanResponder: () => !isOpenRef.current,
            onMoveShouldSetPanResponderCapture: (_, gesture) => isHorizontal(gesture.dx, gesture.dy),
            onMoveShouldSetPanResponder: (_, gesture) => isHorizontal(gesture.dx, gesture.dy),
            onPanResponderTerminationRequest: () => false,

            // Sigue al dedo sin salirse de los límites [cerrado, abierto]
            onPanResponderMove: (_, gesture) => {
                const start = isOpenRef.current ? 0 : CLOSED_POSITION;
                translateX.setValue(Math.min(0, Math.max(CLOSED_POSITION, start + gesture.dx)));
            },

            // Al soltar decide si abrir o cerrar según cuánto se ha arrastrado
            onPanResponderRelease: (_, gesture) => {
                const isTap = Math.abs(gesture.dx) < TAP_SLOP && Math.abs(gesture.dy) < TAP_SLOP;
                if (!isOpenRef.current && (isTap || gesture.dx > SWIPE_THRESHOLD)) {
                    animateTo(true);
                } else if (isOpenRef.current && gesture.dx < -SWIPE_THRESHOLD) {
                    animateTo(false);
                } else {
                    animateTo(isOpenRef.current);
                }
            },
            onPanResponderTerminate: () => animateTo(isOpenRef.current),
        })
    ).current;

    return (
        // Contenedor fijo que recibe los gestos. Cerrado solo ocupa la franja de la pestaña,
        // para no tapar los toques del contenido de la pantalla. Va dentro de AppLayout, cuyo
        // margen lateral lo separaría del borde: se compensa para que quede pegado a la pantalla.
        <View
            className="absolute top-0 bottom-0 z-50 justify-center"
            style={{
                left: -LAYOUT_HORIZONTAL_PADDING,
                width: isOpen ? PANEL_WIDTH + HANDLE_TOUCH_WIDTH : HANDLE_TOUCH_WIDTH,
            }}
            {...panResponder.panHandlers}
        >
            {/* Estilos en línea (no className) en la vista animada: NativeWind pisa el estilo
                animado al re-renderizar (p.ej. con setIsOpen) y el panel saltaba de golpe. */}
            <Animated.View
                style={{
                    flexDirection: 'row',
                    alignItems: 'center',
                    marginLeft: -BOUNCE_OVERFLOW,
                    width: BOUNCE_OVERFLOW + PANEL_WIDTH + HANDLE_TOUCH_WIDTH,
                    transform: [{ translateX }],
                }}
            >
                {/* CAJÓN CON LAS LECCIONES (con un margen extra fuera de pantalla para el rebote) */}
                {/* Fondo negro como los botones: el margen del rebote y las esquinas no asoman en otro color */}
                <View
                    className="bg-background rounded-r-xl overflow-hidden"
                    style={{ width: BOUNCE_OVERFLOW + PANEL_WIDTH, paddingLeft: BOUNCE_OVERFLOW, maxHeight: PANEL_MAX_HEIGHT, elevation: 5 }}
                >
                    <ScrollView showsVerticalScrollIndicator={false}>
                        {lessons.map((lesson, index) => (
                            <EdgeButton
                                key={lesson.id}
                                name={lesson.title}
                                isSelected={activeLessonId === lesson.id}
                                isTopEdge={index === 0}
                                isBottomEdge={index === lessons.length - 1}
                                onPress={() => onSelectLessonId(lesson.id)}
                            />
                        ))}
                    </ScrollView>
                </View>

                {/* PESTAÑA SIEMPRE VISIBLE: zona táctil ancha y transparente con la barra naranja dentro */}
                <View style={{ width: HANDLE_TOUCH_WIDTH, height: 120 }} className="justify-center">
                    <View
                        className="bg-primary rounded-r-xl"
                        style={{ width: HANDLE_WIDTH, height: 100 }}
                    />
                </View>
            </Animated.View>
        </View>
    );
}
