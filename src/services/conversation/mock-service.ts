import { Correction, SessionFeedback } from '@/domain/types';
import {
  ConversationContext,
  ConversationService,
  ConversationTurn,
  TutorReply,
  tutorTurn,
} from '@/services/conversation/types';

/**
 * Serviço simulado. Mantém o app inteiro navegável sem backend configurado
 * (desenvolvimento, demonstração e testes de fluxo).
 */
export class MockConversationService implements ConversationService {
  readonly kind = 'mock' as const;

  async startScenario(context: ConversationContext): Promise<ConversationTurn> {
    const { scenario } = context;
    return tutorTurn(
      `Hi! I'll be your ${scenario.role}. ${scenario.objective} Ready? Tell me how you would start.`
    );
  }

  async reply(
    _context: ConversationContext,
    _history: ConversationTurn[],
    learnerText: string
  ): Promise<TutorReply> {
    const normalized = learnerText.trim();
    const followUp =
      normalized.length < 18
        ? 'Good start. Can you add one more detail?'
        : 'I understand. What result would you like to achieve in this situation?';

    return { turn: tutorTurn(followUp), correction: null };
  }

  async buildFeedback(
    _context: ConversationContext,
    history: ConversationTurn[],
    corrections: Correction[]
  ): Promise<SessionFeedback> {
    const learnerTurns = history.filter((turn) => turn.speaker === 'learner');

    return {
      wins: [
        `Você sustentou ${learnerTurns.length} turno(s) de fala.`,
        'Você priorizou comunicação e conclusão da tarefa.',
      ],
      corrections,
      usefulVocabulary: ['Could you clarify?', 'What I mean is…', 'I’d like to…'],
      fluencyNote:
        'Priorize frases completas e mantenha o turno de fala sem buscar perfeição em cada palavra.',
      nextPractice:
        'Repita o cenário e tente acrescentar uma justificativa e uma pergunta de follow-up.',
    };
  }
}
