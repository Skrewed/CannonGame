// Blocker.java
// Subclasse de GameElement customizada para o Blocker (Capítulo 6, Seção 6.9)
package com.deitel.cannongame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Representa o bloqueador que defende os alvos e impõe uma penalidade de tempo
 * quando atingido pela bola de canhão. Inclui feedback visual de impacto.
 */
public class Blocker extends GameElement {

    private final int missPenalty; // penalidade de tempo ao atingir o Blocker
    private float hitFlashProgress = 0f; // animação suave de brilho ao rebater projétil
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF glowBounds = new RectF();

    // Construtor (conforme Fig. 6.8 do Capítulo 6)
    public Blocker(CannonView view, int color, int missPenalty, int x,
                   int y, int width, int length, float velocityY) {
        super(view, color, CannonView.BLOCKER_SOUND_ID, x, y, width, length, velocityY);
        this.missPenalty = missPenalty;
        this.glowPaint.setStyle(Paint.Style.STROKE);
    }

    // Retorna a penalidade de tempo deste Blocker
    public int getMissPenalty() {
        return missPenalty;
    }

    // Dispara um pulso visual quando atingido pela bola de canhão
    public void triggerHitFlash() {
        this.hitFlashProgress = 1.0f;
    }

    @Override
    public void update(double interval) {
        super.update(interval);
        if (hitFlashProgress > 0f) {
            hitFlashProgress = Math.max(0f, hitFlashProgress - (float) (interval * 3.5));
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (hitFlashProgress > 0f) {
            float expand = 8f * hitFlashProgress;
            glowBounds.set(
                    shape.left - expand,
                    shape.top - expand,
                    shape.right + expand,
                    shape.bottom + expand
            );
            int alpha = (int) (180 * hitFlashProgress);
            glowPaint.setStrokeWidth(4f + 6f * hitFlashProgress);
            glowPaint.setColor(Color.argb(alpha, 255, 100, 120));
            float radius = glowBounds.width() * 0.5f;
            canvas.drawRoundRect(glowBounds, radius, radius, glowPaint);
        }
        super.draw(canvas);
    }
}
