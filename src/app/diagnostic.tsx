import { useState } from 'react';
import { router } from 'expo-router';
import { ScrollView, Text, View } from 'react-native';
import { Card, Choice, palette, PrimaryButton, Screen, textStyles } from '@/components/ui';
import { CefrLevel } from '@/domain/types';
import { useLearning } from '@/state/learning-context';

const questions = [
  {
    prompt: 'Which sentence sounds most natural?',
    options: ['I have 25 years.', 'I am 25 years old.', 'I am with 25 years.'],
    answer: 1,
  },
  {
    prompt: 'Your supplier says: “Our costs went up.” What is the clearest follow-up?',
    options: ['Why?', 'Could you explain which costs increased and by how much?', 'You are wrong.'],
    answer: 1,
  },
  {
    prompt: 'Choose the best phrase to disagree politely in a meeting.',
    options: ['I see it differently. Could we look at the data?', 'No.', 'This makes no sense.'],
    answer: 0,
  },
];

function estimate(score: number): CefrLevel {
  if (score === 3) return 'B2';
  if (score === 2) return 'B1';
  if (score === 1) return 'A2';
  return 'A1';
}

export default function DiagnosticScreen() {
  const { setDiagnosticLevel, profile, setProfile } = useLearning();
  const [answers, setAnswers] = useState<Record<number, number>>({});

  function complete() {
    const score = questions.reduce((total, question, index) => total + (answers[index] === question.answer ? 1 : 0), 0);
    const level = estimate(score);
    setDiagnosticLevel(level);
    if (profile) setProfile({ ...profile, currentLevel: level });
    router.replace('/home');
  }

  return (
    <ScrollView style={{ flex: 1, backgroundColor: palette.bg }} contentContainerStyle={{ flexGrow: 1 }}>
      <Screen>
        <Text style={textStyles.h2}>Primeira leitura do seu nível</Text>
        <Text style={textStyles.body}>Esta fundação usa um diagnóstico curto. A versão completa incluirá áudio, fala, leitura, vocabulário e interação.</Text>
        {questions.map((question, index) => (
          <Card key={question.prompt}>
            <Text style={textStyles.strong}>{index + 1}. {question.prompt}</Text>
            <View style={{ gap: 8 }}>
              {question.options.map((option, optionIndex) => (
                <Choice key={option} title={option} selected={answers[index] === optionIndex} onPress={() => setAnswers((current) => ({ ...current, [index]: optionIndex }))} />
              ))}
            </View>
          </Card>
        ))}
        <PrimaryButton title="Ver meu nível" onPress={complete} disabled={Object.keys(answers).length !== questions.length} />
      </Screen>
    </ScrollView>
  );
}
