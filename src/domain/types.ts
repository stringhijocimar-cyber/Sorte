export type CefrLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';

export type LearningGoal =
  | 'work'
  | 'travel'
  | 'interviews'
  | 'daily-life'
  | 'study';

export type AccentPreference = 'american' | 'british' | 'mixed';

export type SkillName =
  | 'listening'
  | 'vocabulary'
  | 'grammar'
  | 'pronunciation'
  | 'fluency'
  | 'interaction'
  | 'speaking';

export type SkillStage =
  | 'not-started'
  | 'developing'
  | 'practiced'
  | 'consistent'
  | 'mastered';

export interface LearnerProfile {
  name: string;
  goal: LearningGoal;
  dailyMinutes: 5 | 10 | 20 | 40;
  accent: AccentPreference;
  currentLevel: CefrLevel;
  targetLevel: CefrLevel;
}

export interface SkillProgress {
  skill: SkillName;
  score: number;
  stage: SkillStage;
}

export interface Scenario {
  id: string;
  title: string;
  description: string;
  objective: string;
  level: CefrLevel;
  role: string;
  durationMinutes: number;
  tags: string[];
}

export type CorrectionCategory =
  | 'grammar'
  | 'word-choice'
  | 'pronunciation'
  | 'rhythm'
  | 'intonation'
  | 'literal-translation'
  | 'register'
  | 'comprehension';

export interface Correction {
  id: string;
  category: CorrectionCategory;
  original: string;
  natural: string;
  explanation: string;
  alternative: string;
}

export interface SessionFeedback {
  wins: string[];
  corrections: Correction[];
  usefulVocabulary: string[];
  fluencyNote: string;
  nextPractice: string;
}
