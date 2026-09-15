# Zelo 🐾

O **Zelo** é um aplicativo Android voltado para a saúde, bem-estar e organização de rotinas para tutores de pets. Desenvolvido de forma moderna utilizando **Jetpack Compose** integrado com telas em **XML**, seguindo a arquitetura **MVVM**.

## ✨ Funcionalidades principais

- **Dashboard Principal (`InicioScreen`):** Tela rica em Jetpack Compose que traz uma mensagem de boas-vindas ao tutor, exibe o Pet ativo, emite cards de lembretes importantes e um **GridView de Ações Rápidas** estruturado para navegação ágil.
- **Grade de Ações Rápidas (Grid):** Acesso direto em formato de grade para os pilares do app: Agenda, Histórico Clínico, Gerenciamento de Pets e Configurações de Perfil.
- **Agenda Integrada (`AgendaActivity`):** Calendário dinâmico com navegação semanal, listagem de agendamentos diários e fluxo de marcação de novas consultas.
- **Histórico Clínico (`HistoricoScreen`):** Linha do tempo elegante mostrando vacinas, vermífugos e consultas já concluídas do pet selecionado.
- **Perfil do Tutor e Preferências (`PerfilScreen`):** Área de gestão dos dados do tutor, listagem dos pets cadastrados e ativação de lembretes automáticos via WhatsApp.

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Kotlin 
- **Interface Gráfica:** Jetpack Compose & XML ViewBinding
- **Design System:** Material Design 3 (M3) com paleta personalizada (Clay, Sage e tonalidades terra).
- **Arquitetura:** MVVM (Model-View-ViewModel) com gerenciamento de estado via `State` e `LiveData`.

---
*Desenvolvido com carinho para cuidar de quem te dá amor incondicional.*
