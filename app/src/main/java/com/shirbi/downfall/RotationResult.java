package com.shirbi.downfall;

public class RotationResult {
    public Wheel m_wheel;
    public int m_angle;
    public int m_num_token_fall;
    public int m_moved_token_number;
    public int m_num_holes_connected;

    static final int INVALID_TOKEN_NUMBER = 1000;

    public RotationResult() {
        m_wheel = null;
        m_angle = 0;
        m_num_token_fall = 0;
        m_moved_token_number = INVALID_TOKEN_NUMBER;
        m_num_holes_connected = 0;
    }
}
