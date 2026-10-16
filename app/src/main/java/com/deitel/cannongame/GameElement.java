// GameElement.java
// Representa um elemento de jogo delimitado por um retângulo (Capítulo 6, Seção 6.8)
package com.deitel.cannongame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Superclasse para Blocker, Target e Cannonball, contendo os dados e comportamentos
 * comuns de movimentação vertical, renderização em Canvas e reprodução de som.
 */
public class GameElement {

    protected CannonView view; // a view que contém este GameElement
    protected Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); // Paint para desenhar este GameElement
    protected Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // realce sutil minimalista
    protected Rect shape; // limites retangulares do GameElement
    protected final RectF drawBounds = new RectF();

    // Coordenadas em ponto flutuante para preservar precisão em altas taxas de atualização
    protected float preciseX;
    protected float preciseY;
    protected final int width;
    protected final int length;

    protected float velocityY; // velocidade vertical deste GameElement
    private final int soundId; // o som associado a este GameElement

    // Construtor público (conforme Fig. 6.7 do Capítulo 6)
    public GameElement(CannonView view, int color, int soundId, int x,
                       int y, int width, int length, float velocityY) {
        this.view = view;
        this.paint.setColor(color);
        this.borderPaint.setStyle(Paint.Style.STROKE);
        this.borderPaint.setStrokeWidth(2.5f);
        this.borderPaint.setColor(Color.argb(90, 255, 255, 255));

        this.shape = new Rect(x, y, x + width, y + length); // define limites
        this.preciseX = x;
        this.preciseY = y;
        this.width = width;
        this.length = length;
        this.soundId = soundId;
        this.velocityY = velocityY;
    }

    // Atualiza a posição do GameElement e verifica colisões com as bordas superior/inferior
    public void update(double interval) {
        float dy = (float) (velocityY * interval);
        preciseY += dy;
        syncShapeFromPrecisePosition();

        // Se este GameElement colidir com a parede superior ou inferior, inverte a direção
        int screenHeight = view != null ? view.getScreenHeight() : 0;
        if (preciseY < 0 && velocityY < 0) {
            preciseY = 0;
            velocityY *= -1;
            syncShapeFromPrecisePosition();
        } else if (screenHeight > 0 && (preciseY + length) > screenHeight && velocityY > 0) {
            preciseY = screenHeight - length;
            velocityY *= -1;
            syncShapeFromPrecisePosition();
        }
    }

    protected void syncShapeFromPrecisePosition() {
        int left = Math.round(preciseX);
        int top = Math.round(preciseY);
        shape.set(left, top, left + width, top + length);
    }

    // Desenha este GameElement no Canvas fornecido (com cantos arredondados minimalistas)
    public void draw(Canvas canvas) {
        drawBounds.set(shape);
        float cornerRadius = Math.min(drawBounds.width(), drawBounds.height()) * 0.45f;
        canvas.drawRoundRect(drawBounds, cornerRadius, cornerRadius, paint);
        canvas.drawRoundRect(drawBounds, cornerRadius, cornerRadius, borderPaint);
    }

    // Reproduz o som correspondente a este tipo de GameElement
    public void playSound() {
        if (view != null) {
            view.playSound(soundId);
        }
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public int getColor() {
        return paint.getColor();
    }

    public Rect getShape() {
        return shape;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }
}
