package com.quiz.quizgame.model;

import lombok.Data;

@Data
public class Answer {
    private String id;
    private Player player_session_id;
    private Question question;
    private String selectedOptionId;
    private long timeTaken;
    private boolean isCorrect;
}