## Objetivo

Permitir apresentar o fluxo de lembretes inteiramente pela interface Android, sem internet, FastAPI, credenciais ou WhatsApp real.

Este PR parte da branch feat/whatsapp-sqlite-integration corrigida no PR #28. Sua base é essa branch, não main, para conter somente a demonstração e preservar a integração anterior.

## Novas telas e utilização

Perfil → Configurar lembretes pelo WhatsApp → Experimentar demonstração.

WhatsAppDemoActivity exibe indicador permanente de simulação, procedimento fictício, conversa em balões, agenda atualizada, recepção com pendências e histórico cronológico.
Selecionar cenário, Próximo passo e Reiniciar demonstração tornam a apresentação reproduzível. Detalhes expande as informações do procedimento sem sobrecarregar a primeira tela.

## Cenários e regras

Atendimento normal, falta de consentimento, ausência de recorrência, agendamento cancelado, falha transitória/persistente, revogação por PARAR e tutor inativo.

RF-008: cálculo e elegibilidade antes de gerar uma mensagem.
RF-009: confirmação mantém AGENDADO; cancelamento mantém registro; remarcação cria pendência sem trocar data.
RF-010: mensagem original preservada, atendimento humano e observação de resolução.
RN-003/RN-004: tutor ativo e regra válida obrigatórios.
Cancelamento do atendimento preserva vencimento clínico, com aviso sem botões de comparecimento, como no backend corrigido.

## Arquitetura e isolamento

Fixtures/modelos e máquina de estados Kotlin puros, comandos explícitos e ViewModel próprio. Relógio lógico, IDs de mensagens e sequências de entrada controlados; ações repetidas são idempotentes.
Estado em memória; nenhuma dependência de Context, Room, repositórios reais ou HTTP no motor e no ViewModel.
A Activity recebe apenas o tema via Intent. Sem gravação de dados, preferências, journal, tokens ou backend.
Nenhuma dependência nova. Room v3 e o backend não foram alterados.

## Testes executados

- app:assembleDebug: sucesso.
- app:testDebugUnitTest: **38 passaram**, zero ignorados/falhas (26 novos + 12 existentes).
- app:assembleDebugAndroidTest: sucesso.
- Três instrumentados adicionados para navegação, respostas, agenda, recepção, reinício e cenários.
- Instrumentados não executados: emulator-5554 offline.
- Renderização de preview pelo MCP indisponível (Missing ComposeViewAdapter); não declarada como teste visual aprovado.
- Nenhuma mensagem real enviada.

## Apresentação e limitações

docs/whatsapp-demo.md contém o roteiro de cinco minutos e instruções completas.
O estado sobrevive à rotação, mas é descartado ao fechar a Activity. As datas são relativas ao dia de abertura; o relógio das ações é controlado.
O modo simula resultados, não executa Python, API Meta ou webhooks reais, nem comprova entrega de mensagens ou disponibilidade de produção.
Execução visual/instrumentada ainda deve ser conferida em dispositivo online.

Sem merge automático. A base poderá ser atualizada para main após a incorporação do PR #28.
