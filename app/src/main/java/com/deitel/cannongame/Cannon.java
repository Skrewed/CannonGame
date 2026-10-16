// Cannon.java
// Representa o canhão e dispara a Cannonball (Capítulo 6, Seção 6.11)
// Aprimorado com modulação de força por Swipe Control (Recurso Extra #5) e acabamento minimalista
package com.deitel.cannongame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * Representa o canhão posicionado na borda esquerda da tela, responsável por
 * alinhar o cano, ajustar a força de disparo e instanciar a Cannonball.
 */
public class Cannon {

    private final int baseRadius; // raio da base do canhão
    private final int barrelLength; // comprimento do cano do canhão
    private final Point barrelEnd = new Point(); // extremidade do cano do canhão
    private double barrelAngle; // ângulo do cano do canhão em radianos
    private Cannonball cannonball; // a Cannonball atual do canhão
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); // Paint para desenhar o canhão
    private final Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint aimLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final CannonView view; // view que contém o canhão

    // Multiplicador de força controlado por gesto horizontal (Recurso Extra #5: 0.6x a 1.6x)
    private float powerMultiplier = 1.0f;
    // Animação suave de recuo do cano ao disparar
    private float recoilOffset = 0.0f;

    // Construtor (conforme Fig. 6.10 do Capítulo 6)
    public Cannon(CannonView view, int baseRadius, int barrelLength, int barrelWidth) {
        this.view = view;
        this.baseRadius = baseRadius;
        this.barrelLength = barrelLength;
        paint.setStrokeWidth(barrelWidth); // define a espessura do cano
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.BLACK);
        accentPaint.setStyle(Paint.Style.FILL);

        aimLinePaint.setStyle(Paint.Style.STROKE);
        aimLinePaint.setStrokeWidth(Math.max(3f, barrelWidth * 0.08f));
        aimLinePaint.setPathEffect(new DashPathEffect(new float[]{18f, 14f}, 0f));

        align(Math.PI / 2); // cano do canhão apontando inicialmente para a direita
    }

    // Alinha o cano do canhão no ângulo fornecido em radianos (Fig. 6.11)
    public void align(double barrelAngle) {
        // Limita o ângulo a um intervalo seguro para evitar disparos puramente verticais na parede
        this.barrelAngle = CollisionUtils.clamp(
                (float) barrelAngle,
                (float) (Math.PI * 0.12),
                (float) (Math.PI * 0.88)
        );
        updateBarrelEnd();
    }

    private void updateBarrelEnd() {
        float effectiveLength = Math.max(barrelLength * 0.65f, barrelLength - recoilOffset);
        barrelEnd.x = (int) (effectiveLength * Math.sin(barrelAngle));
        barrelEnd.y = (int) (-effectiveLength * Math.cos(barrelAngle)) +
                view.getScreenHeight() / 2;
    }

    public double getBarrelAngle() {
        return barrelAngle;
    }

    public float getPowerMultiplier() {
        return powerMultiplier;
    }

    public void setPowerMultiplier(float powerMultiplier) {
        this.powerMultiplier = CollisionUtils.clamp(powerMultiplier, 0.60f, 1.60f);
    }

    public void updateAnimation(double interval) {
        if (recoilOffset > 0f) {
            recoilOffset = Math.max(0f, recoilOffset - (float) (interval * barrelLength * 2.2));
            updateBarrelEnd();
        }
    }

    // Cria e dispara a Cannonball na direção para a qual o Cannon aponta (Fig. 6.12)
    public void fireCannonball() {
        double effectiveSpeedPercent = CannonView.CANNONBALL_SPEED_PERCENT * powerMultiplier;

        // Calcula o componente horizontal X da velocidade da Cannonball
        int velocityX = (int) (effectiveSpeedPercent *
                view.getScreenWidth() * Math.sin(barrelAngle));

        // Calcula o componente vertical Y da velocidade da Cannonball
        int velocityY = (int) (effectiveSpeedPercent *
                view.getScreenWidth() * -Math.cos(barrelAngle));

        // Calcula o raio da Cannonball
        int radius = (int) (view.getScreenHeight() *
                CannonView.CANNONBALL_RADIUS_PERCENT);

        int ballColor = (view.getThemePalette() != null)
                ? view.getThemePalette().getCannonballColor()
                : Color.BLACK;

        // Constrói a Cannonball e a posiciona no canhão
        cannonball = new Cannonball(view, ballColor,
                CannonView.CANNON_SOUND_ID, -radius,
                view.getScreenHeight() / 2 - radius, radius, velocityX,
                velocityY);

        recoilOffset = barrelLength * 0.22f;
        updateBarrelEnd();

        cannonball.playSound(); // reproduz o som de disparo da Cannonball
    }

    // Desenha o Cannon no Canvas (Fig. 6.13)
    public void draw(Canvas canvas) {
        draw(canvas, false);
    }

    // Desenha o Cannon e, opcionalmente, a linha guia de mira preditiva
    public void draw(Canvas canvas, boolean showAimGuide) {
        int centerY = view.getScreenHeight() / 2;
        ThemePalette palette = view.getThemePalette();
        int primaryColor = (palette != null) ? palette.getCannonPrimaryColor() : Color.BLACK;
        int accentColor = (palette != null) ? palette.getCannonAccentColor() : Color.BLUE;

        paint.setColor(primaryColor);
        accentPaint.setColor(accentColor);

        // Linha guia pontilhada durante mira por toque/swipe (Recurso Extra #5)
        if (showAimGuide) {
            aimLinePaint.setColor(Color.argb(
                    135,
                    Color.red(accentColor),
                    Color.green(accentColor),
                    Color.blue(accentColor)
            ));
            float guideLength = view.getScreenWidth() * 0.35f * powerMultiplier;
            float endX = (float) (guideLength * Math.sin(barrelAngle));
            float endY = centerY + (float) (-guideLength * Math.cos(barrelAngle));
            canvas.drawLine(barrelEnd.x, barrelEnd.y, endX, endY, aimLinePaint);
        }

        // Desenha o cano do canhão (Fig. 6.13, linhas 64-65)
        canvas.drawLine(0, centerY, barrelEnd.x, barrelEnd.y, paint);

        // Desenha a base do canhão (Fig. 6.13, linhas 68-69)
        canvas.drawCircle(0, centerY, baseRadius, paint);

        // Anel central de destaque minimalista
        canvas.drawCircle(0, centerY, baseRadius * 0.48f, accentPaint);
    }

    // Retorna a Cannonball disparada por este Cannon (Fig. 6.14)
    public Cannonball getCannonball() {
        return cannonball;
    }

    // Remove a Cannonball do jogo (Fig. 6.14)
    public void removeCannonball() {
        cannonball = null;
    }
}
