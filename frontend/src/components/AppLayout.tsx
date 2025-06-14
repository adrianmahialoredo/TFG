import { View } from 'react-native';
import { ReactNode } from 'react';

export default function AppLayout({ children }: { children: ReactNode }) {
  return (
    <View className="flex-1 px-8 pt-6 pb-2 bg-background">
      {children}
    </View>
  );
}
