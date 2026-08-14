import { router } from 'expo-router';
import { ScrollView, Text, View } from 'react-native';
import { Card, palette, PrimaryButton, Screen, textStyles } from '@/components/ui';
import { scenarios } from '@/data/scenarios';
import { useLearning } from '@/state/learning-context';

export default function HomeScreen() {
  const { profile, diagnosticLevel } = useLearning();
  const level = diagnosticLevel ?? profile?.currentLevel ?? 'A1';
  const recommended = scenarios.find((scenario) => scenario.level === level) ?? scenarios[0];

  return (
    <ScrollView style={{ flex: 1, backgroundColor: palette.bg }} contentContainerStyle={{ flexGrow: 1 }}>
      <Screen>
        <View style={{ gap: 4 }}>
          <Text style={textStyles.eyebrow}>Sua rotina de hoje</Text>
          <Text style={textStyles.h1}>Olá, {profile?.name ?? 'Learner'}</Text>
          <Text style={textStyles.body}>Nível estimado: {level} · Meta diária: {profile?.dailyMinutes ?? 20} min</Text>
        </View>

        <Card>
          <Text style={textStyles.eyebrow}>Recomendado</Text>
          <Text style={textStyles.h2}>{recommended.title}</Text>
          <Text style={textStyles.body}>{recommended.description}</Text>
          <Text style={textStyles.small}>Objetivo: {recommended.objective}</Text>
          <PrimaryButton title="Iniciar prática" onPress={() => router.push({ pathname: '/session', params: { scenarioId: recommended.id } })} />
        </Card>

        <Text style={textStyles.h2}>Cenários reais</Text>
        {scenarios.map((scenario) => (
          <Card key={scenario.id}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', gap: 12 }}>
              <Text style={[textStyles.strong, { flex: 1 }]}>{scenario.title}</Text>
              <Text style={textStyles.small}>{scenario.level} · {scenario.durationMinutes} min</Text>
            </View>
            <Text style={textStyles.body}>{scenario.description}</Text>
            <PrimaryButton title="Praticar" onPress={() => router.push({ pathname: '/session', params: { scenarioId: scenario.id } })} />
          </Card>
        ))}
      </Screen>
    </ScrollView>
  );
}
