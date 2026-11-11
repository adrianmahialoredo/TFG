import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useAuth } from '../context/AuthContext';
import { ActivityIndicator, View } from 'react-native';
import LoginScreen from '../screens/LoginScreen';
import RegisterScreen from '../screens/RegisterScreen';
import ForgotPasswordScreen from '../screens/ForgotPasswordScreen';
import BottomTabs from './BottomTabs';
import { RootStackParamList } from '../types';

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
            </Stack.Group>
        ) : (
            // --- STACK DE ACCESO ---
            <Stack.Group screenOptions={{ animation: 'fade' }}>
              <Stack.Screen name="LoginScreen" component={LoginScreen} />
              <Stack.Screen name="RegisterScreen" component={RegisterScreen} />
              <Stack.Screen name="ForgotPasswordScreen" component={ForgotPasswordScreen} />
            </Stack.Group>
        )}
      </Stack.Navigator>
  );
}