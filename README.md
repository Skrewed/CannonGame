# 🎯 Cannon Game — Android (Java)

Aplicação Android completa desenvolvida em **Java** para a disciplina de **Programação III**, baseada no **Capítulo 6 – Cannon Game App** do livro *Android 6 for Programmers: An App-Driven Approach (3rd Edition)* (Paul Deitel, Harvey Deitel e Alexander Wald, páginas 217–255), acrescida de **todos os 11 recursos extras** propostos em `References, Requisitos Extras`, visual minimalista.

---

## 👥 Integrantes do Grupo

- **Integrante**: *André Felipe Andrade Oliveira*
- **Suporte**: *Gemini Argon 4*

---

## 🎯 Objetivo do Projeto

Demonstrar o domínio prático dos conceitos essenciais de programação mobile e computação gráfica 2D de baixo nível no Android:
- **Animação manual quadro a quadro (*Frame-by-Frame Animation*)** ponderada pelo intervalo de tempo real ($\Delta t$) entre quadros;
- **Manipulação gráfica direta** utilizando `Canvas` e `Paint`;
- **Gerenciamento de threads e sincronização** do *game loop* com `CannonThread`, `SurfaceView` e `SurfaceHolder.Callback` (`lockCanvas` / `unlockCanvasAndPost`);
- **Execução de efeitos sonoros e trilha contínua** com `SoundPool`, `AudioAttributes` (`USAGE_GAME`), `MediaPlayer` e controle de volume via `AudioManager.STREAM_MUSIC`;
- **Modo imersivo em tela cheia** e orientação paisagem (*landscape*);
- **Tratamento avançado de eventos de toque e gestos** (`onTouchEvent` com `ACTION_DOWN`, `ACTION_MOVE`, `ACTION_UP`).

---

## 🛠️ Ambiente Técnico e Configuração de Build

| Componente | Especificação |
| :--- | :--- |
| **IDE Recomendada** | Android Studio Rabbit 1 (`2026.2.1`) |
| **Java JDK / Runtime** | JBR LTS 25 (`openjdk 25.0.3` integrado ao Android Studio) |
| **Android Gradle Plugin (AGP)** | `9.4.1` (com Gradle Wrapper `9.6.0`) |
| **Versões do SDK** | `compileSdk = 37` • `targetSdk = 36` • `minSdk = 26` |
| **Linguagem** | Java (`com.deitel.cannongame`) |

---

## 🚀 Como Executar a Aplicação

### Pelo Android Studio Rabbit 1 (2026.2.1)
1. Abra o **Android Studio Rabbit 1 (2026.2.1)**.
2. Selecione **File > Open** e escolha a pasta raiz deste projeto (`Cannon Game`).
3. Aguarde a sincronização automática do Gradle (configurado para usar o **JBR 25** e `compileSdk 37`).
4. Selecione um Dispositivo Virtual (AVD) ou conecte um dispositivo físico Android (API 26+) e clique em **Run ('app')** (`Shift + F10`).

### Via Linha de Comando (PowerShell / Terminal)
```powershell
# Compilar o APK de Debug e executar os testes unitários
.\gradlew.bat testDebugUnitTest assembleDebug
```
O APK gerado ficará disponível em `app/build/outputs/apk/debug/app-debug.apk`.

---

## ✨ Funcionalidades Implementadas

### 1. Base Obrigatória do Capítulo 6 (Deitel)
- **8 Classes Fundamentais**:
  - [`MainActivity`](app/src/main/java/com/deitel/cannongame/MainActivity.java): hospeda o fragmento principal em orientação paisagem e tela cheia.
  - [`MainActivityFragment`](app/src/main/java/com/deitel/cannongame/MainActivityFragment.java): gerencia o ciclo de vida (`onPause` -> `stopGame()`, `onDestroy` -> `releaseResources()`) e vincula o volume a `AudioManager.STREAM_MUSIC`.
  - [`GameElement`](app/src/main/java/com/deitel/cannongame/GameElement.java): superclasse de elementos móveis com atualização vertical e colisão nas bordas da tela.
  - [`Blocker`](app/src/main/java/com/deitel/cannongame/Blocker.java): obstáculo defensor central que rebate o projétil e aplica penalidade de `-2s`.
  - [`Target`](app/src/main/java/com/deitel/cannongame/Target.java): 9 alvos destrutíveis que concedem bônus de `+3s`.
  - [`Cannon`](app/src/main/java/com/deitel/cannongame/Cannon.java): canhão articulado na borda esquerda que alinha e dispara a bola de canhão.
  - [`Cannonball`](app/src/main/java/com/deitel/cannongame/Cannonball.java): projétil disparado pelo canhão.
  - [`CannonView`](app/src/main/java/com/deitel/cannongame/CannonView.java): subclasse de `SurfaceView` contendo o *game loop* (`CannonThread`), renderização e áudio (`target_hit.wav`, `blocker_hit.wav`, `cannon_fire.wav`).

