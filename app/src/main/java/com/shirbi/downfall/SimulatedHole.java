package com.shirbi.downfall;

public class SimulatedHole
{
    public int m_base_angle;
    public double m_current_angle;

    public Boolean m_opposite_side;
    public Boolean m_has_resident;

    public SimulatedHole(Hole hole) {
        m_opposite_side = hole.GetOppositeSide();
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
