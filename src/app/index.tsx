import { router } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { View } from 'react-native';
import { PrimaryButton, Screen, textStyles } from '@/components/ui';
import { Text } from 'react-native';

export default function WelcomeScreen() {
  return (
    <SafeAreaView style={{ flex: 1 }}>
      <Screen>
        <View style={{ flex: 1, justifyContent: 'center', gap: 18 }}>
          <Text style={textStyles.eyebrow}>SpeakFlow AI</Text>
          <Text style={textStyles.h1}>Fale mais. Corrija melhor. Evolua com contexto.</Text>
          <Text style={textStyles.body}>
            Prática de inglês focada em situações reais, com diagnóstico, conversação e feedback que respeita sua fluidez.
          </Text>
        </View>
        <PrimaryButton title="Começar" onPress={() => router.push('/onboarding')} />
      </Screen>
    </SafeAreaView>
  );
}
