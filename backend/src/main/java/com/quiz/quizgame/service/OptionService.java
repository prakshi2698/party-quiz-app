package com.quiz.quizgame.service;

import com.quiz.quizgame.model.Option;
import com.quiz.quizgame.repository.OptionRepository;
import com.quiz.quizgame.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OptionService {
    @Autowired
    private OptionRepository optionRepository;

    public List<Option> getOptionListByQuestionId(String questionId) {

        return optionRepository.findByQuestionId(questionId);
    }
    //fetches the list of option by questionIDs
    public List<Option> getOptionListByQuestionsIds(List<String> questionIds) {
        return optionRepository.findByQuestionIdIn(questionIds);
    }

    public Option getOptionById(String optionId) {
        return optionRepository.findById(optionId).orElse(null);
    }

    public void addOption(Option option) {
//        option.setId(UUID.randomUUID().toString());
        option.setId(IdGenerator.generateOptionId());
        optionRepository.save(option);
    }


    public Option updateOption(Option option) {

        //Check in DB
        if (!optionRepository.existsById(option.getId())) {
            throw new RuntimeException("Option not found: " + option.getId());
        }
        //Save to DB
        optionRepository.save(option);
        return optionRepository.findById(option.getId()).orElse(null);
    }

    public void deleteOptionById(String optionId) {
        // Check in DB
        if (!optionRepository.existsById(optionId)) {
            throw new RuntimeException("Option not found: " + optionId);
        }
        //delete in db
        optionRepository.deleteById(optionId);
    }
}
