import { Redirect, router } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Text, View } from 'react-native';
import { Loading, PrimaryButton, Screen, textStyles } from '@/components/ui';
import { useLearning } from '@/state/learning-context';

export default function WelcomeScreen() {
  const { ready, profile, diagnosticLevel } = useLearning();

  if (!ready) {
    return (
      <SafeAreaView style={{ flex: 1 }}>
        <Screen>
          <Loading label="Carregando seu progresso…" />
        </Screen>
      </SafeAreaView>
    );
  }

  // Quem já concluiu onboarding e diagnóstico volta direto para a rotina.
  if (profile && diagnosticLevel) {
    return <Redirect href="/home" />;
  }

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
