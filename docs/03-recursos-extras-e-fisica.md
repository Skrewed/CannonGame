# 03 — Especificação dos 11 Recursos Extras e Física do Jogo

Este documento especifica detalhadamente a modelagem matemática e algorítmica dos **11 recursos extras** propostos em `Requisitos Extras.pdf`, integrados à arquitetura do Capítulo 6.

---

## 1. Sistema de Pontuação Melhorado (`Score` + Multiplicadores)

- **Pontuação Base por Acerto**: Cada alvo destruído concede uma pontuação base de $P_{\text{base}} = 100 \times N_{\text{nível}}$.
- **Sequência de Acertos (*Combo Streak*)**:
  - Variável `currentStreak` incrementada a cada alvo destruído consecutivamente.
  - Se a bola de canhão colidir com o `Blocker` ou sair da tela sem atingir um `Target`, `currentStreak` é reiniciado para `0` (e um aviso visual sutil + som de falha é emitido).
- **Multiplicador Progressivo ($M$)**:
  $$M = \min\left(5, \; 1 + \left\lfloor \frac{\text{currentStreak}}{2} \right\rfloor\right)$$
  - Sequência de 0–1 acerto: $1\times$
  - Sequência de 2–3 acertos: $2\times$
  - Sequência de 4–5 acertos: $3\times$
  - Sequência de 6–7 acertos: $4\times$
  - Sequência de 8+ acertos: $5\times$ (Bônus Máximo!)
- **Bônus por Sequência**: Além do multiplicador $M$, cada acerto em sequência adiciona $+25 \times (\text{currentStreak} - 1)$ pontos extras:
  $$\Delta \text{Score} = (100 \times N_{\text{nível}} \times M) + 25 \times \max(0, \text{currentStreak} - 1)$$
- **Textos Flutuantes de Pontuação (*Floating Combat Text*)**: Ao destruir um alvo ou atingir o bloqueador, um indicador flutuante (`+300 pts (3x)` / `+3.0s` ou `-2.0s`) sobe suavemente com *fade-out*.

---

## 2. Dificuldade Progressiva (Níveis)

O jogo suporta progressão contínua de níveis (`currentLevel = 1, 2, 3...`), bem como aumento dinâmico de tensão dentro do próprio nível conforme os alvos são abatidos:

1. **Aumento de Velocidade**:
   - Multiplicador de velocidade por nível: $S_{\text{nível}} = 1.0 + 0.14 \times (N_{\text{nível}} - 1)$ (limitado a $2.4\times$).
   - Adicionalmente, dentro de um nível, a cada alvo destruído, os alvos restantes recebem um pequeno incremento de $+3\%$ na velocidade vertical.
2. **Redução de Tamanho do Alvo**:
   - O comprimento vertical do alvo é escalonado por $L_{\text{fator}} = \max(0.55, \; 1.0 - 0.06 \times (N_{\text{nível}} - 1))$.
3. **Alternância de Direção e Padrões**:
   - A partir do Nível 2, os alvos combinam direções alternadas com padrões de movimentação complexa (Senoidal, Zigue-Zague e Aceleração Progressiva).
4. **Redução do Tempo de Exposição / Recompensa**:
   - Tempo inicial por nível: $\max(7.5, \; 10.0 - 0.35 \times (N_{\text{nível}} - 1))$ segundos.
   - Recompensa por alvo: $\max(1.8, \; 3.0 - 0.15 \times (N_{\text{nível}} - 1))$ segundos.

---

## 3. Novos Sons e Efeitos Sonoros Customizados

Além dos três arquivos originais (`cannon_fire.wav`, `target_hit.wav`, `blocker_hit.wav`):
- **Som de Falha (`miss_sound.wav`)**: Efeito sonoro curto em frequência descendente (síntese PCM WAV 44.1kHz) reproduzido pelo `SoundPool` quando o tiro sai da tela sem acertar nenhum alvo.
- **Trilha Sonora Contínua (`bg_music.wav`)**: Loop harmônico minimalista em `.wav` reproduzido via `MediaPlayer` (`setLooping(true)`, volume `0.22f`) durante a partida, pausado automaticamente em `stopGame()` e liberado em `releaseResources()`.
- **Controle de Áudio no HUD**: Botão interativo no canto superior direito do Canvas permitindo alternar o som/música em tempo real.

