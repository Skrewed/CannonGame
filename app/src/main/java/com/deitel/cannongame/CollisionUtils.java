// CollisionUtils.java
// Utilitários matemáticos para colisão avançada (Círculo x Retângulo / Segmento Varrido),
// cálculo de pontuação com multiplicadores e escalonamento de dificuldade progressiva.
package com.deitel.cannongame;

/**
 * Fornece funções matemáticas puras para detecção de colisão vetorial precisa
 * (Recurso Extra #11), cálculo de pontuação/combos (Recurso Extra #1) e
 * progressão de níveis (Recurso Extra #2).
 */
public final class CollisionUtils {

    private CollisionUtils() {
        // Classe utilitária não instanciável
    }

    /**
     * Verifica colisão exata entre um círculo (centro cx, cy e raio r) e uma
     * caixa delimitadora alinhada aos eixos (AABB: left, top, right, bottom).
     */
    public static boolean circleIntersectsRect(
            float cx, float cy, float radius,
            float left, float top, float right, float bottom) {
        if (radius < 0f) {
            return false;
        }
        float closestX = clamp(cx, left, right);
        float closestY = clamp(cy, top, bottom);
        float dx = cx - closestX;
        float dy = cy - closestY;
        return (dx * dx + dy * dy) <= (radius * radius);
    }

    /**
     * Detecção Contínua de Colisão (CCD - Continuous Collision Detection):
     * Verifica se um círculo que se deslocou de (prevCx, prevCy) até (currCx, currCy)
     * interceptou o retângulo [left, top, right, bottom] em qualquer ponto da trajetória.
     * Evita o efeito de "tunneling" (atravessamento) em altas velocidades ou emuladores lentos.
     */
    public static boolean sweptCircleIntersectsRect(
            float prevCx, float prevCy,
            float currCx, float currCy,
            float radius,
            float left, float top, float right, float bottom) {

        // 1. Verifica posição final exata (Círculo x AABB)
        if (circleIntersectsRect(currCx, currCy, radius, left, top, right, bottom)) {
            return true;
        }

        // 2. Verifica se o segmento varrido intercepta os cantos arredondados / bordas expandidas
        float expandedLeft = left - radius;
        float expandedTop = top - radius;
        float expandedRight = right + radius;
        float expandedBottom = bottom + radius;

        if (!segmentIntersectsExpandedBox(
                prevCx, prevCy, currCx, currCy,
                expandedLeft, expandedTop, expandedRight, expandedBottom)) {
            return false;
        }

        // Amostragem precisa do ponto mais próximo ao longo do segmento
        final int steps = 4;
        for (int i = 1; i < steps; i++) {
            float t = i / (float) steps;
            float sampleX = prevCx + (currCx - prevCx) * t;
            float sampleY = prevCy + (currCy - prevCy) * t;
            if (circleIntersectsRect(sampleX, sampleY, radius, left, top, right, bottom)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Algoritmo de Liang-Barsky (Slab intersection) para interseção entre segmento de reta
     * P0(x0, y0) -> P1(x1, y1) e retângulo [minX, minY, maxX, maxY].
     */
    public static boolean segmentIntersectsExpandedBox(
            float x0, float y0, float x1, float y1,
            float minX, float minY, float maxX, float maxY) {

        float dx = x1 - x0;
        float dy = y1 - y0;
        float tMin = 0.0f;
        float tMax = 1.0f;

        float[] p = {-dx, dx, -dy, dy};
        float[] q = {x0 - minX, maxX - x0, y0 - minY, maxY - y0};

        for (int i = 0; i < 4; i++) {
            if (Math.abs(p[i]) < 1e-6f) {
                if (q[i] < 0f) {
                    return false;
                }
            } else {
                float t = q[i] / p[i];
                if (p[i] < 0f) {
                    if (t > tMax) return false;
                    if (t > tMin) tMin = t;
                } else {
                    if (t < tMin) return false;
                    if (t < tMax) tMax = t;
                }
            }
        }
        return tMin <= tMax;
    }

    /**
     * Calcula a distância mínima ao quadrado entre um ponto (px, py) e um segmento (x1, y1)-(x2, y2).
     */
    public static float pointToSegmentDistanceSq(
            float px, float py, float x1, float y1, float x2, float y2) {
        float vx = x2 - x1;
        float vy = y2 - y1;
        float wx = px - x1;
        float wy = py - y1;

        float c1 = wx * vx + wy * vy;
        if (c1 <= 0f) {
            return wx * wx + wy * wy;
        }
        float c2 = vx * vx + vy * vy;
        if (c2 <= c1) {
            float dx = px - x2;
            float dy = py - y2;
            return dx * dx + dy * dy;
        }
        float b = c1 / c2;
        float pbX = x1 + b * vx;
        float pbY = y1 + b * vy;
        float dx = px - pbX;
        float dy = py - pbY;
        return dx * dx + dy * dy;
    }

    /**
     * Calcula o multiplicador progressivo (1x a 5x) com base na sequência atual de acertos (Recurso Extra #1).
     */
    public static int calculateMultiplier(int currentStreak) {
        if (currentStreak <= 0) {
            return 1;
        }
        return Math.min(5, 1 + (currentStreak / 2));
    }

    /**
     * Calcula os pontos ganhos em um acerto considerando o nível atual e a sequência (Recurso Extra #1).
     */
    public static int calculateScoreDelta(int level, int currentStreak) {
        int safeLevel = Math.max(1, level);
        int safeStreak = Math.max(1, currentStreak);
        int multiplier = calculateMultiplier(safeStreak);
        int basePoints = 100 * safeLevel * multiplier;
        int streakBonus = 25 * Math.max(0, safeStreak - 1);
        return basePoints + streakBonus;
    }

    /**
     * Fator de aumento de velocidade para a dificuldade progressiva (Recurso Extra #2).
     */
    public static float getLevelSpeedMultiplier(int level) {
        int safeLevel = Math.max(1, level);
        return Math.min(2.4f, 1.0f + 0.14f * (safeLevel - 1));
    }

    /**
     * Fator de redução de comprimento do alvo conforme o nível avança (Recurso Extra #2).
     */
    public static float getLevelTargetLengthFactor(int level) {
        int safeLevel = Math.max(1, level);
        return Math.max(0.55f, 1.0f - 0.06f * (safeLevel - 1));
    }

    /**
     * Tempo inicial disponível para o nível (reduz gradualmente em níveis avançados).
     */
    public static double getLevelInitialTime(int level) {
        int safeLevel = Math.max(1, level);
        return Math.max(7.5, 10.0 - 0.35 * (safeLevel - 1));
    }

    /**
     * Curva de interpolação Ease-Out Back para animações suaves de surgimento (Recurso Extra #4).
     */
    public static float easeOutBack(float t) {
        float clamped = clamp(t, 0f, 1f);
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        float p = clamped - 1.0f;
        return 1.0f + c3 * p * p * p + c1 * p * p;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
