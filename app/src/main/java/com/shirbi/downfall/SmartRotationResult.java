package com.shirbi.downfall;

import java.util.ArrayList;

public class SmartRotationResult {
    public Wheel m_wheel;
    public int m_angle;

    public class SimulatedToken {
        Token m_token;
        int m_target_wheel_num;
    }

    public class SimulatedTokenList extends ArrayList<SimulatedToken> {
    }

    SimulatedTokenList[] m_fall_token_list = new SimulatedTokenList[PlayerType.NUM_PLAYERS];
    Token[] m_top_occupied_token = new Token[PlayerType.NUM_PLAYERS];

    int m_fall_token[] = new int[PlayerType.NUM_PLAYERS];
    int m_top_occupied_hole_count[] = new int[PlayerType.NUM_PLAYERS];
    int m_bottom_empty_hole_with_top_wheel_with_token_count[] = new int[PlayerType.NUM_PLAYERS];
    int m_bottom_empty_hole_count[] = new int[PlayerType.NUM_PLAYERS];

    SmartRotationResult(Wheel wheel) {
        m_wheel = wheel;
        m_angle = 0;
        for (int i = 0; i < PlayerType.NUM_PLAYERS; i++) {
            m_top_occupied_hole_count[i] = 0;
            m_bottom_empty_hole_count[i] = 0;
            m_fall_token[i] = 0;
            m_fall_token_list[i] = new SimulatedTokenList();
            m_top_occupied_token[i] = null;
        }
    }

    SmartRotationResult(SmartRotationResult other) {
        m_wheel = other.m_wheel;
        m_angle = other.m_angle;
        for (int i = 0; i < PlayerType.NUM_PLAYERS; i++) {
            m_top_occupied_hole_count[i] = other.m_top_occupied_hole_count[i];
            m_bottom_empty_hole_count[i] = other.m_bottom_empty_hole_count[i];
            m_bottom_empty_hole_with_top_wheel_with_token_count[i] =
                    other.m_bottom_empty_hole_with_top_wheel_with_token_count[i];
            m_fall_token[i] = other.m_fall_token[i];
            m_fall_token_list[i] = new SimulatedTokenList();
            m_fall_token_list[i].addAll(other.m_fall_token_list[i]);
            m_top_occupied_token[i] = other.m_top_occupied_token[i];
        }
    }

    private int diff_ai_human(int my_array[], int other_array[], PlayerType player_type) {
        int my_diff = my_array[player_type.getInt()] - my_array[player_type.getOppositeInt()];
        int other_diff = other_array[player_type.getInt()] - other_array[player_type.getOppositeInt()];

        return my_diff - other_diff;
    }

    private SimulatedToken GetPreviousSimulatedFallToken(Token token) {
        SimulatedTokenList token_list = m_fall_token_list[token.GetPlayerType().getInt()];

        for (SimulatedToken other_simulated_token : token_list) {
            if (other_simulated_token.m_token == token.GetPreviousToken()) {
                return other_simulated_token;
            }
        }

        return null;
    }

    private int BadOrderCount(PlayerType playerType) {
        int bad_order_count = 0;
        int player_index = playerType.getInt();
        SimulatedTokenList token_list = m_fall_token_list[player_index];

        for (SimulatedToken simulated_token : token_list) {
            Token token = simulated_token.m_token;

            // This can happen when two tokens from input queue falls together
            if (token == null) {
                continue;
            }

            Token previous = token.GetPreviousToken();
            if (previous == null) {
                continue;
            }

            int wheel_num = simulated_token.m_target_wheel_num;

            boolean previous_token_falls_too = false;
            SimulatedToken previous_simulated_token = null;
            int index_of_previous = -1;
            for (SimulatedToken other_simulated_token : token_list) {
                if (other_simulated_token.m_token == previous) {
                    index_of_previous = token_list.indexOf(other_simulated_token);
                    previous_simulated_token = other_simulated_token;
                    previous_token_falls_too = true;
                    // don't break. Maybe previous token falls twice, and then we want to
                    // check the last wheel it reaches.
                }
            }

            if (previous_token_falls_too) {
                if (previous_simulated_token.m_target_wheel_num > simulated_token.m_target_wheel_num) {
                    continue;
                }

                if (previous_simulated_token.m_target_wheel_num < simulated_token.m_target_wheel_num) {
                    bad_order_count++;
                    continue;
                }

                int index_of_this_token = token_list.indexOf(simulated_token);
                if (index_of_previous > index_of_this_token) {
                    bad_order_count++;
                    continue;
                }
            } else {
                // If we got here, previous token doesn't move in this turn.
                int previous_wheel_num = previous.GetOwnerWheel().GetWheelNum();

                if (previous_wheel_num > wheel_num) {
                    continue;
                }

                if (previous_wheel_num < wheel_num) {
                    bad_order_count++;
                    continue;
                }
            }
        }

        return bad_order_count;
    }

