// src/navigation/AppNavigator.tsx
import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import LoginScreen from '../screens/LoginScreen';
import BottomTabs from './BottomTabs';
import SongScreen from '../screens/SongScreen';
import { RootStackParamList } from '../types/RootStackParamList';
import GenreMainScreen from '../screens/GenreMainScreen';
import ProfileScreen from '../screens/ProfileScreen';
import RegisterScreen from '../screens/RegisterScreen';
import MetronomeScreen from "../screens/MetronomeScreen";

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function AppNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="LoginScreen" component={LoginScreen} />
      <Stack.Screen name="RegisterScreen" component={RegisterScreen} />
      <Stack.Screen name="BottomTab" component={BottomTabs} />
      <Stack.Screen name="SongScreen" component={SongScreen} />
      <Stack.Screen name="GenreMainScreen" component={GenreMainScreen} />
      <Stack.Screen name="ProfileScreen" component={ProfileScreen} />
      <Stack.Screen name={"MetronomeScreen"} component={MetronomeScreen} />
    </Stack.Navigator>
  );
}
