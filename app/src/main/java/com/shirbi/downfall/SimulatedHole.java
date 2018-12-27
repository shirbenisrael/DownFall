package com.shirbi.downfall;

public class SimulatedHole
{
    public int m_base_angle;
    public double m_current_angle;

    public PlayerType m_player_type;
    public Boolean m_has_resident;

    public SimulatedHole(Hole hole) {
        m_player_type = hole.GetPlayerType();
        m_has_resident = (hole.GetResident() != null);
        m_current_angle = hole.GetCurrentAngle();
        m_base_angle = hole.GetBaseAngle();
    }

    public void SetWheelAngle(int angle) {
        m_current_angle = m_base_angle + angle;
        while (m_current_angle > 360) {
            m_current_angle -= 360;
        }
        while (m_current_angle < 0) {
            m_current_angle += 360;
        }
    }
}
