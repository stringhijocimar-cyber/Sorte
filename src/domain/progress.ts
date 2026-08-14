import {
  CefrLevel,
  Correction,
  CorrectionCategory,
  PracticeSession,
  SkillName,
  SkillProgress,
  SkillStage,
} from '@/domain/types';

export const skillOrder: SkillName[] = [
  'speaking',
  'fluency',
  'interaction',
  'grammar',
  'vocabulary',
  'pronunciation',
  'listening',
];

export const skillLabels: Record<SkillName, string> = {
  listening: 'Compreensão oral',
  vocabulary: 'Vocabulário',
  grammar: 'Gramática',
  pronunciation: 'Pronúncia',
  fluency: 'Fluência',
  interaction: 'Interação',
  speaking: 'Produção oral',
};

/** Cada categoria de erro alimenta a competência correspondente. */
const categoryToSkill: Record<CorrectionCategory, SkillName> = {
  grammar: 'grammar',
  'word-choice': 'vocabulary',
  'literal-translation': 'vocabulary',
  pronunciation: 'pronunciation',
  rhythm: 'pronunciation',
  intonation: 'pronunciation',
  register: 'interaction',
  comprehension: 'listening',
};

const levelBaseline: Record<CefrLevel, number> = {
  A1: 10,
  A2: 25,
  B1: 45,
  B2: 65,
  C1: 80,
  C2: 92,
};

export function skillForCorrection(correction: Correction): SkillName {
  return categoryToSkill[correction.category];
}

function stageForScore(score: number, practiced: boolean): SkillStage {
  if (!practiced) return 'not-started';
  if (score >= 85) return 'mastered';
  if (score >= 70) return 'consistent';
  if (score >= 50) return 'practiced';
  return 'developing';
}

/**
 * Combina o nível estimado com o histórico real de sessões.
 * A base vem do CEFR, a prática soma e os erros recorrentes descontam.
 */
export function computeSkillProgress(sessions: PracticeSession[], level: CefrLevel): SkillProgress[] {
  const baseline = levelBaseline[level];
  const errorsBySkill = new Map<SkillName, number>();

  for (const session of sessions) {
    for (const correction of session.corrections) {
      const skill = skillForCorrection(correction);
      errorsBySkill.set(skill, (errorsBySkill.get(skill) ?? 0) + 1);
    }
  }

  const totalTurns = sessions.reduce((total, session) => total + session.learnerTurns, 0);
  const practiceCredit = Math.min(totalTurns * 2, 25);

  return skillOrder.map((skill) => {
    const errors = errorsBySkill.get(skill) ?? 0;
    const raw = baseline + practiceCredit - errors * 4;
    const score = sessions.length === 0 ? 0 : Math.max(0, Math.min(100, Math.round(raw)));
    return { skill, score, stage: stageForScore(score, sessions.length > 0) };
  });
}

/** Categorias mais frequentes: base da revisão espaçada e da próxima recomendação. */
export function rankWeakestCategories(sessions: PracticeSession[], limit = 3) {
  const counts = new Map<CorrectionCategory, number>();
  for (const session of sessions) {
    for (const correction of session.corrections) {
      counts.set(correction.category, (counts.get(correction.category) ?? 0) + 1);
    }
  }
  return [...counts.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, limit)
    .map(([category, count]) => ({ category, count }));
}
