import { View } from 'react-native';
import { ReactNode } from 'react';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

/** Separación mínima con el borde superior (la de los móviles con barra de estado baja) */
const MIN_TOP_PADDING = 40;
/** Hueco extra bajo la barra de estado / la cámara */
const TOP_GAP = 8;
/** Margen lateral de todas las pantallas; los elementos pegados al borde (p.ej. EdgePanel) lo compensan */
export const LAYOUT_HORIZONTAL_PADDING = 20;

export default function AppLayout({ children, backgroundColor }: { children: ReactNode, backgroundColor?: string }) {
  // La barra de estado cambia de alto según el móvil (p.ej. con la cámara en un recorte):
  // el padding superior se adapta a ella para que la cabecera no quede tapada
  const insets = useSafeAreaInsets();
  const paddingTop = Math.max(MIN_TOP_PADDING, insets.top + TOP_GAP);

  return (
    <View
      className={`flex-1 pb-2 ${backgroundColor || 'bg-background'}`}
      style={{ paddingTop, paddingHorizontal: LAYOUT_HORIZONTAL_PADDING }}
    >
      {children}
    </View>
  );
}