---

## 4. Animações Suaves no Alvo (*Fade, Bounce, Scale*)

Cada instância de `Target` possui estado próprio de animação quadro a quadro:
1. **Animação de Entrada (*Spawn*)**:
   - Variável `spawnProgress` $\in [0, 1]$ avança durante os primeiros `0.45s` da rodada (com pequeno atraso escalonado por índice `n * 0.04s`).
   - Curva *Ease-Out Back* para efeito *bounce/scale*:
     $$\text{scale}(t) = 1 + c_3 (t - 1)^3 + c_1 (t - 1)^2, \quad c_1 = 1.70158, \; c_3 = c_1 + 1$$
   - Opacidade (`alpha`) interpolada linearmente de `0` a `255`.
2. **Animação ao Ser Atingido (*Hit / Destroy*)**:
   - Quando atingido, o alvo é removido da lista de colisão ativa e transferido para a lista `dyingTargets` por `0.25s`, onde expande em largura/altura (`scale` de `1.0` a `1.45`) enquanto sua opacidade decai suavemente até `0` (*fade-out*).

---

## 5. Controle por Toque Direcional (*Swipe Control*) e Toque Rápido

O método `onTouchEvent(MotionEvent e)` suporta dois modos integrados de interação:
1. **Toque Rápido (Comportamento Clássico do Cap. 6)**:
   - Ao tocar na tela (`ACTION_DOWN`), o canhão alinha imediatamente na direção do ponto tocado e, se não houver bola ativa na tela, dispara ou prepara a mira.
2. **Gesto Direcional (*Swipe Control* com `ACTION_MOVE`)**:
   - **Deslizar para cima / para baixo ($\Delta y$)**: Ajusta continuamente o ângulo do cano (`barrelAngle` $\in [0.15\pi, \; 0.85\pi]$ radianos), permitindo mira fina.
   - **Deslizar para a direita / esquerda ($\Delta x$)**: Ajusta o multiplicador de força do disparo (`powerMultiplier` $\in [0.60, \; 1.60]$), exibido em uma barra/arco de força minimalista junto ao canhão e refletido no espaçamento da linha pontilhada de mira.
   - Ao soltar o dedo (`ACTION_UP`) após um gesto de arraste, caso a bola esteja disponível, o canhão dispara com o ângulo e a força configurados.

---

## 6. Modo Noturno / Modo Dia (Troca de Tema Dinâmica)

A classe `ThemePalette` define dois temas completos selecionáveis a qualquer momento pelo botão de tema no HUD (ou persistidos em `SharedPreferences`):

| Elemento Gráfico | Modo Dia (Minimalist Light) | Modo Noturno (Minimalist Dark) |
| :--- | :--- | :--- |
| **Fundo do Cenário** | `#F8FAFC` (Alabastro Frio) | `#0B1120` (Azul Meia-Noite Profundo) |
| **Grade / Detalhe Sutil** | `#E2E8F0` | `#1E293B` |
| **Canhão (Base e Cano)** | `#0F172A` com detalhe `#3B82F6` | `#E2E8F0` com brilho `#38BDF8` |
| **Bola de Canhão** | `#1E293B` com núcleo `#EF4444` | `#F8FAFC` com halo `#38BDF8` |
| **Bloqueador** | `#DC2626` (Carmesim Moderno) | `#F43F5E` (Rosa Neon Vibrante) |
| **Alvos Pares (`dark`)** | `#2563EB` (Azul Cobalto) | `#38BDF8` (Ciano Luminoso) |
| **Alvos Ímpares (`light`)** | `#F59E0B` (Âmbar Solar) | `#A855F7` (Violeta Neon) |
| **Partículas Simuladas** | Partículas solares suaves flutuantes | Estrelas/vagalumes cintilantes |

---

## 7. Tela de "Fim de Jogo" e "Reiniciar" Aprimorada

