package com.shirbi.downfall;

public class SmartRotationResult {
    public Wheel m_wheel;
    public int m_angle;

    int m_top_occupied_hole_count;
    int m_bottom_empty_hole_count;
    int m_top_occupied_hole_count_opposite;
    int m_bottom_empty_hole_count_opposite;
    int m_fall_token;
    int m_fall_token_opposite;

    SmartRotationResult(Wheel wheel) {
        m_wheel = wheel;
        m_angle = 0;
        m_top_occupied_hole_count = 0;
        m_bottom_empty_hole_count = 0;
        m_top_occupied_hole_count_opposite = 0;
        m_bottom_empty_hole_count_opposite = 0;
        m_fall_token = 0;
        m_fall_token_opposite = 0;
    }

    SmartRotationResult(SmartRotationResult other) {
        m_wheel = other.m_wheel;
        m_angle = other.m_angle;
        m_top_occupied_hole_count = other.m_top_occupied_hole_count;
        m_bottom_empty_hole_count = other.m_bottom_empty_hole_count;
        m_top_occupied_hole_count_opposite = other.m_top_occupied_hole_count_opposite;
        m_bottom_empty_hole_count_opposite = other.m_bottom_empty_hole_count_opposite;
        m_fall_token = other.m_fall_token;
        m_fall_token_opposite = other.m_fall_token_opposite;
    }

    Boolean IsBetterThan(SmartRotationResult other) {
        if (other == null) {
            return true;
        }

        int diff_fall_token = (m_fall_token_opposite - m_fall_token) -
                (other.m_fall_token_opposite - other.m_fall_token);

        if (diff_fall_token > 0) {
            return true;
        }
        if (diff_fall_token < 0) {
            return false;
        }

        int diff_token_fit = (m_top_occupied_hole_count_opposite - m_top_occupied_hole_count) -
                (other.m_top_occupied_hole_count_opposite - other.m_top_occupied_hole_count);

        if (diff_token_fit > 0) {
            return true;
        }
        if (diff_token_fit < 0) {
            return false;
        }

        int diff_hole_fit = (m_bottom_empty_hole_count_opposite - m_top_occupied_hole_count) -
                (other.m_bottom_empty_hole_count_opposite - other.m_top_occupied_hole_count);

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
