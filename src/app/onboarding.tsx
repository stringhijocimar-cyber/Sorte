import { useState } from 'react';
import { router } from 'expo-router';
import { ScrollView, Text, TextInput, View } from 'react-native';
import { Choice, palette, PrimaryButton, Screen, textStyles } from '@/components/ui';
import { AccentPreference, LearningGoal } from '@/domain/types';
import { useLearning } from '@/state/learning-context';

export default function OnboardingScreen() {
  const { setProfile } = useLearning();
  const [name, setName] = useState('');
  const [goal, setGoal] = useState<LearningGoal>('work');
  const [dailyMinutes, setDailyMinutes] = useState<5 | 10 | 20 | 40>(20);
  const [accent, setAccent] = useState<AccentPreference>('american');

  function finish() {
    setProfile({
      name: name.trim() || 'Learner',
      goal,
      dailyMinutes,
      accent,
      currentLevel: 'A1',
      targetLevel: 'B2',
    });
    router.push('/diagnostic');
  }

  return (
    <ScrollView style={{ flex: 1, backgroundColor: palette.bg }} contentContainerStyle={{ flexGrow: 1 }}>
      <Screen>
        <Text style={textStyles.h2}>Personalize sua trilha</Text>
        <Text style={textStyles.body}>Usaremos essas escolhas para ajustar contexto, dificuldade e duração das práticas.</Text>

        <View style={{ gap: 8 }}>
          <Text style={textStyles.strong}>Como quer ser chamado?</Text>
          <TextInput
            value={name}
            onChangeText={setName}
            placeholder="Seu nome"
            placeholderTextColor={palette.muted}
            style={{ color: palette.text, backgroundColor: palette.surface, borderColor: palette.line, borderWidth: 1, borderRadius: 14, padding: 14, fontSize: 16 }}
          />
        </View>

        <View style={{ gap: 8 }}>
          <Text style={textStyles.strong}>Objetivo principal</Text>
          <Choice title="Trabalho e reuniões" selected={goal === 'work'} onPress={() => setGoal('work')} />
          <Choice title="Viagens" selected={goal === 'travel'} onPress={() => setGoal('travel')} />
          <Choice title="Entrevistas" selected={goal === 'interviews'} onPress={() => setGoal('interviews')} />
          <Choice title="Vida cotidiana" selected={goal === 'daily-life'} onPress={() => setGoal('daily-life')} />
        </View>

        <View style={{ gap: 8 }}>
          <Text style={textStyles.strong}>Tempo diário</Text>
          <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
            {[5, 10, 20, 40].map((minutes) => (
              <Choice key={minutes} title={`${minutes} min`} selected={dailyMinutes === minutes} onPress={() => setDailyMinutes(minutes as 5 | 10 | 20 | 40)} />
            ))}
          </View>
        </View>

        <View style={{ gap: 8 }}>
          <Text style={textStyles.strong}>Preferência de sotaque</Text>
          <Choice title="Americano" selected={accent === 'american'} onPress={() => setAccent('american')} />
          <Choice title="Britânico" selected={accent === 'british'} onPress={() => setAccent('british')} />
          <Choice title="Misto / internacional" selected={accent === 'mixed'} onPress={() => setAccent('mixed')} />
        </View>

        <PrimaryButton title="Fazer diagnóstico" onPress={finish} />
      </Screen>
    </ScrollView>
  );
}
