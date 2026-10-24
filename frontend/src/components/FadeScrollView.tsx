import { useRef } from 'react';
import { Animated, NativeScrollEvent, NativeSyntheticEvent, ScrollView, ScrollViewProps, View } from 'react-native';
import Svg, { Defs, LinearGradient, Rect, Stop } from 'react-native-svg';
import { COLORS } from '../constants/Theme';

/** Alto del degradado superior. */
const FADE_HEIGHT = 32;

// Cada degradado necesita un id de SVG propio
let gradientCount = 0;

type FadeScrollViewProps = ScrollViewProps & {
  /** Color del fondo sobre el que se funde el contenido. */
  fadeColor?: string;
};

/**
 * ScrollView vertical con un degradado en la parte superior: el contenido se funde con el
 * fondo al pasar por debajo del borde. El degradado aparece al hacer scroll (con el contenido
 * arriba del todo no tapa nada).
 */
export default function FadeScrollView({
  fadeColor = COLORS.background,
  onScroll,
  scrollEventThrottle = 16,
  children,
  ...props
}: FadeScrollViewProps) {
  const gradientId = useRef(`scroll-fade-${gradientCount++}`).current;
  const scrollY = useRef(new Animated.Value(0)).current;
  const fadeOpacity = scrollY.interpolate({ inputRange: [0, FADE_HEIGHT], outputRange: [0, 1], extrapolate: 'clamp' });

  const handleScroll = (event: NativeSyntheticEvent<NativeScrollEvent>) => {
    scrollY.setValue(event.nativeEvent.contentOffset.y);
    onScroll?.(event);
  };

  return (
    // Mismo flex que un ScrollView, para que el contenedor ocupe lo que ocupaba él
    <View style={{ flexGrow: 1, flexShrink: 1 }}>
      <ScrollView {...props} onScroll={handleScroll} scrollEventThrottle={scrollEventThrottle}>
        {children}
      </ScrollView>
      <Animated.View
        pointerEvents="none"
        style={{ position: 'absolute', top: 0, left: 0, right: 0, height: FADE_HEIGHT, opacity: fadeOpacity }}
      >
        <Svg width="100%" height="100%">
          <Defs>
            <LinearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
              <Stop offset="0" stopColor={fadeColor} stopOpacity="1" />
              <Stop offset="1" stopColor={fadeColor} stopOpacity="0" />
            </LinearGradient>
          </Defs>
          <Rect x="0" y="0" width="100%" height="100%" fill={`url(#${gradientId})`} />
        </Svg>
      </Animated.View>
    </View>
  );
}
