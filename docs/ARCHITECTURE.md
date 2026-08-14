# Arquitetura — SpeakFlow AI

## Objetivo

Manter interface, domínio e integrações separados para que IA, voz, persistência e analytics possam evoluir independentemente.

## Camadas

### `src/app`
Rotas e composição de telas usando Expo Router.

### `src/components`
Componentes de interface reutilizáveis e tokens visuais.

### `src/domain`
Tipos e regras centrais: nível CEFR, perfil, competências, cenários, correções,
sessões e cálculo de progresso (`progress.ts`).

### `src/data`
Conteúdo inicial estático de cenários. Depois poderá ser substituído/estendido por backend/CMS.

### `src/config`
Leitura de variáveis de ambiente. Uma única variável (`EXPO_PUBLIC_AI_BASE_URL`)
decide se a conversação é real ou simulada.

### `src/services`
Contratos de integrações:

- `conversation/` — `ConversationService` com duas implementações
  (`MockConversationService` e `RemoteConversationService`). A UI nunca conhece
  o fornecedor de IA.
- `learning-repository.ts` — persistência local sobre AsyncStorage.
- `speech-service.ts` — síntese de fala e reconhecimento, com degradação
  explícita onde a plataforma não oferece reconhecimento.

### `src/state`
Estado de aprendizagem compartilhado, hidratado do repositório na abertura do app
e responsável por gravar cada mudança relevante.

### `server/`
Proxy de conversação. Detém a credencial do provedor de IA e expõe o contrato
HTTP que o app consome. Ver `server/README.md`.

## Fluxo ponta a ponta implementado

`Boas-vindas -> Onboarding -> Diagnóstico -> Home -> Cenário -> Conversação -> Feedback -> Progresso`

Na reabertura, quem já concluiu onboarding e diagnóstico vai direto para a Home.

## Decisões

### A credencial de IA nunca vai para o aplicativo
O app é distribuído no dispositivo do usuário; qualquer chave embutida nele pode
ser extraída do binário ou do tráfego. Por isso a conversa real passa pelo proxy,
que é também o único ponto onde autenticação por usuário e limite de uso podem
ser aplicados.

### Correção não interrompe a conversa
O tutor responde no papel e, em paralelo, devolve no máximo uma correção por
turno. A correção é acumulada em segundo plano e só é apresentada no feedback
pós-aula. A interface mostra apenas um contador discreto durante a prática.

### As correções da sessão são a fonte da verdade
No fechamento, o modelo escreve acertos, vocabulário e recomendação, mas **não**
reescreve as correções: as que valem são exatamente as registradas durante a
conversa.

### Progresso é derivado, não armazenado
`computeSkillProgress` combina o nível CEFR estimado com o histórico real de
sessões e erros. Mudar a regra de pontuação não exige migração de dados.

## Fronteiras para próximas integrações

- `ConversationService`: já implementado com IA real; próximo passo é streaming
  de resposta.
- `SpeechService`: falta reconhecimento nativo em iOS/Android e análise de pronúncia.
- `LearningRepository`: local; falta conta de usuário e sincronização remota.
- `AssessmentEngine` (próximo): diagnóstico adaptativo e matriz de competências completa.
