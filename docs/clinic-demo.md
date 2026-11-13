# Minha clínica — demonstração offline

## Objetivo e acesso

Abra **Perfil → Minha clínica**. A tela apresenta a **Clínica Jardim Veterinária**, a mesma clínica fictícia do cabeçalho da conversa demonstrativa do WhatsApp.
É uma apresentação educativa; não representa uma clínica ou geografia reais.

## Dados fictícios

- Identificação: Clínica Jardim Veterinária, clínica demonstrativa, atendimento disponível na simulação.
- Endereço: Rua das Patinhas, 120 • Bairro Jardim dos Pets, em uma cidade ilustrativa.
- Serviços: consultas veterinárias, vacinação e prevenção, exames de rotina e orientação de cuidados.
- Horário demonstrativo: segunda a sexta, 08h–18h; sábado, 08h–12h.
- Distância e duração ilustrativas: 1,2 km e 4 min.

Esses dados são fixtures exclusivas da apresentação. Não são associados a pets, tutores, estabelecimentos ou endereços reais.

## Mapa e trajeto

O mapa é desenhado com Canvas/Compose: ruas, quadras, árvores, marcador e identificação da clínica.
O aviso **Mapa ilustrativo — localização fictícia.** permanece na área do mapa.

**Simular trajeto** mostra uma origem fictícia e um caminho destacado até o marcador da clínica, além da distância e duração ilustrativas.
**Ocultar trajeto** restaura o desenho inicial. A interação pode ser repetida; não existe cálculo de rota.
Os pontos são frações de um desenho retangular (0 a 1), sem latitude ou longitude.

## Arquitetura, temas e isolamento

- `demo/clinic/ClinicDemoData.kt`: dados imutáveis e pontos do diagrama.
- `viewmodel/ClinicDemoViewModel.kt`: apenas visibilidade do trajeto, em memória.
- `ui/clinic/ClinicDemoScreen.kt`: cards Material 3, informações e desenho nativo.
- `ClinicDemoActivity.kt`: recebe somente o tema visual e oferece retorno ao perfil.

Não utiliza GPS, Google Maps, mapas externos, permissões de localização, internet, Room, sincronização ou backend.
Não recebe repositórios reais, não lê pets e não grava preferências.
A indicação de trajeto sobrevive à recriação da Activity pelo ViewModel, mas é descartada ao fechar a sessão.
A paleta do mapa acompanha a superfície clara ou escura do tema atual.

## Apresentação

Reserve aproximadamente 45 segundos: abra Minha clínica, aponte os dados fictícios, simule o trajeto e mostre o aviso de localização ilustrativa.
O roteiro completo de cinco minutos, incluindo a conversa de lembretes, está em [whatsapp-demo.md](whatsapp-demo.md).

## Validação e limitações

Testes unitários verificam o caminho nos limites do desenho, segmentos pelas ruas, alternância/reinício de sessões e ausência de acesso ao Room.
Testes instrumentados verificam o acesso pelo perfil, a ida e volta ao WhatsApp, o botão de trajeto e a conservação do estado na recriação.
A suíte completa passou com 41 testes unitários e 11 instrumentados. Os instrumentados foram executados em 360 dp/tema claro e 400 dp/tema escuro, neste último caso com modo avião e Wi-Fi desativado.
O mapa, o trajeto e a navegação também foram inspecionados visualmente no emulador Android 16/API 36. Não houve validação em aparelho físico.

Esta tela não oferece geolocalização, navegação real, disponibilidade real da clínica, horários atualizados ou agendamento.
O indicador de atendimento é inteiramente fictício. O funcionamento do desenho não depende de conectividade.
