// CannonView.java
// Exibe e controla o Cannon Game (Capítulo 6, Seção 6.13 + todos os 11 Recursos Extras)
package com.deitel.cannongame;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Subclasse customizada de SurfaceView que implementa a lógica do Cannon Game,
 * renderização gráfica em thread separada (CannonThread), áudio via SoundPool/MediaPlayer
 * e todos os 11 recursos extras propostos em SUGESTIONS.pdf.
 */
public class CannonView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CannonView"; // para registro de erros no Logcat

    // Constantes de jogabilidade (Fig. 6.20)
    public static final int MISS_PENALTY = 2; // segundos deduzidos ao atingir o bloqueador
    public static final int HIT_REWARD = 3; // segundos adicionados ao destruir um alvo

    // Constantes do Canhão (Cannon)
    public static final double CANNON_BASE_RADIUS_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_WIDTH_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_LENGTH_PERCENT = 1.0 / 10;

    // Constantes da Bola de Canhão (Cannonball)
    public static final double CANNONBALL_RADIUS_PERCENT = 3.0 / 80;
    public static final double CANNONBALL_SPEED_PERCENT = 3.0 / 2;

    // Constantes dos Alvos (Targets)
    public static final double TARGET_WIDTH_PERCENT = 1.0 / 40;
    public static final double TARGET_LENGTH_PERCENT = 3.0 / 20;
    public static final double TARGET_FIRST_X_PERCENT = 3.0 / 5;
    public static final double TARGET_SPACING_PERCENT = 1.0 / 60;
    public static final double TARGET_PIECES = 9;
    public static final double TARGET_MIN_SPEED_PERCENT = 3.0 / 4;
    public static final double TARGET_MAX_SPEED_PERCENT = 6.0 / 4;

    // Constantes do Bloqueador (Blocker)
    public static final double BLOCKER_WIDTH_PERCENT = 1.0 / 40;
    public static final double BLOCKER_LENGTH_PERCENT = 1.0 / 4;
    public static final double BLOCKER_X_PERCENT = 1.0 / 2;
    public static final double BLOCKER_SPEED_PERCENT = 1.0;

    // Tamanho base de texto proporcional à altura da tela
    public static final double TEXT_SIZE_PERCENT = 1.0 / 18;

    private CannonThread cannonThread; // controla o game loop
    private final Activity activity; // para exibir o diálogo de Fim de Jogo na GUI thread
    private boolean dialogIsDisplayed = false;

    // Objetos do jogo
    private Cannon cannon;
    private Blocker blocker;
    private ArrayList<Target> targets;
    private final ArrayList<Target> dyingTargets = new ArrayList<>(); // alvos animando saída (#4)

    // Variáveis de dimensão
    private int screenWidth;
    private int screenHeight;

    // Variáveis do loop de jogo e estatísticas (Fig. 6.20 + Recursos Extras #1 e #2)
    private boolean gameOver;
    private double timeLeft; // tempo restante em segundos
    private int shotsFired; // tiros disparados pelo jogador
    private double totalElapsedTime; // segundos decorridos

    // Estado dos Recursos Extras (#1 Pontuação/Combos, #2 Níveis, #5 Swipe Control)
    private int score = 0;
    private int currentStreak = 0;
    private int maxStreak = 0;
    private int totalTargetsHit = 0;
    private int currentLevel = 1;
    private float hintTimer = 4.5f;

    // Controle de gesto direcional (Swipe Control - Recurso Extra #5)
    private boolean isTouching = false;
    private float touchStartX;
    private float touchStartY;
    private double touchStartAngle;
    private float touchStartPower;
    private boolean draggingGesture = false;

    // Botões interativos desenhados no HUD do Canvas (Modo Dia/Noite e Som)
    private final RectF themeButtonRect = new RectF();
    private final RectF soundButtonRect = new RectF();
    private final RectF hudPillRect = new RectF();

    // Constantes e variáveis para gerenciamento de sons (Fig. 6.20 + Recurso Extra #3)
    public static final int TARGET_SOUND_ID = 0;
    public static final int CANNON_SOUND_ID = 1;
    public static final int BLOCKER_SOUND_ID = 2;
    public static final int MISS_SOUND_ID = 3;
    private SoundPool soundPool; // reproduz efeitos sonoros
    private SparseIntArray soundMap; // mapeia IDs para o SoundPool
    private MediaPlayer backgroundMusicPlayer; // trilha sonora contínua (#3)
    private boolean soundEnabled = true;

    // Subsistemas visuais e de persistência (#6, #8, #9)
    private final ThemePalette themePalette;
    private final ParticleSystem particleSystem = new ParticleSystem();
    private final HighScoreManager highScoreManager;

    // Variáveis Paint utilizadas para desenhar elementos e HUD no Canvas
    private final Paint textPaint;
    private final Paint hudTextPaint;
    private final Paint hudSubTextPaint;
    private final Paint hudPillPaint;
    private final Paint hudBorderPaint;
    private final Paint gridPaint;
    private final Paint backgroundPaint;

    // Construtor (conforme Fig. 6.21 do Capítulo 6)
    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (context instanceof Activity) ? (Activity) context : null;

        // Registra o listener SurfaceHolder.Callback
        getHolder().addCallback(this);

        // Carrega preferências salvas e ranking local (#9)
        highScoreManager = new HighScoreManager(context);
        themePalette = new ThemePalette(highScoreManager.isNightMode());
        soundEnabled = highScoreManager.isSoundEnabled();

        // Configura atributos de áudio para jogo (Seção 6.13.3)
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME);

        // Inicializa o SoundPool para reproduzir os efeitos sonoros do app
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(4);
        builder.setAudioAttributes(attrBuilder.build());
        soundPool = builder.build();

        // Cria o mapa de sons e pré-carrega os arquivos de res/raw
        soundMap = new SparseIntArray(4);
        soundMap.put(TARGET_SOUND_ID,
                soundPool.load(context, R.raw.target_hit, 1));
        soundMap.put(CANNON_SOUND_ID,
                soundPool.load(context, R.raw.cannon_fire, 1));
        soundMap.put(BLOCKER_SOUND_ID,
                soundPool.load(context, R.raw.blocker_hit, 1));
        soundMap.put(MISS_SOUND_ID,
                soundPool.load(context, R.raw.miss_sound, 1));

        initBackgroundMusic(context);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        hudTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        hudSubTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudSubTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));

        hudPillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudPillPaint.setStyle(Paint.Style.FILL);

        hudBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudBorderPaint.setStyle(Paint.Style.STROKE);
        hudBorderPaint.setStrokeWidth(2f);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1.5f);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(themePalette.getBackgroundColor());
    }

    private void initBackgroundMusic(Context context) {
        try {
            backgroundMusicPlayer = MediaPlayer.create(context, R.raw.bg_music);
            if (backgroundMusicPlayer != null) {
                backgroundMusicPlayer.setLooping(true);
                backgroundMusicPlayer.setVolume(0.22f, 0.22f);
            }
        } catch (Exception e) {
            Log.w(TAG, "Não foi possível inicializar a trilha sonora contínua", e);
        }
    }

    private void updateBackgroundMusicState() {
        if (backgroundMusicPlayer == null) {
            return;
        }
        try {
            if (soundEnabled && !gameOver && !dialogIsDisplayed) {
                if (!backgroundMusicPlayer.isPlaying()) {
                    backgroundMusicPlayer.start();
                }
            } else if (backgroundMusicPlayer.isPlaying()) {
                backgroundMusicPlayer.pause();
            }
        } catch (IllegalStateException ignored) {
        }
    }

    // Chamado quando o tamanho da SurfaceView muda (Fig. 6.22)
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        screenWidth = w; // armazena a largura do CannonView
        screenHeight = h; // armazena a altura do CannonView

        // Configura propriedades de texto
        textPaint.setTextSize((int) (TEXT_SIZE_PERCENT * screenHeight));
        textPaint.setAntiAlias(true);

        hudTextPaint.setTextSize(Math.max(24f, screenHeight * 0.034f));
        hudSubTextPaint.setTextSize(Math.max(18f, screenHeight * 0.025f));

        particleSystem.initAmbientParticles(screenWidth, screenHeight);
    }

    // Obtém a largura da tela do jogo (Fig. 6.23)
    public int getScreenWidth() {
        return screenWidth;
    }

    // Obtém a altura da tela do jogo (Fig. 6.23)
    public int getScreenHeight() {
        return screenHeight;
    }

    public ThemePalette getThemePalette() {
        return themePalette;
    }

    // Reproduz um som com o soundId fornecido em soundMap (Fig. 6.23)
    public void playSound(int soundId) {
        if (soundEnabled && soundPool != null && soundMap != null) {
            int sampleId = soundMap.get(soundId);
            if (sampleId != 0) {
                soundPool.play(sampleId, 1, 1, 1, 0, 1f);
            }
        }
    }

    // Reinicia todos os elementos da tela e inicia um novo jogo a partir do Nível 1 (Fig. 6.24)
    public void newGame() {
        startLevel(1, false);
    }

    /**
     * Inicia ou avança para um nível específico (Recurso Extra #2: Dificuldade Progressiva).
     *
     * @param level     Nível a ser iniciado (1, 2, 3...)
     * @param keepStats Se true, preserva a pontuação e estatísticas acumuladas da partida
     */
    public void startLevel(int level, boolean keepStats) {
        this.currentLevel = Math.max(1, level);

        if (!keepStats) {
            this.score = 0;
            this.currentStreak = 0;
            this.maxStreak = 0;
            this.shotsFired = 0;
            this.totalTargetsHit = 0;
            this.totalElapsedTime = 0.0;
        }

        float prevPower = (cannon != null) ? cannon.getPowerMultiplier() : 1.0f;

        // Constrói um novo Cannon (Fig. 6.24, linhas 160-163)
        cannon = new Cannon(this,
                (int) (CANNON_BASE_RADIUS_PERCENT * screenHeight),
                (int) (CANNON_BARREL_LENGTH_PERCENT * screenWidth),
                (int) (CANNON_BARREL_WIDTH_PERCENT * screenHeight));
        cannon.setPowerMultiplier(prevPower);

        Random random = new Random();
        targets = new ArrayList<>();
        dyingTargets.clear();
        particleSystem.clear();

        // Fatores de Dificuldade Progressiva do nível atual (Recurso Extra #2)
        float speedMultiplier = CollisionUtils.getLevelSpeedMultiplier(currentLevel);
        float lengthFactor = CollisionUtils.getLevelTargetLengthFactor(currentLevel);

        // Inicializa targetX para o primeiro Target a partir da esquerda
        int targetX = (int) (TARGET_FIRST_X_PERCENT * screenWidth);

        int targetWidth = (int) (TARGET_WIDTH_PERCENT * screenWidth);
        int targetLength = Math.max(24, (int) (TARGET_LENGTH_PERCENT * screenHeight * lengthFactor));

        // Calcula a coordenada Y inicial dos Targets
        int targetY = (int) (0.5 * screenHeight - targetLength / 2.0);

        float maxHorizontalAmp = (float) (TARGET_SPACING_PERCENT * screenWidth * 0.36);

        // Adiciona TARGET_PIECES (9) Targets à lista de alvos (Fig. 6.24, linhas 176-203)
        for (int n = 0; n < TARGET_PIECES; n++) {

            // Determina uma velocidade aleatória entre min e max escalonada pelo nível
            double velocity = screenHeight * (random.nextDouble() *
                    (TARGET_MAX_SPEED_PERCENT - TARGET_MIN_SPEED_PERCENT) +
                    TARGET_MIN_SPEED_PERCENT) * speedMultiplier;

            // Alterna as cores dos alvos entre os tons do tema atual (Modo Dia / Modo Noite)
            int color = (n % 2 == 0)
                    ? themePalette.getTargetEvenColor()
                    : themePalette.getTargetOddColor();

            // Alterna a direção inicial dos alvos conforme o nível e o índice
            if ((n + currentLevel) % 2 == 0) {
                velocity *= -1;
            }

            // Seleciona o padrão de movimentação complexa (Recurso Extra #10)
            Target.MovementType movementType = selectMovementType(n, currentLevel);
            float amplitude = (movementType == Target.MovementType.SINOIDAL ||
                    movementType == Target.MovementType.ZIGZAG) ? maxHorizontalAmp : 0f;

            // Recompensa de tempo ajustada suavemente pela dificuldade
            int reward = (currentLevel >= 4 && n % 3 == 0) ? Math.max(1, HIT_REWARD - 1) : HIT_REWARD;

            targets.add(new Target(this, color, reward, targetX, targetY,
                    targetWidth, targetLength, (float) velocity,
                    n, movementType, amplitude));

            // Incrementa a coordenada X para posicionar o próximo Target mais à direita
            targetX += (TARGET_WIDTH_PERCENT + TARGET_SPACING_PERCENT) * screenWidth;
        }

        // Cria um novo Blocker (Fig. 6.24, linhas 206-211) com velocidade escalonada suavemente
        float blockerSpeed = (float) (BLOCKER_SPEED_PERCENT * screenHeight *
                (1.0f + 0.08f * (currentLevel - 1)));
        blocker = new Blocker(this, themePalette.getBlockerColor(), MISS_PENALTY,
                (int) (BLOCKER_X_PERCENT * screenWidth),
                (int) ((0.5 - BLOCKER_LENGTH_PERCENT / 2) * screenHeight),
                (int) (BLOCKER_WIDTH_PERCENT * screenWidth),
                (int) (BLOCKER_LENGTH_PERCENT * screenHeight),
                blockerSpeed);

        // Define o tempo inicial (10 segundos no Nível 1, ajustado nos níveis seguintes)
        timeLeft = keepStats
                ? Math.max(10.0, timeLeft + 4.0)
                : CollisionUtils.getLevelInitialTime(currentLevel);

        hintTimer = (currentLevel == 1) ? 4.5f : 2.0f;

        if (gameOver) { // inicia uma nova rodada após o término da anterior
            gameOver = false;
            cannonThread = new CannonThread(getHolder());
            cannonThread.start(); // inicia a thread do game loop
        }

        updateBackgroundMusicState();
        hideSystemBars();
    }

    private Target.MovementType selectMovementType(int targetIndex, int level) {
        if (level == 1) {
            if (targetIndex % 3 == 1) {
                return Target.MovementType.SINOIDAL;
            } else if (targetIndex % 3 == 2) {
                return Target.MovementType.PROGRESSIVE_ACCEL;
            }
            return Target.MovementType.LINEAR;
        }
        int pattern = (targetIndex + level) % 4;
        switch (pattern) {
            case 1:
                return Target.MovementType.SINOIDAL;
            case 2:
                return Target.MovementType.ZIGZAG;
            case 3:
                return Target.MovementType.PROGRESSIVE_ACCEL;
            default:
                return Target.MovementType.LINEAR;
        }
    }

    private void refreshThemeColorsOnElements() {
        backgroundPaint.setColor(themePalette.getBackgroundColor());
        if (blocker != null) {
            blocker.setColor(themePalette.getBlockerColor());
        }
        if (cannon != null && cannon.getCannonball() != null) {
            cannon.getCannonball().setColor(themePalette.getCannonballColor());
        }
        if (targets != null) {
            for (Target t : targets) {
                int c = (t.getTargetIndex() % 2 == 0)
                        ? themePalette.getTargetEvenColor()
                        : themePalette.getTargetOddColor();
                t.setColor(c);
            }
        }
    }

    // Chamado repetidamente pela CannonThread para atualizar os elementos do jogo (Fig. 6.25)
    private void updatePositions(double elapsedTimeMS) {
        double interval = elapsedTimeMS / 1000.0; // converte para segundos

        if (cannon != null) {
            cannon.updateAnimation(interval);
        }

        // Atualiza a posição da bola de canhão se estiver na tela
        if (cannon != null && cannon.getCannonball() != null) {
            cannon.getCannonball().update(interval);
        }

        if (blocker != null) {
            blocker.update(interval); // atualiza a posição do bloqueador
        }

        if (targets != null) {
            for (GameElement target : targets) {
                target.update(interval); // atualiza a posição de cada alvo ativo
            }
        }

        // Atualiza alvos em animação de destruição (Recurso Extra #4)
        Iterator<Target> dyingIt = dyingTargets.iterator();
        while (dyingIt.hasNext()) {
            Target dying = dyingIt.next();
            dying.update(interval);
            if (dying.isDestroyAnimationFinished()) {
                dyingIt.remove();
            }
        }

        // Atualiza partículas e textos flutuantes (Recurso Extra #8)
        particleSystem.update(interval, screenWidth, screenHeight);

        if (hintTimer > 0f) {
            hintTimer = Math.max(0f, hintTimer - (float) interval);
        }

        timeLeft -= interval; // subtrai do tempo restante

        // Se o cronômetro chegou a zero (Fig. 6.25, linhas 243-248)
        if (timeLeft <= 0) {
            timeLeft = 0.0;
            gameOver = true;
            cannonThread.setRunning(false);
            updateBackgroundMusicState();
            showGameOverDialog(R.string.lose, false); // exibe o diálogo de derrota
        }

        // Se todas as peças de alvo foram destruídas (Fig. 6.25, linhas 251-255)
        if (!gameOver && targets != null && targets.isEmpty()) {
            gameOver = true;
            cannonThread.setRunning(false);
            updateBackgroundMusicState();
            showGameOverDialog(R.string.win, true); // exibe o diálogo de vitória
        }
    }

    // Alinha o cano e dispara uma Cannonball se não houver outra na tela (Fig. 6.26)
    public void alignAndFireCannonball(MotionEvent event) {
        Point touchPoint = new Point((int) event.getX(), (int) event.getY());

        // Calcula a distância do toque em relação ao centro vertical da tela
        double centerMinusY = (screenHeight / 2.0 - touchPoint.y);

        // Calcula o ângulo que o cano forma com a vertical superior
        double angle = Math.atan2(Math.max(1, touchPoint.x), centerMinusY);

        // Aponta o cano para o ponto onde a tela foi tocada
        cannon.align(angle);

        // Dispara a Cannonball se não houver outra Cannonball ativa na tela
        fireIfAvailable();
    }

    private void fireIfAvailable() {
        if (cannon != null && (cannon.getCannonball() == null ||
                !cannon.getCannonball().isOnScreen())) {
            cannon.fireCannonball();
            ++shotsFired;
        }
    }

    // Exibe um AlertDialog aprimorado quando o jogo termina (Fig. 6.27 + Recurso Extra #7)
    private void showGameOverDialog(final int messageId, final boolean playerWon) {
        if (activity == null) {
            return;
        }

        final float accuracy = (shotsFired > 0)
                ? (totalTargetsHit * 100.0f / shotsFired)
                : 0.0f;
        final boolean isNewRecord = highScoreManager.recordScore(score, currentLevel, accuracy);
        final List<HighScoreManager.ScoreEntry> topScores = highScoreManager.getTopScores();

        // Na GUI thread, usa o FragmentManager para exibir o DialogFragment
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
                showSystemBars(); // sai do modo imersivo
                dialogIsDisplayed = true;

                GameOverDialogFragment dialogFragment = GameOverDialogFragment.newInstance(
                        messageId,
                        playerWon,
                        score,
                        currentLevel,
                        shotsFired,
                        accuracy,
                        maxStreak,
                        totalElapsedTime,
                        isNewRecord,
                        topScores
                );
                dialogFragment.setAttachView(CannonView.this);
                dialogFragment.setCancelable(false);

                if (activity instanceof FragmentActivity) {
                    dialogFragment.show(
                            ((FragmentActivity) activity).getSupportFragmentManager(),
                            "results"
                    );
                }
            }
        });
    }

    /**
     * DialogFragment aprimorado (Recurso Extra #7) exibindo estatísticas completas,
     * Ranking Local (Top 5), animação suave de entrada e botões de Reiniciar ou Próximo Nível.
     */
    public static class GameOverDialogFragment extends DialogFragment {

        private CannonView cannonView;

        public static GameOverDialogFragment newInstance(
                int titleResId, boolean won, int score, int level,
                int shotsFired, float accuracy, int maxStreak,
                double elapsedSeconds, boolean isNewRecord,
                List<HighScoreManager.ScoreEntry> topScores) {

            GameOverDialogFragment frag = new GameOverDialogFragment();
            Bundle args = new Bundle();
            args.putInt("titleResId", titleResId);
            args.putBoolean("won", won);
            args.putInt("score", score);
            args.putInt("level", level);
            args.putInt("shotsFired", shotsFired);
            args.putFloat("accuracy", accuracy);
            args.putInt("maxStreak", maxStreak);
            args.putDouble("elapsedSeconds", elapsedSeconds);
            args.putBoolean("isNewRecord", isNewRecord);

            ArrayList<String> formattedScores = new ArrayList<>();
            for (int i = 0; i < topScores.size(); i++) {
                HighScoreManager.ScoreEntry e = topScores.get(i);
                formattedScores.add(String.format(
                        Locale.getDefault(),
                        "%dº  —  %d pts  (Nível %d • %.0f%%)",
                        i + 1, e.score, e.level, e.accuracy
                ));
            }
            args.putStringArrayList("topScores", formattedScores);
            frag.setArguments(args);
            return frag;
        }

        public void setAttachView(CannonView view) {
            this.cannonView = view;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = requireArguments();
            int titleResId = args.getInt("titleResId", R.string.lose);
            boolean won = args.getBoolean("won", false);
            int score = args.getInt("score", 0);
            int level = args.getInt("level", 1);
            int shotsFired = args.getInt("shotsFired", 0);
            float accuracy = args.getFloat("accuracy", 0f);
            int maxStreak = args.getInt("maxStreak", 0);
            double elapsedSeconds = args.getDouble("elapsedSeconds", 0.0);
            boolean isNewRecord = args.getBoolean("isNewRecord", false);
            ArrayList<String> topScores = args.getStringArrayList("topScores");

            Context ctx = requireContext();
            LinearLayout root = new LinearLayout(ctx);
            root.setOrientation(LinearLayout.VERTICAL);
            int pad = (int) (20 * ctx.getResources().getDisplayMetrics().density);
            root.setPadding(pad, pad / 2, pad, pad / 4);

            if (isNewRecord) {
                TextView recordBadge = new TextView(ctx);
                recordBadge.setText(R.string.new_record_banner);
                recordBadge.setTextColor(Color.parseColor("#D97706"));
                recordBadge.setTypeface(Typeface.DEFAULT_BOLD);
                recordBadge.setGravity(Gravity.CENTER_HORIZONTAL);
                recordBadge.setPadding(0, 0, 0, pad / 3);
                root.addView(recordBadge);
            }

            TextView statsView = new TextView(ctx);
            statsView.setText(getString(
                    R.string.results_format,
                    score, level, shotsFired, accuracy, maxStreak, elapsedSeconds
            ));
            statsView.setTextSize(15f);
            statsView.setLineSpacing(6f, 1.05f);
            root.addView(statsView);

            TextView rankingTitle = new TextView(ctx);
            rankingTitle.setText(R.string.highscores_header);
            rankingTitle.setTypeface(Typeface.DEFAULT_BOLD);
            rankingTitle.setTextSize(14f);
            rankingTitle.setPadding(0, pad / 2, 0, 6);
            root.addView(rankingTitle);

            TextView rankingBody = new TextView(ctx);
            if (topScores == null || topScores.isEmpty()) {
                rankingBody.setText(R.string.no_highscores_yet);
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < topScores.size(); i++) {
                    if (i > 0) sb.append("\n");
                    sb.append(topScores.get(i));
                }
                rankingBody.setText(sb.toString());
            }
            rankingBody.setTextSize(13f);
            root.addView(rankingBody);

            // Animação suave de entrada do painel de fim de jogo (Recurso Extra #7)
            root.setScaleX(0.86f);
            root.setScaleY(0.86f);
            root.setAlpha(0f);
            root.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(320)
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(getString(titleResId));
            builder.setView(root);

            builder.setPositiveButton(R.string.reset_game, (dialog, which) -> {
                if (cannonView != null) {
                    cannonView.dialogIsDisplayed = false;
                    cannonView.newGame();
                }
            });

            if (won) {
                int nextLvl = level + 1;
                builder.setNeutralButton(getString(R.string.next_level, nextLvl), (dialog, which) -> {
                    if (cannonView != null) {
                        cannonView.dialogIsDisplayed = false;
                        cannonView.startLevel(nextLvl, true);
                    }
                });
            }

            return builder.create();
        }
    }

    // Desenha o jogo no Canvas fornecido (Fig. 6.28)
    public void drawGameElements(Canvas canvas) {
        if (canvas == null) {
            return;
        }

        // 1. Limpa o fundo com a cor do tema ativo (Modo Dia / Modo Noturno)
        backgroundPaint.setColor(themePalette.getBackgroundColor());
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

        // 2. Desenha grade geométrica minimalista sutil e partículas de ambiente (#6)
        drawMinimalistBackdrop(canvas);
        particleSystem.drawAmbient(canvas, themePalette);

        // 3. Desenha o Blocker
        if (blocker != null) {
            blocker.draw(canvas);
        }

        // 4. Desenha todos os Targets ativos e em animação de saída (#4)
        if (targets != null) {
            for (GameElement target : targets) {
                target.draw(canvas);
            }
        }
        for (Target dying : dyingTargets) {
            dying.draw(canvas);
        }

        // 5. Desenha a Cannonball (se estiver na tela) e o Cannon
        if (cannon != null) {
            if (cannon.getCannonball() != null && cannon.getCannonball().isOnScreen()) {
                cannon.getCannonball().draw(canvas);
            }
            cannon.draw(canvas, isTouching);
        }

        // 6. Desenha explosões de partículas e textos flutuantes (#8)
        particleSystem.drawEffects(canvas, screenHeight);

        // 7. Desenha o HUD minimalista moderno em Português do Brasil (#1, #5, #6)
        drawMinimalistHud(canvas);
    }

    private void drawMinimalistBackdrop(Canvas canvas) {
        gridPaint.setColor(themePalette.getGridLineColor());
        int step = Math.max(60, screenHeight / 8);
        for (int x = step; x < screenWidth; x += step) {
            canvas.drawLine(x, 0, x, screenHeight, gridPaint);
        }
        for (int y = step; y < screenHeight; y += step) {
            canvas.drawLine(0, y, screenWidth, y, gridPaint);
        }
    }

    private void drawMinimalistHud(Canvas canvas) {
        float margin = Math.max(16f, screenWidth * 0.018f);
        float pillHeight = Math.max(44f, screenHeight * 0.078f);
        float topY = margin;
        float corner = pillHeight * 0.5f;

        hudPillPaint.setColor(themePalette.getHudSurfaceColor());
        hudBorderPaint.setColor(themePalette.getHudBorderColor());
        hudTextPaint.setColor(themePalette.getTextPrimaryColor());
        hudSubTextPaint.setColor(themePalette.getTextSecondaryColor());

        // Pill 1: Tempo Restante (fica vermelho quando <= 3.0s)
        String timeText = getResources().getString(R.string.time_remaining_format, timeLeft);
        float timeWidth = hudTextPaint.measureText(timeText) + margin * 2.2f;
        hudPillRect.set(margin, topY, margin + timeWidth, topY + pillHeight);
        canvas.drawRoundRect(hudPillRect, corner, corner, hudPillPaint);
        canvas.drawRoundRect(hudPillRect, corner, corner, hudBorderPaint);

        if (timeLeft <= 3.0) {
            hudTextPaint.setColor(themePalette.getWarningColor());
        }
        float textBaseline = hudPillRect.centerY() - ((hudTextPaint.descent() + hudTextPaint.ascent()) / 2f);
        canvas.drawText(timeText, hudPillRect.left + margin * 1.1f, textBaseline, hudTextPaint);
        hudTextPaint.setColor(themePalette.getTextPrimaryColor());

        // Pill 2: Pontuação + Multiplicador de Combo (#1)
        int multiplier = CollisionUtils.calculateMultiplier(currentStreak);
        String scoreText = getResources().getString(R.string.score_hud_format, score);
        if (multiplier > 1) {
            scoreText += "  (" + getResources().getString(R.string.combo_hud_format, multiplier) + ")";
        }
        float scoreLeft = hudPillRect.right + margin * 0.75f;
        float scoreWidth = hudTextPaint.measureText(scoreText) + margin * 2.2f;
        hudPillRect.set(scoreLeft, topY, scoreLeft + scoreWidth, topY + pillHeight);
        canvas.drawRoundRect(hudPillRect, corner, corner, hudPillPaint);
        canvas.drawRoundRect(hudPillRect, corner, corner, hudBorderPaint);
        if (multiplier > 1) {
            hudTextPaint.setColor(themePalette.getAccentBadgeColor());
        }
        canvas.drawText(scoreText, hudPillRect.left + margin * 1.1f, textBaseline, hudTextPaint);
        hudTextPaint.setColor(themePalette.getTextPrimaryColor());

        // Pill 3: Nível Atual + Alvos Restantes (#2)
        int remainingTargets = (targets != null) ? targets.size() : 0;
        String levelText = getResources().getString(
                R.string.level_hud_format,
                currentLevel, remainingTargets, (int) TARGET_PIECES
        );
        float levelLeft = hudPillRect.right + margin * 0.75f;
        float levelWidth = hudSubTextPaint.measureText(levelText) + margin * 2.0f;
        hudPillRect.set(levelLeft, topY, levelLeft + levelWidth, topY + pillHeight);
        canvas.drawRoundRect(hudPillRect, corner, corner, hudPillPaint);
        canvas.drawRoundRect(hudPillRect, corner, corner, hudBorderPaint);
        float subBaseline = hudPillRect.centerY() - ((hudSubTextPaint.descent() + hudSubTextPaint.ascent()) / 2f);
        canvas.drawText(levelText, hudPillRect.left + margin, subBaseline, hudSubTextPaint);

        // Botões interativos à direita: Som (#3) e Modo Dia/Noite (#6)
        String soundLabel = getResources().getString(
                soundEnabled ? R.string.sound_on_label : R.string.sound_off_label
        );
        float soundBtnWidth = hudSubTextPaint.measureText(soundLabel) + margin * 2.0f;
        float soundRight = screenWidth - margin;
        soundButtonRect.set(soundRight - soundBtnWidth, topY, soundRight, topY + pillHeight);
        canvas.drawRoundRect(soundButtonRect, corner, corner, hudPillPaint);
        canvas.drawRoundRect(soundButtonRect, corner, corner, hudBorderPaint);
        canvas.drawText(soundLabel, soundButtonRect.left + margin, subBaseline, hudSubTextPaint);

        String themeLabel = getResources().getString(
                themePalette.isNightMode() ? R.string.theme_night_label : R.string.theme_day_label
        );
        float themeBtnWidth = hudSubTextPaint.measureText(themeLabel) + margin * 2.0f;
        float themeRight = soundButtonRect.left - margin * 0.6f;
        themeButtonRect.set(themeRight - themeBtnWidth, topY, themeRight, topY + pillHeight);
        canvas.drawRoundRect(themeButtonRect, corner, corner, hudPillPaint);
        canvas.drawRoundRect(themeButtonRect, corner, corner, hudBorderPaint);
        canvas.drawText(themeLabel, themeButtonRect.left + margin, subBaseline, hudSubTextPaint);

        // Indicador de Força de Disparo e Recorde no rodapé esquerdo (#5, #9)
        if (cannon != null) {
            int powerPct = Math.round(cannon.getPowerMultiplier() * 100f);
            int best = Math.max(score, highScoreManager.getBestScore());
            String bottomInfo = getResources().getString(R.string.power_hud_format, powerPct)
                    + "   •   "
                    + getResources().getString(R.string.record_hud_format, best);
            canvas.drawText(bottomInfo, margin, screenHeight - margin * 1.1f, hudSubTextPaint);
        }

        // Dica minimalista temporária de controles por gesto no início da partida
        if (hintTimer > 0f) {
            int alpha = (int) (210 * Math.min(1f, hintTimer));
            hudSubTextPaint.setAlpha(alpha);
            String hint = getResources().getString(R.string.swipe_hint);
            float hintW = hudSubTextPaint.measureText(hint);
            canvas.drawText(hint, (screenWidth - hintW) * 0.5f, screenHeight - margin * 1.1f, hudSubTextPaint);
            hudSubTextPaint.setAlpha(255);
        }
    }

    // Verifica se a bola colide com o Blocker ou com qualquer um dos Targets (Fig. 6.29)
    public void testForCollisions() {
        Cannonball ball = (cannon != null) ? cannon.getCannonball() : null;

        // Remove qualquer um dos alvos com o qual a Cannonball colidir
        if (ball != null && ball.isOnScreen()) {
            for (int n = 0; n < targets.size(); n++) {
                Target hitTarget = targets.get(n);
                if (ball.collidesWith(hitTarget)) {
                    hitTarget.playSound(); // reproduz som de acerto no Target

                    // Adiciona a recompensa de tempo ao tempo restante
                    timeLeft += hitTarget.getHitReward();

                    // Atualiza sequência (combo), multiplicador e pontuação (Recurso Extra #1)
                    currentStreak++;
                    maxStreak = Math.max(maxStreak, currentStreak);
                    totalTargetsHit++;
                    int gainedPoints = CollisionUtils.calculateScoreDelta(currentLevel, currentStreak);
                    score += gainedPoints;
                    int mult = CollisionUtils.calculateMultiplier(currentStreak);

                    // Explosão de partículas e texto flutuante (Recurso Extra #8)
                    float hitX = hitTarget.getShape().exactCenterX();
                    float hitY = hitTarget.getShape().exactCenterY();
                    particleSystem.spawnTargetExplosion(hitX, hitY, hitTarget.getColor(), screenHeight);
                    particleSystem.spawnFloatingText(
                            String.format(Locale.getDefault(), "+%d pts (%dx)  +%ds",
                                    gainedPoints, mult, hitTarget.getHitReward()),
                            hitX - screenWidth * 0.05f,
                            hitY,
                            hitTarget.getColor(),
                            screenHeight
                    );

                    // Inicia animação suave de destruição no alvo (Recurso Extra #4)
                    hitTarget.startDestroyAnimation();
                    dyingTargets.add(hitTarget);

                    cannon.removeCannonball(); // remove a Cannonball do jogo
                    targets.remove(n); // remove o Target atingido da lista ativa
                    --n; // garante que não pularemos o teste do novo alvo na posição n

                    // Pequeno incremento dinâmico de velocidade nos alvos restantes (Recurso Extra #2)
                    for (Target remaining : targets) {
                        remaining.setVelocityY(remaining.getVelocityY() * 1.03f);
                    }
                    break;
                }
            }
        } else if (ball != null && !ball.isOnScreen()) {
            // A bola saiu da tela sem acertar um alvo: reseta o combo e toca som de falha (#1, #3)
            if (ball.getVelocityX() > 0) {
                currentStreak = 0;
                playSound(MISS_SOUND_ID);
            }
            cannon.removeCannonball();
        }

        // Verifica se a bola colide com o Blocker (Fig. 6.29, linhas 380-389)
        ball = (cannon != null) ? cannon.getCannonball() : null;
        if (ball != null && blocker != null && ball.collidesWith(blocker)) {
            blocker.playSound(); // reproduz som de impacto no Blocker
            blocker.triggerHitFlash();

            // Inverte a direção horizontal da bola
            ball.reverseVelocityX();

            // Deduz a penalidade do Blocker do tempo restante e zera a sequência de combo
            timeLeft = Math.max(0.0, timeLeft - blocker.getMissPenalty());
            currentStreak = 0;

            particleSystem.spawnBlockerImpact(
                    blocker.getShape().left,
                    ball.getCenterY(),
                    blocker.getColor(),
                    screenHeight
            );
            particleSystem.spawnFloatingText(
                    String.format(Locale.getDefault(), "-%d.0 s", blocker.getMissPenalty()),
                    blocker.getShape().exactCenterX(),
                    ball.getCenterY(),
                    themePalette.getWarningColor(),
                    screenHeight
            );
        }
    }

    // Interrompe o jogo: chamado pelo método onPause do MainActivityFragment (Fig. 6.30)
    public void stopGame() {
        if (cannonThread != null) {
            cannonThread.setRunning(false); // solicita o encerramento da thread
        }
        if (backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
            try {
                backgroundMusicPlayer.pause();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    // Libera recursos: chamado pelo método onDestroy do MainActivityFragment (Fig. 6.30)
    public void releaseResources() {
        if (soundPool != null) {
            soundPool.release(); // libera todos os recursos utilizados pelo SoundPool
            soundPool = null;
        }
        if (backgroundMusicPlayer != null) {
            try {
                backgroundMusicPlayer.release();
            } catch (Exception ignored) {
            }
            backgroundMusicPlayer = null;
        }
    }

    // Chamado quando a superfície muda de tamanho (Fig. 6.31)
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    // Chamado quando a superfície é criada pela primeira vez (Fig. 6.31)
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (!dialogIsDisplayed) {
            newGame(); // configura e inicia um novo jogo
            cannonThread = new CannonThread(holder); // cria a thread
            cannonThread.setRunning(true); // inicia a execução do jogo
            cannonThread.start(); // inicia a thread do game loop
        }
    }

    // Chamado quando a superfície é destruída (Fig. 6.31)
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        // Garante que a thread seja encerrada adequadamente
        boolean retry = true;
        if (cannonThread != null) {
            cannonThread.setRunning(false); // encerra a cannonThread

            while (retry) {
                try {
                    cannonThread.join(); // aguarda a finalização da cannonThread
                    retry = false;
                } catch (InterruptedException e) {
                    Log.e(TAG, "Thread interrompida", e);
                }
            }
        }
    }

    // Chamado quando o usuário toca na tela (Fig. 6.32 + Recurso Extra #5 Swipe Control)
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        float x = e.getX();
        float y = e.getY();

        // Verifica cliques nos botões interativos do HUD (Modo Dia/Noite e Som)
        if (action == MotionEvent.ACTION_DOWN) {
            if (themeButtonRect.contains(x, y)) {
                themePalette.toggle();
                highScoreManager.setNightMode(themePalette.isNightMode());
                refreshThemeColorsOnElements();
                return true;
            }
            if (soundButtonRect.contains(x, y)) {
                soundEnabled = !soundEnabled;
                highScoreManager.setSoundEnabled(soundEnabled);
                updateBackgroundMusicState();
                return true;
            }

            isTouching = true;
            draggingGesture = false;
            touchStartX = x;
            touchStartY = y;

            // Alinha imediatamente para o ponto tocado
            if (cannon != null) {
                double centerMinusY = (screenHeight / 2.0 - y);
                double angle = Math.atan2(Math.max(1f, x), centerMinusY);
                cannon.align(angle);
                touchStartAngle = cannon.getBarrelAngle();
                touchStartPower = cannon.getPowerMultiplier();
            }

            // Dispara imediatamente se não houver bola ativa (fiel ao comportamento responsivo do Cap. 6)
            alignAndFireCannonball(e);
            return true;
        }

        // Controle por Toque Direcional (Swipe Control - Recurso Extra #5)
        if (action == MotionEvent.ACTION_MOVE && cannon != null) {
            float dx = x - touchStartX;
            float dy = y - touchStartY;
            float dist = (float) Math.hypot(dx, dy);

            if (dist > 18f) {
                draggingGesture = true;
                // Deslizar para cima/baixo ajusta o ângulo; deslizar para os lados ajusta a força
                double deltaAngle = (dy / Math.max(1f, screenHeight * 0.55f)) * (Math.PI * 0.45);
                cannon.align(touchStartAngle + deltaAngle);

                float deltaPower = dx / Math.max(1f, screenWidth * 0.45f);
                cannon.setPowerMultiplier(touchStartPower + deltaPower);

                // Se a bola anterior já saiu da tela durante o arraste, dispara na nova direção
                fireIfAvailable();
            } else {
                alignAndFireCannonball(e);
            }
            return true;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (draggingGesture) {
                fireIfAvailable();
            }
            isTouching = false;
            draggingGesture = false;
            performClick();
            return true;
        }

        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    // Subclasse de Thread para controlar o loop do jogo (Fig. 6.33)
    private class CannonThread extends Thread {
        private final SurfaceHolder surfaceHolder; // para manipulação do Canvas
        private volatile boolean threadIsRunning = true; // executando por padrão

        // Inicializa o SurfaceHolder
        public CannonThread(SurfaceHolder holder) {
            surfaceHolder = holder;
            setName("CannonThread");
        }

        // Altera o estado de execução
        public void setRunning(boolean running) {
            threadIsRunning = running;
        }

        // Controla o game loop (Fig. 6.33, linhas 471-499)
        @Override
        public void run() {
            Canvas canvas = null;
            long previousFrameTime = System.currentTimeMillis();

            while (threadIsRunning) {
                try {
                    // Obtém o Canvas para desenho exclusivo a partir desta thread
                    canvas = surfaceHolder.lockCanvas(null);

                    // Bloqueia o surfaceHolder para desenho sincronizado
                    synchronized (surfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;
                        totalElapsedTime += elapsedTimeMS / 1000.0;
                        updatePositions(elapsedTimeMS); // atualiza o estado do jogo
                        testForCollisions(); // testa colisões entre GameElements
                        if (canvas != null) {
                            drawGameElements(canvas); // desenha utilizando o Canvas
                        }
                        previousFrameTime = currentTime; // atualiza o tempo do quadro anterior
                    }
                } finally {
                    // Exibe o conteúdo do Canvas no CannonView e libera para outras threads
                    if (canvas != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        } catch (IllegalStateException e) {
                            Log.w(TAG, "Superfície já liberada ao postar Canvas", e);
                        }
                    }
                }
            }
        }
    }

    // Oculta as barras de sistema e coloca o app em modo imersivo (Fig. 6.34)
    @SuppressWarnings("deprecation")
    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // Exibe as barras de sistema ao sair do modo imersivo (Fig. 6.34)
    @SuppressWarnings("deprecation")
    private void showSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }
}
