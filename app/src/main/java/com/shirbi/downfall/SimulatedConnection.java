package com.shirbi.downfall;

public class SimulatedConnection {
    public Boolean m_top_connection;
    public Connection m_original_connection;

    // Top wheel has tokens that are connected to this connection.
    public int m_max_num_tokens_to_wheel[] = new int[PlayerType.NUM_PLAYERS];

    // Top wheel has tokens that are not connected to this connection.
    public int m_max_num_tokens_to_wheel_later[] = new int[PlayerType.NUM_PLAYERS];

    public int m_max_num_tokens_from_wheel[] = new int[PlayerType.NUM_PLAYERS];

    public SimulatedConnection(Connection connection, Wheel wheel) {
        m_original_connection = connection;

        for (int i = 0 ; i < PlayerType.NUM_PLAYERS; i++) {
            m_max_num_tokens_to_wheel[i] = 0;
            m_max_num_tokens_to_wheel_later[i] = 0;
            m_max_num_tokens_from_wheel[i] = 0;
        }

        if (connection.m_top_wheel == wheel) {
            m_top_connection = true;

            if (connection.m_bottom_wheel instanceof Output) {
                for (int i = 0 ; i < PlayerType.NUM_PLAYERS ; i++) {
                    m_max_num_tokens_from_wheel[i] = 10;
                }
            } else {
                for (Hole hole : connection.m_bottom_holes) {
                    if (hole.GetResident() == null) {
                        m_max_num_tokens_from_wheel[hole.GetPlayerType().getInt()] = 1;
                    }
                }
            }
        } else {
            m_top_connection = false;

            for (Hole hole : connection.m_top_holes) {
                if (hole.GetResident() != null) {
                    m_max_num_tokens_to_wheel[hole.GetPlayerType().getInt()] = 1;
                }
            }

            // Top wheel has token that is not connected now.
            for (Hole hole : m_original_connection.m_top_wheel.m_holes) {
                if ((hole.GetResident() != null) &&
                        (m_max_num_tokens_to_wheel[hole.GetPlayerType().getInt()] == 0)) {
                    m_max_num_tokens_to_wheel_later[hole.GetPlayerType().getInt()] = 1;
                }
            }

            if (connection.m_top_wheel instanceof InputTokenQueue) {
                InputTokenQueue input_queue = (InputTokenQueue) connection.m_top_wheel;
                for (int i = 0 ; i < PlayerType.NUM_PLAYERS ; i++) {
                    m_max_num_tokens_to_wheel[i] += input_queue.GetNumTokensInQueue(PlayerType.values()[i]);
                }
            }
        }
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

