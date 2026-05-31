package com.quiz.quizgame.model;

import java.util.List;
import lombok.Data;

@Data
public class Player {
    private String player_session_id;
    private User user;
    private GameSession gameSessionId;
    private int score;
    private List<Answer> answers_lst;
}