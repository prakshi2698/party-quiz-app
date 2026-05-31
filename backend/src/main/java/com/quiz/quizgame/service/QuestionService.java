package com.quiz.quizgame.service;

import com.quiz.quizgame.model.Option;
import com.quiz.quizgame.model.Question;
import com.quiz.quizgame.repository.QuestionRepository;
import com.quiz.quizgame.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OptionService optionService;


    private Question fillQuestionWithOptions(Question original) {
        List<Option> options = optionService.getOptionListByQuestionId(original.getId());
        original.setOptionList(new ArrayList<>(options));
        return original;
    }

    public List<Question> getQuestionListByQuizId(String quizId) {
        //1. fetch list of questions
        List<Question> questions = questionRepository.findByQuizId(quizId);
        if (questions.isEmpty()) {
            return questions;
        }
        //2. collect question ID to list
        List<String> questionIds = questions.stream().map(Question::getId).toList();

        //3. fetch all options for the questions
        List<Option> allOptions = optionService.getOptionListByQuestionsIds(questionIds);
        // [{1, 1, "xyz", false}, {}, {}, {}]

        Map<String,List<Option>> questionIdToOptionList = new HashMap<>();
        for (Option option : allOptions) {
            String questionId = option.getQuestionId();
            //if dict dont have matching ques id in key then create empty list for it
            if (!questionIdToOptionList.containsKey(questionId)) {
                questionIdToOptionList.put(questionId, new ArrayList<>());
            }
            //else add option to question list
            questionIdToOptionList.get(questionId).add(option);
        }
        //attach option to each questions
        //questions:[list of options]
        for (Question question : questions) {
            String questionId = question.getId();
            //check if map has questionID as key
            if (questionIdToOptionList.containsKey(questionId)) {
                //if it has then check for ques id from map and append options to question
                question.setOptionList(questionIdToOptionList.get(questionId));
            }
            else{
                question.setOptionList(new ArrayList<>());
            }
        }
        return questions;



    }

    public Question getQuestionById(String questionId) {
        Question question = questionRepository.findById(questionId).orElse(null);
        if (question != null) {
            return fillQuestionWithOptions(question);
        }
        return null;
    }

    public void addQuestion(Question question) {
//        question.setId(UUID.randomUUID().toString());
        question.setId(IdGenerator.generateQuestionId());
        questionRepository.save(question);
    }

    public Question updateQuestion(Question question) {

        //check question if exist in DB
        if (!questionRepository.existsById(question.getId())) {
            throw new RuntimeException("Question not found: " + question.getId());
        }
        //save to DB
        questionRepository.save(question);
        return fillQuestionWithOptions(question);
    }

    public void deleteQuestion(String questionId) {
        //Check in DB
        if (!questionRepository.existsById(questionId)) {
            throw new RuntimeException("Question not found: " + questionId);
        }
        //delete ques from DB
        questionRepository.deleteById(questionId);
    }
}
