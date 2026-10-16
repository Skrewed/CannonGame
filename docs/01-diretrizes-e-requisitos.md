# 01 — Diretrizes, Requisitos e Ambiente de Desenvolvimento

## 1. Contexto Acadêmico e Objetivo Geral

Este documento consolida as diretrizes estabelecidas previamente, os recursos bônus e a referência bibliográfica do **Capítulo 6 – Cannon Game App** do livro *Android 6 for Programmers: An App-Driven Approach (3rd Edition)*, de Paul Deitel, Harvey Deitel e Alexander Wald.

O objetivo do projeto é aplicar os conceitos fundamentais do desenvolvimento mobile em **Java para Android**, demonstrando domínio prático sobre:
- **Animação quadro a quadro manual** (*manual frame-by-frame animation*) baseada no tempo decorrido entre quadros ($\Delta t$);
- **Manipulação gráfica em baixo nível** utilizando `android.graphics.Canvas` e `android.graphics.Paint`;
- **Execução de efeitos sonoros e áudio** utilizando `SoundPool`, `AudioAttributes`, `MediaPlayer` e controle de volume pelo hardware (`AudioManager.STREAM_MUSIC`);
- **Controle de threads e sincronização** para lógica de jogo e *game loop* (`CannonThread` sincronizado via `SurfaceHolder`);
- **Renderização dedicada** com `SurfaceView` e `SurfaceHolder.Callback`;
- **Modo imersivo e tela cheia** em orientação paisagem (*landscape*);
- **Interatividade por toque e gestos** (`onTouchEvent` com `ACTION_DOWN`, `ACTION_MOVE`, `ACTION_UP`) e atualização responsiva da interface gráfica em **Português do Brasil**.

---

## 2. Especificações do Ambiente Técnico

Para garantir compatibilidade total com o ambiente instalado no computador de desenvolvimento, o projeto deve seguir estritamente os parâmetros abaixo:

| Parâmetro | Valor Especificado | Observações Técnicas |
| :--- | :--- | :--- |
| **IDE Alvo** | Android Studio Rabbit 1 (`2026.2.1`) | Build `AI-262.9437.185.2621.16467767` |
| **Java JDK / Runtime** | JBR LTS 25 (`openjdk 25.0.3`) | Localizado em `C:\Program Files\Android\Android Studio\jbr` |
| **Linguagem** | Java | Pacote base: `com.deitel.cannongame` |
| **Android Gradle Plugin (AGP)** | `9.4.1` | Versão estável oficial suportada pelo Android Studio 2026.2.1 |
| **Gradle Wrapper** | `9.6.0` | Mapeamento nativo do AGP `9.4.x` com suporte completo ao Java 25 |
| **compileSdk** | `37` | Plataforma `android-37.0` instalada no Android SDK local |
| **targetSdk** | `36` | Conformidade com políticas modernas de janela, áudio e ciclo de vida |
| **minSdk** | `26` | Android 8.0 (Oreo) — suporte nativo a ícones adaptativos, `AudioAttributes` e `SoundPool.Builder` |
| **Idioma da Interface (UI)** | Português do Brasil (`pt-BR`) | Todos os textos, HUD, botões, diálogos e estatísticas em português |
| **Estilo Visual** | Minimalista e Moderno | Geometria limpa, cantos arredondados, tipografia anti-aliased e suporte a Modo Dia/Noite |

---

## 3. Requisitos Funcionais Obrigatórios (Base — Capítulo 6)

1. **Orientação Paisagem e Modo Imersivo**:
   - A `MainActivity` deve operar exclusivamente em modo `landscape` e ocultar barras de título, ActionBar e barras de sistema durante a partida (`hideSystemBars()`), exibindo-as novamente ao abrir o diálogo de fim de jogo (`showSystemBars()`).
2. **Elementos Clássicos do Jogo**:
   - **Canhão (`Cannon`)**: posicionado na borda esquerda central da tela, composto por base semicircular e cano orientável que aponta para o ponto tocado e dispara projéteis (`Cannonball`).
   - **Bola de Canhão (`Cannonball`)**: projétil circular disparado a partir do canhão; apenas uma bola pode estar ativa na tela por vez no modo padrão (ou até sair da tela / atingir um alvo).
   - **Bloqueador (`Blocker`)**: barreira vertical indestrutível posicionada no meio do campo (`BLOCKER_X_PERCENT = 0.5`), movendo-se verticalmente e rebatendo a bola de canhão (`reverseVelocityX()`), aplicando uma penalidade de tempo (`MISS_PENALTY = 2` segundos).
   - **Alvos (`Target`)**: 9 seções verticais (`TARGET_PIECES = 9`) posicionadas à direita do bloqueador, alternando cores e velocidades iniciais, concedendo bônus de tempo (`HIT_REWARD = 3` segundos) ao serem destruídas.
