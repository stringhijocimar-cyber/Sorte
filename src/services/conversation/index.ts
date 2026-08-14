import { aiConfig, normalizeBaseUrl } from '@/config/env';
import { MockConversationService } from '@/services/conversation/mock-service';
import { RemoteConversationService } from '@/services/conversation/remote-service';
import { ConversationService } from '@/services/conversation/types';

export * from '@/services/conversation/types';
export { ConversationServiceError } from '@/services/conversation/remote-service';

const mockService = new MockConversationService();

let activeBaseUrl: string | null = aiConfig.defaultBaseUrl;
let remoteService: RemoteConversationService | null = activeBaseUrl
  ? new RemoteConversationService(activeBaseUrl)
  : null;

/**
 * Aponta o app para um proxy de conversação em tempo de execução.
 * Chamado na abertura (valor persistido) e pela tela de Ajustes.
 */
export function configureConversationService(baseUrl: string | null): void {
  const normalized = normalizeBaseUrl(baseUrl);
  if (normalized === activeBaseUrl) return;
  activeBaseUrl = normalized;
  remoteService = normalized ? new RemoteConversationService(normalized) : null;
}

export function getActiveBaseUrl(): string | null {
  return activeBaseUrl;
}

function active(): ConversationService {
  return remoteService ?? mockService;
}

/**
 * Fachada estável: as telas importam este objeto uma vez e continuam válidas
 * mesmo quando o usuário troca o proxy no meio da execução.
 */
export const conversationService: ConversationService = {
  get kind() {
    return active().kind;
  },
  startScenario: (context) => active().startScenario(context),
  reply: (context, history, learnerText) => active().reply(context, history, learnerText),
  buildFeedback: (context, history, corrections) =>
    active().buildFeedback(context, history, corrections),
};

/** Verifica se um proxy responde antes de o usuário salvar a URL. */
export async function checkProxyHealth(baseUrl: string): Promise<void> {
  const normalized = normalizeBaseUrl(baseUrl);
  if (!normalized) throw new Error('Informe uma URL.');
  if (!/^https?:\/\//i.test(normalized)) {
    throw new Error('A URL precisa começar com http:// ou https://');
  }

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 10000);
  try {
    const response = await fetch(`${normalized}/health`, { signal: controller.signal });
    if (!response.ok) throw new Error(`O servidor respondeu ${response.status}.`);
    const body = (await response.json()) as { status?: string };
    if (body.status !== 'ok') throw new Error('Resposta inesperada de /health.');
  } catch (error) {
    if ((error as Error)?.name === 'AbortError') {
      throw new Error('O servidor não respondeu a tempo.');
    }
    throw error instanceof Error ? error : new Error('Falha ao contatar o servidor.');
  } finally {
    clearTimeout(timer);
  }
}
