import { router } from 'expo-router';
import { ScrollView, Text, View } from 'react-native';
import {
  Card,
  Loading,
  Notice,
  palette,
  PrimaryButton,
  Screen,
  SecondaryButton,
  Tag,
  textStyles,
} from '@/components/ui';
import { scenarios } from '@/data/scenarios';
import { useLearning } from '@/state/learning-context';

function formatDate(iso: string) {
  const date = new Date(iso);
  return date.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' });
}

export default function HomeScreen() {
  const { ready, profile, level, sessions, aiBaseUrl } = useLearning();

  if (!ready) {
    return (
      <Screen>
        <Loading label="Carregando sua rotina…" />
      </Screen>
    );
  }

  const recommended = scenarios.find((scenario) => scenario.level === level) ?? scenarios[0];
  const recentSessions = sessions.slice(0, 3);
  const totalTurns = sessions.reduce((total, session) => total + session.learnerTurns, 0);

  return (
    <ScrollView style={{ flex: 1, backgroundColor: palette.bg }} contentContainerStyle={{ flexGrow: 1 }}>
      <Screen>
        <View style={{ gap: 4 }}>
          <Text style={textStyles.eyebrow}>Sua rotina de hoje</Text>
          <Text style={textStyles.h1}>Olá, {profile?.name ?? 'Learner'}</Text>
          <Text style={textStyles.body}>
            Nível estimado: {level} · Meta diária: {profile?.dailyMinutes ?? 20} min
          </Text>
          <View style={{ flexDirection: 'row', gap: 8, flexWrap: 'wrap', marginTop: 6 }}>
            <Tag label={`${sessions.length} sessão(ões)`} />
            <Tag label={`${totalTurns} turno(s) de fala`} />
            <Tag label={aiBaseUrl ? 'Tutor por IA' : 'Tutor simulado'} />
          </View>
        </View>

        <Card>
          <Text style={textStyles.eyebrow}>Recomendado</Text>
          <Text style={textStyles.h2}>{recommended.title}</Text>
          <Text style={textStyles.body}>{recommended.description}</Text>
          <Text style={textStyles.small}>Objetivo: {recommended.objective}</Text>
          <PrimaryButton
            title="Iniciar prática"
            onPress={() => router.push({ pathname: '/session', params: { scenarioId: recommended.id } })}
          />
        </Card>

        <View style={{ flexDirection: 'row', gap: 8 }}>
          <View style={{ flex: 1 }}>
            <SecondaryButton title="Meu progresso" onPress={() => router.push('/progress')} />
          </View>
          <View style={{ flex: 1 }}>
            <SecondaryButton title="Ajustes" onPress={() => router.push('/settings')} />
          </View>
        </View>

        {!aiBaseUrl ? (
          <Notice message="Tutor simulado ativo: as respostas são fixas e não há correções. Configure seu servidor em Ajustes para conversar com a IA." />
        ) : null}

        {recentSessions.length > 0 ? (
          <View style={{ gap: 10 }}>
            <Text style={textStyles.h2}>Sessões recentes</Text>
            {recentSessions.map((session) => (
              <Card key={session.id}>
                <View style={{ flexDirection: 'row', justifyContent: 'space-between', gap: 12 }}>
                  <Text style={[textStyles.strong, { flex: 1 }]}>{session.scenarioTitle}</Text>
                  <Text style={textStyles.small}>{formatDate(session.endedAt)}</Text>
                </View>
                <Text style={textStyles.small}>
                  {session.learnerTurns} turno(s) · {session.corrections.length} correção(ões) · {session.level}
                </Text>
              </Card>
            ))}
          </View>
        ) : null}

        <Text style={textStyles.h2}>Cenários reais</Text>
        {scenarios.map((scenario) => (
          <Card key={scenario.id}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', gap: 12 }}>
              <Text style={[textStyles.strong, { flex: 1 }]}>{scenario.title}</Text>
              <Text style={textStyles.small}>
                {scenario.level} · {scenario.durationMinutes} min
              </Text>
            </View>
            <Text style={textStyles.body}>{scenario.description}</Text>
            <PrimaryButton
              title="Praticar"
              onPress={() => router.push({ pathname: '/session', params: { scenarioId: scenario.id } })}
            />
          </Card>
        ))}
      </Screen>
    </ScrollView>
  );
}
