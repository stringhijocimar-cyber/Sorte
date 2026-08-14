# Arquitetura inicial — SpeakFlow AI

## Objetivo

Manter interface, domínio e integrações separados para que IA, voz, persistência e analytics possam evoluir independentemente.

## Camadas

### `src/app`
Rotas e composição de telas usando Expo Router.

### `src/components`
Componentes de interface reutilizáveis e tokens visuais.

### `src/domain`
Tipos e regras centrais: nível CEFR, perfil, competências, cenários, correções e feedback.

### `src/data`
Conteúdo inicial estático de cenários. Depois poderá ser substituído/estendido por backend/CMS.

### `src/services`
Contratos de integrações. O `ConversationService` já impede que a UI dependa diretamente de um fornecedor específico de IA.

### `src/state`
Estado de aprendizagem compartilhado. Nesta fundação é em memória; a próxima etapa deve implementar persistência e sincronização.

## Fluxo ponta a ponta implementado

`Boas-vindas -> Onboarding -> Diagnóstico -> Home -> Cenário -> Conversação -> Feedback -> Home`

## Fronteiras para próximas integrações

- `ConversationService`: LLM/conversação e feedback pedagógico.
- `SpeechService` (próximo): STT, TTS e análise de pronúncia.
- `LearningRepository` (próximo): perfil, sessões, revisão e progresso.
- `AssessmentEngine` (próximo): diagnóstico adaptativo e matriz de competências.
