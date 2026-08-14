import AsyncStorage from '@react-native-async-storage/async-storage';
import { CefrLevel, LearnerProfile, PersistedLearningState, PracticeSession } from '@/domain/types';

const KEYS = {
  profile: 'speakflow:profile',
  diagnostic: 'speakflow:diagnostic-level',
  sessions: 'speakflow:sessions',
  aiBaseUrl: 'speakflow:ai-base-url',
} as const;

/** Limite de sessões mantidas no dispositivo até existir sincronização remota. */
const MAX_SESSIONS = 100;

export interface LearningRepository {
  load(): Promise<PersistedLearningState>;
  saveProfile(profile: LearnerProfile): Promise<void>;
  saveDiagnosticLevel(level: CefrLevel): Promise<void>;
  appendSession(session: PracticeSession): Promise<PracticeSession[]>;
  saveAiBaseUrl(baseUrl: string | null): Promise<void>;
  clear(): Promise<void>;
}

/**
 * Gravação é best-effort: em navegadores com armazenamento bloqueado (janela
 * privada, iframe restrito) o AsyncStorage lança. Perder a persistência é ruim,
 * mas travar a navegação do aluno é pior — o erro é registrado e o fluxo segue.
 */
async function writeJson(key: string, value: unknown): Promise<void> {
  try {
    await AsyncStorage.setItem(key, JSON.stringify(value));
  } catch (error) {
    console.warn(`[speakflow] não foi possível salvar "${key}":`, error);
  }
}

async function readJson<T>(key: string): Promise<T | null> {
  try {
    const raw = await AsyncStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : null;
  } catch {
    // Dado corrompido não pode travar a abertura do app.
    await AsyncStorage.removeItem(key).catch(() => undefined);
    return null;
  }
}

class AsyncStorageLearningRepository implements LearningRepository {
  async load(): Promise<PersistedLearningState> {
    const [profile, diagnosticLevel, sessions, aiBaseUrl] = await Promise.all([
      readJson<LearnerProfile>(KEYS.profile),
      readJson<CefrLevel>(KEYS.diagnostic),
      readJson<PracticeSession[]>(KEYS.sessions),
      readJson<string>(KEYS.aiBaseUrl),
    ]);

    return {
      profile,
      diagnosticLevel,
      sessions: Array.isArray(sessions) ? sessions : [],
      aiBaseUrl: typeof aiBaseUrl === 'string' ? aiBaseUrl : null,
    };
  }

  async saveProfile(profile: LearnerProfile): Promise<void> {
    await writeJson(KEYS.profile, profile);
  }

  async saveDiagnosticLevel(level: CefrLevel): Promise<void> {
    await writeJson(KEYS.diagnostic, level);
  }

  async appendSession(session: PracticeSession): Promise<PracticeSession[]> {
    const current = (await readJson<PracticeSession[]>(KEYS.sessions)) ?? [];
    const next = [session, ...current].slice(0, MAX_SESSIONS);
    await writeJson(KEYS.sessions, next);
    return next;
  }

  async saveAiBaseUrl(baseUrl: string | null): Promise<void> {
    if (baseUrl === null) {
      await AsyncStorage.removeItem(KEYS.aiBaseUrl).catch(() => undefined);
      return;
    }
    await writeJson(KEYS.aiBaseUrl, baseUrl);
  }

  async clear(): Promise<void> {
    // A URL do proxy é configuração do aparelho, não progresso: sobrevive ao reset.
    await AsyncStorage.removeMany([KEYS.profile, KEYS.diagnostic, KEYS.sessions]).catch(
      (error) => console.warn('[speakflow] não foi possível apagar o progresso:', error)
    );
  }
}

export const learningRepository: LearningRepository = new AsyncStorageLearningRepository();
