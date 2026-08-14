import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { computeSkillProgress } from '@/domain/progress';
import {
  CefrLevel,
  LearnerProfile,
  PracticeSession,
  SessionFeedback,
  SkillProgress,
} from '@/domain/types';
import { learningRepository } from '@/services/learning-repository';
import { configureConversationService } from '@/services/conversation';

interface LearningState {
  /** `false` enquanto o estado persistido ainda está sendo carregado. */
  ready: boolean;
  profile: LearnerProfile | null;
  diagnosticLevel: CefrLevel | null;
  sessions: PracticeSession[];
  lastFeedback: SessionFeedback | null;
  /** Proxy de conversação ativo. `null` = tutor simulado. */
  aiBaseUrl: string | null;
  /** Nível efetivo: diagnóstico mais recente, senão o perfil, senão A1. */
  level: CefrLevel;
  skills: SkillProgress[];
  setProfile: (profile: LearnerProfile) => Promise<void>;
  setDiagnosticLevel: (level: CefrLevel) => Promise<void>;
  recordSession: (session: PracticeSession) => Promise<void>;
  setAiBaseUrl: (baseUrl: string | null) => Promise<void>;
  resetProgress: () => Promise<void>;
}

const LearningContext = createContext<LearningState | undefined>(undefined);

export function LearningProvider({ children }: { children: React.ReactNode }) {
  const [ready, setReady] = useState(false);
  const [profile, setProfileState] = useState<LearnerProfile | null>(null);
  const [diagnosticLevel, setDiagnosticLevelState] = useState<CefrLevel | null>(null);
  const [sessions, setSessions] = useState<PracticeSession[]>([]);
  const [lastFeedback, setLastFeedback] = useState<SessionFeedback | null>(null);
  const [aiBaseUrl, setAiBaseUrlState] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    learningRepository
      .load()
      .then((state) => {
        if (!active) return;
        setProfileState(state.profile);
        setDiagnosticLevelState(state.diagnosticLevel);
        setSessions(state.sessions);
        if (state.aiBaseUrl) {
          // Aplica o proxy salvo antes de qualquer tela poder iniciar uma conversa.
          configureConversationService(state.aiBaseUrl);
          setAiBaseUrlState(state.aiBaseUrl);
        }
      })
      .finally(() => {
        if (active) setReady(true);
      });
    return () => {
      active = false;
    };
  }, []);

  const setProfile = useCallback(async (next: LearnerProfile) => {
    setProfileState(next);
    await learningRepository.saveProfile(next);
  }, []);

  const setDiagnosticLevel = useCallback(async (next: CefrLevel) => {
    setDiagnosticLevelState(next);
    await learningRepository.saveDiagnosticLevel(next);
    // O nível estimado passa a valer como nível atual do perfil.
    setProfileState((current) => {
      if (!current) return current;
      const updated = { ...current, currentLevel: next };
      void learningRepository.saveProfile(updated);
      return updated;
    });
  }, []);

  const recordSession = useCallback(async (session: PracticeSession) => {
    setLastFeedback(session.feedback);
    const stored = await learningRepository.appendSession(session);
    setSessions(stored);
  }, []);

  const setAiBaseUrl = useCallback(async (baseUrl: string | null) => {
    configureConversationService(baseUrl);
    setAiBaseUrlState(baseUrl);
    await learningRepository.saveAiBaseUrl(baseUrl);
  }, []);

  const resetProgress = useCallback(async () => {
    await learningRepository.clear();
    setProfileState(null);
    setDiagnosticLevelState(null);
    setSessions([]);
    setLastFeedback(null);
  }, []);

  const level = diagnosticLevel ?? profile?.currentLevel ?? 'A1';
  const skills = useMemo(() => computeSkillProgress(sessions, level), [sessions, level]);

  const value = useMemo(
    () => ({
      ready,
      profile,
      diagnosticLevel,
      sessions,
      lastFeedback,
      aiBaseUrl,
      level,
      skills,
      setProfile,
      setDiagnosticLevel,
      recordSession,
      setAiBaseUrl,
      resetProgress,
    }),
    [
      ready,
      profile,
      diagnosticLevel,
      sessions,
      lastFeedback,
      aiBaseUrl,
      level,
      skills,
      setProfile,
      setDiagnosticLevel,
      recordSession,
      setAiBaseUrl,
      resetProgress,
    ]
  );

  return <LearningContext.Provider value={value}>{children}</LearningContext.Provider>;
}

export function useLearning() {
  const context = useContext(LearningContext);
  if (!context) {
    throw new Error('useLearning must be used inside LearningProvider');
  }
  return context;
}
