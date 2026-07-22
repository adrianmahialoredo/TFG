import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useAuth } from '../hooks/useAuth';
import { ActivityIndicator, View } from 'react-native';
import LoginScreen from '../screens/LoginScreen';
import RegisterScreen from '../screens/RegisterScreen';
import BottomTabs from './BottomTabs';
import SongScreen from '../screens/SongScreen';
import GenreMainScreen from '../screens/GenreMainScreen';
import ProfileScreen from '../screens/ProfileScreen';
import MetronomeScreen from "../screens/MetronomeScreen";
import { RootStackParamList } from '../types';
import InstrumentGenreScreen from "../screens/InstrumentGenreScreen";
import ListScreen from "../screens/ListScreen";
import AddItemScreen from "../screens/AddItemScreen";

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function AppNavigator() {
  // 1. Extraemos todo del Hook de Nivel 6
  const { isAuthenticated, isReady } = useAuth();

  // 2. Mientras la Store no se haya hidratado desde el disco,
  // mostramos una pantalla de carga (Splash) para evitar parpadeos.
  if (!isReady) {
    return (
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#000' }}>
          <ActivityIndicator size="large" color="#fff" />
        </View>
    );
  }

  return (
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        {isAuthenticated ? (
            // --- STACK AUTENTICADO ---
            <Stack.Group>
              <Stack.Screen name="BottomTab" component={BottomTabs} />
              <Stack.Screen name="SongScreen" component={SongScreen} />
              <Stack.Screen name="GenreMainScreen" component={GenreMainScreen} />
              <Stack.Screen name="ProfileScreen" component={ProfileScreen} />
              <Stack.Screen name="MetronomeScreen" component={MetronomeScreen} />
              <Stack.Screen name="InstrumentGenreScreen" component={InstrumentGenreScreen} />
              <Stack.Screen name="ListScreen" component={ListScreen} />
              <Stack.Screen name="AddItemScreen" component={AddItemScreen} />
            </Stack.Group>
        ) : (
            // --- STACK DE ACCESO ---
            <Stack.Group screenOptions={{ animation: 'fade' }}>
              <Stack.Screen name="LoginScreen" component={LoginScreen} />
              <Stack.Screen name="RegisterScreen" component={RegisterScreen} />
            </Stack.Group>
        )}
      </Stack.Navigator>
  );
}