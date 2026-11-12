## Objetivo

Adicionar lembretes automáticos de procedimentos recorrentes, com FastAPI, SQLAlchemy e SQLite exclusivo do servidor, preservando Room v3 e os fluxos Android existentes.

## Requisitos atendidos

- RF-008: recorrência calculada, elegibilidade, scheduler persistente, templates, tentativas e histórico.
- RF-009: confirmação separada da conclusão clínica, cancelamento e solicitação de remarcação.
- RF-010: mensagem original, pendência de recepção, consulta e resolução auditadas.
- RN-002: endpoints autenticados, papéis e escopo da clínica derivado da credencial.
- RN-003/RN-004: tutor ativo autorizado e mesma clínica; somente origem clínica concluída com recorrência válida.
- RNF-003: idempotência, locks SQLite, backoff, falhas persistentes e métricas de janelas perdidas.

## Alterações técnicas

Backend independente em backend/zelo, com SQLite persistente, BEGIN IMMEDIATE, WAL e lock de processo para o scheduler. Provider desacoplado: disabled, mock explícito ou Meta Cloud API. Webhooks HMAC SHA256, deduplicação e correlação por mensagem, remetente e UUID do lembrete.

Android: cliente HTTPS, journal atômico privado para versões e outbox, sincronização manual com resolução explícita de conflitos, configuração de telefone/consentimento e histórico de lembretes. Atualizações locais usam os repositórios existentes. Nenhuma dependência Android nova; nenhum token Meta no APK.

Room, entidades e migrações permanecem intactos. A alteração preexistente em .idea/deploymentTargetSelector.xml não integra este PR.

## Evidências dos testes

- Fase 1: 9 testes selecionados passaram.
- Backend completo: 30 testes passaram, com SQLite isolado e mocks, incluindo concorrência em arquivo temporário.
- Inicialização do servidor persistente e FastAPI local verificadas.
- /health respondeu ok; /v1/records sem credencial respondeu 401.
- app:assembleDebug e app:assembleDebugAndroidTest passaram.
- app:testDebugUnitTest falha por testes antigos de HistoricoViewModelTest, não alterados: construtor sem Application e expectativas do modelo anterior.
- Instrumentados de SyncJournal compilados, sem execução por ausência de dispositivo conectado.
- Nenhum envio real Meta foi realizado.

## Configuração

Consultar docs/whatsapp-sqlite.md e backend/.env.example. Criar ambiente Python, instalar requirements.txt (ou requirements.lock para reproduzir as versões verificadas), configurar credenciais pessoais como hashes e executar zelo.bootstrap, Uvicorn e zelo.worker.

PROVIDER=mock permite validar sem mensagens reais; zelo.pilot fornece registros fictícios somente nesse modo.
Para integração real: token de sistema Meta exclusivamente no servidor, número empresarial, template de quatro parâmetros e três quick replies, Graph version suportada e webhook HTTPS público com assinatura validada.

## Riscos e limitações

Piloto para atendente autorizado, uma clínica e um tutor local por aparelho; não implementa login completo nas telas legadas. Recorrências são cadastradas pela recepção via API. Sincronização manual, sem propagação de exclusões ou recuperação automática de journal após reinstalação.

A recepção opera por endpoints autenticados; console visual e resposta manual pelo sistema ficam pendentes. Disponibilidade é conservadora (um atendimento por horário na clínica); remarcação não muda data automaticamente.

Envio contínuo depende de backend/worker ativos e disco persistente. Timeout ambíguo exige reconciliação humana; não se promete exactly-once na API externa. Revogação offline só chega ao servidor ao sincronizar; PARAR revoga via WhatsApp.

Backup, retenção, alertas, gestão de identidades e teste autenticado com Meta precisam ser concluídos antes de produção.

## Checklist

- [x] SQLite do servidor separado do Room
- [x] Room v3 sem migração ou mudança de esquema
- [x] Credenciais Meta apenas no backend
- [x] Testes sem mensagens reais
- [x] Assinatura, autorização, idempotência e conflitos implementados
- [x] Backend e APK compilável verificados
- [x] Documentação e matriz de rastreabilidade
- [ ] Executar instrumentados em dispositivo
- [ ] Atualizar testes unitários históricos preexistentes
- [ ] Configurar credenciais/template/HTTPS e validar envio real
- [ ] Validação operacional e de privacidade pela clínica

Sem merge automático.
