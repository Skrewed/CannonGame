// Cannonball.java
// Representa a bola de canhão disparada pelo Cannon (Capítulo 6, Seção 6.12)
// Aprimorada com Colisão Avançada (Círculo x Retângulo / Segmento Varrido - Recurso Extra #11)
package com.deitel.cannongame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Representa o projétil circular disparado pelo canhão, com suporte a detecção
 * contínua de colisão (CCD) e rastro visual minimalista.
 */
public class Cannonball extends GameElement {

    private float velocityX;
    private boolean onScreen;

    // Posição do centro no frame anterior para detecção contínua de colisão (CCD)
    private float prevCenterX;
    private float prevCenterY;

    // Histórico curto de posições para desenhar rastro suave (motion trail)
    private static final int TRAIL_LENGTH = 6;
    private final float[] trailX = new float[TRAIL_LENGTH];
    private final float[] trailY = new float[TRAIL_LENGTH];
    private int trailCount = 0;
    private final Paint trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint corePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Construtor (conforme Fig. 6.15 do Capítulo 6)
    public Cannonball(CannonView view, int color, int soundId, int x,
                      int y, int radius, float velocityX, float velocityY) {
        super(view, color, soundId, x, y, 2 * radius, 2 * radius, velocityY);
        this.velocityX = velocityX;
        this.onScreen = true;
        this.prevCenterX = x + radius;
        this.prevCenterY = y + radius;
        this.corePaint.setColor(Color.WHITE);
    }

    // Obtém o raio da Cannonball (Fig. 6.16)
    public int getRadius() {
        return (shape.right - shape.left) / 2;
    }

    public float getCenterX() {
        return preciseX + getRadius();
    }

    public float getCenterY() {
        return preciseY + getRadius();
    }

    // Testa se a Cannonball colide com o GameElement fornecido usando Colisão Avançada (Recurso Extra #11)
    public boolean collidesWith(GameElement element) {
        if (element == null || velocityX <= 0f) {
            return false;
        }
        Rect targetRect = element.shape;
        float radius = getRadius();
        float currCx = getCenterX();
        float currCy = getCenterY();

        return CollisionUtils.sweptCircleIntersectsRect(
                prevCenterX, prevCenterY,
                currCx, currCy,
                radius,
                targetRect.left, targetRect.top, targetRect.right, targetRect.bottom
        );
    }

    // Retorna true se esta Cannonball estiver na tela
    public boolean isOnScreen() {
        return onScreen;
    }

    // Inverte a velocidade horizontal da Cannonball (quando rebate no Blocker)
    public void reverseVelocityX() {
        velocityX *= -1;
    }

    public float getVelocityX() {
        return velocityX;
    }

    // Atualiza a posição da Cannonball (Fig. 6.17)
    @Override
    public void update(double interval) {
        prevCenterX = getCenterX();
        prevCenterY = getCenterY();

        // Registra posição atual no rastro visual
        for (int i = Math.min(trailCount, TRAIL_LENGTH - 1); i > 0; i--) {
            trailX[i] = trailX[i - 1];
            trailY[i] = trailY[i - 1];
        }
        trailX[0] = prevCenterX;
        trailY[0] = prevCenterY;
        if (trailCount < TRAIL_LENGTH) {
            trailCount++;
        }

        // Atualiza posição vertical (e quique nas paredes superior/inferior via super.update)
        super.update(interval);

        // Atualiza posição horizontal com precisão em ponto flutuante
        preciseX += (float) (velocityX * interval);
        syncShapeFromPrecisePosition();

        // Verifica se a Cannonball saiu dos limites laterais ou verticais da tela
        int screenWidth = view != null ? view.getScreenWidth() : 0;
        int screenHeight = view != null ? view.getScreenHeight() : 0;
        if (shape.top < 0 || shape.left < 0 ||
                (screenHeight > 0 && shape.bottom > screenHeight) ||
                (screenWidth > 0 && shape.right > screenWidth)) {
            onScreen = false; // marca para remoção
        }
    }

    // Desenha a Cannonball e seu rastro no Canvas fornecido (Fig. 6.18)
    @Override
    public void draw(Canvas canvas) {
        int radius = getRadius();
        float cx = shape.left + radius;
        float cy = shape.top + radius;

        int glowColor = (view != null && view.getThemePalette() != null)
                ? view.getThemePalette().getCannonballGlowColor()
                : paint.getColor();
        int r = Color.red(glowColor);
        int g = Color.green(glowColor);
        int b = Color.blue(glowColor);

        // Desenha o rastro de movimento (motion trail)
        for (int i = 0; i < trailCount; i++) {
            float factor = (float) (TRAIL_LENGTH - i) / (TRAIL_LENGTH + 1);
            int alpha = (int) (110 * factor);
            trailPaint.setColor(Color.argb(alpha, r, g, b));
            canvas.drawCircle(trailX[i], trailY[i], radius * factor * 0.85f, trailPaint);
        }

        // Halo externo sutil
        trailPaint.setColor(Color.argb(75, r, g, b));
        canvas.drawCircle(cx, cy, radius * 1.25f, trailPaint);

        // Círculo principal da bola de canhão (conforme Capítulo 6)
        canvas.drawCircle(cx, cy, radius, paint);

        // Brilho central minimalista
        corePaint.setColor(Color.argb(140, 255, 255, 255));
        canvas.drawCircle(cx - radius * 0.25f, cy - radius * 0.25f, radius * 0.32f, corePaint);
    }
}
