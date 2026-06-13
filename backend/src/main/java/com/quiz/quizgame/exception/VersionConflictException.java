package com.quiz.quizgame.exception;

public class VersionConflictException extends RuntimeException {
    public VersionConflictException(String quizId) {
        super("Quiz " + quizId + "was modified by someone else. " +
                "Please fetch latest data and retry!");
    }

}
