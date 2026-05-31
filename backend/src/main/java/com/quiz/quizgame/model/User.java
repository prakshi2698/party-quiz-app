package com.quiz.quizgame.model;
import lombok.Data;
import com.quiz.quizgame.enums.UserRole;
@Data
public class User {
    private String name;
    private String id;
    private UserRole role;
}

