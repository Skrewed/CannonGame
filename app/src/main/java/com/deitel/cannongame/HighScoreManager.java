// HighScoreManager.java
// Persistência de Ranking Local (Top 5 Highscores) e preferências via SharedPreferences (Recurso Extra #9)
package com.deitel.cannongame;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Gerencia o armazenamento persistente das maiores pontuações locais (Top 5)
 * e configurações de usuário (Modo Dia/Noite e Áudio) usando SharedPreferences.
 */
public class HighScoreManager {

    private static final String PREFS_NAME = "cannon_game_preferences";
    private static final String KEY_HIGHSCORES = "top_highscores_v1";
    private static final String KEY_NIGHT_MODE = "pref_night_mode";
    private static final String KEY_SOUND_ENABLED = "pref_sound_enabled";
    private static final int MAX_ENTRIES = 5;

    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public final int score;
        public final int level;
        public final float accuracy;

        public ScoreEntry(int score, int level, float accuracy) {
            this.score = score;
            this.level = level;
            this.accuracy = accuracy;
        }

        @Override
        public int compareTo(ScoreEntry other) {
            if (this.score != other.score) {
                return Integer.compare(other.score, this.score);
            }
            return Integer.compare(other.level, this.level);
        }
    }

    private final SharedPreferences prefs;

    public HighScoreManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Registra uma nova pontuação no ranking local e retorna true se for um novo recorde (#1).
     */
    public boolean recordScore(int score, int level, float accuracy) {
        if (score <= 0) {
            return false;
        }
        List<ScoreEntry> entries = getTopScores();
        int previousBest = entries.isEmpty() ? 0 : entries.get(0).score;

        entries.add(new ScoreEntry(score, level, accuracy));
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) {
            entries = new ArrayList<>(entries.subList(0, MAX_ENTRIES));
        }

        saveEntries(entries);
        return score > previousBest;
    }

    public int getBestScore() {
        List<ScoreEntry> entries = getTopScores();
        return entries.isEmpty() ? 0 : entries.get(0).score;
    }

    public List<ScoreEntry> getTopScores() {
        String serialized = prefs.getString(KEY_HIGHSCORES, "");
        List<ScoreEntry> list = new ArrayList<>();
        if (serialized == null || serialized.trim().isEmpty()) {
            return list;
        }
        String[] rows = serialized.split(";");
        for (String row : rows) {
            String[] parts = row.split("\\|");
            if (parts.length == 3) {
                try {
                    int s = Integer.parseInt(parts[0]);
                    int lvl = Integer.parseInt(parts[1]);
                    float acc = Float.parseFloat(parts[2]);
                    list.add(new ScoreEntry(s, lvl, acc));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    private void saveEntries(List<ScoreEntry> entries) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            ScoreEntry e = entries.get(i);
            if (i > 0) {
                sb.append(";");
            }
            sb.append(e.score)
              .append("|")
              .append(e.level)
              .append("|")
              .append(String.format(Locale.US, "%.1f", e.accuracy));
        }
        prefs.edit().putString(KEY_HIGHSCORES, sb.toString()).apply();
    }

    public boolean isNightMode() {
        return prefs.getBoolean(KEY_NIGHT_MODE, true); // Inicia no elegante Modo Noturno por padrão
    }

    public void setNightMode(boolean nightMode) {
        prefs.edit().putBoolean(KEY_NIGHT_MODE, nightMode).apply();
    }

    public boolean isSoundEnabled() {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true);
    }

    public void setSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply();
    }
}
