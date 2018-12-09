package com.shirbi.downfall;

import static java.lang.Math.abs;

public class Connection {
    Wheel m_top_wheel;
    Wheel m_bottom_wheel;
    double m_bottom_angle;
    double m_top_angle;

    Connection(Wheel top, Wheel bottom, double bottom_angle) {
        m_top_wheel = top;
        m_bottom_wheel = bottom;
        m_bottom_angle = bottom_angle;
        m_top_angle = (bottom_angle + 180) % 360;
    }

    private double angle_diff(double angle1, double angle2) {
        double diff =  abs(angle1 - angle2) % 360;
        if (diff > 180) {
            diff = 360 - diff;
        }
        return diff;
    }

    Boolean CompareHoleAngle(Wheel owner_wheel, double angle) {
        if (owner_wheel == m_top_wheel) {
            return (angle_diff(angle, m_top_angle)  < 20);
        }

        if (owner_wheel == m_bottom_wheel) {
            return (angle_diff(angle, m_bottom_angle) < 20);
        }

        return false;
    }
}
