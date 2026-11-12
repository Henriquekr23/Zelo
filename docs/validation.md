# Validação da integração WhatsApp/SQLite

Branch: feat/whatsapp-sqlite-integration.

| Verificação | Resultado |
|---|---|
| Fase 1: testes selecionados de recorrência, validação, persistência e autorização | 9 passaram |
| Backend completo, após reforços de concorrência, replay, consentimento e respostas | 30 passaram |
| SQLite concorrente em arquivo temporário com dois workers | Passou; uma única solicitação de envio |
| Fixture mock + scheduler --once em data/pilot-smoke.db separado | Executaram com sucesso, sem acesso à Meta |
| Banco de servidor persistente: python -m zelo.bootstrap | Executou com sucesso; envios desativados |
| FastAPI/Uvicorn em 127.0.0.1:8765 | Inicializou; /health retornou status ok / provider disabled |
| GET /v1/records sem credencial | HTTP 401 |
| Android app:assembleDebug | Sucesso |
| Android app:assembleDebugAndroidTest | Sucesso |
| Android app:testDebugUnitTest | Bloqueado por erros preexistentes em HistoricoViewModelTest.kt |
| Testes instrumentados de SyncJournal | Criados e compilados; não executados por ausência de dispositivo/emulador |
| Envio real Meta | Não executado; sem credenciais e template aprovado |
| Sincronização Android/backend em dispositivo real | Pendente; requer dispositivo e endpoint HTTPS confiável |

A compilação unitária existente falha porque HistoricoViewModelTest ainda instancia HistoricoViewModel sem Application, acessa petAtivo como não nulo e espera tipo em Agendamento.
Esse arquivo e o ViewModel correspondente não foram alterados por esta implementação.
Não foram desabilitados testes para ocultar esse erro.

Os testes do backend usam SQLite em memória isolado; o teste de concorrência usa um arquivo no diretório temporário do pytest. Não acessam Room nem os arquivos persistentes de servidor do piloto.
Todas as chamadas Meta nos testes usam fake ou mock de HTTP, sem mensagem real ou cobrança.

Existe um aviso de depreciação TestClient/HTTPX nas versões atuais de Starlette; não houve falha de teste associada.
requirements.lock registra as versões instaladas na validação.

Room permanece versão 3. Não há mudanças nas entidades, DAOs, banco ou migrações Android.
A alteração local preexistente .idea/deploymentTargetSelector.xml foi preservada e excluída dos commits da entrega.

O servidor temporário de validação HTTP foi encerrado após o teste.
