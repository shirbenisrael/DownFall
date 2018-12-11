package com.shirbi.downfall;

import static java.lang.Math.abs;

public class Connection {
    Wheel m_top_wheel;
    Wheel m_bottom_wheel;
    double m_bottom_angle;
    double m_top_angle;
    Hole m_bottom_hole;
    Hole m_top_hole;

    public static final int MAX_DIFF_DEGREE = 5;

    Connection(Wheel top, Wheel bottom, double bottom_angle) {
        m_top_wheel = top;
        m_bottom_wheel = bottom;
        m_bottom_angle = bottom_angle;
        m_top_angle = (bottom_angle + 180) % 360;
        m_bottom_hole = null;
        m_top_hole = null;
    }

    private double angle_diff(double angle1, double angle2) {
        double diff =  abs(angle1 - angle2) % 360;
        if (diff > 180) {
            diff = 360 - diff;
        }
        return diff;
    }

    Boolean CompareHoleAngle(Wheel owner_wheel, Hole hole, double angle) {
        if (owner_wheel == m_top_wheel) {
            if (angle_diff(angle, m_top_angle) < MAX_DIFF_DEGREE) {
                m_top_hole = hole;
                m_top_hole.FallDownToken(m_bottom_hole);
                return true;
            } else {
                if (m_top_hole == hole) {
                    m_top_hole = null;
                }
            }
        }

        if (owner_wheel == m_bottom_wheel) {
            if (angle_diff(angle, m_bottom_angle) < MAX_DIFF_DEGREE) {
                m_bottom_hole = hole;
                if (m_top_hole != null) {
                    m_top_hole.FallDownToken(m_bottom_hole);
                }
                return true;
            } else {
                if (m_bottom_hole == hole) {
                    m_bottom_hole = null;
                }
            }
        }

        return false;
    }
}
