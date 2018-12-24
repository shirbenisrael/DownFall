package com.shirbi.downfall;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SmartAI extends OppositePlayer {
    private int[][] m_interesting_angles;

    private void CalculateInterestingAnglesForWheel(int wheel_num ) {
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
                    connection_angle = (int)connection.m_top_angle;
                } else {
                    connection_angle = (int)connection.m_bottom_angle;
                }
                int hole_angle = hole.GetBaseAngle();

                int interesting_angle = connection_angle - hole_angle;
                while(interesting_angle < 0) {
                    interesting_angle += 360;
                }

                temp_array[angle_num] = interesting_angle;
                angle_num++;
            }
        }

        // Find how many unique angles found.
        Arrays.sort(temp_array);
        int unique_values = 1;
        for (int i = 1; i <temp_array.length; i++) {
            if (temp_array[i] != temp_array[i-1]) {
                unique_values++;
            }
        }

        // Store only the unique angles
        m_interesting_angles[wheel_num] = new int[unique_values];
        m_interesting_angles[wheel_num][0] = temp_array[0];
        int j = 1;
        for (int i = 1; i <temp_array.length; i++) {
            if (temp_array[i] != temp_array[i-1]) {
                m_interesting_angles[wheel_num][j] = temp_array[i];
                j++;
            }
        }
    }

    SmartAI(Wheel[] wheels) {
        super(wheels);

        m_interesting_angles = new int[wheels.length][];

        for (int i = 0; i < wheels.length; i++) {
            CalculateInterestingAnglesForWheel(i);
        }
    }

    private Set<SmartRotationResult> RotateWheelResult(int wheel_num) {
        Set<SmartRotationResult> rotation_result = new HashSet<>();
        int first_interesting_angle;
        int wheel_interesting_angles[] = m_interesting_angles[wheel_num];
        int wheel_current_angle = (int)m_wheels[wheel_num].GetCurrentAngle();
        int num_angles =  wheel_interesting_angles.length;

        first_interesting_angle = 0;

        while(wheel_current_angle > wheel_interesting_angles[first_interesting_angle] ) {
            first_interesting_angle++;
            if (first_interesting_angle == num_angles) {
                first_interesting_angle = 0;
                break;
            }
        }

        for (int i = 0 ; i < num_angles * 2; i++) {
            int next_angle_index = (first_interesting_angle + i) % num_angles;
            int next_angle = wheel_interesting_angles[next_angle_index];




        }



        return rotation_result;
    }

    @Override
    public int Run() {
        return 5;
    }
}
