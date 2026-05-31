package com.quiz.quizgame.model;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question")
public class Question {
    @Id
    private String id;
    private String quizId;
    private String text;
    private String type;
    private int timeLimit;

    @Transient
    private List<Option> optionList = new ArrayList<>();

}
