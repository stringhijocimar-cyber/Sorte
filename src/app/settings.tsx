import { useState } from 'react';
import { router } from 'expo-router';
import { ScrollView, Text, TextInput, View } from 'react-native';
import {
  Card,
  Notice,
  palette,
  PrimaryButton,
  Screen,
  SecondaryButton,
  Tag,
  textStyles,
} from '@/components/ui';
import { checkProxyHealth } from '@/services/conversation';
import { useLearning } from '@/state/learning-context';

type Status = { tone: 'info' | 'error'; message: string } | null;

export default function SettingsScreen() {
  const { aiBaseUrl, setAiBaseUrl, resetProgress } = useLearning();
  const [url, setUrl] = useState(aiBaseUrl ?? '');
  const [status, setStatus] = useState<Status>(null);
  const [busy, setBusy] = useState(false);

  async function connect() {
    setBusy(true);
    setStatus(null);
    try {
      // Só salva depois de confirmar que o servidor responde: evita deixar o
      // app apontado para um endereço errado e sem tutor.
      await checkProxyHealth(url);
      await setAiBaseUrl(url.trim());
      setStatus({ tone: 'info', message: 'Conectado. Suas práticas agora usam o tutor por IA.' });
    } catch (error) {
      setStatus({ tone: 'error', message: (error as Error).message });
    } finally {
      setBusy(false);
    }
  }

  async function disconnect() {
    setBusy(true);
    try {
      await setAiBaseUrl(null);
      setUrl('');
      setStatus({ tone: 'info', message: 'Desconectado. O app voltou ao tutor simulado.' });
    } finally {
      setBusy(false);
    }
  }

  async function reset() {
    setBusy(true);
    try {
      await resetProgress();
      router.replace('/');
    } finally {
      setBusy(false);
    }
  }

  return (
    <ScrollView style={{ flex: 1, backgroundColor: palette.bg }} contentContainerStyle={{ flexGrow: 1 }}>
      <Screen>
        <View style={{ gap: 4 }}>
          <Text style={textStyles.eyebrow}>Ajustes</Text>
          <Text style={textStyles.h1}>Tutor por IA</Text>
        </View>

        <Card>
          <View style={{ flexDirection: 'row', gap: 8, flexWrap: 'wrap' }}>
            <Tag label={aiBaseUrl ? 'Tutor por IA ativo' : 'Tutor simulado'} />
            {aiBaseUrl ? <Tag label={aiBaseUrl} /> : null}
          </View>

          <Text style={textStyles.body}>
            O app não guarda chave de IA. Ele conversa com um servidor seu — o proxy em `server/` —
            que é quem detém a credencial. Informe o endereço dele abaixo.
          </Text>

          <TextInput
            value={url}
            onChangeText={setUrl}
            autoCapitalize="none"
            autoCorrect={false}
            keyboardType="url"
            placeholder="https://meu-servidor.exemplo.com"
            placeholderTextColor={palette.muted}
            style={{
              color: palette.text,
              backgroundColor: palette.surface2,
              borderColor: palette.line,
              borderWidth: 1,
              borderRadius: 14,
              padding: 14,
              fontSize: 16,
            }}
          />

          {status ? <Notice message={status.message} tone={status.tone} /> : null}

          <PrimaryButton
            title={busy ? 'Verificando…' : 'Testar e conectar'}
            onPress={() => void connect()}
            disabled={busy || !url.trim()}
          />
          {aiBaseUrl ? (
            <SecondaryButton
              title="Desconectar e usar tutor simulado"
              onPress={() => void disconnect()}
              disabled={busy}
            />
          ) : null}
        </Card>

        <Card>
          <Text style={textStyles.h2}>Sem servidor configurado?</Text>
          <Text style={textStyles.body}>
            O app funciona sem ele: o tutor simulado mantém todo o fluxo navegável, mas as respostas
            são fixas e nenhuma correção é gerada. Para conversação real, suba o proxy seguindo o
            `server/README.md` do repositório.
          </Text>
        </Card>

        <Card>
          <Text style={textStyles.h2}>Apagar meu progresso</Text>
          <Text style={textStyles.body}>
            Remove perfil, diagnóstico e histórico deste aparelho. O endereço do servidor é mantido.
          </Text>
          <SecondaryButton title="Apagar progresso" onPress={() => void reset()} disabled={busy} />
        </Card>

        <SecondaryButton title="Voltar" onPress={() => router.back()} />
      </Screen>
    </ScrollView>
  );
}
