package com.quiz.quizgame.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quiz")
public class Quiz {

    @Id
    private String id;

    private String name;
    private int maxPlayers;

    @Transient
    private List<Question> questionList = new ArrayList<>();
}