### 2. Todos os 11 Recursos Extras (`SUGESTIONS.pdf`)
1. **Sistema de Pontuação Melhorado (`Score` + Multiplicadores)**: pontos por acerto, bônus de sequência (*Combo Streak*), multiplicadores progressivos de `1x` até `5x`, textos flutuantes de pontuação e HUD dinâmico.
2. **Dificuldade Progressiva (Níveis)**: avanço contínuo de níveis com aumento gradual da velocidade dos alvos e bloqueador, redução do comprimento dos alvos, alternância de direção e ajuste dinâmico de tempo.
3. **Novos Sons e Efeitos Sonoros Customizados**: inclusão de som de falha (`miss_sound.wav`), trilha sonora ambiente contínua em loop (`bg_music.wav` via `MediaPlayer`) e botão de Mudo/Som direto no HUD.
4. **Animações Suaves no Alvo (*Fade, Bounce, Scale*)**: surgimento suave com interpolação *Ease-Out Back* e animação de expansão + *fade-out* quando um alvo é atingido.
5. **Controle por Toque Direcional (*Swipe Control*)**: além do toque rápido para atirar, deslizar verticalmente refina o ângulo do cano e deslizar horizontalmente ajusta a força do disparo (`60%` a `160%`), com linha pontilhada de mira preditiva.
6. **Modo Noturno / Modo Dia (Troca de Tema Dinâmica)**: botão interativo no HUD (`☀ Dia` / `☾ Noite`) que alterna instantaneamente as cores do cenário, canhão, bloqueador, alvos e partículas ambientes.
7. **Tela de "Fim de Jogo" e "Reiniciar" Aprimorada**: diálogo modal animado exibindo pontuação total, recorde, estatísticas completas (nível, tiros, precisão, maior combo, tempo), ranking local Top 5 e botões para **Reiniciar Jogo** ou avançar para o **Próximo Nível**.
8. **Partículas ao Acertar o Alvo**: explosão radial de partículas coloridas com movimento vetorial, desaceleração e *fade-out* quadro a quadro ([`ParticleSystem`](app/src/main/java/com/deitel/cannongame/ParticleSystem.java)).
9. **Ranking Local (*Highscores*)**: persistência das 5 maiores pontuações e preferências de usuário utilizando `SharedPreferences` ([`HighScoreManager`](app/src/main/java/com/deitel/cannongame/HighScoreManager.java)).
10. **Alvos com Movimentação Complexa**: suporte aos padrões Linear, Senoidal, Zigue-Zague e Aceleração Progressiva.
11. **Colisão Avançada (Círculo x Retângulo + Segmento Varrido / CCD)**: detecção matemática exata em [`CollisionUtils`](app/src/main/java/com/deitel/cannongame/CollisionUtils.java) combinando distância ponto-AABB e *Continuous Collision Detection* (Liang-Barsky), eliminando atravessamentos em altas velocidades.

---

## 📂 Documentação Técnica (`docs/`)

- [01-diretrizes-e-requisitos.md](docs/01-diretrizes-e-requisitos.md) — Diretrizes acadêmicas, ambiente técnico e checklist de requisitos.
- [02-arquitetura-cap6-deitel.md](docs/02-arquitetura-cap6-deitel.md) — Arquitetura detalhada do Capítulo 6, constantes proporcionais, threads e `SoundPool`.
- [03-recursos-extras-e-fisica.md](docs/03-recursos-extras-e-fisica.md) — Modelagem matemática e física dos 11 recursos extras.
- [04-guia-ui-ux-minimalista.md](docs/04-guia-ui-ux-minimalista.md) — Sistema de design minimalista Dia/Noite e localização em Português do Brasil.
