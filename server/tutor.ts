import Anthropic from '@anthropic-ai/sdk';
import { feedbackSchema, startSchema, turnSchema } from './schemas.ts';

const MODEL = process.env.SPEAKFLOW_MODEL ?? 'claude-opus-5';

const client = new Anthropic();

export interface ConversationContext {
  scenario: {
    id: string;
    title: string;
    description: string;
    objective: string;
    level: string;
    role: string;
    durationMinutes: number;
    tags: string[];
  };
  level: string;
  goal: string;
  learnerName: string;
}

export interface ConversationTurn {
  id: string;
  speaker: 'learner' | 'tutor';
  text: string;
}

export interface Correction {
  id: string;
  category: string;
  original: string;
  natural: string;
  explanation: string;
  alternative: string;
}

export class TutorRefusalError extends Error {
  // Campo declarado explicitamente: o Node executa TypeScript apenas removendo
  // tipos, e nesse modo parameter properties não existem.
  readonly category: string | null;

  constructor(category: string | null) {
    super('O tutor não pôde responder a esta solicitação.');
    this.name = 'TutorRefusalError';
    this.category = category;
  }
}

function systemPrompt(context: ConversationContext): string {
  const { scenario } = context;
  return [
    `Você é o tutor de conversação do SpeakFlow AI, um app de prática de inglês falado para brasileiros.`,
    `Neste cenário você interpreta: ${scenario.role}.`,
    `Situação: ${scenario.description}`,
    `Objetivo do aluno: ${scenario.objective}`,
    `Aluno: ${context.learnerName}, nível CEFR estimado ${context.level}, objetivo de aprendizagem "${context.goal}".`,
    '',
    'Como conduzir a conversa:',
    '- Fale sempre em inglês, no papel do personagem. Nunca saia do papel dentro do campo "reply".',
    `- Calibre vocabulário e velocidade para o nível ${context.level}: mais simples em A1/A2, mais idiomático em B2+.`,
    '- Mantenha cada fala curta (1 a 3 frases) e termine com algo que faça o aluno falar de novo.',
    '- Conduza a tarefa até uma conclusão realista; não fique repetindo perguntas genéricas.',
    '- Nunca corrija o aluno dentro de "reply". A conversa não é interrompida para correção.',
    '',
    'Como registrar correções:',
    '- Em "correction", registre no máximo um erro — o mais relevante da última fala do aluno.',
    '- Use null quando a fala estiver comunicativamente adequada, quando o erro for irrelevante',
    '  para o entendimento, ou quando já tiver sido registrado antes na mesma conversa.',
    '- "original" deve citar o trecho exato dito pelo aluno.',
    '- "explanation" e "alternative" são escritos em português, curtos e diretos.',
  ].join('\n');
}

function transcript(history: ConversationTurn[]): string {
  return history
    .map((turn) => `${turn.speaker === 'learner' ? 'Aluno' : 'Tutor'}: ${turn.text}`)
    .join('\n');
}

/** Extrai o JSON da resposta, tratando recusa e conteúdo vazio explicitamente. */
function parseStructured<T>(message: Anthropic.Beta.BetaMessage): T {
  if (message.stop_reason === 'refusal') {
    throw new TutorRefusalError(message.stop_details?.category ?? null);
  }

  const text = message.content
    .filter((block): block is Anthropic.Beta.BetaTextBlock => block.type === 'text')
    .map((block) => block.text)
    .join('');

  if (!text.trim()) {
    throw new Error(`Resposta vazia do modelo (stop_reason: ${message.stop_reason}).`);
  }

  return JSON.parse(text) as T;
}

interface AskOptions {
  system: string;
  prompt: string;
  schema: unknown;
  maxTokens: number;
  effort: 'low' | 'medium' | 'high';
}

async function ask<T>({ system, prompt, schema, maxTokens, effort }: AskOptions): Promise<T> {
  const message = await client.beta.messages.create({
    model: MODEL,
    max_tokens: maxTokens,
    // Recusa de classificador é reencaminhada ao modelo de fallback recomendado.
    betas: ['server-side-fallback-2026-07-01'],
    fallbacks: 'default',
    system,
    output_config: {
      effort,
      format: { type: 'json_schema', schema: schema as Record<string, unknown> },
    },
    messages: [{ role: 'user', content: prompt }],
  });

  return parseStructured<T>(message);
}

export async function startScenario(context: ConversationContext): Promise<{ text: string }> {
  return ask<{ text: string }>({
    system: systemPrompt(context),
    prompt:
      'Abra a conversa: apresente-se no papel, situe o contexto em uma frase e faça a primeira pergunta que leva o aluno a falar.',
    schema: startSchema,
    maxTokens: 4096,
    effort: 'low',
  });
}

export async function nextTurn(
  context: ConversationContext,
  history: ConversationTurn[],
  learnerText: string
): Promise<{ reply: string; correction: Correction | null }> {
  const result = await ask<{ reply: string; correction: Omit<Correction, 'id'> | null }>({
    system: systemPrompt(context),
    prompt: [
      'Conversa até aqui:',
      transcript(history),
      '',
      `Última fala do aluno: ${learnerText}`,
      '',
      'Responda no papel e registre no máximo uma correção relevante.',
    ].join('\n'),
    schema: turnSchema,
    // Latência importa numa conversa ao vivo: esforço baixo com raciocínio adaptativo.
    maxTokens: 4096,
    effort: 'low',
  });

  return {
    reply: result.reply,
    correction: result.correction
      ? { id: `correction-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`, ...result.correction }
      : null,
  };
}

export async function buildFeedback(
  context: ConversationContext,
  history: ConversationTurn[],
  corrections: Correction[]
) {
  const result = await ask<{
    wins: string[];
    usefulVocabulary: string[];
    fluencyNote: string;
    nextPractice: string;
  }>({
    system: systemPrompt(context),
    prompt: [
      'A prática terminou. Escreva o feedback pós-aula em português.',
      '',
      'Transcrição completa:',
      transcript(history),
      '',
      corrections.length > 0
        ? `Correções já registradas durante a conversa:\n${corrections
            .map((item) => `- [${item.category}] "${item.original}" → "${item.natural}"`)
            .join('\n')}`
        : 'Nenhuma correção foi registrada durante a conversa.',
      '',
      'Avalie a conclusão da tarefa, não a perfeição gramatical. Seja específico e encorajador.',
    ].join('\n'),
    schema: feedbackSchema,
    maxTokens: 8192,
    effort: 'medium',
  });

  // As correções da sessão são a fonte da verdade; o modelo não as reescreve.
  return { ...result, corrections };
}
