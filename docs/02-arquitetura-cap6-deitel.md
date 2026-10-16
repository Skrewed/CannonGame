# 02 — Arquitetura Base do Capítulo 6 (Deitel)

## 1. Visão Geral das Classes do Capítulo 6

A arquitetura base do aplicativo segue fielmente a estrutura orientada a objetos apresentada nas páginas 217 a 255 (PDF 254 a 292) do livro *Android 6 for Programmers: An App-Driven Approach (3rd Edition)*. O sistema é composto pelas **8 classes principais** abaixo, acrescidas de classes auxiliares modulares para os recursos extras:

| Classe | Superclasse / Interfaces | Responsabilidade Principal (Cap. 6) |
| :--- | :--- | :--- |
| `MainActivity` | `AppCompatActivity` | Activity hospedeira em tela cheia e orientação paisagem; infla `activity_main.xml`. |
| `MainActivityFragment` | `Fragment` (`androidx.fragment.app.Fragment`) | Infla `fragment_main.xml`, vincula o botão de volume à stream `AudioManager.STREAM_MUSIC` e coordena `stopGame()` em `onPause()` e `releaseResources()` em `onDestroy()`. |
| `GameElement` | `Object` | Superclasse para elementos móveis retangulares (`Blocker`, `Target`, `Cannonball`); armazena `view`, `paint`, `shape` (`Rect`), `velocityY` e `soundId`. |
| `Blocker` | `GameElement` | Representa o obstáculo defensor com penalidade `missPenalty`; rebate a bola de canhão. |
| `Target` | `GameElement` | Representa cada uma das 9 peças de alvo com bônus de tempo `hitReward`. |
| `Cannon` | `Object` | Representa o canhão na borda esquerda; calcula o ângulo do cano (`align`), instancia e dispara a `Cannonball` (`fireCannonball`) e desenha cano + base. |
| `Cannonball` | `GameElement` | Projétil circular com velocidade horizontal (`velocityX`) e vertical (`velocityY`); verifica se está na tela (`isOnScreen`), testa colisões (`collidesWith`) e inverte velocidade horizontal (`reverseVelocityX`). |
| `CannonView` | `SurfaceView implements SurfaceHolder.Callback` | Gerencia o estado do jogo, constantes de dimensionamento percentual, efeitos sonoros (`SoundPool`), eventos de toque (`onTouchEvent`), modo imersivo e a thread do *game loop* (`CannonThread`). |

---

## 2. Constantes Proporcionais de Dimensionamento (`CannonView`)

Para garantir independência de resolução e densidade de pixels em diferentes telas Android, todas as dimensões e velocidades no Capítulo 6 são calculadas como frações de `screenWidth` e `screenHeight` obtidas em `onSizeChanged`:

| Constante | Valor no Cap. 6 | Dimensão Base | Significado |
| :--- | :--- | :--- | :--- |
| `MISS_PENALTY` | `2` | Segundos | Penalidade de tempo ao atingir o `Blocker` |
| `HIT_REWARD` | `3` | Segundos | Recompensa de tempo ao destruir um `Target` |
| `CANNON_BASE_RADIUS_PERCENT` | `3.0 / 40` (`0.075`) | `screenHeight` | Raio da base semicircular do canhão |
| `CANNON_BARREL_WIDTH_PERCENT` | `3.0 / 40` (`0.075`) | `screenHeight` | Espessura do cano do canhão |
| `CANNON_BARREL_LENGTH_PERCENT` | `1.0 / 10` (`0.10`) | `screenWidth` | Comprimento do cano do canhão |
| `CANNONBALL_RADIUS_PERCENT` | `3.0 / 80` (`0.0375`) | `screenHeight` | Raio da bola de canhão |
| `CANNONBALL_SPEED_PERCENT` | `3.0 / 2` (`1.50`) | `screenWidth` | Velocidade base escalar da bola de canhão |
| `TARGET_WIDTH_PERCENT` | `1.0 / 40` (`0.025`) | `screenWidth` | Largura de cada alvo |
| `TARGET_LENGTH_PERCENT` | `3.0 / 20` (`0.15`) | `screenHeight` | Comprimento vertical base de cada alvo |
| `TARGET_FIRST_X_PERCENT` | `3.0 / 5` (`0.60`) | `screenWidth` | Posição X inicial do primeiro alvo à esquerda |
| `TARGET_SPACING_PERCENT` | `1.0 / 60` (`0.01667`) | `screenWidth` | Espaçamento horizontal entre alvos consecutivos |
| `TARGET_PIECES` | `9` | Quantidade | Número total de alvos por rodada |
| `TARGET_MIN_SPEED_PERCENT` | `3.0 / 4` (`0.75`) | `screenHeight` | Velocidade vertical mínima absoluta dos alvos |
| `TARGET_MAX_SPEED_PERCENT` | `6.0 / 4` (`1.50`) | `screenHeight` | Velocidade vertical máxima absoluta dos alvos |
| `BLOCKER_WIDTH_PERCENT` | `1.0 / 40` (`0.025`) | `screenWidth` | Largura do bloqueador |
| `BLOCKER_LENGTH_PERCENT` | `1.0 / 4` (`0.25`) | `screenHeight` | Comprimento vertical do bloqueador |
| `BLOCKER_X_PERCENT` | `1.0 / 2` (`0.50`) | `screenWidth` | Posição horizontal do bloqueador (centro da tela) |
| `BLOCKER_SPEED_PERCENT` | `1.0` (`1.00`) | `screenHeight` | Velocidade vertical inicial do bloqueador |
| `TEXT_SIZE_PERCENT` | `1.0 / 18` (`0.0555`) | `screenHeight` | Tamanho base de referência para texto no Canvas |

