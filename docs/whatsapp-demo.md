# Modo Demonstração do WhatsApp — Zelo

## Objetivo e acesso

Demonstração educativa interativa para apresentação acadêmica, executada inteiramente no Android.
Não requer internet, backend, número real, credenciais ou conta Meta.

Abra **Perfil → Configurar lembretes pelo WhatsApp → Experimentar demonstração**.
O card funciona mesmo sem preencher qualquer campo de configuração.
A tela exclusiva mostra **MODO DEMONSTRAÇÃO** e **Simulação educativa. Nenhuma mensagem real será enviada.**
Voltar retorna à configuração; as telas reais e a tela original de lembretes permanecem disponíveis.

## Utilização na interface

A tela tem o seletor de cenário, o card do procedimento fictício, acompanhamento imediato da agenda e quatro abas:

- **Conversa:** clínica fictícia, balões do tutor e da clínica, horários simulados e respostas interativas.
- **Agenda:** estado clínico, confirmação do tutor, data prevista e última interação.
- **Recepção:** pendências abertas, mensagem original e resolução com observação fictícia.
- **Histórico:** ações efetivamente executadas, em ordem e com horário lógico.

Use **Detalhes** para mostrar tutor, atendimento anterior, intervalo, vencimento, antecedência e consentimento.
**Próximo passo** conduz o fluxo normal: geração → envio simulado → confirmação → agenda.
Nos cenários de falha, avança o relógio até a próxima tentativa; quando há pendência, abre a recepção.
**Reiniciar demonstração** restaura somente os dados fictícios do cenário atual.
**Selecionar cenário** começa outra sessão fictícia independente. Não existe limite de repetições.

## Cenários disponíveis

| Cenário | Ação e resultado |
|---|---|
| Atendimento normal | Simular envio automático; Confirmar mantém AGENDADO e marca comparecimento Confirmado |
| Tutor sem consentimento | Envio bloqueado, sem mensagem ou tentativa ao provedor |
| Procedimento sem recorrência | Nenhum lembrete automático gerado |
| Agendamento cancelado | Vencimento preservado; aviso clínico sem botões de comparecimento |
| Falha no envio | Primeira tentativa falha; Nova tentativa com sucesso avança o relógio; Simular nova falha permite atingir três falhas e criar pendência |
| Revogação por PARAR | Após o aviso, responder PARAR revoga consentimento; Verificar novo envio comprova o bloqueio |
| Tutor inativo | Envio bloqueado porque o pet precisa de responsável ativo |

Em um cenário com agendamento ativo:
**Remarcar** cria pendência, sem modificar a data; **Cancelar** altera o estado para CANCELADO, preservando ocorrência e histórico.
**Enviar outra mensagem** permite editar o texto fictício; a mensagem original é mantida e encaminhada ao atendimento humano.
Na recepção, **Resolver pendência** abre o campo de observação e **Registrar resolução** registra a intervenção sem mudar a agenda automaticamente.

## Roteiro de aproximadamente cinco minutos

| Tempo | Apresentação observando apenas o aplicativo |
|---|---|
| 0:00–0:40 | Entre pelo perfil, toque Experimentar demonstração e aponte a indicação de simulação. Abra Detalhes e mostre Carlos, Thor, a última dose, 30 dias de recorrência e vencimento em sete dias da referência. |
| 0:40–1:20 | Atendimento normal: Simular envio automático. Mostre o balão e confirme. Abra Agenda ou use Próximo passo: AGENDADO permanece e o comparecimento fica Confirmado. |
| 1:20–2:00 | Reinicie, gere o lembrete e escolha Remarcar. Use Próximo passo para abrir a recepção. Mostre a pendência, resolva com observação e destaque que a data não mudou. |
| 2:00–2:35 | Reinicie, envie e escolha Cancelar. Abra Agenda e mostre CANCELADO. Selecione Agendamento cancelado e envie: o aviso clínico continua, sem confirmar consulta cancelada. |
| 2:35–3:15 | Volte ao cenário normal, envie e selecione Enviar outra mensagem. Use o exemplo, envie e abra a recepção: mensagem original e necessidade de intervenção humana ficam visíveis. |
| 3:15–4:10 | Escolha Falha no envio. Execute a primeira tentativa e destaque o horário programado. Avance com Nova tentativa com sucesso. Se desejar, reinicie e repita Simular nova falha para mostrar falha persistente. |
| 4:10–4:45 | Mostre Tutor sem consentimento ou Procedimento sem recorrência bloqueando o envio. No cenário Revogação por PARAR, avance duas vezes e verifique que um novo envio está bloqueado. |
| 4:45–5:00 | Abra Histórico, mostrando operações reais da simulação em ordem. Reinicie para comprovar que a sessão pode ser repetida e que os dados são exclusivamente fictícios. |

## Arquitetura e isolamento

