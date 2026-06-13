package com.quiz.quizgame.controller;

import com.quiz.quizgame.exception.VersionConflictException;
import com.quiz.quizgame.model.Option;
import com.quiz.quizgame.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class OptionController {

//    @Autowired
//    private OptionService optionService;
//
//    @Autowired
//    private QuestionService questionService;
    @Autowired
    private QuizService quizService;

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
            List<Option> options = quizService.getOptionListByQuestionId(quizId,questionId);
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

            Option option = quizService.getOptionById(quizId,questionId,optionId);

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
            if (option.getText() == null || option.getText().isEmpty()) {
                return new ResponseEntity<>("Option text cannot be empty!", HttpStatus.BAD_REQUEST);
            }
            quizService.addOption(quizId, questionId, option);
            return new ResponseEntity<>("Option created successfully", HttpStatus.CREATED);
        } catch (VersionConflictException e) {
            // 409 Conflict
            // tells client quiz was modified by someone else
            // client should fetch fresh data and retry
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
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
            option.setId(optionId);
            option.setQuestionId(questionId);
            quizService.updateOption(quizId, questionId, option);
            return new ResponseEntity<>("Option updated successfully", HttpStatus.OK);
        } catch (VersionConflictException e) {
            // 409 Conflict
            // tells client quiz was modified by someone else
            // client should fetch fresh data and retry
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);

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
            quizService.deleteOption(quizId, questionId, optionId);
            return new ResponseEntity<>("Option deleted successfully", HttpStatus.OK);
        } catch (VersionConflictException e) {
            // 409 Conflict
            // tells client quiz was modified by someone else
            // client should fetch fresh data and retry
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
