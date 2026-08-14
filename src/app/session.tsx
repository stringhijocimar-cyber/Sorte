import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { router, useLocalSearchParams } from 'expo-router';
import { ScrollView, Text, TextInput, View } from 'react-native';
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
import { Correction, PracticeSession } from '@/domain/types';
import {
  ConversationContext,
  ConversationTurn,
  conversationService,
  learnerTurn as makeLearnerTurn,
} from '@/services/conversation';
import { speechService } from '@/services/speech-service';
import { useLearning } from '@/state/learning-context';

export default function SessionScreen() {
  const params = useLocalSearchParams<{ scenarioId?: string }>();
  const scenario = useMemo(
    () => scenarios.find((item) => item.id === params.scenarioId) ?? scenarios[0],
    [params.scenarioId]
  );
  const { profile, level, recordSession } = useLearning();

  const [turns, setTurns] = useState<ConversationTurn[]>([]);
  const [draft, setDraft] = useState('');
  const [partial, setPartial] = useState('');
  const [busy, setBusy] = useState(false);
  const [starting, setStarting] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [voiceEnabled, setVoiceEnabled] = useState(true);
  const [listening, setListening] = useState(false);
  /** Erros registrados durante a conversa — sem interromper o aluno. */
  const [corrections, setCorrections] = useState<Correction[]>([]);

  const startedAt = useRef(new Date().toISOString());
  const scrollRef = useRef<ScrollView>(null);
  const recognitionRef = useRef<{ stop: () => void } | null>(null);
  const accent = profile?.accent ?? 'american';
  const recognitionAvailable = speechService.isRecognitionAvailable();

  const context = useMemo<ConversationContext>(
    () => ({
      scenario,
      level,
      goal: profile?.goal ?? 'work',
      learnerName: profile?.name ?? 'Learner',
    }),
    [scenario, level, profile?.goal, profile?.name]
  );

  const speak = useCallback(
    (text: string) => {
      if (!voiceEnabled) return;
      void speechService.speak(text, accent);
    },
    [voiceEnabled, accent]
  );

  const start = useCallback(async () => {
    setStarting(true);
    setError(null);
    try {
      const opening = await conversationService.startScenario(context);
      setTurns([opening]);
      speak(opening.text);
    } catch (cause) {
      setError((cause as Error).message);
    } finally {
      setStarting(false);
    }
  }, [context, speak]);

  useEffect(() => {
    void start();
    return () => {
      recognitionRef.current?.stop();
      void speechService.stopSpeaking();
    };
    // A abertura depende apenas do cenário escolhido nesta tela.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [scenario.id]);

  async function send() {
    const text = draft.trim();
    if (!text || busy) return;

    setDraft('');
    setPartial('');
    setError(null);
    setBusy(true);

    const nextHistory = [...turns, makeLearnerTurn(text)];
    setTurns(nextHistory);

    try {
      const { turn, correction } = await conversationService.reply(context, nextHistory, text);
      setTurns([...nextHistory, turn]);
      if (correction) setCorrections((current) => [...current, correction]);
      speak(turn.text);
    } catch (cause) {
      setError((cause as Error).message);
      // A fala do aluno é devolvida ao campo para que nada se perca.
      setDraft(text);
      setTurns(turns);
    } finally {
      setBusy(false);
    }
  }

  function toggleListening() {
    if (listening) {
      recognitionRef.current?.stop();
      return;
    }
    setError(null);
    setListening(true);
    void speechService.stopSpeaking();
    recognitionRef.current = speechService.startRecognition(accent, {
      onPartial: setPartial,
      onResult: (text) => {
        setPartial('');
        setDraft((current) => (current ? `${current} ${text}` : text));
      },
      onError: (message) => setError(message),
      onEnd: () => {
        setListening(false);
        setPartial('');
        recognitionRef.current = null;
      },
    });
  }

  async function finish() {
    if (busy) return;
    setBusy(true);
    setError(null);
    void speechService.stopSpeaking();

    try {
      const feedback = await conversationService.buildFeedback(context, turns, corrections);
      const session: PracticeSession = {
        id: `session-${Date.now()}`,
        scenarioId: scenario.id,
        scenarioTitle: scenario.title,
        level,
        startedAt: startedAt.current,
        endedAt: new Date().toISOString(),
        learnerTurns: turns.filter((turn) => turn.speaker === 'learner').length,
        corrections,
        feedback,
      };
      await recordSession(session);
      router.replace('/feedback');
    } catch (cause) {
      setError((cause as Error).message);
    } finally {
      setBusy(false);
    }
  }

  if (starting) {
    return (
      <Screen>
        <Loading label="Preparando o cenário…" />
        {error ? (
          <View style={{ gap: 10 }}>
            <Notice message={error} tone="error" />
            <PrimaryButton title="Tentar novamente" onPress={() => void start()} />
          </View>
        ) : null}
      </Screen>
    );
  }

  const learnerTurnCount = turns.filter((turn) => turn.speaker === 'learner').length;

  return (
    <Screen>
      <View style={{ gap: 6 }}>
        <Text style={textStyles.eyebrow}>{scenario.role}</Text>
        <Text style={textStyles.h2}>{scenario.title}</Text>
        <Text style={textStyles.small}>{scenario.objective}</Text>
        <View style={{ flexDirection: 'row', gap: 8, flexWrap: 'wrap', marginTop: 4 }}>
          <Tag label={`${learnerTurnCount} turno(s)`} />
          {corrections.length > 0 ? <Tag label={`${corrections.length} anotação(ões)`} /> : null}
        </View>
      </View>

      <ScrollView
        ref={scrollRef}
        style={{ flex: 1 }}
        contentContainerStyle={{ gap: 10, paddingVertical: 8 }}
        onContentSizeChange={() => scrollRef.current?.scrollToEnd({ animated: true })}
      >
        {turns.map((turn) => (
          <Card
            key={turn.id}
            style={{
              alignSelf: turn.speaker === 'learner' ? 'flex-end' : 'flex-start',
              maxWidth: '90%',
              backgroundColor: turn.speaker === 'learner' ? palette.surface2 : palette.surface,
            }}
          >
            <Text style={textStyles.small}>{turn.speaker === 'learner' ? 'Você' : 'Tutor'}</Text>
            <Text style={textStyles.strong}>{turn.text}</Text>
          </Card>
        ))}
        {busy ? <Text style={textStyles.small}>Tutor está respondendo…</Text> : null}
      </ScrollView>

      {error ? <Notice message={error} tone="error" /> : null}

      <TextInput
        value={listening && partial ? `${draft} ${partial}`.trim() : draft}
        onChangeText={setDraft}
        multiline
        editable={!listening}
        placeholder={listening ? 'Ouvindo…' : 'Responda em inglês…'}
        placeholderTextColor={palette.muted}
        style={{
          minHeight: 58,
          maxHeight: 120,
          color: palette.text,
          backgroundColor: palette.surface,
          borderColor: listening ? palette.accent : palette.line,
          borderWidth: 1,
          borderRadius: 16,
          padding: 14,
          fontSize: 16,
        }}
      />

      <View style={{ flexDirection: 'row', gap: 8 }}>
        <View style={{ flex: 1 }}>
          <SecondaryButton
            title={voiceEnabled ? '🔊 Voz do tutor' : '🔇 Voz desligada'}
            tone={voiceEnabled ? 'active' : 'neutral'}
            onPress={() => {
              if (voiceEnabled) void speechService.stopSpeaking();
              setVoiceEnabled(!voiceEnabled);
            }}
          />
        </View>
        <View style={{ flex: 1 }}>
          <SecondaryButton
            title={listening ? '⏹ Parar' : '🎙 Falar'}
            tone={listening ? 'active' : 'neutral'}
            onPress={toggleListening}
            disabled={!recognitionAvailable || busy}
          />
        </View>
      </View>

      {!recognitionAvailable ? (
        <Text style={textStyles.small}>
          Ditado por voz ainda não disponível nesta plataforma — responda por escrito.
        </Text>
      ) : null}

      <View style={{ gap: 8 }}>
        <PrimaryButton
          title={busy ? 'Processando…' : 'Enviar resposta'}
          onPress={() => void send()}
          disabled={busy || listening || !draft.trim()}
        />
        <SecondaryButton
          title="Encerrar e ver feedback"
          onPress={() => void finish()}
          disabled={busy || turns.length < 2}
        />
      </View>
    </Screen>
  );
}
