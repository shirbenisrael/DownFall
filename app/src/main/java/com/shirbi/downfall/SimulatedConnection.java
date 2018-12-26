package com.shirbi.downfall;

public class SimulatedConnection {
    public Boolean m_top_connection;
    public Connection m_original_connection;
    public int m_max_num_tokens_to_wheel;
    public int m_max_num_tokens_from_wheel;
    public int m_max_num_tokens_to_wheel_opposite;
    public int m_max_num_tokens_from_wheel_opposite;

    public SimulatedConnection(Connection connection, Wheel wheel) {
        m_max_num_tokens_to_wheel = 0;
        m_max_num_tokens_from_wheel = 0;
        m_max_num_tokens_to_wheel_opposite = 0;
        m_max_num_tokens_from_wheel_opposite = 0;

        if (connection.m_top_wheel == wheel) {
            m_top_connection = true;

            if (connection.m_bottom_wheel instanceof Output) {
                m_max_num_tokens_from_wheel = 10;
                m_max_num_tokens_from_wheel_opposite = 10;
            } else {
                for (Hole hole : connection.m_bottom_holes) {
                    if (hole.GetResident() == null) {
                        if (hole.GetOppositeSide()) {
                            m_max_num_tokens_from_wheel_opposite = 1;
                        } else {
                            m_max_num_tokens_from_wheel =1;
                        }
                    }
                }
            }
        } else {
            m_top_connection = false;

            for (Hole hole : connection.m_top_holes) {
                if (hole.GetResident() != null) {
                    if (hole.GetOppositeSide()) {
                        m_max_num_tokens_to_wheel_opposite = 1;
                    } else {
                        m_max_num_tokens_to_wheel =1;
                    }
                }
            }

            if (connection.m_top_wheel instanceof InputTokenQueue) {
                InputTokenQueue input_queue = (InputTokenQueue) connection.m_top_wheel;
                m_max_num_tokens_to_wheel += input_queue.GetNumTokensInQueue();
                m_max_num_tokens_to_wheel_opposite += input_queue.GetNumTokensInQueueOpposite();
            }
        }

        m_original_connection = connection;
    }

    public Boolean IsEfficient() {
        if (m_original_connection.m_top_wheel instanceof Wheel) {
            if (((Wheel)(m_original_connection.m_top_wheel)).GetWheelNum() != 2) {
                return true;
            }

            if (((Wheel)(m_original_connection.m_bottom_wheel)).GetWheelNum() == 3) {
                return false;
            }
        }

        return true;
    }
}