    private int BadOrderTokenReadyToFall(PlayerType playerType) {
        int bad_order_count = 0;

        if (m_wheel.GetWheelNum() == 0) {
            return 0;
        }

        Token token = m_top_occupied_token[playerType.getInt()];
        if (token == null) {
            return 0;
        }

        Token previous = token.GetPreviousToken();
        if (previous == null) {
            return 0;
        }

        SimulatedToken previous_simulated = GetPreviousSimulatedFallToken(token);

        int previous_wheel_num = (previous_simulated == null) ? previous.GetOwnerWheel().GetWheelNum() :
                previous_simulated.m_target_wheel_num;

        if (previous_wheel_num <= m_wheel.GetWheelNum()) {
            return 1;
        }

        return 0;
    }

    Boolean IsBetterThan(SmartRotationResult other, PlayerType player_type) {
        if (other == null) {
            return true;
        }

        int ai_bad_order = BadOrderCount(player_type) - other.BadOrderCount(player_type);
        if (ai_bad_order != 0) {
            return (ai_bad_order < 0);
        }

        int human_bad_order = BadOrderCount(player_type.GetOpposite()) - other.BadOrderCount(player_type.GetOpposite());
        if (human_bad_order != 0) {
            return (human_bad_order > 0);
        }

        int diff_fall_token = diff_ai_human(m_fall_token, other.m_fall_token, player_type);
        if (diff_fall_token > 0) {
            return true;
        }
        if (diff_fall_token < 0) {
            return false;
        }

        int ai_bad_order_ready_to_fall = BadOrderTokenReadyToFall(player_type) - other.BadOrderTokenReadyToFall(player_type);
        if (ai_bad_order_ready_to_fall != 0) {
            return (ai_bad_order_ready_to_fall < 0);
        }

        int human_bad_order_ready_to_fall = BadOrderTokenReadyToFall(player_type.GetOpposite()) - other.BadOrderTokenReadyToFall(player_type.GetOpposite());
        if (human_bad_order_ready_to_fall != 0) {
            return (human_bad_order_ready_to_fall > 0);
        }

        int diff_token_fit = diff_ai_human(m_top_occupied_hole_count, other.m_top_occupied_hole_count, player_type);
        if (diff_token_fit > 0) {
            return true;
        }
        if (diff_token_fit < 0) {
            return false;
        }

        int diff_hole_fit_to_top_wheel_with_token = diff_ai_human(
                m_bottom_empty_hole_with_top_wheel_with_token_count,
                other.m_bottom_empty_hole_with_top_wheel_with_token_count, player_type);

        if (diff_hole_fit_to_top_wheel_with_token > 0) {
            return true;
        }
        if (diff_hole_fit_to_top_wheel_with_token < 0) {
            return false;
        }

        int diff_hole_fit = diff_ai_human(m_bottom_empty_hole_count, other.m_bottom_empty_hole_count, player_type);
        if (diff_hole_fit > 0) {
            return true;
        }
        if (diff_hole_fit < 0) {
            return false;
        }

        if (Math.abs(m_angle) < Math.abs(other.m_angle)) {
            return true;
        }

        return false;
    }

    public void AddFallToken(int player_index, Token token, int target_wheel_num) {
        m_fall_token[player_index]++;

        SimulatedToken simulated_token = new SimulatedToken();
        simulated_token.m_token = token;
        simulated_token.m_target_wheel_num = target_wheel_num;

        m_fall_token_list[player_index].add(simulated_token);
    }
}
