package com.deitel.cannongame;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes unitários automatizados para validar:
 * 1) Detecção de colisão avançada (Círculo x Retângulo / Segmento Varrido - Recurso Extra #11);
 * 2) Sistema de pontuação, sequência de combos e multiplicadores progressivos (Recurso Extra #1);
 * 3) Escalonamento de dificuldade progressiva por níveis (Recurso Extra #2);
 * 4) Funções de interpolação suave (Ease-Out Back - Recurso Extra #4).
 */
public class CannonGameLogicTest {

    @Test
    public void testCircleIntersectsRect_DirectHitAndCorner() {
        // Retângulo de (100, 100) até (140, 200)
        assertTrue(CollisionUtils.circleIntersectsRect(95f, 150f, 10f, 100f, 100f, 140f, 200f));
        assertFalse(CollisionUtils.circleIntersectsRect(85f, 150f, 10f, 100f, 100f, 140f, 200f));

        // Colisão exata no canto superior esquerdo (distância de (94, 94) até (100, 100) é sqrt(72) ~ 8.48 <= 10)
        assertTrue(CollisionUtils.circleIntersectsRect(94f, 94f, 10f, 100f, 100f, 140f, 200f));
        // Fora do canto (distância de (90, 90) até (100, 100) é sqrt(200) ~ 14.14 > 10)
        assertFalse(CollisionUtils.circleIntersectsRect(90f, 90f, 10f, 100f, 100f, 140f, 200f));
    }

    @Test
    public void testSweptCircleIntersectsRect_PreventsTunneling() {
        // Simula projétil rápido que estava em X=80 no frame anterior e pulou para X=160 no frame atual,
        // atravessando um alvo fino em X=[115, 125], Y=[100, 200]
        boolean collided = CollisionUtils.sweptCircleIntersectsRect(
                80f, 150f,
                160f, 150f,
                8f,
                115f, 100f, 125f, 200f
        );
        assertTrue("A detecção contínua (CCD) deve detectar colisão mesmo quando a bola atravessa o alvo entre frames", collided);

        // Trajetória que passa acima do alvo (Y=50) não deve colidir
        boolean missedAbove = CollisionUtils.sweptCircleIntersectsRect(
                80f, 50f,
                160f, 50f,
                8f,
                115f, 100f, 125f, 200f
        );
        assertFalse(missedAbove);
    }

    @Test
    public void testScoringAndMultipliers() {
        assertEquals(1, CollisionUtils.calculateMultiplier(0));
        assertEquals(1, CollisionUtils.calculateMultiplier(1));
        assertEquals(2, CollisionUtils.calculateMultiplier(2));
        assertEquals(2, CollisionUtils.calculateMultiplier(3));
        assertEquals(3, CollisionUtils.calculateMultiplier(4));
        assertEquals(5, CollisionUtils.calculateMultiplier(10)); // limite máximo 5x

        // Nível 1, 1º acerto (streak 1 -> mult 1x): 100 * 1 * 1 + 0 = 100
        assertEquals(100, CollisionUtils.calculateScoreDelta(1, 1));
        // Nível 1, 2º acerto seguido (streak 2 -> mult 2x): 100 * 1 * 2 + 25 = 225
        assertEquals(225, CollisionUtils.calculateScoreDelta(1, 2));
        // Nível 2, 4º acerto seguido (streak 4 -> mult 3x): 100 * 2 * 3 + 75 = 675
        assertEquals(675, CollisionUtils.calculateScoreDelta(2, 4));
    }

    @Test
    public void testProgressiveDifficultyScaling() {
        float speedL1 = CollisionUtils.getLevelSpeedMultiplier(1);
        float speedL3 = CollisionUtils.getLevelSpeedMultiplier(3);
        assertTrue(speedL3 > speedL1);

        float lenL1 = CollisionUtils.getLevelTargetLengthFactor(1);
        float lenL4 = CollisionUtils.getLevelTargetLengthFactor(4);
        assertTrue(lenL4 < lenL1);
        assertTrue(CollisionUtils.getLevelTargetLengthFactor(50) >= 0.55f);

        assertEquals(10.0, CollisionUtils.getLevelInitialTime(1), 0.001);
        assertTrue(CollisionUtils.getLevelInitialTime(5) >= 7.5);
    }

    @Test
    public void testEaseOutBackAnimationCurve() {
        assertEquals(0.0f, CollisionUtils.easeOutBack(0.0f), 0.001f);
        assertEquals(1.0f, CollisionUtils.easeOutBack(1.0f), 0.001f);
        // Em t=0.7, a curva Ease-Out Back deve ultrapassar 1.0 (efeito bounce/overshoot)
        assertTrue(CollisionUtils.easeOutBack(0.7f) > 1.0f);
    }
}
