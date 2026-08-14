import { useEffect, useMemo, useState } from 'react';
import { router, useLocalSearchParams } from 'expo-router';
import { ScrollView, Text, TextInput, View } from 'react-native';
import { Card, palette, PrimaryButton, Screen, textStyles } from '@/components/ui';
import { scenarios } from '@/data/scenarios';
import { conversationService, ConversationTurn } from '@/services/conversation-service';
import { useLearning } from '@/state/learning-context';

export default function SessionScreen() {
  const params = useLocalSearchParams<{ scenarioId?: string }>();
  const scenario = useMemo(() => scenarios.find((item) => item.id === params.scenarioId) ?? scenarios[0], [params.scenarioId]);
  const { setLastFeedback } = useLearning();
  const [turns, setTurns] = useState<ConversationTurn[]>([]);
  const [draft, setDraft] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    conversationService.startScenario(scenario).then((turn) => setTurns([turn]));
  }, [scenario]);

  async function send() {
    const text = draft.trim();
    if (!text || busy) return;
    setDraft('');
    setBusy(true);
    const learnerTurn: ConversationTurn = { id: `learner-${Date.now()}`, speaker: 'learner', text };
    const nextHistory = [...turns, learnerTurn];
    setTurns(nextHistory);
    const tutorTurn = await conversationService.reply(scenario, nextHistory, text);
    setTurns([...nextHistory, tutorTurn]);
    setBusy(false);
  }

  async function finish() {
    setBusy(true);
    const feedback = await conversationService.buildFeedback(scenario, turns);
    setLastFeedback(feedback);
    router.replace('/feedback');
  }

  return (
    <Screen>
      <View style={{ gap: 4 }}>
        <Text style={textStyles.eyebrow}>{scenario.role}</Text>
        <Text style={textStyles.h2}>{scenario.title}</Text>
        <Text style={textStyles.small}>{scenario.objective}</Text>
      </View>

      <ScrollView style={{ flex: 1 }} contentContainerStyle={{ gap: 10, paddingVertical: 8 }}>
        {turns.map((turn) => (
          <Card key={turn.id} style={{ alignSelf: turn.speaker === 'learner' ? 'flex-end' : 'flex-start', maxWidth: '90%', backgroundColor: turn.speaker === 'learner' ? palette.surface2 : palette.surface }}>
            <Text style={textStyles.small}>{turn.speaker === 'learner' ? 'Você' : 'Tutor'}</Text>
            <Text style={textStyles.strong}>{turn.text}</Text>
          </Card>
        ))}
      </ScrollView>

      <TextInput
        value={draft}
        onChangeText={setDraft}
        multiline
        placeholder="Responda em inglês…"
        placeholderTextColor={palette.muted}
        style={{ minHeight: 58, maxHeight: 120, color: palette.text, backgroundColor: palette.surface, borderColor: palette.line, borderWidth: 1, borderRadius: 16, padding: 14, fontSize: 16 }}
      />
      <View style={{ gap: 8 }}>
        <PrimaryButton title={busy ? 'Processando…' : 'Enviar resposta'} onPress={send} disabled={busy || !draft.trim()} />
        <PrimaryButton title="Encerrar e ver feedback" onPress={finish} disabled={busy || turns.length < 2} />
      </View>
    </Screen>
  );
}
