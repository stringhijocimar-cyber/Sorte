import { router } from 'expo-router';
import { ScrollView, Text, View } from 'react-native';
import {
  Card,
  Meter,
  palette,
  PrimaryButton,
  Screen,
  Tag,
  textStyles,
} from '@/components/ui';
import { rankWeakestCategories, skillLabels } from '@/domain/progress';
import { CorrectionCategory, SkillStage } from '@/domain/types';
import { useLearning } from '@/state/learning-context';

const stageLabels: Record<SkillStage, string> = {
  'not-started': 'não iniciado',
  developing: 'em desenvolvimento',
  practiced: 'praticado',
  consistent: 'consistente',
  mastered: 'dominado',
};

const categoryLabels: Record<CorrectionCategory, string> = {
  grammar: 'Gramática',
  'word-choice': 'Escolha de palavras',
  pronunciation: 'Pronúncia',
  rhythm: 'Ritmo',
  intonation: 'Entonação',
  'literal-translation': 'Tradução literal',
  register: 'Registro',
  comprehension: 'Compreensão',
};

export default function ProgressScreen() {
  const { sessions, skills, level } = useLearning();
  const weakest = rankWeakestCategories(sessions);
  const totalCorrections = sessions.reduce((total, session) => total + session.corrections.length, 0);

  return (
    <ScrollView style={{ flex: 1, backgroundColor: palette.bg }} contentContainerStyle={{ flexGrow: 1 }}>
      <Screen>
        <View style={{ gap: 4 }}>
          <Text style={textStyles.eyebrow}>Painel</Text>
          <Text style={textStyles.h1}>Seu progresso</Text>
          <Text style={textStyles.body}>
            Calculado a partir do nível {level} e do seu histórico real de prática.
          </Text>
        </View>

        {sessions.length === 0 ? (
          <Card>
            <Text style={textStyles.h2}>Ainda sem dados</Text>
            <Text style={textStyles.body}>
              Conclua uma prática para começar a acompanhar suas competências.
            </Text>
            <PrimaryButton title="Ir para a rotina" onPress={() => router.replace('/home')} />
          </Card>
        ) : null}

        <Card>
          <Text style={textStyles.h2}>Competências</Text>
          {skills.map((skill) => (
            <View key={skill.skill} style={{ gap: 6, marginTop: 8 }}>
              <View style={{ flexDirection: 'row', justifyContent: 'space-between', gap: 12 }}>
                <Text style={[textStyles.strong, { flex: 1 }]}>{skillLabels[skill.skill]}</Text>
                <Text style={textStyles.small}>
                  {skill.score} · {stageLabels[skill.stage]}
                </Text>
              </View>
              <Meter value={skill.score} />
            </View>
          ))}
        </Card>

        <Card>
          <Text style={textStyles.h2}>Focos de revisão</Text>
          {weakest.length === 0 ? (
            <Text style={textStyles.body}>Nenhum erro recorrente registrado até agora.</Text>
          ) : (
            <View style={{ gap: 8, marginTop: 4 }}>
              {weakest.map((item) => (
                <View
                  key={item.category}
                  style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 12 }}
                >
                  <Text style={[textStyles.body, { flex: 1 }]}>{categoryLabels[item.category]}</Text>
                  <Tag label={`${item.count}x`} />
                </View>
              ))}
            </View>
          )}
          <Text style={textStyles.small}>
            {totalCorrections} correção(ões) registrada(s) em {sessions.length} sessão(ões).
          </Text>
        </Card>

        <Card>
          <Text style={textStyles.h2}>Histórico</Text>
          {sessions.length === 0 ? (
            <Text style={textStyles.body}>Suas práticas aparecerão aqui.</Text>
          ) : (
            sessions.map((session) => (
              <View key={session.id} style={{ gap: 3, marginTop: 8 }}>
                <Text style={textStyles.strong}>{session.scenarioTitle}</Text>
                <Text style={textStyles.small}>
                  {new Date(session.endedAt).toLocaleString('pt-BR')} · {session.learnerTurns} turno(s) ·{' '}
                  {session.corrections.length} correção(ões)
                </Text>
              </View>
            ))
          )}
        </Card>

        <PrimaryButton title="Voltar para início" onPress={() => router.replace('/home')} />
      </Screen>
    </ScrollView>
  );
}
