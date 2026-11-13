## Objetivo

A apresentação acadêmica exibia campos de servidor, credenciais e sincronização antes da demonstração. Agora o perfil abre uma introdução simples e a experiência offline, com conversa interativa e uma clínica fictícia com mapa.

Este PR mantém a base **feat/whatsapp-sqlite-integration** (PR #28), contendo somente a demonstração e seu refinamento. Nenhum merge automático.

## Telas e utilização

- **Perfil → Lembretes pelo WhatsApp → Experimentar demonstração**: linha clicável com chevron, sem interruptor; introdução sem formulários técnicos.
- **WhatsAppDemoActivity**: cabeçalho de simulação, procedimento compacto com Detalhes, resumo do estado, quatro abas fixas e legíveis, conversa com paleta verde própria, balões de clínica/tutor e respostas integradas ao lembrete.
- **Perfil → Minha clínica**: Clínica Jardim Veterinária fictícia, endereço/serviços/horários ilustrativos e mapa nativo Canvas.
- **Simular trajeto / Ocultar trajeto**: origem, caminho, distância e duração fictícios, inteiramente dentro do aplicativo.

As opções **💬 Lembretes pelo WhatsApp** e **🏥 Minha clínica** seguem o mesmo padrão compacto de Tema do aplicativo e Histórico clínico completo: uma linha, emoji, espaçamento uniforme e indicador de navegação discreto, sem subtítulos.

O formulário técnico do piloto foi preservado em WhatsAppPilotScreen.kt, sem acesso no fluxo acadêmico. O ViewModel e os serviços reais continuam no projeto.

## Cenários e regras preservados

Atendimento normal, falta de consentimento, ausência de recorrência, agendamento cancelado, falhas transitórias/persistentes, revogação por PARAR e tutor inativo.

- RF-008: elegibilidade, vencimento e envio simulado.
- RF-009: confirmação mantém AGENDADO; cancelamento preserva registros; remarcação cria pendência sem trocar a data.
- RF-010: texto original, atendimento humano, observação e resolução.
- RN-003/RN-004: tutor ativo e recorrência válida obrigatórios.
- RNF-003: retentativas, histórico e idempotência demonstrados, sem afirmar SLA real.

As regras de DemoEngine.kt não foram alteradas neste refinamento. A escolha de uma resposta desabilita as ações de comparecimento contraditórias.

## Arquitetura e isolamento

Estado em memória e ViewModels próprios. Fixtures, máquina de estados e relógio lógico isolados dos repositórios reais.
A clínica usa pontos normalizados de um desenho, sem coordenadas geográficas, GPS, permissões de localização ou APIs de mapas.

Room v3, entidades, DAOs, migrações, backend FastAPI/SQLite e serviços Meta permanecem intactos.
A demonstração não grava cadastros, preferências, journal ou comunicações reais. Nenhuma credencial ou mensagem real utilizada.
Nenhuma dependência adicionada.

## Validação

| Verificação | Resultado |
|---|---|
| app:assembleDebug | Sucesso |
| app:testDebugUnitTest | 41 aprovados; zero falhas/ignorados |
| app:assembleDebugAndroidTest | Sucesso |
| app:connectedDebugAndroidTest — 360 dp, claro | 11 aprovados |
| app:connectedDebugAndroidTest — 400 dp, escuro, modo avião/Wi-Fi desativado | 11 aprovados |

Emulador: Medium_Phone_2, Android 16/API 36. Inspeção visual realizada no perfil, introdução, mapa/trajeto, conversa, respostas e agenda.
Novos testes cobrem clínica, navegação de retorno, ausência de campos técnicos, reinício de sessão e rolagem automática sem intervenção.
Os testes existentes continuam ativos; foram corrigidos o contexto do diretório temporário do journal e a busca/sincronização de nós virtuais Compose.

No ajuste posterior das linhas de Preferências, os APKs de aplicativo e de testes foram recompilados e os três testes de navegação de lembretes/clínica foram executados novamente, todos aprovados. O alinhamento visual das quatro opções foi conferido no emulador com o tema Azul.

O emulador inicialmente congelou a renderização e perdeu o foco da janela. Um reinício sem snapshot com renderização por software permitiu executar a validação, sem alterações de produção para contornar o ambiente.

## Documentação e limites

- docs/whatsapp-demo.md: acesso atualizado, cenários e roteiro de cinco minutos incluindo a clínica.
- docs/clinic-demo.md: fixtures, mapa, trajeto, isolamento e limitações.

Não há geolocalização, rota real, agenda de clínica real ou envio pelo WhatsApp. O modo não substitui os testes autenticados da Meta nem o backend de produção.
Estado descartado ao fechar a sessão; o ViewModel conserva a sessão durante recriação. Datas relativas ao dia de abertura.
Não validado em aparelho físico. A integração real continua dependente da infraestrutura documentada no PR #28.

## Checklist

- [x] Perfil sem interruptor enganoso e com acesso à clínica.
- [x] Introdução acadêmica sem configuração técnica.
- [x] Clínica e mapa/trajeto fictícios funcionando offline.
- [x] Conversa, botões, agenda, recepção e histórico preservados.
- [x] Sete cenários e regras existentes preservados.
- [x] Compilação, testes unitários e instrumentados executados.
- [x] Temas claro/escuro e larguras 360/400 dp conferidos.
- [x] Room/backend/Meta preservados; nenhum envio real.
- [x] Arquivos pessoais do Android Studio fora dos commits.
- [x] PR #28 preservado; sem merge automático.
