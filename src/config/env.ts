/**
 * Configuração de ambiente do cliente.
 *
 * O app nunca guarda chave de IA. Ele fala apenas com o proxy (ver `server/`),
 * que é quem detém a credencial.
 *
 * `EXPO_PUBLIC_AI_BASE_URL` define apenas o valor *padrão*, fixado no momento do
 * build. O usuário pode apontar o app para o próprio proxy em Ajustes, sem
 * recompilar — é isso que torna um APK distribuível utilizável.
 */
export function normalizeBaseUrl(value: string | null | undefined): string | null {
  const trimmed = value?.trim();
  if (!trimmed) return null;
  return trimmed.replace(/\/+$/, '');
}

export const aiConfig = {
  /** Padrão de build. `null` = tutor simulado até o usuário configurar. */
  defaultBaseUrl: normalizeBaseUrl(process.env.EXPO_PUBLIC_AI_BASE_URL),
  /** Tempo máximo por requisição de conversação, em milissegundos. */
  timeoutMs: Number(process.env.EXPO_PUBLIC_AI_TIMEOUT_MS ?? 30000),
} as const;