3. **Regras de Tempo, Vitória e Derrota**:
   - Contagem regressiva iniciando em `10.0` segundos (`timeLeft = 10.0`).
   - Atingir um alvo adiciona `+3` segundos (ajustável pela dificuldade progressiva); atingir o bloqueador subtrai `-2` segundos.
   - Se todos os 9 alvos forem destruídos antes do tempo zerar, o jogador vence a rodada/nível; se o cronômetro chegar a `0.0`, o jogo termina em derrota.
4. **Recursos de Áudio Originais**:
   - Os arquivos fornecidos na pasta `resources/` devem ser integrados em `app/src/main/res/raw/`:
     - `cannon_fire.wav`: disparo do canhão;
     - `target_hit.wav`: destruição de um alvo;
     - `blocker_hit.wav`: colisão com o bloqueador.

---

## 4. Requisitos Extras Implementados

Todos os 11 recursos sugeridos no documento `Requisitos Extras.pdf` fazem parte do escopo implementado neste projeto:

- **RF-EXTRA-01**: Sistema de Pontuação Melhorado (`Score` + bônus por sequência/combo + multiplicadores progressivos `1x` a `5x` + HUD dinâmico em tempo real).
- **RF-EXTRA-02**: Dificuldade Progressiva por Níveis (aumento de velocidade dos alvos e bloqueador, redução gradual de tamanho dos alvos, alternância de direção e redução moderada do bônus de tempo a cada nível superado).
- **RF-EXTRA-03**: Novos Sons e Efeitos Sonoros Customizados (som de falha/erro `miss_sound.wav` quando o tiro sai pela borda sem acertar alvo e trilha sonora contínua `bg_music.wav` gerenciada via `MediaPlayer`, além de controle de mudo no HUD).
- **RF-EXTRA-04**: Animações Suaves no Alvo (*Fade*, *Bounce*, *Scale* na entrada dos alvos e animação de expansão + *fade-out* ao serem atingidos).
- **RF-EXTRA-05**: Controle por Toque Direcional (*Swipe Control* em `onTouchEvent` permitindo deslizar verticalmente para refinar o ângulo, deslizar horizontalmente para modular a força de disparo de `60%` a `160%` com indicador visual de força e linha de trajetória pontilhada, mantendo também o disparo rápido por toque simples).
- **RF-EXTRA-06**: Modo Noturno / Modo Dia com troca dinâmica de tema direto na tela de jogo (alterando cores do cenário, canhão, bloqueador, alvos, HUD e partículas ambientes).
- **RF-EXTRA-07**: Tela de "Fim de Jogo" e "Reiniciar" aprimorada (exibindo pontuação total, recorde, estatísticas detalhadas de tiros, precisão, combos, nível alcançado, ranking local Top 5, animação de entrada e botões para Reiniciar ou Avançar para o Próximo Nível em caso de vitória).
- **RF-EXTRA-08**: Sistema de Partículas ao acertar alvos e bloqueador (explosão radial de círculos coloridos com velocidade vetorial, atrito e *fade-out* quadro a quadro).
- **RF-EXTRA-09**: Ranking Local de Maiores Pontuações (*Highscores*) persistido via `SharedPreferences`.
- **RF-EXTRA-10**: Alvos com Movimentação Complexa (suporte a trajetórias Linear, Senoidal, Zigue-Zague e Aceleração Progressiva).
- **RF-EXTRA-11**: Detecção de Colisão Avançada (matemática vetorial exata Círculo vs. Retângulo/AABB combinada com detecção contínua por segmento varrido — *Swept Circle-Segment CCD* — evitando atravessamento em altas velocidades).

---

## 5. Padrões de Entrega e Qualidade de Código

- Estrutura padrão do Android Studio com código-fonte limpo, comentado em estilo profissional e organizado no pacote `com.deitel.cannongame`.
- Testes unitários automatizados em `app/src/test/` validando a matemática de colisão, sistema de pontuação/multiplicadores e física de movimentação.
- Arquivo `README.md` completo na raiz do projeto documentando arquitetura, instruções de compilação/execução e funcionalidades.
