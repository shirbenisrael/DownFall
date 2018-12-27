package com.shirbi.downfall;

public enum PlayerType {
    HUMAN_PLAYER(0), AI_PLAYER(1);

    static public final int NUM_PLAYERS = 2;

    private final int m_player_type;

    PlayerType(int player_type) {
        this.m_player_type = player_type;
    }

    public int getInt() {
        return this.m_player_type;
    }
}
