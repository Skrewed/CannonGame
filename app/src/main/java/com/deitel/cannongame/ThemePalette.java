// ThemePalette.java
// Gerencia os estados visuais dos modos Dia e Noturno (Recurso Extra #6)
package com.deitel.cannongame;

import android.graphics.Color;

/**
 * Define as paletas minimalistas para o Modo Dia e o Modo Noturno,
 * permitindo troca dinâmica de tema durante a execução do jogo.
 */
public class ThemePalette {

    private boolean nightMode;

    public ThemePalette(boolean initialNightMode) {
        this.nightMode = initialNightMode;
    }

    public boolean isNightMode() {
        return nightMode;
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
    }

    public void toggle() {
        this.nightMode = !this.nightMode;
    }

    public int getBackgroundColor() {
        return nightMode ? Color.parseColor("#0B1120") : Color.parseColor("#F8FAFC");
    }

    public int getSecondaryBackgroundColor() {
        return nightMode ? Color.parseColor("#111827") : Color.parseColor("#EFF6FF");
    }

    public int getGridLineColor() {
        return nightMode ? Color.parseColor("#1838BDF8") : Color.parseColor("#140F172A");
    }

    public int getCannonPrimaryColor() {
        return nightMode ? Color.parseColor("#F8FAFC") : Color.parseColor("#0F172A");
    }

    public int getCannonAccentColor() {
        return nightMode ? Color.parseColor("#38BDF8") : Color.parseColor("#2563EB");
    }

    public int getCannonballColor() {
        return nightMode ? Color.parseColor("#F8FAFC") : Color.parseColor("#0F172A");
    }

    public int getCannonballGlowColor() {
        return nightMode ? Color.parseColor("#38BDF8") : Color.parseColor("#3B82F6");
    }

    public int getBlockerColor() {
        return nightMode ? Color.parseColor("#F43F5E") : Color.parseColor("#DC2626");
    }

    public int getTargetEvenColor() {
        return nightMode ? Color.parseColor("#38BDF8") : Color.parseColor("#2563EB");
    }

    public int getTargetOddColor() {
        return nightMode ? Color.parseColor("#A855F7") : Color.parseColor("#F59E0B");
    }

    public int getHudSurfaceColor() {
        return nightMode ? Color.parseColor("#CC1E293B") : Color.parseColor("#E6FFFFFF");
    }

    public int getHudBorderColor() {
        return nightMode ? Color.parseColor("#334155") : Color.parseColor("#CBD5E1");
    }

    public int getTextPrimaryColor() {
        return nightMode ? Color.parseColor("#F8FAFC") : Color.parseColor("#0F172A");
    }

    public int getTextSecondaryColor() {
        return nightMode ? Color.parseColor("#94A3B8") : Color.parseColor("#475569");
    }

    public int getWarningColor() {
        return nightMode ? Color.parseColor("#FB7185") : Color.parseColor("#DC2626");
    }

    public int getAccentBadgeColor() {
        return nightMode ? Color.parseColor("#10B981") : Color.parseColor("#059669");
    }

    public int getAmbientParticleColor() {
        return nightMode ? Color.parseColor("#5538BDF8") : Color.parseColor("#3394A3B8");
    }
}
