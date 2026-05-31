package com.quiz.quizgame.controller;

import com.quiz.quizgame.model.Option;
import com.quiz.quizgame.model.Question;
import com.quiz.quizgame.service.OptionService;
import com.quiz.quizgame.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class OptionController {

    @Autowired
    private OptionService optionService;

    @Autowired
    private QuestionService questionService;

    @GetMapping("/{quizId}/question/{questionId}/option")
    public ResponseEntity<List<Option>> getOptionListByQuestionId(@PathVariable String quizId,
                                                                  @PathVariable String questionId) {
        try {
            if (quizId == null || quizId.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (questionId == null || questionId.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            List<Option> options = optionService.getOptionListByQuestionId(questionId);
            return new ResponseEntity<>(options, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{quizId}/question/{questionId}/option/{optionId}")
    public ResponseEntity<Option> getOptionById(@PathVariable String quizId,
                                                @PathVariable String questionId,
                                                @PathVariable String optionId) {
        try {
            if (quizId == null || quizId.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (questionId == null || questionId.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (optionId == null || optionId.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Option option = optionService.getOptionById(optionId);

            if (option == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            if (!option.getQuestionId().equals(questionId)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(option, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //Quiz->question->Option
    @PostMapping("/{quizId}/question/{questionId}/option")
    public ResponseEntity<String> addOption(@PathVariable String quizId,
                          @PathVariable String questionId,
                          @RequestBody Option option) {

        try {
//            if (quizId == null || quizId.isEmpty()) {
//                return new ResponseEntity<>("Quiz ID cannot be empty!", HttpStatus.BAD_REQUEST);
//            }
//            if (questionId == null || questionId.isEmpty()) {
//                return new ResponseEntity<>("Question ID cannot be empty!", HttpStatus.BAD_REQUEST);
//            }
            if (option == null) {
                return new ResponseEntity<>("Option cannot be null!", HttpStatus.BAD_REQUEST);
            }
//            if (option.getId() == null || option.getId().isEmpty()) {
//                return new ResponseEntity<>("Option ID cannot be empty!", HttpStatus.BAD_REQUEST);
//            }
            if (option.getText() == null || option.getText().isEmpty()) {
                return new ResponseEntity<>("Option text cannot be empty!", HttpStatus.BAD_REQUEST);
            }

            Question question = questionService.getQuestionById(questionId);
            if (question == null) {
                return new ResponseEntity<>("Question not found!", HttpStatus.NOT_FOUND);
            }

            if (!question.getQuizId().equals(quizId)) {
                return new ResponseEntity<>("Question doesn't belong to this quiz!", HttpStatus.BAD_REQUEST);
            }
            // Create option
            option.setQuestionId(questionId);
            optionService.addOption(option);
            return new ResponseEntity<>("Option created successfully", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{quizId}/question/{questionId}/option/{optionId}")
    public ResponseEntity<String> updateOption(@PathVariable String quizId,
                                               @PathVariable String questionId,
                                               @PathVariable String optionId,
                                               @RequestBody Option option) {
        try {
            if (quizId == null || quizId.isEmpty()) {
                return new ResponseEntity<>("Quiz ID cannot be empty!", HttpStatus.BAD_REQUEST);
            }
            if (questionId == null || questionId.isEmpty()) {
                return new ResponseEntity<>("Question ID cannot be empty!", HttpStatus.BAD_REQUEST);
            }
            if (optionId == null || optionId.isEmpty()) {
                return new ResponseEntity<>("Option ID cannot be empty!", HttpStatus.BAD_REQUEST);
            }
            if (option == null) {
                return new ResponseEntity<>("Option cannot be null!", HttpStatus.BAD_REQUEST);
            }

            Question question = questionService.getQuestionById(questionId);
            if (question == null) {
                return new ResponseEntity<>("Question not found!", HttpStatus.NOT_FOUND);
            }

            if (!question.getQuizId().equals(quizId)) {
                return new ResponseEntity<>("Question doesn't belong to quiz!", HttpStatus.BAD_REQUEST);
            }

            option.setId(optionId);
            option.setQuestionId(questionId);
            optionService.updateOption(option);
            return new ResponseEntity<>("Option updated successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/{quizId}/question/{questionId}/option/{optionId}")
    public ResponseEntity<String> deleteOptionById(@PathVariable String quizId,
                                                   @PathVariable String questionId,
                                                   @PathVariable String optionId) {
        try {
            if (quizId == null || quizId.isEmpty()) {
                return new ResponseEntity<>("Quiz ID cannot be empty!", HttpStatus.BAD_REQUEST);
            }
            if (questionId == null || questionId.isEmpty()) {
                return new ResponseEntity<>("Question ID cannot be empty!", HttpStatus.BAD_REQUEST);
            }
            if (optionId == null || optionId.isEmpty()) {
                return new ResponseEntity<>("Option ID cannot be empty!", HttpStatus.BAD_REQUEST);
            }
            Question question = questionService.getQuestionById(questionId);
            if (question == null) {
                return new ResponseEntity<>("Question not found!", HttpStatus.NOT_FOUND);
            }

            if (!question.getQuizId().equals(quizId)) {
                return new ResponseEntity<>("Question doesn't belong to this quiz!", HttpStatus.BAD_REQUEST);
            }

            // Delete option
            optionService.deleteOptionById(optionId);
            return new ResponseEntity<>("Option deleted successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
