package com.quiz.quizgame.controller;
import com.quiz.quizgame.model.Question;
import com.quiz.quizgame.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/{quizId}/question")
    public ResponseEntity<List<Question>> getQuestionListByQuizId(@PathVariable String quizId) {
        try {
            // Validate input at controller level
            if (quizId == null || quizId.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            List<Question> questions = questionService.getQuestionListByQuizId(quizId);
            return new ResponseEntity<>(questions, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{quizId}/question/{questionId}")
    public ResponseEntity<Question> getQuestionById(@PathVariable String quizId,
                                                    @PathVariable String questionId) {
        try {
            if (quizId == null || quizId.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (questionId == null || questionId.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Question question = questionService.getQuestionById(questionId);

            if (question == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // check if question belongs to quiz
            if (!question.getQuizId().equals(quizId)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(question, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/{quizId}/question")
    public ResponseEntity<String> addQuestion(@PathVariable String quizId, @RequestBody Question question) {
        try {
//            if (quizId == null || quizId.isEmpty()) {
//                return new ResponseEntity<>("Quiz ID cannot be empty!", HttpStatus.BAD_REQUEST);
//            }
            if (question == null) {
                return new ResponseEntity<>("Question cannot be null!", HttpStatus.BAD_REQUEST);
            }
//            if (question.getId() == null || question.getId().isEmpty()) {
//                return new ResponseEntity<>("Question ID cannot be empty!", HttpStatus.BAD_REQUEST);
//            }
            if (question.getText() == null || question.getText().isEmpty()) {
                return new ResponseEntity<>("Question text cannot be empty!", HttpStatus.BAD_REQUEST);
            }

            question.setQuizId(quizId);
            questionService.addQuestion(question);
            return new ResponseEntity<>("Question created successfully", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{quizId}/question/{questionId}")
    public ResponseEntity<String> updateQuestion(@PathVariable String quizId, @PathVariable String questionId, @RequestBody Question question) {
        try {
            if (quizId == null || quizId.isEmpty()) {
                return new ResponseEntity<>("Quiz ID cannot be empty!", HttpStatus.BAD_REQUEST);
            }
            if (questionId == null || questionId.isEmpty()) {
                return new ResponseEntity<>("Question ID cannot be empty!", HttpStatus.BAD_REQUEST);
            }
            if (question == null) {
                return new ResponseEntity<>("Question cannot be null!", HttpStatus.BAD_REQUEST);
            }
            question.setId(questionId);
            question.setQuizId(quizId);
            questionService.updateQuestion(question);
            return new ResponseEntity<>("Question updated successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{quizId}/question/{questionId}")
    public ResponseEntity<String> deleteQuestion(@PathVariable String quizId, @PathVariable String questionId) {
        try {
            if (quizId == null || quizId.isEmpty()) {
                return new ResponseEntity<>("Quiz ID cannot be empty!", HttpStatus.BAD_REQUEST);
            }
            if (questionId == null || questionId.isEmpty()) {
                return new ResponseEntity<>("Question ID cannot be empty!", HttpStatus.BAD_REQUEST);
            }

            questionService.deleteQuestion(questionId);
            return new ResponseEntity<>("Question deleted successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}