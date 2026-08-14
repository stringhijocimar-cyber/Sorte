import { router } from 'expo-router';
import { ScrollView, Text, View } from 'react-native';
import { Card, palette, PrimaryButton, Screen, textStyles } from '@/components/ui';
import { useLearning } from '@/state/learning-context';

export default function FeedbackScreen() {
  const { lastFeedback } = useLearning();

  return (
    <ScrollView style={{ flex: 1, backgroundColor: palette.bg }} contentContainerStyle={{ flexGrow: 1 }}>
      <Screen>
        <Text style={textStyles.eyebrow}>Pós-aula</Text>
        <Text style={textStyles.h1}>O que levar desta prática</Text>

        <Card>
          <Text style={textStyles.h2}>Acertos</Text>
          {(lastFeedback?.wins ?? ['Sessão concluída.']).map((item) => <Text key={item} style={textStyles.body}>• {item}</Text>)}
        </Card>

        <Card>
          <Text style={textStyles.h2}>Correções prioritárias</Text>
          {(lastFeedback?.corrections ?? []).length === 0 ? (
            <Text style={textStyles.body}>Nenhuma correção registrada nesta sessão.</Text>
          ) : lastFeedback?.corrections.map((item) => (
            <View key={item.id} style={{ gap: 5 }}>
              <Text style={textStyles.small}>{item.category}</Text>
              <Text style={textStyles.body}>Você disse: {item.original}</Text>
              <Text style={textStyles.strong}>Forma natural: {item.natural}</Text>
              <Text style={textStyles.small}>{item.explanation}</Text>
            </View>
          ))}
        </Card>

        <Card>
          <Text style={textStyles.h2}>Vocabulário útil</Text>
          <Text style={textStyles.body}>{(lastFeedback?.usefulVocabulary ?? []).join(' · ')}</Text>
          <Text style={textStyles.strong}>Próxima prática</Text>
          <Text style={textStyles.body}>{lastFeedback?.nextPractice ?? 'Faça uma nova prática curta.'}</Text>
        </Card>

        <PrimaryButton title="Voltar para início" onPress={() => router.replace('/home')} />
      </Screen>
    </ScrollView>
  );
}
