import { Scenario } from '@/domain/types';

export const scenarios: Scenario[] = [
  {
    id: 'work-price-review',
    title: 'Negociar revisão de preço',
    description: 'Converse com um fornecedor sobre aumento de custos e revisão de preço.',
    objective: 'Explicar o problema, pedir revisão e negociar próximos passos.',
    level: 'B1',
    role: 'Supplier account manager',
    durationMinutes: 8,
    tags: ['trabalho', 'negociação', 'compras'],
  },
  {
    id: 'airport-checkin',
    title: 'Check-in no aeroporto',
    description: 'Faça check-in, confirme bagagem e resolva uma dúvida de assento.',
    objective: 'Concluir o check-in com clareza.',
    level: 'A2',
    role: 'Airline agent',
    durationMinutes: 6,
    tags: ['viagem', 'aeroporto'],
  },
  {
    id: 'hotel-problem',
    title: 'Resolver problema no hotel',
    description: 'O quarto reservado não corresponde ao combinado.',
    objective: 'Explicar o problema e chegar a uma solução.',
    level: 'B1',
    role: 'Hotel receptionist',
    durationMinutes: 8,
    tags: ['vida real', 'hotel', 'problemas'],
  },
  {
    id: 'small-talk',
    title: 'Small talk profissional',
    description: 'Converse antes do início de uma reunião internacional.',
    objective: 'Manter uma conversa natural por alguns minutos.',
    level: 'A2',
    role: 'International colleague',
    durationMinutes: 5,
    tags: ['trabalho', 'networking'],
  },
  {
    id: 'interview',
    title: 'Entrevista de emprego',
    description: 'Responda perguntas sobre experiência, resultados e objetivos.',
    objective: 'Apresentar sua experiência com clareza e confiança.',
    level: 'B2',
    role: 'Recruiter',
    durationMinutes: 10,
    tags: ['carreira', 'entrevista'],
  }
];
