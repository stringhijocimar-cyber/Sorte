import React from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, View, ViewStyle } from 'react-native';

export const palette = {
  bg: '#07111F',
  surface: '#0E1D2F',
  surface2: '#132940',
  text: '#F5F7FA',
  muted: '#9EB0C5',
  accent: '#4FD1C5',
  accentText: '#042725',
  line: '#1C3550',
  danger: '#F98080',
};

export function Screen({ children }: { children: React.ReactNode }) {
  return <View style={styles.screen}>{children}</View>;
}

export function Card({ children, style }: { children: React.ReactNode; style?: ViewStyle }) {
  return <View style={[styles.card, style]}>{children}</View>;
}

export function PrimaryButton({ title, onPress, disabled }: { title: string; onPress: () => void; disabled?: boolean }) {
  return (
    <Pressable onPress={onPress} disabled={disabled} style={({ pressed }) => [styles.primaryButton, pressed && !disabled && { opacity: 0.85 }, disabled && { opacity: 0.45 }]}>
      <Text style={styles.primaryButtonText}>{title}</Text>
    </Pressable>
  );
}

export function Choice({ title, selected, onPress }: { title: string; selected: boolean; onPress: () => void }) {
  return (
    <Pressable onPress={onPress} style={[styles.choice, selected && styles.choiceSelected]}>
      <Text style={[styles.choiceText, selected && styles.choiceTextSelected]}>{title}</Text>
    </Pressable>
  );
}

export function SecondaryButton({
  title,
  onPress,
  disabled,
  tone = 'neutral',
}: {
  title: string;
  onPress: () => void;
  disabled?: boolean;
  tone?: 'neutral' | 'active';
}) {
  return (
    <Pressable
      onPress={onPress}
      disabled={disabled}
      style={({ pressed }) => [
        styles.secondaryButton,
        tone === 'active' && styles.secondaryButtonActive,
        pressed && !disabled && { opacity: 0.85 },
        disabled && { opacity: 0.45 },
      ]}
    >
      <Text style={[styles.secondaryButtonText, tone === 'active' && { color: palette.accent }]}>{title}</Text>
    </Pressable>
  );
}

export function Tag({ label }: { label: string }) {
  return (
    <View style={styles.tag}>
      <Text style={styles.tagText}>{label}</Text>
    </View>
  );
}

/** Barra de progresso simples para pontuação de competência (0–100). */
export function Meter({ value }: { value: number }) {
  const clamped = Math.max(0, Math.min(100, value));
  return (
    <View style={styles.meterTrack}>
      <View style={[styles.meterFill, { width: `${clamped}%` }]} />
    </View>
  );
}

export function Loading({ label }: { label: string }) {
  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center', gap: 12 }}>
      <ActivityIndicator color={palette.accent} />
      <Text style={textStyles.small}>{label}</Text>
    </View>
  );
}

export function Notice({ message, tone = 'info' }: { message: string; tone?: 'info' | 'error' }) {
  return (
    <View style={[styles.notice, tone === 'error' && { borderColor: palette.danger }]}>
      <Text style={[textStyles.small, tone === 'error' && { color: palette.danger }]}>{message}</Text>
    </View>
  );
}

export const textStyles = StyleSheet.create({
  eyebrow: { color: palette.accent, fontSize: 13, fontWeight: '700', textTransform: 'uppercase', letterSpacing: 1 },
  h1: { color: palette.text, fontSize: 34, lineHeight: 40, fontWeight: '800' },
  h2: { color: palette.text, fontSize: 22, lineHeight: 28, fontWeight: '700' },
  body: { color: palette.muted, fontSize: 16, lineHeight: 23 },
  strong: { color: palette.text, fontSize: 16, lineHeight: 23, fontWeight: '700' },
  small: { color: palette.muted, fontSize: 13, lineHeight: 18 },
});

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: palette.bg, padding: 20, gap: 16 },
  card: { backgroundColor: palette.surface, borderWidth: 1, borderColor: palette.line, borderRadius: 18, padding: 18, gap: 10 },
  primaryButton: { backgroundColor: palette.accent, minHeight: 52, paddingHorizontal: 18, borderRadius: 16, alignItems: 'center', justifyContent: 'center' },
  primaryButtonText: { color: palette.accentText, fontSize: 16, fontWeight: '800' },
  choice: { borderWidth: 1, borderColor: palette.line, borderRadius: 14, paddingVertical: 12, paddingHorizontal: 14, backgroundColor: palette.surface },
  choiceSelected: { borderColor: palette.accent, backgroundColor: palette.surface2 },
  choiceText: { color: palette.muted, fontSize: 15, fontWeight: '600' },
  choiceTextSelected: { color: palette.text },
  secondaryButton: {
    borderWidth: 1,
    borderColor: palette.line,
    backgroundColor: palette.surface,
    minHeight: 46,
    paddingHorizontal: 16,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  secondaryButtonActive: { borderColor: palette.accent, backgroundColor: palette.surface2 },
  secondaryButtonText: { color: palette.text, fontSize: 15, fontWeight: '700' },
  tag: {
    borderWidth: 1,
    borderColor: palette.line,
    backgroundColor: palette.surface2,
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 4,
  },
  tagText: { color: palette.muted, fontSize: 12, fontWeight: '600' },
  meterTrack: { height: 8, borderRadius: 999, backgroundColor: palette.surface2, overflow: 'hidden' },
  meterFill: { height: 8, borderRadius: 999, backgroundColor: palette.accent },
  notice: {
    borderWidth: 1,
    borderColor: palette.line,
    backgroundColor: palette.surface2,
    borderRadius: 12,
    padding: 12,
  },
});
