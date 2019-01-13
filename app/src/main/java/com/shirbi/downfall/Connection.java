package com.shirbi.downfall;

import static java.lang.Math.abs;

public class Connection {
    ConnectableImage m_top_wheel;
    ConnectableImage m_bottom_wheel;
    double m_bottom_angle;
    double m_top_angle;

    protected Hole m_bottom_holes[];
    protected Hole m_top_holes[];

    public static final int MAX_DIFF_DEGREE = 5;

    Connection(ConnectableImage top, ConnectableImage bottom, double bottom_angle) {
        m_top_wheel = top;
        m_bottom_wheel = bottom;
        m_bottom_angle = bottom_angle;
        m_top_angle = (bottom_angle + 180) % 360;
        m_bottom_holes = new Hole[PlayerType.NUM_PLAYERS];
        m_top_holes = new Hole[PlayerType.NUM_PLAYERS];

    }

    public double angle_diff(double angle1, double angle2) {
        double diff =  abs(angle1 - angle2) % 360;
        if (diff > 180) {
            diff = 360 - diff;
        }
        return diff;
    }

    Boolean IsTopWheel(ConnectableImage wheel) {
        return (wheel == m_top_wheel);
    }

    public Hole GetBottomConnected(Hole hole) {
        return m_bottom_holes[hole.GetPlayerType().getInt()];
    }

    public Hole GetTopConnected(Hole hole) {
        return m_top_holes[hole.GetPlayerType().getInt()];
    }

    Boolean CompareHoleAngle(ConnectableImage owner_wheel, Hole hole, double angle) {
        int player_type_num = hole.GetPlayerType().getInt();
        if (owner_wheel == m_top_wheel) {
            if (angle_diff(angle, m_top_angle) < MAX_DIFF_DEGREE) {
                m_top_holes[player_type_num] = hole;
                hole.FallDownToken(GetBottomConnected(hole));
                return true;
            } else {
                if (m_top_holes[player_type_num] == hole) {
                    m_top_holes[player_type_num] = null;
                }
            }
        }

        if (owner_wheel == m_bottom_wheel) {
            if (angle_diff(angle, m_bottom_angle) < MAX_DIFF_DEGREE) {
                m_bottom_holes[player_type_num] = hole;
                Hole top_hole = GetTopConnected(hole);
                if (top_hole != null) {
                    top_hole.FallDownToken(hole);
                }
                return true;
            } else {
                if (m_bottom_holes[player_type_num] == hole) {
                    m_bottom_holes[player_type_num] = null;
                }
            }
        }

        return false;
    }
}
