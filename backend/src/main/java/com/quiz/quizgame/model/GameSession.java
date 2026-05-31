package com.quiz.quizgame.model;

import java.util.List;
import lombok.Data;
import com.quiz.quizgame.enums.GameStatus;

@Data
public class GameSession {
    private String session_id;
    private Quiz quiz_id;
    private String hostId;
    private GameStatus status;
    private short game_pin;
    private long start_time;
    private List<Player> players_lst;

}