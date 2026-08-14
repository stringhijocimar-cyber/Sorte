# SpeakFlow AI

Aplicativo multiplataforma para prática de inglês falado com IA, desenhado para unir diagnóstico, conversação, feedback e revisão em um único fluxo de aprendizagem.

## O que já funciona ponta a ponta

- Expo + React Native + TypeScript, com Expo Router
- Onboarding guiado e diagnóstico inicial simplificado
- **Perfil, diagnóstico e histórico de sessões persistidos no dispositivo**
- Home com rotina diária, sessões recentes e biblioteca de cenários reais
- Sessão de conversação com **tutor real por IA** (via proxy) ou tutor simulado
- **Voz**: fala do tutor (TTS) e ditado do aluno (STT, onde a plataforma oferece)
- **Registro de erros durante a conversa**, sem interromper o aluno
- Feedback pós-sessão e **painel de progresso por competência**

## Rodar localmente

Só o aplicativo, com tutor simulado:

```bash
npm install
npm start
```

Com tutor real por IA, suba também o proxy (ele detém a credencial):

```bash
# terminal 1
cd server && npm install
export ANTHROPIC_API_KEY=sk-ant-...
npm start

# terminal 2, na raiz
export EXPO_PUBLIC_AI_BASE_URL=http://localhost:8787
npm start
```

Sem `EXPO_PUBLIC_AI_BASE_URL`, o app usa o tutor simulado e todo o fluxo continua
navegável. A Home indica qual motor está ativo.

Verificações:

```bash
npm run typecheck              # aplicativo
cd server && npm run typecheck # proxy
```

## Onde fica a chave de IA

No proxy, nunca no aplicativo. O app é distribuído no dispositivo do usuário:
qualquer credencial embutida nele pode ser extraída. O app conhece apenas um
contrato HTTP (`server/README.md`) — trocar o motor de IA por trás do proxy não
exige nova versão do aplicativo.

## Regra de produto

Um módulo só é considerado pronto quando funciona ponta a ponta: entrada do usuário, regra de negócio, persistência/integração necessária e retorno perceptível ao usuário.

## Próximas integrações

1. Conta de usuário e sincronização entre dispositivos.
2. Reconhecimento de fala nativo em iOS/Android e análise de pronúncia.
3. Diagnóstico adaptativo completo (listening, reading, vocabulary, grammar…).
4. Revisão espaçada a partir dos erros já registrados.
5. Rotina adaptativa de 5, 10, 20 e 40 minutos.
