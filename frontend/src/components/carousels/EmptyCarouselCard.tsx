import { View, Text } from 'react-native';

type EmptyCarouselCardProps = {
  message: string;
  /** Alto mínimo: el de las portadas del carrusel, para que la pantalla no salte al llegar datos */
  minHeight: number;
};

/**
 * Card dorada transparente que ocupa el hueco de un carrusel vacío
 * (p.ej. "Recientes" la primera vez que se entra a la app).
 */
export default function EmptyCarouselCard({ message, minHeight }: EmptyCarouselCardProps) {
  return (
    <View
      className="bg-primary/15 border border-primary/60 rounded-xl px-6 justify-center"
      style={{ minHeight }}
    >
      <Text className="text-primary text-base text-center leading-snug">{message}</Text>
    </View>
  );
}
