import { Scenario, SessionFeedback } from '@/domain/types';

export interface ConversationTurn {
  id: string;
  speaker: 'learner' | 'tutor';
  text: string;
}

export interface ConversationService {
  startScenario(scenario: Scenario): Promise<ConversationTurn>;
  reply(scenario: Scenario, history: ConversationTurn[], learnerText: string): Promise<ConversationTurn>;
  buildFeedback(scenario: Scenario, history: ConversationTurn[]): Promise<SessionFeedback>;
}

class MockConversationService implements ConversationService {
  async startScenario(scenario: Scenario): Promise<ConversationTurn> {
    return {
      id: `tutor-${Date.now()}`,
      speaker: 'tutor',
      text: `Hi! I’ll be your ${scenario.role}. ${scenario.objective} Ready? Tell me how you would start.`,
    };
  }

  async reply(_scenario: Scenario, _history: ConversationTurn[], learnerText: string): Promise<ConversationTurn> {
    const normalized = learnerText.trim();
    const followUp = normalized.length < 18
      ? 'Good start. Can you add one more detail?'
      : 'I understand. What result would you like to achieve in this situation?';

    return {
      id: `tutor-${Date.now()}`,
      speaker: 'tutor',
      text: followUp,
    };
  }

  async buildFeedback(_scenario: Scenario, history: ConversationTurn[]): Promise<SessionFeedback> {
    const learnerTurns = history.filter((turn) => turn.speaker === 'learner');
    return {
      wins: [
        `Você sustentou ${learnerTurns.length} turno(s) de fala.`,
        'Você priorizou comunicação e conclusão da tarefa.',
      ],
      corrections: learnerTurns.length > 0
        ? [
            {
              id: 'correction-1',
              category: 'grammar',
              original: learnerTurns[0].text,
              natural: learnerTurns[0].text,
              explanation: 'A integração real de IA substituirá este feedback demonstrativo por uma correção contextual.',
              alternative: 'Tente repetir a ideia usando uma frase um pouco mais completa.',
            },
          ]
        : [],
      usefulVocabulary: ['Could you clarify?', 'What I mean is…', 'I’d like to…'],
      fluencyNote: 'Priorize frases completas e mantenha o turno de fala sem buscar perfeição em cada palavra.',
      nextPractice: 'Repita o cenário e tente acrescentar uma justificativa e uma pergunta de follow-up.',
    };
  }
}

export const conversationService: ConversationService = new MockConversationService();
