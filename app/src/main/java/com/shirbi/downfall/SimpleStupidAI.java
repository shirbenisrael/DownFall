package com.shirbi.downfall;

public class SimpleStupidAI {
    private Wheel m_wheels[];

    SimpleStupidAI( Wheel wheels[]) {
        m_wheels = wheels;
    }

    private RotationResult RotateWheelResultTokenOnWheel(Wheel wheel, Hole hole) {
        for (Connection connection : wheel.m_connections) {
            if (connection.m_top_wheel != wheel) {
                continue; // This is the bottom wheel for this connection. The token cannot fall any more
            }
            for (Hole bottom_hole : connection.m_bottom_holes) {
                if ((bottom_hole.GetResident() == null) && bottom_hole.GetOppositeSide()) {
                    // Great, we found a bottom hole that can get this token.
                    int hole_base_angle = hole.GetBaseAngle();
                    int hole_current_angle = hole_base_angle + (int)wheel.GetCurrentAngle();
                    int hole_desired_angle = (int)connection.m_top_angle;

                    RotationResult rotationResult = new RotationResult();

                    rotationResult.m_wheel = wheel;
                    rotationResult.m_angle = hole_desired_angle - hole_current_angle;
                    rotationResult.m_num_token_fall = 1;
                    rotationResult.m_num_holes_connected = 0;

                    return rotationResult;
                }
            }
        }

        return null;
    }

    private RotationResult RotateWheelResultTokenInTopWheel(Wheel wheel, Hole hole) {
        for (Connection connection : wheel.m_connections) {
            if (connection.m_bottom_wheel != wheel) {
                continue; // This is the top wheel for this connection. We don't have a token to fall.
            }
            for (Hole top_hole : connection.m_top_holes) {
                if ((top_hole.GetResident() != null) && top_hole.GetOppositeSide()) {
                    // Great, we found a top hole that can give us its token.
                    int hole_base_angle = hole.GetBaseAngle();
                    int hole_current_angle = hole_base_angle + (int)wheel.GetCurrentAngle();
                    int hole_desired_angle = (int)connection.m_bottom_angle;

                    RotationResult rotationResult = new RotationResult();

                    rotationResult.m_wheel = wheel;
                    rotationResult.m_angle = hole_desired_angle - hole_current_angle;
                    rotationResult.m_num_token_fall = 1;
                    rotationResult.m_num_holes_connected = 0;

                    return rotationResult;
                }
            }
        }

        return null;
    }

    private RotationResult RotateWheelResultWithTokenFall(Wheel wheel) {
        for (Hole hole : wheel.m_holes) {
            if (!hole.GetOppositeSide()) {
                // Simple stupid AI doesn't care about the human player tokens
                continue;
            }
            if (hole.GetResident() != null) {
                // Hole has resident. Lets see if there is a connection to of this wheel that is
                // connected to other hole in bottom wheel
                RotationResult this_hole_result = RotateWheelResultTokenOnWheel(wheel, hole);
                if (this_hole_result != null) {
                    return this_hole_result;
                }
            } else {
                // Hole is empty. Lets see if there is a connection of this wheel to top wheel
                // that has token inside it.
                RotationResult this_hole_result = RotateWheelResultTokenInTopWheel(wheel, hole);
                if (this_hole_result != null) {
                    return this_hole_result;
                }
            }
        }

        return null;
    }

    private RotationResult RotateWheelResult(Wheel wheel) {
        RotationResult rotationResult = new RotationResult();
        rotationResult.m_wheel = null;
        rotationResult.m_angle = 0;
        rotationResult.m_num_holes_connected = 0;

        RotationResult result_will_fall_token = RotateWheelResultWithTokenFall(wheel);
        if (result_will_fall_token != null) {
            return result_will_fall_token;
        }

        // Can't fall any token? at least put a hole in better position
        for (Hole hole : wheel.m_holes) {
            if (!hole.GetOppositeSide()) {
                // Simple stupid AI doesn't care about the human player tokens
                continue;
            }
            if (hole.GetResident() == null) {
                continue;
            }

            for (Connection connection : wheel.m_connections) {
                if (connection.m_top_wheel != wheel) {
                    continue;
                }

                if (connection.GetTopConnected(hole) == hole) {
                    // Hole is already connected
                    return null;
                }
            }

            for (Connection connection : wheel.m_connections) {
                if (connection.m_top_wheel != wheel) {
                    continue;
                }

                int hole_base_angle = hole.GetBaseAngle();
                int hole_current_angle = hole_base_angle + (int) wheel.GetCurrentAngle();
                int hole_desired_angle = (int) connection.m_top_angle;

                rotationResult.m_wheel = wheel;
                rotationResult.m_angle = hole_desired_angle - hole_current_angle;
                rotationResult.m_num_token_fall = 0;
                rotationResult.m_num_holes_connected = 1;

                return rotationResult;
            }
        }

        return rotationResult;
    }

    public int Run() {
        RotationResult bestResult = new RotationResult();
        bestResult.m_wheel = null;
        bestResult.m_angle = 0;
        bestResult.m_num_holes_connected = 0;

        for (Wheel wheel : m_wheels) {
            if (!wheel.GetAllowRotation()) {
                continue;
            }

            RotationResult rotationResult = RotateWheelResult(wheel);
            if (rotationResult == null) {
                continue;
            }

            if (bestResult.m_num_token_fall < rotationResult.m_num_token_fall) {
                bestResult = rotationResult;
                continue;
            }

            if (bestResult.m_num_token_fall > rotationResult.m_num_token_fall) {
                continue;
            }

            if (bestResult.m_num_holes_connected < rotationResult.m_num_holes_connected) {
                bestResult = rotationResult;
                continue;
            }
        }

        if (bestResult.m_wheel != null) {
            bestResult.m_wheel.AddRotation(bestResult.m_angle);
            return bestResult.m_wheel.GetWheelNum();
        } else {
            return m_wheels.length;
        }

    }

}
