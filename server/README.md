# Proxy de conversação — SpeakFlow AI

Serviço mínimo que fica entre o aplicativo e o provedor de IA.

## Por que ele existe

O aplicativo é distribuído no dispositivo do usuário: qualquer chave embutida
nele pode ser extraída do binário ou do tráfego. Por isso o app **nunca** guarda
credencial de IA — ele fala apenas com este proxy, que detém a chave e é o único
ponto onde a política de uso pode ser aplicada.

Isso também mantém o app independente de fornecedor: trocar o motor por trás do
proxy não exige nova versão do aplicativo.

## Requisitos

- Node.js 22.18 ou superior (executa TypeScript diretamente).
- Uma chave da API da Anthropic.

## Rodar

```bash
cd server
npm install
export ANTHROPIC_API_KEY=sk-ant-...
npm start
```

Variáveis opcionais:

| Variável                   | Padrão            | Uso                                              |
| -------------------------- | ----------------- | ------------------------------------------------ |
| `PORT`                     | `8787`            | Porta do serviço.                                 |
| `SPEAKFLOW_MODEL`          | `claude-opus-5`   | Modelo usado nas três rotas.                      |
| `SPEAKFLOW_ALLOWED_ORIGIN` | `*`               | Origem liberada por CORS. Restrinja em produção.  |

Depois, aponte o app para o proxy:

```bash
# na raiz do repositório
export EXPO_PUBLIC_AI_BASE_URL=http://localhost:8787
npm start
```

Sem `EXPO_PUBLIC_AI_BASE_URL`, o app roda com o tutor simulado.

## Contrato HTTP

Todas as rotas recebem e devolvem JSON. `context` traz cenário, nível CEFR,
objetivo e nome do aluno.

### `POST /v1/conversation/start`

```json
{ "context": { "scenario": { "...": "..." }, "level": "B1", "goal": "work", "learnerName": "Ana" } }
```

Resposta: `{ "text": "..." }` — primeira fala do tutor, em inglês.

### `POST /v1/conversation/turn`

```json
{ "context": { "...": "..." }, "history": [{ "id": "...", "speaker": "learner", "text": "..." }], "learnerText": "..." }
```

Resposta:

```json
{
  "reply": "...",
  "correction": {
    "id": "correction-...",
    "category": "grammar",
    "original": "...",
    "natural": "...",
    "explanation": "...",
    "alternative": "..."
  }
}
```

`correction` é `null` quando não há nada relevante a registrar. A correção nunca
aparece dentro de `reply`: a conversa não é interrompida para corrigir.

### `POST /v1/conversation/feedback`

```json
{ "context": { "...": "..." }, "history": [], "corrections": [] }
```

Resposta: `wins`, `corrections`, `usefulVocabulary`, `fluencyNote`, `nextPractice`.
As correções devolvidas são exatamente as coletadas durante a sessão — o modelo
não as reescreve.

### `GET /health`

`{ "status": "ok" }`.

## Erros

| Status | Significado                                            |
| ------ | ------------------------------------------------------ |
| 400    | Corpo inválido (contexto ou fala do aluno ausentes).   |
| 404    | Rota inexistente.                                       |
| 422    | O provedor recusou a solicitação.                       |
| 500    | Falha inesperada; o detalhe fica no log do servidor.    |

## Antes de ir para produção

Este serviço cobre o contrato e a proteção da credencial. Ainda faltam, e são
pré-requisitos para exposição pública:

- Autenticação por usuário e limite de uso por conta.
- Restringir `SPEAKFLOW_ALLOWED_ORIGIN` às origens conhecidas.
- Observabilidade (latência, custo por sessão, taxa de recusa).
