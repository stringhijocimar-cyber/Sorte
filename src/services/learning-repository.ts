import AsyncStorage from '@react-native-async-storage/async-storage';
import { CefrLevel, LearnerProfile, PersistedLearningState, PracticeSession } from '@/domain/types';

const KEYS = {
  profile: 'speakflow:profile',
  diagnostic: 'speakflow:diagnostic-level',
  sessions: 'speakflow:sessions',
} as const;

/** Limite de sessões mantidas no dispositivo até existir sincronização remota. */
const MAX_SESSIONS = 100;

export interface LearningRepository {
  load(): Promise<PersistedLearningState>;
  saveProfile(profile: LearnerProfile): Promise<void>;
  saveDiagnosticLevel(level: CefrLevel): Promise<void>;
  appendSession(session: PracticeSession): Promise<PracticeSession[]>;
  clear(): Promise<void>;
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
    const [profile, diagnosticLevel, sessions] = await Promise.all([
      readJson<LearnerProfile>(KEYS.profile),
      readJson<CefrLevel>(KEYS.diagnostic),
      readJson<PracticeSession[]>(KEYS.sessions),
    ]);

    return {
      profile,
      diagnosticLevel,
      sessions: Array.isArray(sessions) ? sessions : [],
    };
  }

  async saveProfile(profile: LearnerProfile): Promise<void> {
    await AsyncStorage.setItem(KEYS.profile, JSON.stringify(profile));
  }

  async saveDiagnosticLevel(level: CefrLevel): Promise<void> {
    await AsyncStorage.setItem(KEYS.diagnostic, JSON.stringify(level));
  }

  async appendSession(session: PracticeSession): Promise<PracticeSession[]> {
    const current = (await readJson<PracticeSession[]>(KEYS.sessions)) ?? [];
    const next = [session, ...current].slice(0, MAX_SESSIONS);
    await AsyncStorage.setItem(KEYS.sessions, JSON.stringify(next));
    return next;
  }

  async clear(): Promise<void> {
    await AsyncStorage.removeMany([KEYS.profile, KEYS.diagnostic, KEYS.sessions]);
  }
}

export const learningRepository: LearningRepository = new AsyncStorageLearningRepository();
