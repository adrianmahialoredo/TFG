// src/navigation/AppNavigator.tsx
import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import LoginScreen from '../screens/LoginScreen';
import BottomTabs from './BottomTabs';
import SongScreen from '../screens/SongScreen';
import { RootStackParamList } from '../types/RootStackParamList';

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function AppNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="Login" component={LoginScreen} />
      <Stack.Screen name="BottomTab" component={BottomTabs} />
      <Stack.Screen name="SongScreen" component={SongScreen} />
    </Stack.Navigator>
  );
}
