# Validação do PR #28 — correções de revisão

Branch: feat/whatsapp-sqlite-integration. Mesmo PR, sem merge automático.

## Execuções após as correções

| Verificação | Resultado |
|---|---|
| Backend: .venv/Scripts/python.exe -m pytest -q | **48 passaram**, um aviso de depreciação TestClient/HTTPX |
| Android: app:testDebugUnitTest | **12 passaram**, zero ignorados e zero falhas |
| Android: app:assembleDebug | **Sucesso** |
| Android: app:assembleDebugAndroidTest | **Sucesso**, incluindo SyncJournalTest e LembretesNavigationTest |
| adb devices -l | emulator-5554 listado como **offline** |
| Android: app:connectedDebugAndroidTest | Não executado: sem dispositivo online |
| Envio real Meta | Não executado; nenhuma credencial real utilizada |

Os três comandos Android foram executados por Gradle no Android Studio, após sync das dependências de teste.

## Correções e novos cenários

O backend conserva os testes anteriores e acrescenta 18 casos parametrizados:
agendamento ativo/cancelado/concluído e procedimento sem agendamento; vencimento e procedimento preservados após cancelamento; uma mensagem por ocorrência; template clínico sem botões; ausência de template bloqueando envio; cancelamento durante a revalidação; três intenções tardias sobre atendimento cancelado; resposta a aviso exclusivamente clínico; desativação sem pets; recusa atômica com pets e auditoria; transferência prévia; isolamento entre clínicas; três fluxos completos com PROVIDER=mock.

A expectativa antiga de desativar tutor com pet foi corrigida para respeitar RN-003. O teste de tutor inativo continua verificando a recusa de novos vínculos.
Os testes anteriores de recorrência inválida, falhas transitórias, backoff, revogação, desconhecidos, assinatura, concorrência e idempotência continuam passando.

O teste completo simulado cadastra tutor autorizado e pet, atendimento concluído e procedimento recorrente, calcula a próxima data, executa o worker com MockProvider real, registra a saída simulated, recebe webhook assinado fictício e verifica attendance/status.
Confirmação mantém AGENDADO; cancelamento mantém o registro e muda para CANCELADO; remarcação cria pendência sem mudar data.
Repetição do webhook e do ciclo não duplica operações.

HistoricoViewModelTest foi atualizado sem alterar o ViewModel, Room, repositórios ou modelos de produção. Preserva os quatro objetivos originais (pet, conteúdo clínico, vínculos e ordem) e acrescenta seleção, novas emissões/horário/ano e estado vazio.
Somente DAOs e criação do banco são simulados; corrotinas são executadas por dispatcher de teste.
Foram necessárias duas dependências exclusivas de testes: MockK e kotlinx-coroutines-test.

A navegação original foi restaurada e recebeu aviso demonstrativo. Configuração WhatsApp permanece no perfil. O teste instrumentado desse percurso foi compilado; a navegação em execução ainda precisa ser verificada com dispositivo online.

## Preservação e limites

Room continua versão 3: entidades, DAOs, migrações, banco e estrutura local não foram alterados.
O arquivo pessoal .idea/deploymentTargetSelector.xml permanece fora dos commits.
Não houve envios reais, exclusão de registros ou acesso ao Room pelos testes do backend.
Bases SQLite Python são isoladas em memória; concorrência usa arquivo temporário do pytest.

Envio real requer aprovação dos dois templates (confirmação e vencimento sem botões), credenciais no servidor e webhook HTTPS.
Sincronização manual, cadastro de recorrência por API e atendimento humano por endpoints autenticados continuam com as limitações descritas em whatsapp-sqlite.md.
A inicialização HTTP/persistência/fixture local foi validada na entrega inicial; essa verificação histórica não substitui os resultados atuais acima.