- `demo/whatsapp/DemoModels.kt`: fixtures, cenários, relógio, mensagens, eventos e pendências imutáveis.
- `demo/whatsapp/DemoEngine.kt`: máquina de estados pura e comandos explícitos de envio, resposta, tentativa e resolução.
- `WhatsAppDemoViewModel`: sessão em memória, seleção de cenário, reinício, abas e passos guiados.
- `WhatsAppDemoActivity` e `WhatsAppDemoScreen`: tela própria e componentes Material 3, com animações de apresentação.
- O estado clínico reutiliza StatusAgendamento; comparecimento tem estado separado, compatível com pending/confirmed/reschedule_requested/cancelled do backend.

O motor e o ViewModel não recebem Context, Application, DAOs, repositórios, tokens ou clientes HTTP.
A Activity recebe somente o identificador do tema visual via Intent, sem ler nem gravar preferências.
O modo não abre Room, SQLite do servidor, journal da sincronização ou serviços de mensagens.
A tela anterior de configuração pode ler seu estado local normalmente, mas o botão da demonstração não salva nem sincroniza dados.
Nenhuma dependência nova foi adicionada.

A sessão sobrevive à rotação pelo ViewModel, mas não é persistida. Fechar a Activity descarta o estado; reabrir inicia outra demonstração.
O dia de referência é obtido uma única vez ao abrir a sessão; datas são calculadas relativamente a ele.
Depois disso, horários avançam por ações, nunca pela espera de um serviço ou pelo relógio de parede.
A tentativa simulada tem no máximo três execuções, com atraso lógico exponencial a partir de dois minutos, para permitir uma apresentação curta.

Os comandos validam contexto da mensagem, estado atual e sequência da entrada. Replays de envio, respostas, retentativas e resolução não duplicam operações.
Um evento só aparece quando a operação correspondente ocorre. Falhas sem envio não criam mensagens falsamente entregues.

## Relação com o PR #28 e requisitos

| Requisito | Comportamento apresentado |
|---|---|
| RF-008 | Recorrência válida, vencimento calculado, antecedência, tutor/telefone/consentimento validados, geração e envio simulado |
| RF-009 | Botões tipados; confirmação sem conclusão clínica, cancelamento preservando registro e remarcação sem mudança automática de data |
| RF-010 | Texto desconhecido preservado, pendência de atendimento e resolução auditada na sessão |
| RN-003 | Responsável ativo obrigatório; cenário de tutor inativo demonstra bloqueio de entrada inelegível, não autoriza desativar tutor real |
| RN-004 | Regra inválida não gera lembrete |
| RNF-003 | Retentativa lógica, limite de falhas, histórico e idempotência demonstrados, sem afirmar um SLA real |

O motor reproduz os contratos e resultados observáveis do backend corrigido, mas não executa Python.
Avisos de atendimentos cancelados continuam clínicos e não possuem ações de comparecimento.
Texto livre não é interpretado como confirmação; PARAR/SAIR/STOP revogam consentimento.
As confirmações e respostas recebidas são simuladas localmente: não são webhooks assinados ou mensagens aceitas/entregues pela Meta.
Mensagens de confirmação visual da clínica também pertencem somente à simulação educativa.

## Testes e validação

```powershell
./gradlew.bat app:assembleDebug
./gradlew.bat app:testDebugUnitTest
./gradlew.bat app:assembleDebugAndroidTest
# Somente com dispositivo online:
./gradlew.bat app:connectedDebugAndroidTest
```

Os três primeiros comandos passaram na validação. Foram executados **38 testes unitários**, sem testes ignorados ou falhas: 26 novos e 12 existentes.
Os novos testes cobrem elegibilidade, limites de recorrência e janela, confirmação, cancelamento, remarcação, mensagem original, resolução, PARAR, retentativa, falha persistente, replays, ordem temporal, reinício, sessões independentes, passos guiados e isolamento do Room.
O teste de isolamento faz a abertura de ZeloDatabase falhar explicitamente e verifica zero chamadas durante os cenários do ViewModel.
`WhatsAppDemoTest` contém três testes instrumentados de entrada/retorno, conversa/agenda/recepção/reinício e cenários alternativos.
O teste de navegação existente foi adaptado para localizar controles abaixo da dobra após o novo card, sem remover verificações.

O emulador emulator-5554 permaneceu **offline**; instrumentados foram compilados, mas **não executados**.
A tentativa de renderização de preview pelo MCP retornou Missing ComposeViewAdapter; não é uma evidência de validação visual. O Composable de preview está disponível para um ambiente IDE com tooling apropriado.
Assim, a interação visual em dispositivo continua pendente dessa verificação. Nenhum envio real foi realizado.

## Limitações

Este modo não testa conectividade, credenciais, disponibilidade de horários reais, políticas ou entrega da Meta.
Não altera cadastros existentes, não persiste conversas e não substitui o backend de produção.
O roteiro é determinístico dentro de cada sessão, e as datas acompanham o dia de abertura.
A configuração real do WhatsApp e seus requisitos de HTTPS, templates e autorização permanecem no PR #28.
