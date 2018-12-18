package com.shirbi.downfall;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.abs;

public class Connection {
    ConnectableImage m_top_wheel;
    ConnectableImage m_bottom_wheel;
    double m_bottom_angle;
    double m_top_angle;
    protected Set<Hole> m_bottom_holes;
    protected Set<Hole> m_top_holes;

    public static final int MAX_DIFF_DEGREE = 5;

    Connection(ConnectableImage top, ConnectableImage bottom, double bottom_angle) {
        m_top_wheel = top;
        m_bottom_wheel = bottom;
        m_bottom_angle = bottom_angle;
        m_top_angle = (bottom_angle + 180) % 360;
        m_bottom_holes = new HashSet<Hole>();
        m_top_holes = new HashSet<Hole>();

    }

    private double angle_diff(double angle1, double angle2) {
        double diff =  abs(angle1 - angle2) % 360;
        if (diff > 180) {
            diff = 360 - diff;
        }
        return diff;
    }

    Boolean IsTopWheel(ConnectableImage wheel) {
        return (wheel == m_top_wheel);
    }

    private Hole GetBottomConnected(Hole hole) {
        for (Hole bottom_hole : m_bottom_holes) {
            if (bottom_hole.GetOppositeSide() == hole.GetOppositeSide()) {
                return bottom_hole;
            }
        }
        return null;
    }

    private Hole GetTopConnected(Hole hole) {
        for (Hole top_hole : m_top_holes) {
            if (top_hole.GetOppositeSide() == hole.GetOppositeSide()) {
                return top_hole;
            }
        }
        return null;
    }

    Boolean CompareHoleAngle(ConnectableImage owner_wheel, Hole hole, double angle) {
        if (owner_wheel == m_top_wheel) {
            if (angle_diff(angle, m_top_angle) < MAX_DIFF_DEGREE) {
                m_top_holes.add(hole);
                hole.FallDownToken(GetBottomConnected(hole));
                return true;
            } else {
                m_top_holes.remove(hole);
            }
        }

        if (owner_wheel == m_bottom_wheel) {
            if (angle_diff(angle, m_bottom_angle) < MAX_DIFF_DEGREE) {
                m_bottom_holes.add(hole);
                Hole top_hole = GetTopConnected(hole);
                if (top_hole != null) {
                    top_hole.FallDownToken(hole);
                }
                return true;
            } else {
                m_bottom_holes.remove(hole);
            }
        }

        return false;
    }
}
