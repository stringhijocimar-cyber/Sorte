import { createServer, type IncomingMessage, type ServerResponse } from 'node:http';
import {
  buildFeedback,
  nextTurn,
  startScenario,
  TutorRefusalError,
  type ConversationContext,
  type ConversationTurn,
  type Correction,
} from './tutor.ts';

const PORT = Number(process.env.PORT ?? 8787);
const MAX_BODY_BYTES = 512 * 1024;

if (!process.env.ANTHROPIC_API_KEY) {
  console.error('ANTHROPIC_API_KEY não definida. Veja server/README.md.');
  process.exit(1);
}

function sendJson(res: ServerResponse, status: number, payload: unknown) {
  const body = JSON.stringify(payload);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(body),
    'Access-Control-Allow-Origin': process.env.SPEAKFLOW_ALLOWED_ORIGIN ?? '*',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Allow-Methods': 'POST, OPTIONS',
  });
  res.end(body);
}

async function readBody(req: IncomingMessage): Promise<unknown> {
  const chunks: Buffer[] = [];
  let size = 0;

  for await (const chunk of req) {
    size += chunk.length;
    if (size > MAX_BODY_BYTES) throw new Error('Corpo da requisição excede o limite.');
    chunks.push(chunk as Buffer);
  }

  const raw = Buffer.concat(chunks).toString('utf8');
  if (!raw) return {};
  return JSON.parse(raw);
}

interface TurnBody {
  context?: ConversationContext;
  history?: ConversationTurn[];
  learnerText?: string;
  corrections?: Correction[];
}

function requireContext(body: TurnBody): ConversationContext {
  if (!body.context?.scenario?.id) {
    throw Object.assign(new Error('Campo "context.scenario" ausente ou inválido.'), { status: 400 });
  }
  return body.context;
}

const routes: Record<string, (body: TurnBody) => Promise<unknown>> = {
  '/v1/conversation/start': async (body) => startScenario(requireContext(body)),

  '/v1/conversation/turn': async (body) => {
    const learnerText = body.learnerText?.trim();
    if (!learnerText) {
      throw Object.assign(new Error('Campo "learnerText" é obrigatório.'), { status: 400 });
    }
    return nextTurn(requireContext(body), body.history ?? [], learnerText);
  },

  '/v1/conversation/feedback': async (body) =>
    buildFeedback(requireContext(body), body.history ?? [], body.corrections ?? []),
};

const server = createServer((req, res) => {
  void (async () => {
    const path = (req.url ?? '').split('?')[0];

    if (req.method === 'OPTIONS') {
      sendJson(res, 204, {});
      return;
    }

    if (req.method === 'GET' && path === '/health') {
      sendJson(res, 200, { status: 'ok' });
      return;
    }

    const handler = req.method === 'POST' ? routes[path] : undefined;
    if (!handler) {
      sendJson(res, 404, { error: 'Rota não encontrada.' });
      return;
    }

    try {
      sendJson(res, 200, await handler((await readBody(req)) as TurnBody));
    } catch (error) {
      // O detalhe completo fica sempre no log do servidor.
      console.error(`[${path}]`, error);

      if (error instanceof TutorRefusalError) {
        sendJson(res, 422, { error: error.message, category: error.category });
        return;
      }

      // Só mensagens de validação nossas voltam ao cliente. Erros do provedor
      // podem conter detalhes de infraestrutura e credencial, então viram 502.
      const status = (error as { status?: number }).status;
      if (status === 400) {
        sendJson(res, 400, { error: (error as Error).message });
        return;
      }

      sendJson(res, 502, { error: 'O tutor está indisponível no momento. Tente novamente.' });
    }
  })();
});

server.listen(PORT, () => {
  console.log(`SpeakFlow AI proxy ouvindo em http://localhost:${PORT}`);
});
