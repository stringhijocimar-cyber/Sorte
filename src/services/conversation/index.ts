import { aiConfig } from '@/config/env';
import { MockConversationService } from '@/services/conversation/mock-service';
import { RemoteConversationService } from '@/services/conversation/remote-service';
import { ConversationService } from '@/services/conversation/types';

export * from '@/services/conversation/types';
export { ConversationServiceError } from '@/services/conversation/remote-service';

/**
 * Uma única decisão de configuração define o motor: havendo proxy configurado,
 * a conversa é real; sem ele, o app usa o serviço simulado.
 */
export const conversationService: ConversationService = aiConfig.baseUrl
  ? new RemoteConversationService(aiConfig.baseUrl)
  : new MockConversationService();

export const isUsingRealAi = conversationService.kind === 'remote';
