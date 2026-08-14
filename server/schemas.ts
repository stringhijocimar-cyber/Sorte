/**
 * Esquemas de saída estruturada.
 *
 * Eles são a fronteira do contrato com o modelo: o app recebe sempre o mesmo
 * formato, independentemente de como o prompt evolua.
 */

export const correctionCategories = [
  'grammar',
  'word-choice',
  'pronunciation',
  'rhythm',
  'intonation',
  'literal-translation',
  'register',
  'comprehension',
] as const;

const correctionSchema = {
  type: 'object',
  additionalProperties: false,
  required: ['category', 'original', 'natural', 'explanation', 'alternative'],
  properties: {
    category: {
      type: 'string',
      enum: [...correctionCategories],
      description: 'Tipo do erro observado.',
    },
    original: { type: 'string', description: 'Trecho exato dito pelo aluno.' },
    natural: { type: 'string', description: 'Como um falante nativo diria.' },
    explanation: {
      type: 'string',
      description: 'Explicação curta em português, sem jargão gramatical pesado.',
    },
    alternative: {
      type: 'string',
      description: 'Uma segunda forma natural de dizer a mesma ideia.',
    },
  },
} as const;

export const startSchema = {
  type: 'object',
  additionalProperties: false,
  required: ['text'],
  properties: {
    text: {
      type: 'string',
      description: 'Primeira fala do tutor em inglês, no papel do cenário.',
    },
  },
} as const;

export const turnSchema = {
  type: 'object',
  additionalProperties: false,
  required: ['reply', 'correction'],
  properties: {
    reply: {
      type: 'string',
      description: 'Resposta do tutor em inglês, mantendo o papel e o cenário.',
    },
    correction: {
      anyOf: [correctionSchema, { type: 'null' }],
      description:
        'Erro mais relevante da última fala do aluno, ou null quando não houver nada que valha corrigir.',
    },
  },
} as const;

export const feedbackSchema = {
  type: 'object',
  additionalProperties: false,
  required: ['wins', 'usefulVocabulary', 'fluencyNote', 'nextPractice'],
  properties: {
    wins: {
      type: 'array',
      items: { type: 'string' },
      description: 'De 2 a 4 acertos concretos, em português.',
    },
    usefulVocabulary: {
      type: 'array',
      items: { type: 'string' },
      description: 'De 3 a 6 expressões em inglês úteis para repetir este cenário.',
    },
    fluencyNote: { type: 'string', description: 'Observação sobre fluência, em português.' },
    nextPractice: { type: 'string', description: 'Recomendação da próxima prática, em português.' },
  },
} as const;
