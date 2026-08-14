/**
 * Configuração de ambiente do cliente.
 *
 * O app nunca guarda chave de IA. Ele fala apenas com o proxy definido em
 * `EXPO_PUBLIC_AI_BASE_URL` (ver `server/`), que é quem detém a credencial.
 * Sem essa variável, o app roda com o serviço de conversação simulado.
 */
const rawBaseUrl = process.env.EXPO_PUBLIC_AI_BASE_URL?.trim();

export const aiConfig = {
  /** URL do proxy de conversação, sem barra final. `null` = modo simulado. */
  baseUrl: rawBaseUrl ? rawBaseUrl.replace(/\/+$/, '') : null,
  /** Tempo máximo por requisição de conversação, em milissegundos. */
  timeoutMs: Number(process.env.EXPO_PUBLIC_AI_TIMEOUT_MS ?? 30000),
} as const;

export const isRemoteAiEnabled = aiConfig.baseUrl !== null;
