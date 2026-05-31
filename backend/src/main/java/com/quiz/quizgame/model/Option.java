package com.quiz.quizgame.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quiz.quizgame.enums.Symbol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "option")
public class Option {

    @Id
    private String id;

    private String questionId;
    private String text;

    @Enumerated(EnumType.STRING)
    private Symbol symbol;

    @JsonProperty("isCorrect")
    private boolean correct;
}