- Exibida via `DialogFragment` na *UI Thread* (`activity.runOnUiThread(...)`), preservando o fluxo do Capítulo 6.
- Apresenta layout moderno em Português do Brasil contendo:
  - Título dinâmico (**"Vitória! Nível Concluído"** ou **"Fim de Jogo!"**) com animação suave de escala e opacidade na abertura;
  - **Pontuação Total (`Score`)** e destaque de **Novo Recorde!** caso supere o histórico;
  - **Estatísticas da Partida**: Nível alcançado, Tiros disparados, Alvos destruídos, Precisão ($\frac{\text{acertos}}{\text{tiros}} \times 100\%$), Maior sequência (*Combo*) e Tempo total decorrido;
  - **Ranking Local (Top 5 Highscores)**;
  - Botões de ação: **"Reiniciar Jogo"** (volta ao Nível 1 zerando placar) e, em caso de vitória, **"Próximo Nível"** (avança para `currentLevel + 1` mantendo a pontuação acumulada!).

---

## 8. Sistema de Partículas (`ParticleSystem`)

- **Explosão Radial no Alvo**: Gera de 20 a 26 partículas circulares na posição da colisão, herdando a cor do alvo atingido (com variações de brilho).
- **Física Quadro a Quadro**:
  - Cada partícula possui posição $(x, y)$, velocidade $(v_x, v_y)$ distribuída radialmente ($\theta \in [0, 2\pi)$), raio inicial $r_0$, tempo de vida $T_{\text{life}} \in [0.35\text{s}, 0.65\text{s}]$ e desaceleração por atrito ($v \leftarrow v \cdot (1 - 2.5 \Delta t)$).
  - A cada frame, o raio e o canal alfa (`0..255`) diminuem proporcionalmente ao tempo restante de vida.

---

## 9. Ranking Local (*Highscores* com `SharedPreferences`)

- A classe `HighScoreManager` utiliza `SharedPreferences` (`"cannon_game_prefs"`) para armazenar:
  - As 5 maiores pontuações (`Top 5`), incluindo pontuação, nível atingido e precisão;
  - A preferência do usuário para o tema (Modo Dia / Modo Noturno) e estado do áudio.

---

## 10. Alvos com Movimentação Complexa

Cada `Target` recebe um `MovementType`:
1. **`LINEAR`**: Movimento vertical clássico com inversão nas bordas superior/inferior.
2. **`SINOIDAL`**: Além do deslocamento vertical, oscila horizontalmente em torno de sua coluna base $x_0$:
   $$x(t) = x_0 + A \sin(\omega t + \phi)$$
   onde $A$ é a amplitude (limitada a $40\%$ do espaçamento entre alvos para nunca sobrepor vizinhos) e $\omega$ é a frequência angular.
3. **`ZIGZAG`**: Deslocamento horizontal triangular periódico combinado ao movimento vertical:
   $$x(t) = x_0 + A \cdot \frac{2}{\pi} \arcsin(\sin(\omega t + \phi))$$
4. **`PROGRESSIVE_ACCEL`**: A velocidade vertical aumenta progressivamente a cada segundo ($\text{vel}_Y \leftarrow \text{vel}_Y \cdot (1 + 0.35 \Delta t)$) até um limite máximo seguro, mantendo o jogador desafiado.

---

## 11. Colisão Avançada (Círculo vs. Retângulo + Linha Varrida / CCD)

No Capítulo 6 original, `Cannonball.collidesWith` usava apenas `Rect.intersects(shape, element.shape)`. Este projeto implementa matemática vetorial exata em `CollisionUtils`:

1. **Colisão Exata Círculo vs. AABB (*Axis-Aligned Bounding Box*)**:
   Dado o centro da bola $C = (c_x, c_y)$ com raio $R$ e o retângulo $B = [x_{\min}, x_{\max}] \times [y_{\min}, y_{\max}]$, o ponto mais próximo $P = (p_x, p_y)$ no retângulo é:
   $$p_x = \max(x_{\min}, \min(c_x, x_{\max})), \quad p_y = \max(y_{\min}, \min(c_y, y_{\max}))$$
   Há colisão se e somente se:
   $$(c_x - p_x)^2 + (c_y - p_y)^2 \le R^2$$
2. **Detecção Contínua Círculo vs. Segmento de Trajetória (*Swept Collision / CCD*)**:
   Para evitar que a bola atravesse um alvo fino entre dois quadros consecutivos ($C_{\text{prev}} \to C_{\text{curr}}$), verifica-se também se o segmento de reta $\overline{C_{\text{prev}} C_{\text{curr}}}$ intercepta o retângulo do elemento expandido pelo raio $R$ (*algoritmo de Liang-Barsky / Slab intersection*).
