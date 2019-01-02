package com.shirbi.downfall;

import java.util.ArrayList;
import java.util.Arrays;

public class SmartAI extends OppositePlayer {
    private int[][] m_interesting_angles;
    SimulatedHole m_simulated_wheel_holes[][];
    SimulatedConnection m_simulated_wheel_connection[][];

    private void CalculateInterestingAnglesForWheel(int wheel_num) {
        Wheel wheel = m_wheels[wheel_num];
        int num_holes = wheel.m_holes.size();
        int num_connections = wheel.m_connections.size();
        int num_interesting_angles = num_holes * num_connections;

        int angle_num = 0;
        int temp_array[] = new int[num_interesting_angles];

        for (Connection connection : wheel.m_connections) {
            for (Hole hole : wheel.m_holes) {
                int connection_angle;
                if (connection.m_top_wheel == wheel) {
                    connection_angle = (int) connection.m_top_angle;
                } else {
                    connection_angle = (int) connection.m_bottom_angle;
                }
                int hole_angle = hole.GetBaseAngle();

                int interesting_angle = connection_angle - hole_angle;
                while (interesting_angle < 0) {
                    interesting_angle += 360;
                }

                temp_array[angle_num] = interesting_angle;
                angle_num++;
            }
        }

        // Find how many unique angles found.
        Arrays.sort(temp_array);
        int unique_values = 1;
        for (int i = 1; i < temp_array.length; i++) {
            if (temp_array[i] != temp_array[i - 1]) {
                unique_values++;
            }
        }

        // Store only the unique angles
        m_interesting_angles[wheel_num] = new int[unique_values];
        m_interesting_angles[wheel_num][0] = temp_array[0];
        int j = 1;
        for (int i = 1; i < temp_array.length; i++) {
            if (temp_array[i] != temp_array[i - 1]) {
                m_interesting_angles[wheel_num][j] = temp_array[i];
                j++;
            }
        }
    }

    private void SetupWheelHoles(int wheel_num) {
        Wheel wheel = m_wheels[wheel_num];
        int num_holes = wheel.m_holes.size();
        m_simulated_wheel_holes[wheel_num] = new SimulatedHole[num_holes];
        int i = 0;
        for (Hole hole : wheel.m_holes) {
            m_simulated_wheel_holes[wheel_num][i] = new SimulatedHole(hole);
            i++;
        }
    }

    private void SetupSimulatedWheelConnections(int wheel_num) {
        Wheel wheel = m_wheels[wheel_num];
        int num_connections = wheel.m_connections.size();
        m_simulated_wheel_connection[wheel_num] = new SimulatedConnection[num_connections];
        int i = 0;
        for (Connection connection : wheel.m_connections) {
            m_simulated_wheel_connection[wheel_num][i] = new SimulatedConnection(connection, wheel);
            i++;
        }
    }

    SmartAI(Wheel[] wheels) {
        super(wheels);

        m_interesting_angles = new int[wheels.length][];
        m_simulated_wheel_holes = new SimulatedHole[wheels.length][];
        m_simulated_wheel_connection = new SimulatedConnection[wheels.length][];

        for (int i = 0; i < wheels.length; i++) {
            CalculateInterestingAnglesForWheel(i);
        }
    }

    private SimulatedConnection IsHoleConnected(SimulatedConnection connections[], SimulatedHole hole) {
        for (SimulatedConnection connection : connections) {
            if (connection.m_top_connection) {
                double angle_diff = connection.m_original_connection.angle_diff(
                        hole.m_current_angle, connection.m_original_connection.m_top_angle);
                if (angle_diff < Connection.MAX_DIFF_DEGREE) {
                    return connection;
                }
            } else {
                double angle_diff = connection.m_original_connection.angle_diff(
                        hole.m_current_angle, connection.m_original_connection.m_bottom_angle);
                if (angle_diff < Connection.MAX_DIFF_DEGREE) {
                    return connection;
                }
            }
        }

        return null;
    }

    enum ROTATION_DIRECTION {
        ROTATE_LEFT,
        ROTATE_RIGHT
    }

