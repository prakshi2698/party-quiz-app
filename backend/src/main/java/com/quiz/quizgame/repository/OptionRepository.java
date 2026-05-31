package com.quiz.quizgame.repository;

import com.quiz.quizgame.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<Option, String> {
    List<Option> findByQuestionId(String questionId);

    //this will select all options where question Id is in string of question ids
    //spring boot will automatically convert it to the sql query-
    // SELECT * FROM option WHERE question_id IN ('q1', 'q2', 'q3')
    List<Option> findByQuestionIdIn(Collection<String> questionIds);
}