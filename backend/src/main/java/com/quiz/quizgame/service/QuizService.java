package com.quiz.quizgame.service;

import com.quiz.quizgame.model.Question;
import com.quiz.quizgame.model.Quiz;
import com.quiz.quizgame.repository.QuizRepository;
import com.quiz.quizgame.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionService questionService;

    private Quiz fillQuizWithQuestions(Quiz original) {

        Quiz quizCopy = new Quiz();
        quizCopy.setId(original.getId());
        quizCopy.setName(original.getName());
        quizCopy.setMaxPlayers(original.getMaxPlayers());

        List<Question> questions = questionService.getQuestionListByQuizId(original.getId());

        quizCopy.setQuestionList(new ArrayList<>(questions));

        return quizCopy;
    }


    public List<Quiz> getQuizList() {
        //get list of quiz from DB
        List<Quiz> quizzes = quizRepository.findAll();
        List<Quiz> quizzesCopy = new ArrayList<>();

        for (Quiz quiz : quizzes) {
            quizzesCopy.add(fillQuizWithQuestions(quiz));
        }

        return quizzesCopy;
    }


    public Quiz getQuizById(String quizId) {
        // get quiz by id
        Quiz finalQuiz = quizRepository.findById(quizId).orElse(null);
        //check if question is there in DB if yes then put the question in the quiz
        if (finalQuiz == null) {
            return null;
        }

        return fillQuizWithQuestions(finalQuiz);
    }

    public void addQuiz(final Quiz quiz) {
//        quiz.setId(UUID.randomUUID().toString());
        quiz.setId(IdGenerator.generateQuizId());
        quizRepository.save(quiz);
    }

    public void updateQuiz(final Quiz quiz) {
        //check for quiz in DB
        if (!quizRepository.existsById(quiz.getId())) {
            throw new RuntimeException("Quiz not found: " + quiz.getId());
        }
        //update/save quiz in db
        quizRepository.save(quiz);
    }

    public void deleteQuiz(final String id) {
        // check if quiz in db
        if (!quizRepository.existsById(id)) {
            throw new RuntimeException("Quiz not found: " + id);
        }
        //delete quiz from db
        quizRepository.deleteById(id);
    }
}
