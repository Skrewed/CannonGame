// ParticleSystem.java
// Sistema de partículas para explosão de alvos, faíscas no bloqueador, partículas ambientes
// e textos flutuantes de pontuação (Recursos Extras #1, #6 e #8)
package com.deitel.cannongame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Gerencia partículas de explosão radial, partículas ambientes do tema Dia/Noite
 * e indicadores flutuantes de pontuação/tempo quadro a quadro.
 */
public class ParticleSystem {

    private static class Particle {
        float x, y;
        float vx, vy;
        float radius;
        float initialRadius;
        float life;
        float maxLife;
        int baseColor;
    }

    private static class AmbientParticle {
        float x, y;
        float vy;
        float radius;
        float phase;
    }

    private static class FloatingText {
        String text;
        float x, y;
        float vy;
        float life;
        float maxLife;
        int color;
    }

    private final List<Particle> particles = new ArrayList<>();
    private final List<AmbientParticle> ambientParticles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint floatingTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    public ParticleSystem() {
        floatingTextPaint.setFakeBoldText(true);
        floatingTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * Inicializa partículas sutis de ambiente no cenário (Modo Dia / Modo Noturno).
     */
    public void initAmbientParticles(int screenWidth, int screenHeight) {
        ambientParticles.clear();
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }
        int count = 28;
        for (int i = 0; i < count; i++) {
            AmbientParticle ap = new AmbientParticle();
            ap.x = random.nextFloat() * screenWidth;
            ap.y = random.nextFloat() * screenHeight;
            ap.vy = -(12f + random.nextFloat() * 22f);
            ap.radius = 2.0f + random.nextFloat() * 4.5f;
            ap.phase = random.nextFloat() * (float) (Math.PI * 2);
            ambientParticles.add(ap);
        }
    }

    /**
     * Cria uma explosão radial de círculos coloridos quando um alvo é destruído (Recurso Extra #8).
     */
    public void spawnTargetExplosion(float centerX, float centerY, int color, int screenHeight) {
        int count = 24;
        float baseSpeed = screenHeight * 0.38f;
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            double angle = (Math.PI * 2.0 * i / count) + (random.nextDouble() - 0.5) * 0.25;
            float speed = baseSpeed * (0.35f + random.nextFloat() * 0.85f);
            p.x = centerX;
            p.y = centerY;
            p.vx = (float) (Math.cos(angle) * speed);
            p.vy = (float) (Math.sin(angle) * speed);
            p.initialRadius = Math.max(4f, screenHeight * (0.006f + random.nextFloat() * 0.010f));
            p.radius = p.initialRadius;
            p.maxLife = 0.38f + random.nextFloat() * 0.32f;
            p.life = p.maxLife;
            p.baseColor = color;
            particles.add(p);
        }
    }

    /**
     * Cria faíscas direcionais quando a bola de canhão colide com o bloqueador.
     */
    public void spawnBlockerImpact(float x, float y, int color, int screenHeight) {
        int count = 14;
        float baseSpeed = screenHeight * 0.28f;
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            double angle = (Math.PI * 0.65) + random.nextDouble() * (Math.PI * 0.70);
            float speed = baseSpeed * (0.4f + random.nextFloat() * 0.7f);
            p.x = x;
            p.y = y;
            p.vx = (float) (Math.cos(angle) * speed);
            p.vy = (float) (Math.sin(angle) * speed);
            p.initialRadius = Math.max(3.5f, screenHeight * 0.007f);
            p.radius = p.initialRadius;
            p.maxLife = 0.28f + random.nextFloat() * 0.20f;
            p.life = p.maxLife;
            p.baseColor = color;
            particles.add(p);
        }
    }

    /**
     * Adiciona texto flutuante animado (ex.: "+300 pts (2x)" ou "-2.0s").
     */
    public void spawnFloatingText(String text, float x, float y, int color, int screenHeight) {
        FloatingText ft = new FloatingText();
        ft.text = text;
        ft.x = x;
        ft.y = y;
        ft.vy = -screenHeight * 0.12f;
        ft.maxLife = 0.85f;
        ft.life = ft.maxLife;
        ft.color = color;
        floatingTexts.add(ft);
    }

    /**
     * Atualiza todas as partículas ativas e textos flutuantes com base no intervalo (em segundos).
     */
    public void update(double interval, int screenWidth, int screenHeight) {
        float dt = (float) interval;

        // Atualiza partículas de ambiente
        for (AmbientParticle ap : ambientParticles) {
            ap.y += ap.vy * dt;
            ap.phase += dt * 2.2f;
            if (ap.y < -10f && screenHeight > 0) {
                ap.y = screenHeight + 10f;
                ap.x = random.nextFloat() * Math.max(1, screenWidth);
            }
        }

        // Atualiza partículas de explosão (movimento radial + desaceleração + fade out)
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.life -= dt;
            if (p.life <= 0f) {
                it.remove();
                continue;
            }
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            float drag = Math.max(0f, 1.0f - 2.6f * dt);
            p.vx *= drag;
            p.vy *= drag;
            float ratio = p.life / p.maxLife;
            p.radius = p.initialRadius * (0.25f + 0.75f * ratio);
        }

        // Atualiza textos flutuantes
        Iterator<FloatingText> textIt = floatingTexts.iterator();
        while (textIt.hasNext()) {
            FloatingText ft = textIt.next();
            ft.life -= dt;
            if (ft.life <= 0f) {
                textIt.remove();
                continue;
            }
            ft.y += ft.vy * dt;
        }
    }

    /**
     * Desenha partículas de ambiente no fundo do cenário.
     */
    public void drawAmbient(Canvas canvas, ThemePalette palette) {
        int baseColor = palette.getAmbientParticleColor();
        int r = Color.red(baseColor);
        int g = Color.green(baseColor);
        int b = Color.blue(baseColor);
        int baseAlpha = Color.alpha(baseColor);

        for (AmbientParticle ap : ambientParticles) {
            float pulse = 0.55f + 0.45f * (float) Math.sin(ap.phase);
            int alpha = Math.min(255, Math.max(15, (int) (baseAlpha * pulse)));
            particlePaint.setColor(Color.argb(alpha, r, g, b));
            canvas.drawCircle(ap.x, ap.y, ap.radius * pulse, particlePaint);
        }
    }

    /**
     * Desenha as explosões de partículas e textos flutuantes de pontuação.
     */
    public void drawEffects(Canvas canvas, int screenHeight) {
        for (Particle p : particles) {
            float ratio = CollisionUtils.clamp(p.life / p.maxLife, 0f, 1f);
            int alpha = (int) (255 * ratio);
            int r = Color.red(p.baseColor);
            int g = Color.green(p.baseColor);
            int b = Color.blue(p.baseColor);
            particlePaint.setColor(Color.argb(alpha, r, g, b));
            canvas.drawCircle(p.x, p.y, p.radius, particlePaint);
        }

        floatingTextPaint.setTextSize(Math.max(22f, screenHeight * 0.032f));
        for (FloatingText ft : floatingTexts) {
            float ratio = CollisionUtils.clamp(ft.life / ft.maxLife, 0f, 1f);
            int alpha = (int) (255 * ratio);
            int r = Color.red(ft.color);
            int g = Color.green(ft.color);
            int b = Color.blue(ft.color);
            floatingTextPaint.setColor(Color.argb(alpha, r, g, b));
            canvas.drawText(ft.text, ft.x, ft.y, floatingTextPaint);
        }
    }

    public void clear() {
        particles.clear();
        floatingTexts.clear();
    }
}
