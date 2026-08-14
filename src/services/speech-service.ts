import { Platform } from 'react-native';
import * as Speech from 'expo-speech';
import { AccentPreference } from '@/domain/types';

export const accentToLocale: Record<AccentPreference, string> = {
  american: 'en-US',
  british: 'en-GB',
  mixed: 'en-US',
};

export interface RecognitionHandlers {
  /** Transcrição parcial enquanto o aluno ainda fala. */
  onPartial?: (text: string) => void;
  /** Transcrição final do trecho falado. */
  onResult: (text: string) => void;
  onError?: (message: string) => void;
  onEnd?: () => void;
}

export interface RecognitionSession {
  stop: () => void;
}

export interface SpeechService {
  speak(text: string, accent: AccentPreference): Promise<void>;
  stopSpeaking(): Promise<void>;
  /** `false` quando a plataforma não expõe reconhecimento; a UI cai para digitação. */
  isRecognitionAvailable(): boolean;
  startRecognition(accent: AccentPreference, handlers: RecognitionHandlers): RecognitionSession;
}

type WebSpeechRecognition = {
  lang: string;
  continuous: boolean;
  interimResults: boolean;
  start: () => void;
  stop: () => void;
  abort: () => void;
  onresult: ((event: any) => void) | null;
  onerror: ((event: any) => void) | null;
  onend: (() => void) | null;
};

function getWebRecognitionConstructor(): (new () => WebSpeechRecognition) | null {
  if (Platform.OS !== 'web' || typeof window === 'undefined') return null;
  const w = window as any;
  return w.SpeechRecognition ?? w.webkitSpeechRecognition ?? null;
}

class PlatformSpeechService implements SpeechService {
  async speak(text: string, accent: AccentPreference): Promise<void> {
    await Speech.stop().catch(() => undefined);
    return new Promise<void>((resolve) => {
      Speech.speak(text, {
        language: accentToLocale[accent],
        rate: 0.95,
        onDone: () => resolve(),
        onStopped: () => resolve(),
        onError: () => resolve(),
      });
    });
  }

  async stopSpeaking(): Promise<void> {
    await Speech.stop().catch(() => undefined);
  }

  isRecognitionAvailable(): boolean {
    return getWebRecognitionConstructor() !== null;
  }

  startRecognition(accent: AccentPreference, handlers: RecognitionHandlers): RecognitionSession {
    const Recognition = getWebRecognitionConstructor();
    if (!Recognition) {
      handlers.onError?.('Reconhecimento de voz indisponível nesta plataforma.');
      handlers.onEnd?.();
      return { stop: () => undefined };
    }

    const recognition = new Recognition();
    recognition.lang = accentToLocale[accent];
    recognition.continuous = false;
    recognition.interimResults = true;

    recognition.onresult = (event: any) => {
      let finalText = '';
      let partialText = '';
      for (let index = event.resultIndex; index < event.results.length; index += 1) {
        const result = event.results[index];
        if (result.isFinal) finalText += result[0].transcript;
        else partialText += result[0].transcript;
      }
      if (partialText) handlers.onPartial?.(partialText.trim());
      if (finalText) handlers.onResult(finalText.trim());
    };

    recognition.onerror = (event: any) => {
      handlers.onError?.(
        event?.error === 'not-allowed'
          ? 'Permissão de microfone negada.'
          : 'Não foi possível capturar sua fala.'
      );
    };

    recognition.onend = () => handlers.onEnd?.();

    recognition.start();
    return { stop: () => recognition.stop() };
  }
}

export const speechService: SpeechService = new PlatformSpeechService();
