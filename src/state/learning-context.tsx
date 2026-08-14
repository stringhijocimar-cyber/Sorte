import React, { createContext, useContext, useMemo, useState } from 'react';
import { CefrLevel, LearnerProfile, SessionFeedback } from '@/domain/types';

interface LearningState {
  profile: LearnerProfile | null;
  setProfile: (profile: LearnerProfile) => void;
  diagnosticLevel: CefrLevel | null;
  setDiagnosticLevel: (level: CefrLevel) => void;
  lastFeedback: SessionFeedback | null;
  setLastFeedback: (feedback: SessionFeedback) => void;
}

const LearningContext = createContext<LearningState | undefined>(undefined);

export function LearningProvider({ children }: { children: React.ReactNode }) {
  const [profile, setProfile] = useState<LearnerProfile | null>(null);
  const [diagnosticLevel, setDiagnosticLevel] = useState<CefrLevel | null>(null);
  const [lastFeedback, setLastFeedback] = useState<SessionFeedback | null>(null);

  const value = useMemo(
    () => ({
      profile,
      setProfile,
      diagnosticLevel,
      setDiagnosticLevel,
      lastFeedback,
      setLastFeedback,
    }),
    [profile, diagnosticLevel, lastFeedback]
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