---

## 3. Ciclo de Vida, Threading e `SurfaceView`

### 3.1 `SurfaceHolder.Callback` e `CannonThread`
Diferente de `View` comum (que desenha na *UI Thread* via `onDraw`), o `CannonView` estende `SurfaceView` para permitir renderização direta a partir de uma thread secundária (`CannonThread`):

1. **`surfaceCreated(SurfaceHolder holder)`**:
   - Se o diálogo de fim de jogo não estiver visível (`!dialogIsDisplayed`), chama `newGame()`, instancia `cannonThread = new CannonThread(holder)`, chama `cannonThread.setRunning(true)` e `cannonThread.start()`.
2. **Loop de Jogo (`CannonThread.run()`)**:
   - Registra `previousFrameTime = System.currentTimeMillis()`.
   - Enquanto `threadIsRunning` for `true`:
     - Obtém o `Canvas` via `canvas = surfaceHolder.lockCanvas(null)`.
     - Dentro de um bloco `synchronized (surfaceHolder)`:
       - Calcula `elapsedTimeMS = currentTime - previousFrameTime`.
       - Acumula `totalElapsedTime += elapsedTimeMS / 1000.0`.
       - Invoca `updatePositions(elapsedTimeMS)`.
       - Invoca `testForCollisions()`.
       - Invoca `drawGameElements(canvas)`.
       - Atualiza `previousFrameTime = currentTime`.
     - No bloco `finally`, se `canvas != null`, libera e publica o quadro com `surfaceHolder.unlockCanvasAndPost(canvas)`.
3. **`surfaceDestroyed(SurfaceHolder holder)`**:
   - Sinaliza `cannonThread.setRunning(false)` e aguarda o término seguro com `cannonThread.join()` em um laço `while (retry)` tratando `InterruptedException`, garantindo que nenhum desenho ocorra após a destruição da superfície.

---

## 4. Gerenciamento de Áudio (`SoundPool` e `AudioAttributes`)

No construtor do `CannonView`:
- Configura-se um `AudioAttributes` com `AudioAttributes.USAGE_GAME`.
- Constrói-se o `SoundPool` via `SoundPool.Builder` (aumentando `maxStreams` de `1` para `4` de modo a permitir sobreposição limpa entre tiros, explosões de alvos e quiques no bloqueador sem cortar efeitos ativos).
- Mapeiam-se os identificadores lógicos em um `SparseIntArray soundMap`:
  - `TARGET_SOUND_ID = 0` $\rightarrow$ `R.raw.target_hit`
  - `CANNON_SOUND_ID = 1` $\rightarrow$ `R.raw.cannon_fire`
  - `BLOCKER_SOUND_ID = 2` $\rightarrow$ `R.raw.blocker_hit`
  - `MISS_SOUND_ID = 3` $\rightarrow$ `R.raw.miss_sound` (recurso extra #3)
- Em `releaseResources()`, invocado por `MainActivityFragment.onDestroy()`, tanto o `SoundPool` quanto o `MediaPlayer` da trilha sonora contínua são liberados com segurança.
