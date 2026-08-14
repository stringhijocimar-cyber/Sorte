# Backlog do MVP — ordem de execução

## P0 — fluxo essencial

- [x] Estrutura mobile multiplataforma.
- [x] Onboarding básico.
- [x] Diagnóstico simplificado e nível CEFR estimado.
- [x] Home e seleção de cenário.
- [x] Sessão conversacional desacoplada via serviço.
- [x] Feedback pós-aula.
- [x] Persistir perfil, resultado do diagnóstico e sessões.
- [x] Integrar conversação real por IA.
- [x] Reprodução de voz do tutor (TTS).
- [~] Entrada de voz do aluno (STT): disponível onde a plataforma oferece
      reconhecimento; falta módulo nativo em iOS/Android.
- [x] Registrar erros relevantes durante a sessão sem interromper excessivamente.

## P1 — aprendizagem adaptativa

- [ ] Diagnóstico com listening, reading, vocabulary, grammar, pronunciation, fluency, interaction e speaking.
- [x] Matriz de competências com estados de domínio.
- [ ] Biblioteca de frases pessoais.
- [ ] Revisão espaçada por dificuldade, acerto e tempo de resposta.
- [ ] Rotina adaptativa de 5, 10, 20 e 40 minutos.
- [x] Histórico de sessões.
- [x] Painel de progresso por habilidade.

## P2 — diferenciação

- [ ] Missões de “vida real” avaliadas pela conclusão da tarefa.
- [ ] Feedback de pronúncia: inteligibilidade, ritmo, word stress, linking e entonação.
- [ ] Sotaques e registros variados.
- [ ] Conteúdo profissional avançado.
- [ ] Testes de qualidade e experimentação pedagógica.

## Pré-requisitos antes de expor o proxy publicamente

- [ ] Autenticação por usuário e limite de uso por conta no `server/`.
- [ ] Restringir CORS às origens conhecidas (`SPEAKFLOW_ALLOWED_ORIGIN`).
- [ ] Observabilidade: latência, custo por sessão e taxa de recusa.
