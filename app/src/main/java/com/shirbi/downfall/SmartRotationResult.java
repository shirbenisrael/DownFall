package com.shirbi.downfall;

public class SmartRotationResult {
    public Wheel m_wheel;
    public int m_angle;

    int m_fall_token[] = new int[PlayerType.NUM_PLAYERS];
    int m_top_occupied_hole_count[] = new int[PlayerType.NUM_PLAYERS];
    int m_bottom_empty_hole_count[] = new int[PlayerType.NUM_PLAYERS];

    SmartRotationResult(Wheel wheel) {
        m_wheel = wheel;
        m_angle = 0;
        for (int i = 0 ; i < PlayerType.NUM_PLAYERS; i++) {
            m_top_occupied_hole_count[0] = 0;
            m_bottom_empty_hole_count[0] = 0;
            m_fall_token[0] = 0;
        }
    }

    SmartRotationResult(SmartRotationResult other) {
        m_wheel = other.m_wheel;
        m_angle = other.m_angle;
        for (int i = 0 ; i < PlayerType.NUM_PLAYERS; i++) {
            m_top_occupied_hole_count[i] = other.m_top_occupied_hole_count[i];
            m_bottom_empty_hole_count[i] = other.m_bottom_empty_hole_count[i];
            m_fall_token[i] = other.m_fall_token[i];
        }
    }

    private int diff_ai_human(int my_array[], int other_array[]) {
        int my_diff = my_array[PlayerType.AI_PLAYER.getInt()] - my_array[PlayerType.HUMAN_PLAYER.getInt()];
        int other_diff = other_array[PlayerType.AI_PLAYER.getInt()] - other_array[PlayerType.HUMAN_PLAYER.getInt()];

        return my_diff - other_diff;
    }

    Boolean IsBetterThan(SmartRotationResult other) {
        if (other == null) {
            return true;
        }

        int diff_fall_token = diff_ai_human(m_fall_token, other.m_fall_token);
        if (diff_fall_token > 0) {
            return true;
        }
        if (diff_fall_token < 0) {
            return false;
        }

        int diff_token_fit = diff_ai_human(m_top_occupied_hole_count, other.m_top_occupied_hole_count);
        if (diff_token_fit > 0) {
            return true;
        }
        if (diff_token_fit < 0) {
            return false;
        }

        int diff_hole_fit = diff_ai_human(m_bottom_empty_hole_count, other.m_bottom_empty_hole_count);
        if (diff_hole_fit > 0) {
            return true;
        }
        if (diff_hole_fit < 0) {
            return false;
        }

        if (Math.abs(m_angle) < Math.abs(other.m_angle)) {
            return true;
        }

        return false;
    }

}
