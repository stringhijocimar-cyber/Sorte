import { CefrLevel, Correction, LearningGoal, Scenario, SessionFeedback } from '@/domain/types';

export interface ConversationTurn {
  id: string;
  speaker: 'learner' | 'tutor';
  text: string;
}

/** Contexto do aluno enviado junto de cada pedido, para calibrar dificuldade. */
export interface ConversationContext {
  scenario: Scenario;
  level: CefrLevel;
  goal: LearningGoal;
  learnerName: string;
}

/**
 * Resposta do tutor. A correção vem anexada ao turno e é registrada em segundo
 * plano: a conversa não é interrompida para corrigir.
 */
export interface TutorReply {
  turn: ConversationTurn;
  correction: Correction | null;
}

export interface ConversationService {
  readonly kind: 'mock' | 'remote';
  startScenario(context: ConversationContext): Promise<ConversationTurn>;
  reply(
    context: ConversationContext,
    history: ConversationTurn[],
    learnerText: string
  ): Promise<TutorReply>;
  buildFeedback(
    context: ConversationContext,
    history: ConversationTurn[],
    corrections: Correction[]
  ): Promise<SessionFeedback>;
}

export function tutorTurn(text: string): ConversationTurn {
  return { id: `tutor-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`, speaker: 'tutor', text };
}

export function learnerTurn(text: string): ConversationTurn {
  return { id: `learner-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`, speaker: 'learner', text };
}
