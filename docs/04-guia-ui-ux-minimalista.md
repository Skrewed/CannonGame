# 04 — Guia de UI/UX Minimalista e Localização (PT-BR)

## 1. Filosofia Visual Minimalista e Moderna

A interface gráfica do **Cannon Game** foi projetada para elevar os gráficos 2D nativos do `Canvas` a um padrão visual contemporâneo, limpo e legível, sem abrir mão da estrutura de desenho do Capítulo 6:

- **Geometria Suavizada**:
  - Os alvos (`Target`) e o bloqueador (`Blocker`) são renderizados com cantos arredondados (`canvas.drawRoundRect`) e um sutil destaque interno, preservando seus limites `Rect shape` para a lógica do jogo.
  - O canhão (`Cannon`) possui acabamento em camadas: cano com `Paint.Cap.ROUND`, anel de articulação central e base semicircular com núcleo contrastante.
  - A bola de canhão (`Cannonball`) apresenta um rastro translúcido decrescente (*motion trail* das últimas posições) que reforça a sensação de velocidade e direção.
- **HUD Superior em *Pills* Translúcidas**:
  - Em vez de apenas texto cru solto no canto superior esquerdo, o HUD organiza as informações em cartões horizontais minimalistas (*pills*) no topo da tela:
    1. **Cronômetro (`Tempo`)**: muda de cor para âmbar/vermelho pulsante quando `timeLeft <= 3.0s`;
    2. **Pontuação e Multiplicador (`Pontos` + badge `2x..5x`)**: destaca a sequência ativa (*Combo*);
    3. **Nível e Alvos Restantes (`Nível X • Y/9`)**;
    4. **Botões Rápidos de Toque no Canvas**:
       - Alternância instantânea **Modo Dia / Modo Noite** (`☀ Dia` / `☾ Noite`);
       - Alternância de **Áudio / Música** (`🔊 Som` / `🔇 Mudo`).
- **Feedback Visual de Mira e Força (*Swipe Control*)**:
  - Enquanto o jogador toca ou desliza o dedo na tela, uma linha tracejada suave projeta a direção do tiro a partir da ponta do cano, e uma barra de potência indica a força selecionada (`60%` a `160%`).

---

## 2. Tabela de Strings em Português do Brasil (`res/values/strings.xml`)

| Chave (`name`) | Valor em Português do Brasil (`pt-BR`) | Uso |
| :--- | :--- | :--- |
| `app_name` | `Cannon Game` | Nome do aplicativo |
| `time_remaining_format` | `Tempo: %.1f s` | Cronômetro no HUD |
| `score_format` | `Pontos: %1$d` | Placar atual no HUD |
| `combo_format` | `Combo %1$dx` | Indicador de multiplicador ativo |
| `level_format` | `Nível %1$d` | Indicador do nível atual |
| `highscore_format` | `Recorde: %1$d` | Maior pontuação registrada |
| `win` | `Vitória! Você destruiu todos os alvos!` | Título do diálogo ao vencer |
| `lose` | `Fim de Jogo! O tempo esgotou!` | Título do diálogo ao perder |
| `results_format` | `Pontuação Final: %1$d pts\nNível Alcançado: %2$d\nTiros Disparados: %3$d\nPrecisão: %4$.0f%%\nMaior Sequência: %5$d acertos\nTempo Total: %6$.1f s` | Resumo estatístico da partida |
| `reset_game` | `Reiniciar Jogo` | Botão para iniciar nova partida do Nível 1 |
| `next_level` | `Próximo Nível` | Botão para avançar de nível após vitória |
| `highscores_title` | `Ranking Local (Top 5)` | Cabeçalho do ranking no diálogo final |
| `new_record_badge` | `★ NOVO RECORDE LOCAL! ★` | Destaque quando a partida bate o recorde |
| `theme_day` | `☀ Dia` | Rótulo do botão de tema claro |
| `theme_night` | `☾ Noite` | Rótulo do botão de tema escuro |
| `sound_on` | `🔊 Som` | Rótulo do botão de áudio ativo |
| `sound_off` | `🔇 Mudo` | Rótulo do botão de áudio silenciado |
| `swipe_hint` | `Toque para atirar • Deslize para ajustar ângulo e força` | Dica minimalista exibida no início da rodada |
