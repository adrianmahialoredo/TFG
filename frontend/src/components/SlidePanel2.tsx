import React, { useEffect, useRef, useState } from 'react';
import {
  View,
  Text,
  Image,
  TouchableOpacity,
  Dimensions,
  Animated,
  PanResponder,
  Switch,
  ScrollView,
} from 'react-native';
import TopButton from './buttons/TopButton';
import EdgeButton from './buttons/EdgeButton';

// Obtenemos las dimensiones de la pantalla del dispositivo
const { width, height } = Dimensions.get('window');

type Lesson = {
  id: string;
  title: string;
};

type Props = {
  lessons: Lesson[];
  onSelectLesson: (lesson: Lesson) => void;
}; 

const PANEL_WIDTH = 152;
const PANEL_OFFSET = -28; // Cuánto quieres que sobresalga el panel (típicamente entre 20 y 50)
const CLOSED_POSITION = -PANEL_WIDTH + PANEL_OFFSET; // Ej: -180 si el ancho es 220 y quieres que sobresalga 40

/**
 * COMPONENTE PRINCIPAL: EdgePanel con NativeWind
 * 
 * Implementa un panel lateral deslizable usando clases de Tailwind CSS
 * a través de NativeWind para el styling.
 */
export default function EdgePanel({ lessons, onSelectLesson }: Props){
  
  useEffect(() => {
  translateX.setValue(isOpen ? 0 : CLOSED_POSITION);
  }, []);

  /**
   * ESTADO Y REFERENCIAS
   */
  
  // Valor animado para controlar la posición horizontal del panel
  // Inicia en CLOSED_POSITION (panel parcialmente visible)
  const translateX = useRef(new Animated.Value(CLOSED_POSITION)).current;
  
  // Estado para saber si el panel está abierto o cerrado
  const [isOpen, setIsOpen] = useState(false);
  
  const [selectedLesson, setSelectedLesson] = useState<Lesson>(lessons[0]);

  // Estado para alternar entre panel de apps y panel de contactos
  const [usePanel2, setUsePanel2] = useState(false);

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

  /**
   * RENDERIZADO DEL COMPONENTE CON NATIVEWIND
   */
  return (
    <>
      {/* CONTENIDO PRINCIPAL DE LA PANTALLA - FONDO COMPLETO */}
      <View className="flex-1 bg-black" style={{ height: height }}>
        <ScrollView 
          className="flex-1"
          contentContainerStyle={{ paddingBottom: 100 }}
          showsVerticalScrollIndicator={false}
        >
          {/* Header del contenido */}
          <View className="px-4 pt-8 pb-4">
            <Text className="text-3xl font-bold text-orange-400 text-center mb-2">
              {selectedLesson?.title}
            </Text>
            <Text className="text-gray-400 text-center text-sm">
              Género Musical
            </Text>
          </View>

          {/* Imagen del género con mejor presentación */}
          <View className="items-center mb-6">
            <View className="w-20 h-20 bg-orange-400/20 rounded-full items-center justify-center mb-4">
              <Image 
                source={{ uri: 'https://upload.wikimedia.org/wikipedia/commons/thumb/3/34/Guitar_Icon.png/240px-Guitar_Icon.png' }} 
                className="w-12 h-12"
                style={{ tintColor: '#fb923c' }}
                resizeMode="contain"
              />
            </View>
            
            {/* Estadísticas del género */}
            <View className="flex-row space-x-6 mb-6">
              <View className="items-center">
                <Text className="text-white text-lg font-bold">127</Text>
                <Text className="text-gray-400 text-xs">Canciones</Text>
              </View>
              <View className="items-center">
                <Text className="text-white text-lg font-bold">4.2h</Text>
                <Text className="text-gray-400 text-xs">Duración</Text>
              </View>
              <View className="items-center">
                <Text className="text-white text-lg font-bold">1994</Text>
                <Text className="text-gray-400 text-xs">Origen</Text>
              </View>
            </View>
          </View>

          {/* Contenido principal con mejor tipografía */}
          <View className="px-4">
            <Text className="text-white text-base leading-6 mb-4">
              La guitarra en el pop punk es uno de los elementos más distintivos del género, 
              aportando una mezcla de <Text className="text-orange-400 font-semibold">energía cruda</Text> y 
              melodía accesible.
            </Text>
            
            <Text className="text-gray-300 text-sm leading-5 mb-4">
              Se caracteriza por el uso intensivo de distorsión para lograr un sonido fuerte y 
              rasgado, que llena el espacio sonoro sin necesidad de demasiada complejidad técnica. 
              Este estilo busca un impacto inmediato, directo al oyente.
            </Text>

            <View className="bg-gray-900/50 p-4 rounded-lg mb-4 border-l-4 border-orange-400">
              <Text className="text-orange-400 text-sm font-semibold mb-2">
                Características Principales:
              </Text>
              <Text className="text-gray-300 text-sm leading-5">
                El recurso más utilizado es el <Text className="text-white font-medium">power chord</Text> (acorde de quinta), 
                tocado en secuencias rápidas y repetitivas que refuerzan la base rítmica.
              </Text>
            </View>

            <Text className="text-gray-300 text-sm leading-5">
              En conjunto, la guitarra en el pop punk no busca brillar por su complejidad, 
              sino por su capacidad de transmitir emociones como 
              <Text className="text-orange-400"> rebeldía, nostalgia o euforia</Text>. 
              Es una herramienta directa y efectiva que define gran parte del sonido del género.
            </Text>
          </View>
        </ScrollView>
      </View>

      {/* EDGE PANEL - SOBREPUESTO */}
      <View 
        className="flex-row absolute top-0 left-0 bottom-0 z-50"
        style={{ height: height }}
        {...panResponder.panHandlers}
      >
        {/* PANEL PRINCIPAL DESLIZABLE */}
        <Animated.View 
          className="bg-secondary rounded-xl self-center"
          style={{ 
            width: PANEL_WIDTH, // Asegura el ancho correcto
            transform: [{ translateX }],
            elevation: 5,
            maxHeight: 400,
          }}
        >
           {/* Lista de lecciones */}
          <View className="flex-col">
            {lessons.map((lesson, index) => (
              <EdgeButton
                key={lesson.id}
                name={lesson.title}
                isSelected={selectedLesson?.id === lesson.id}
                isTopEdge={index === 0}
                isBottomEdge={index === lessons.length - 1}
                onPress={() => {
                  setSelectedLesson(lesson);
                  onSelectLesson(lesson);
                  closePanel();
                }}
              />
            ))}
          </View>
        </Animated.View>
        {/* INDICADOR VISUAL DEL BORDE - SIEMPRE VISIBLE */}
        <Animated.View 
          className="w-1.5 bg-primary rounded-r-xl self-center"
          style={{ 
            height: isOpen ? 120 : 100,
            // El borde debe moverse igual que el panel
            transform: [{ translateX }],
          }}
        />
      </View>
    </>
  );
};