    private ArrayList<SmartRotationResult> RotateWheelResult(int wheel_num, ROTATION_DIRECTION direction) {
        Wheel wheel = m_wheels[wheel_num];
        SimulatedHole wheel_holes[] = m_simulated_wheel_holes[wheel_num];
        SimulatedConnection wheel_connections[] = m_simulated_wheel_connection[wheel_num];

        ArrayList<SmartRotationResult> rotation_results = new ArrayList<>();

        int first_interesting_angle;
        int wheel_interesting_angles[] = m_interesting_angles[wheel_num];
        int wheel_current_angle = (int) m_wheels[wheel_num].GetCurrentAngle();
        int num_angles = wheel_interesting_angles.length;

        first_interesting_angle = 0;

        while (wheel_current_angle > wheel_interesting_angles[first_interesting_angle]) {
            first_interesting_angle++;
            if (first_interesting_angle == num_angles) {
                first_interesting_angle = 0;
                break;
            }
        }

        if (direction == ROTATION_DIRECTION.ROTATE_LEFT) {
            first_interesting_angle--;
            if (first_interesting_angle < 0) {
                first_interesting_angle = num_angles - 1;
            }
        }

        SmartRotationResult rotation_result = new SmartRotationResult(wheel);

        int previous_angle = 0;

        for (int i = 0; i < num_angles * 2; i++) {
            int next_angle_index;
            int num_full_cycles;
            int next_angle;
            int previous_angle_index;
            int previous_interesting_angle;
            int diff_angels;

            if (direction == ROTATION_DIRECTION.ROTATE_RIGHT) {
                next_angle_index = (first_interesting_angle + i) % num_angles;
                previous_angle_index = (first_interesting_angle + i - 1 + num_angles * 2) % num_angles;
                next_angle = wheel_interesting_angles[next_angle_index];
            } else {
                next_angle_index = (first_interesting_angle - i + num_angles * 2) % num_angles;
                previous_angle_index = (first_interesting_angle - i + 1 + num_angles * 2) % num_angles;
                next_angle = wheel_interesting_angles[next_angle_index] - 360;
            }

            if (i != 0) {
                previous_interesting_angle = wheel_interesting_angles[previous_angle_index];
            } else {
                previous_interesting_angle = (int) wheel.GetCurrentAngle();
            }

            if (direction == ROTATION_DIRECTION.ROTATE_RIGHT) {
                diff_angels = (next_angle - previous_interesting_angle + 360) % 360;
            } else {
                diff_angels = (next_angle - previous_interesting_angle - 360) % 360;
            }

            rotation_result = new SmartRotationResult(rotation_result);
            rotation_result.m_angle = previous_angle + diff_angels;
            previous_angle = rotation_result.m_angle;

            for (SimulatedHole hole : wheel_holes) {
                int player_index = hole.m_player_type.getInt();
                SimulatedConnection connection = IsHoleConnected(wheel_connections, hole);
                if (connection != null && connection.IsEfficient()) {
                    if ((!hole.m_has_resident) && (!connection.m_top_connection)) {
                        if (connection.m_max_num_tokens_to_wheel_later[player_index] > 0) {
                            rotation_result.m_bottom_empty_hole_with_top_wheel_with_token_count[player_index]--;
                        } else {
                            rotation_result.m_bottom_empty_hole_count[player_index]--;
                        }
                    }

                    if (hole.m_has_resident && connection.m_top_connection) {
                        rotation_result.m_top_occupied_hole_count[player_index]--;
                    }
                }

                hole.SetWheelAngle(next_angle);

                connection = IsHoleConnected(wheel_connections, hole);

                if (connection != null) {
                    if (connection.m_top_connection) {
                        if (hole.m_has_resident) {
                            if (connection.m_max_num_tokens_from_wheel[player_index] > 0) {
                                connection.m_max_num_tokens_from_wheel[player_index]--;
                                if (connection.IsEfficient()) {
                                    rotation_result.m_fall_token[player_index]++;
                                }
                                hole.m_has_resident = false;
                            } else {
                                if (connection.IsEfficient()) {
                                    rotation_result.m_top_occupied_hole_count[player_index]++;
                                }
                            }

                        }
                    } else { // else: bottom connection.
                        if (!hole.m_has_resident) {
                            if (connection.m_max_num_tokens_to_wheel[player_index] > 0) {
                                connection.m_max_num_tokens_to_wheel[player_index]--;
                                if (connection.IsEfficient()) {
                                    rotation_result.m_fall_token[player_index]++;
                                }
                                hole.m_has_resident = true;
                            } else {
                                if (connection.IsEfficient()) {
                                    if (connection.m_max_num_tokens_to_wheel_later[player_index] > 0) {
                                        rotation_result.m_bottom_empty_hole_with_top_wheel_with_token_count[player_index]++;
                                    } else {
                                        rotation_result.m_bottom_empty_hole_count[player_index]++;
                                    }
                                }
                            }

                        } // empty hole
                    } // connection type
                } // has connection
            } // for each hole
            rotation_results.add(rotation_result);
        } // for each angle

        return rotation_results;
    }

    private ArrayList<SmartRotationResult> m_last_wheel_result;

    @Override
    public int Run() {

        SmartRotationResult best_result = null;

        m_last_wheel_result = new ArrayList<>();

        for (int i = 0; i < m_wheels.length; i++) {
            if (!m_wheels[i].GetAllowRotation()) {
                continue;
            }

            SetupWheelHoles(i);
            SetupSimulatedWheelConnections(i);
            ArrayList<SmartRotationResult> wheel_results = RotateWheelResult(i, ROTATION_DIRECTION.ROTATE_RIGHT);

            SetupWheelHoles(i);
            SetupSimulatedWheelConnections(i);
            wheel_results.addAll(RotateWheelResult(i, ROTATION_DIRECTION.ROTATE_LEFT));

            for (SmartRotationResult result : wheel_results) {
                if ((result != null) && result.IsBetterThan(best_result)) {
                    if (result.IsBetterThan(best_result)) {
                        best_result = result;
                    }
                }
            }

            m_last_wheel_result.addAll(wheel_results);
        }

        if (best_result != null) {
            best_result.m_wheel.AddRotation(best_result.m_angle);
            return best_result.m_wheel.GetWheelNum();
        } else {
            return m_wheels.length;
        }
    }
}
