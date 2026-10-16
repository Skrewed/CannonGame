// Target.java
// Subclasse de GameElement customizada para o Target (Capítulo 6, Seção 6.10)
// Aprimorada com animações suaves (Fade, Bounce, Scale - Recurso Extra #4)
// e movimentação complexa (Senoidal, Zigue-Zague, Aceleração Progressiva - Recurso Extra #10)
package com.deitel.cannongame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

/**
 * Representa um alvo destrutível que concede bônus de tempo e pontuação ao jogador.
 */
public class Target extends GameElement {

    public enum MovementType {
        LINEAR,
        SINOIDAL,
        ZIGZAG,
        PROGRESSIVE_ACCEL
    }

    private final int hitReward; // recompensa de tempo ao destruir este Target
    private final int targetIndex;
    private final float baseAnchorX;
    private final float horizontalAmplitude;
    private final MovementType movementType;

    // Estado para movimentação complexa (Recurso Extra #10)
    private double elapsedLifeTime = 0.0;
    private final double phaseOffset;
    private final float maxSpeedY;

    // Estado para animações suaves no alvo (Fade, Bounce, Scale - Recurso Extra #4)
    private float spawnDelay;
    private float spawnProgress = 0.0f; // 0.0 -> 1.0 na entrada
    private boolean destroying = false;
    private float destroyProgress = 0.0f; // 0.0 -> 1.0 ao ser atingido
    private final RectF animatedRect = new RectF();

    // Construtor compatível com o Capítulo 6 (Fig. 6.9)
    public Target(CannonView view, int color, int hitReward, int x, int y,
                  int width, int length, float velocityY) {
        this(view, color, hitReward, x, y, width, length, velocityY,
                0, MovementType.LINEAR, 0f);
    }

    // Construtor estendido com suporte aos Recursos Extras #4 e #10
    public Target(CannonView view, int color, int hitReward, int x, int y,
                  int width, int length, float velocityY,
                  int targetIndex, MovementType movementType, float horizontalAmplitude) {
        super(view, color, CannonView.TARGET_SOUND_ID, x, y, width, length, velocityY);
        this.hitReward = hitReward;
        this.targetIndex = targetIndex;
        this.baseAnchorX = x;
        this.movementType = movementType;
        this.horizontalAmplitude = horizontalAmplitude;
        this.phaseOffset = targetIndex * 0.65;
        this.spawnDelay = targetIndex * 0.035f;
        this.maxSpeedY = Math.abs(velocityY) * 1.85f;
    }

    // Retorna a recompensa de tempo deste Target
    public int getHitReward() {
        return hitReward;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    /**
     * Inicia a animação suave de destruição (expansão + fade-out) quando o alvo é atingido.
     */
    public void startDestroyAnimation() {
        this.destroying = true;
        this.destroyProgress = 0.0f;
    }

    public boolean isDestroyAnimationFinished() {
        return destroying && destroyProgress >= 1.0f;
    }

    @Override
    public void update(double interval) {
        float dt = (float) interval;

        // Se estiver animando sua destruição, apenas avança o progresso de saída
        if (destroying) {
            destroyProgress = Math.min(1.0f, destroyProgress + dt * 4.2f);
            return;
        }

        // Animação suave de entrada (Spawn Fade + Bounce Scale)
        if (spawnDelay > 0f) {
            spawnDelay = Math.max(0f, spawnDelay - dt);
        } else if (spawnProgress < 1.0f) {
            spawnProgress = Math.min(1.0f, spawnProgress + dt * 2.8f);
        }

        elapsedLifeTime += interval;

        // Movimentação Complexa (Recurso Extra #10)
        if (movementType == MovementType.PROGRESSIVE_ACCEL) {
            float sign = velocityY >= 0 ? 1f : -1f;
            float nextMag = Math.min(maxSpeedY, Math.abs(velocityY) * (1.0f + 0.25f * dt));
            velocityY = sign * nextMag;
        }

        // Atualiza posição vertical e colisão com as bordas superior/inferior
        super.update(interval);

        // Deslocamento horizontal suave conforme o padrão do alvo
        if (horizontalAmplitude > 0f) {
            double omega = 3.2; // frequência angular
            double angle = omega * elapsedLifeTime + phaseOffset;
            if (movementType == MovementType.SINOIDAL) {
                preciseX = baseAnchorX + (float) (horizontalAmplitude * Math.sin(angle));
            } else if (movementType == MovementType.ZIGZAG) {
                double triangleWave = (2.0 / Math.PI) * Math.asin(Math.sin(angle * 1.25));
                preciseX = baseAnchorX + (float) (horizontalAmplitude * triangleWave);
            }
            syncShapeFromPrecisePosition();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        int alpha = 255;

        if (destroying) {
            // Ao ser atingido: expande e desaparece suavemente (Fade + Scale)
            scaleX = 1.0f + 0.65f * destroyProgress;
            scaleY = 1.0f + 0.28f * destroyProgress;
            alpha = (int) (255 * (1.0f - destroyProgress));
        } else if (spawnProgress < 1.0f) {
            // Ao aparecer: cresce com efeito Bounce (Ease-Out Back) e anima opacidade
            float bounceScale = CollisionUtils.easeOutBack(spawnProgress);
            scaleX = bounceScale;
            scaleY = bounceScale;
            alpha = (int) (255 * CollisionUtils.clamp(spawnProgress * 1.25f, 0f, 1f));
        }

        if (alpha <= 0) {
            return;
        }

        float cx = shape.exactCenterX();
        float cy = shape.exactCenterY();
        float halfW = (shape.width() * 0.5f) * scaleX;
        float halfH = (shape.height() * 0.5f) * scaleY;

        animatedRect.set(cx - halfW, cy - halfH, cx + halfW, cy + halfH);

        int baseColor = paint.getColor();
        paint.setColor(Color.argb(alpha, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)));
        borderPaint.setAlpha(Math.min(110, alpha / 2));

        float cornerRadius = Math.min(animatedRect.width(), animatedRect.height()) * 0.45f;
        canvas.drawRoundRect(animatedRect, cornerRadius, cornerRadius, paint);
        canvas.drawRoundRect(animatedRect, cornerRadius, cornerRadius, borderPaint);

        // Restaura alfa original para manter a cor íntegra
        paint.setColor(Color.argb(255, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)));
    }
}
