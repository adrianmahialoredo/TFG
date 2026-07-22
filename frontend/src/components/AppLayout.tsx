import { View } from 'react-native';
import { ReactNode } from 'react';

export default function AppLayout({ children, backgroundColor }: { children: ReactNode, backgroundColor?: string }) {
  return (
    //<View className="flex-1 px-8 pt-6 pb-2 bg-background">
    <View className={`flex-1 px-5 pt-8 pb-2 ${backgroundColor || 'bg-background'}`}>
      {children}
    </View>
  );
}
