package com.quiz.quizgame.controller;

import com.quiz.quizgame.model.Quiz;
import com.quiz.quizgame.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;


    @GetMapping
    public ResponseEntity<List<Quiz>> getQuizList() {
        try {
            List<Quiz> quizzes = quizService.getQuizList();
            return new ResponseEntity<>(quizzes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable String id) {
        try {
            if (id == null || id.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Quiz quiz = quizService.getQuizById(id);

            if (quiz == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(quiz, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping
    public ResponseEntity<String> addQuiz(@RequestBody Quiz quiz) {
        try {
            if (quiz == null) {
                return new ResponseEntity<>("Quiz cannot be null!", HttpStatus.BAD_REQUEST);
            }
//            if (quiz.getId() == null || quiz.getId().isEmpty()) {
//                return new ResponseEntity<>("Quiz ID cannot be empty!", HttpStatus.BAD_REQUEST);
//            }
            if (quiz.getName() == null || quiz.getName().isEmpty()) {
                return new ResponseEntity<>("Quiz name cannot be empty!", HttpStatus.BAD_REQUEST);
            }
            quizService.addQuiz(quiz);
            return new ResponseEntity<>("Quiz created successfully", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping
    public ResponseEntity<String> updateQuiz(@RequestBody Quiz quiz) {
        try {
            if (quiz == null) {
                return new ResponseEntity<>("Quiz cannot be null!", HttpStatus.BAD_REQUEST);
            }
            if (quiz.getId() == null || quiz.getId().isEmpty()) {
                return new ResponseEntity<>("Quiz ID cannot be empty!", HttpStatus.BAD_REQUEST);
            }
            quizService.updateQuiz(quiz);
            return new ResponseEntity<>("Quiz updated successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuiz(@PathVariable String id) {
        try {
            if (id == null || id.isEmpty()) {
                return new ResponseEntity<>("Quiz ID cannot be empty!", HttpStatus.BAD_REQUEST);
            }

            quizService.deleteQuiz(id);
            return new ResponseEntity<>("Quiz deleted successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}