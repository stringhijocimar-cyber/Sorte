import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { LearningProvider } from '@/state/learning-context';
import { palette } from '@/components/ui';

export default function RootLayout() {
  return (
    <LearningProvider>
      <StatusBar style="light" />
      <Stack
        screenOptions={{
          headerStyle: { backgroundColor: palette.bg },
          headerTintColor: palette.text,
          contentStyle: { backgroundColor: palette.bg },
          headerShadowVisible: false,
        }}
      >
        <Stack.Screen name="index" options={{ headerShown: false }} />
        <Stack.Screen name="onboarding" options={{ title: 'Seu objetivo' }} />
        <Stack.Screen name="diagnostic" options={{ title: 'Diagnóstico' }} />
        <Stack.Screen name="home" options={{ title: 'SpeakFlow AI', headerBackVisible: false }} />
        <Stack.Screen name="session" options={{ title: 'Prática ao vivo' }} />
        <Stack.Screen name="feedback" options={{ title: 'Feedback' }} />
      </Stack>
    </LearningProvider>
  );
}
