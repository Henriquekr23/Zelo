# Modo Demonstração do WhatsApp — Zelo

## Objetivo e acesso

Demonstração educativa interativa para apresentação acadêmica, executada inteiramente no Android.
Não requer internet, backend, número real, credenciais ou conta Meta.

Abra **Perfil → Lembretes pelo WhatsApp → Experimentar demonstração**.
A página introdutória contém somente a explicação e o card de demonstração; não pede configuração.
A tela exclusiva mostra **MODO DEMONSTRAÇÃO** e **Simulação educativa. Nenhuma mensagem real será enviada.**
Voltar retorna à introdução e depois ao perfil. A tela original de lembretes continua acessível.
O formulário técnico do piloto foi preservado em `WhatsAppPilotScreen.kt`, sem rota na apresentação acadêmica. Os serviços e o ViewModel da integração real permanecem no projeto.

## Utilização na interface

A tela tem o seletor de cenário, o card do procedimento fictício, acompanhamento imediato da agenda e quatro abas:

- **Conversa:** Clínica Jardim Veterinária, cabeçalho verde, fundo próprio e balões à esquerda (clínica) e à direita (tutor). Horários e indicadores sempre identificados como simulação; respostas ficam dentro do balão de lembrete.
- **Agenda:** estado clínico, confirmação do tutor, data prevista e última interação.
- **Recepção:** pendências abertas, mensagem original e resolução com observação fictícia.
- **Histórico:** ações efetivamente executadas, em ordem e com horário lógico.

As quatro abas têm a mesma largura e permanecem acessíveis durante a rolagem. Novas mensagens deslocam a conversa até a última interação. Os controles inferiores respeitam a barra de navegação.
A paleta familiar de mensagens é aplicada somente à conversa; cards e navegação seguem o tema do Zelo.
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
| 0:00–0:45 | Perfil → Minha clínica. Mostre a Clínica Jardim Veterinária e o aviso de localização fictícia. Toque Simular trajeto: origem, caminho, distância e duração são ilustrativos. Volte ao perfil. |
| 0:45–1:15 | Lembretes pelo WhatsApp → Experimentar demonstração. Aponte o aviso de simulação, selecione Atendimento normal e abra Detalhes para explicar o vencimento relativo ao dia da sessão. |
| 1:15–1:55 | Próximo passo gera o lembrete. Mostre a conversa da clínica, escolha Confirmar dentro do balão e abra Agenda: AGENDADO permanece, com comparecimento Confirmado. |
| 1:55–2:35 | Reinicie, envie e escolha Remarcar. Abra Recepção, mostre a pendência e registre uma observação. Destaque que a data não mudou automaticamente. |
| 2:35–3:00 | Reinicie, envie e escolha Cancelar. Mostre CANCELADO na Agenda. O procedimento e o histórico são preservados. |
| 3:00–3:35 | No cenário normal, envie e toque Enviar outra mensagem. Envie o exemplo e abra Recepção: a mensagem original permanece disponível para atendimento humano. |
| 3:35–4:10 | Selecione Falha no envio. Execute a tentativa e avance para Nova tentativa com sucesso. O relógio da demonstração avança sem esperar tempo real. |
| 4:10–4:45 | Mostre o bloqueio em Tutor sem consentimento e o aviso sem botões em Agendamento cancelado. Se desejar, use Revogação por PARAR e Verificar novo envio. |
| 4:45–5:00 | Abra Histórico e destaque que cada evento veio de uma ação. Reinicie e retorne ao perfil, pronto para repetir a apresentação. |

## Arquitetura e isolamento

- `demo/whatsapp/DemoModels.kt`: fixtures, cenários, relógio, mensagens, eventos e pendências imutáveis.
- `demo/whatsapp/DemoEngine.kt`: máquina de estados pura e comandos explícitos de envio, resposta, tentativa e resolução.
- `WhatsAppDemoViewModel`: sessão em memória, seleção de cenário, reinício, abas e passos guiados.
- `WhatsAppDemoActivity` e `WhatsAppDemoScreen`: tela própria e componentes Material 3, com animações de apresentação.
- O estado clínico reutiliza StatusAgendamento; comparecimento tem estado separado, compatível com pending/confirmed/reschedule_requested/cancelled do backend.

O motor e o ViewModel não recebem Context, Application, DAOs, repositórios, tokens ou clientes HTTP.
A Activity recebe somente o identificador do tema visual via Intent, sem ler nem gravar preferências.
O modo não abre Room, SQLite do servidor, journal da sincronização ou serviços de mensagens.
A introdução não instancia o ViewModel da integração real e não carrega o journal da sincronização. Apenas o tema visual atual é consultado; nenhuma preferência é alterada.
A clínica tem dados e ViewModel independentes, documentados em [clinic-demo.md](clinic-demo.md).
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

Os três primeiros comandos passaram. A suíte unitária tem **41 testes aprovados**, sem falhas ou ignorados (38 existentes e três da clínica).
Os testes do motor cobrem os sete cenários, elegibilidade, confirmação, cancelamento, remarcação, atendimento humano, PARAR, retentativas, idempotência e isolamento do Room.
A clínica acrescenta verificações dos pontos/caminho ilustrativos, sessões independentes e zero chamadas ao Room.

A suíte instrumentada inclui navegação original, entrada e retorno da demonstração, conversa, agenda, recepção, reinício e clínica/trajeto.
Na primeira execução, o teste de journal usava o contexto do APK de testes, sem acesso ao diretório; foi corrigido para uma pasta temporária exclusiva no contexto do processo alvo.
A busca de nós virtuais Compose também foi ajustada, preservando as verificações e sem adicionar dependências.

A suíte de **11 testes instrumentados passou**, sem falhas ou ignorados, no emulador Medium_Phone_2 / Android 16 (API 36):
- largura de 360 dp, tema claro;
- largura de 400 dp, tema escuro, modo avião e Wi-Fi confirmado como desativado.

Também foi feita inspeção visual no dispositivo: perfil sem interruptor, introdução sem campos técnicos, abas inteiras, mapa/trajeto, balões, respostas, rolagem e agenda.
O teste de rolagem exige que os botões completos fiquem visíveis sem usar a busca que rola a tela.
O driver dos testes percorre os nós virtuais Compose, limpa o cache de acessibilidade em API 33+ e aguarda as transições antes de procurar outra página.

Inicialmente, o emulador apresentou tela preta e ANR sem foco de janela. Reiniciar o AVD sem snapshot e com renderização por software permitiu a validação; não foi aplicada correção no código do aplicativo para essa ocorrência.
Houve ajustes nos testes de journal e na sincronização das buscas da interface durante a validação; nenhum teste foi removido ou desabilitado.
Não houve validação em aparelho físico.

Nenhuma mensagem real foi enviada.

## Limitações

Este modo não testa conectividade, credenciais, disponibilidade de horários reais, políticas ou entrega da Meta.
Não altera cadastros existentes, não persiste conversas e não substitui o backend de produção.
O roteiro é determinístico dentro de cada sessão, e as datas acompanham o dia de abertura.
A configuração real do WhatsApp e seus requisitos de HTTPS, templates e autorização permanecem no PR #28.
