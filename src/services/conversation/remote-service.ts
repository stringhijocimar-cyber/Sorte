import { aiConfig } from '@/config/env';
import { Correction, SessionFeedback } from '@/domain/types';
import {
  ConversationContext,
  ConversationService,
  ConversationTurn,
  TutorReply,
  tutorTurn,
} from '@/services/conversation/types';

export class ConversationServiceError extends Error {
  constructor(message: string, readonly cause?: unknown) {
    super(message);
    this.name = 'ConversationServiceError';
  }
}

interface StartResponse {
  text: string;
}

interface TurnResponse {
  reply: string;
  correction: Correction | null;
}

/**
 * Cliente do proxy de conversação (ver `server/`).
 *
 * O app não conhece provedor de IA nem credencial: só este contrato HTTP.
 * Trocar o motor por trás do proxy não exige mudança no aplicativo.
 */
export class RemoteConversationService implements ConversationService {
  readonly kind = 'remote' as const;

  constructor(private readonly baseUrl: string, private readonly timeoutMs = aiConfig.timeoutMs) {}

  private async post<T>(path: string, body: unknown): Promise<T> {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), this.timeoutMs);

    try {
      const response = await fetch(`${this.baseUrl}${path}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
        signal: controller.signal,
      });

      if (!response.ok) {
        const detail = await response.text().catch(() => '');
        throw new ConversationServiceError(
          `Serviço de conversação respondeu ${response.status}. ${detail}`.trim()
        );
      }

      return (await response.json()) as T;
    } catch (error) {
      if (error instanceof ConversationServiceError) throw error;
      if ((error as Error)?.name === 'AbortError') {
        throw new ConversationServiceError('O tutor demorou demais para responder.', error);
      }
      throw new ConversationServiceError('Não foi possível falar com o tutor.', error);
    } finally {
      clearTimeout(timer);
    }
  }

  async startScenario(context: ConversationContext): Promise<ConversationTurn> {
    const data = await this.post<StartResponse>('/v1/conversation/start', { context });
    return tutorTurn(data.text);
  }

  async reply(
    context: ConversationContext,
    history: ConversationTurn[],
    learnerText: string
  ): Promise<TutorReply> {
    const data = await this.post<TurnResponse>('/v1/conversation/turn', {
      context,
      history,
      learnerText,
    });
    return { turn: tutorTurn(data.reply), correction: data.correction ?? null };
  }

  async buildFeedback(
    context: ConversationContext,
    history: ConversationTurn[],
    corrections: Correction[]
  ): Promise<SessionFeedback> {
    return this.post<SessionFeedback>('/v1/conversation/feedback', {
      context,
      history,
      corrections,
    });
  }
}
