package com.quiz.quizgame.service;

import com.quiz.quizgame.cache.LRUCache;
import com.quiz.quizgame.model.Option;
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
    private LRUCache lruCache; //inject LRU cache

    //returns all quizzes, each with questions and options inside
    public List<Quiz> getQuizList() {
        //calls findAll method in quizRepo & repo will scan entire quiz table in dynamo
        //Dynamo DB will return each quiz item with ques & options nested inside
        return  quizRepository.findAll(); //return list of all quizzes
    }

    //returns one quiz with all its questions and options
    //Caching:
    //1)in this check cache first->find quiz id in cache(CACHE HIT) return from cache-no DB call
    //2)cache Miss: fetch from dynamoDB and save to cache for next time
    public Quiz getQuizById(String quizId) {
        //1) Check cache first
        Quiz cachedQuiz = lruCache.get(quizId);
        if (cachedQuiz != null) { //cache HIT
            return cachedQuiz;//return from cache-no DB call
        }
        //2) Cache miss: find from Dynamo DB
        //findByID goes to dynamo, finds item with matching quizID
        //& returns the final quiz object with ques list nested in it
        //if quiz not found: return null
        Quiz quiz = quizRepository.findById(quizId).orElse(null);

        //3) save to cache for next time
        if (quiz != null) {
            lruCache.put(quizId, quiz);
        }
        return quiz;
    }

    //creates new quiz with empty quesList in DynamoDB
    public void addQuiz(final Quiz quiz) {
        //generate unique id like "abc123" and set it on quiz
        quiz.setId(IdGenerator.generateQuizId());
        //for new quiz set initial version at 0
        quiz.setVersion(0);
        if (quiz.getQuestionList() == null) {
            quiz.setQuestionList(new ArrayList<>());
        }
        //save quiz to dynamoDB, quiz starts with empty quesList
        quizRepository.save(quiz);
        //save/put in cache as well so next GET is fast
        lruCache.put(quiz.getId(), quiz);
    }

    //update Quiz name or maxPlayers
    public void updateQuiz(final Quiz quiz) {
        //check for quiz in DB
        if (!quizRepository.existsById(quiz.getId())) {
            throw new RuntimeException("Quiz not found: " + quiz.getId());
        }
        //Get the current version, increment the version and save with current version
        //will either save to DDB if version matches or new version else rejects
        int expectedVersion = quiz.getVersion();
        quiz.setVersion(expectedVersion + 1);
        //update/save quiz in db
        quizRepository.saveWithVersionCheck(quiz, expectedVersion);
        // update cache directly with new data
        // instead of evicting, put updated quiz in cache
        lruCache.put(quiz.getId(), quiz);
    }

    //deletes entire quiz including all questions & options in one shot
    public void deleteQuiz(final String id) {
        // check if quiz in db
        if (!quizRepository.existsById(id)) {
            throw new RuntimeException("Quiz not found: " + id);
        }
        //delete quiz from db- this will dlt everything:quiz along with ques & optn
        //since its stored in one item now
        quizRepository.deleteById(id);
        // remove from cache as well
        // quiz is gone, no point keeping it in cache
        lruCache.evict(id);
    }

    // For Questions

    //return all ques for quiz
    public List<Question> getQuestionListByQuizId(String quizId) {
        //fetch all quiz from Dynamodb-quiz has quesList in it
        Quiz quiz = getQuizById(quizId);

        //if quiz not found:return empty list
        if (quiz == null) return new ArrayList<>();

        //quiz obj has quesList field we'll return it directly
        return quiz.getQuestionList();
    }

    //finds and return the specific ques by quizId
    public Question getQuestionById(String quizId, String questionId) {
        //fetch full quiz from DynamoDB
        Quiz quiz = getQuizById(quizId);

        //if quiz not found return null
        if (quiz == null) return null;

        //loop through each question and for each ques check if quesId
        //we're looking for matches id of curr ques we are on
        for (Question question : quiz.getQuestionList()) {
            if (questionId.equals(question.getId())) {
                return question;
            }
        }
        return null;

    }

    //add new ques in quesList of dynamoDB's quiz
    //first fetch quiz, then add ques to quesLst and save quiz back
    public void addQuestion(String quizId, Question question) {

        //1)Fetch quiz from dynamoDB
        Quiz quiz = getQuizById(quizId);

        //if quiz not found then throw error
        if (quiz == null) throw new RuntimeException("Quiz not found: " + quizId);

        //2)generate unique ID for new ques like "wpiSP"
        question.setId(IdGenerator.generateQuestionId());

        //3)Link ques to its quiz by storing quizId inside question
        question.setQuizId(quizId);

        //4)if quesList in quiz is null, then create the empty quesList first
        if (quiz.getQuestionList() == null) {
            quiz.setQuestionList(new ArrayList<>());
        }

        //5)Add new ques to quiz's quesList in memory
        quiz.getQuestionList().add(question);//so quiz obj now has ques in it

        // two race conditions, who saves quiz last with their question
        // secondly, who updates lru cache last. There can be mismatches between db and cache
        // states

        //Get the current version, increment the version and save with current version
        //will either save to DDB if version matches or new version else rejects
        int expectedVersion = quiz.getVersion();
        quiz.setVersion(expectedVersion + 1);
        //6)Save whole quiz back to dynamoDB
        //it will store whole quiz with nested ques in it.
        quizRepository.saveWithVersionCheck(quiz, expectedVersion);

        // quiz object already has updated question in memory
        // directly update cache with this updated quiz object
        lruCache.put(quizId, quiz);

    }
    //updates existing ques type/txt/timeLimit present in quiz
    public void updateQuestion(String quizId, Question updatedQuestion) {
        //1)Fetch quiz from dynamoDB
        Quiz quiz = getQuizById(quizId);

        if (quiz == null) throw new RuntimeException("Quiz not found: " + quizId);

        //2)get quesList from quiz
        List<Question> questions = quiz.getQuestionList();

        //3)Loop through each ques one by one
        for (int i = 0; i < questions.size(); i++) {

            //found ques we want to update
            if (questions.get(i).getId().equals(updatedQuestion.getId())) {

                //4) copy existing options to updated question
                updatedQuestion.setOptionList(questions.get(i).getOptionList());

                //5) replace old ques with updated ques at same pos/index
                questions.set(i, updatedQuestion);

                //Get the current version, increment the version and save with current version
                //will either save to DDB if version matches or new version else rejects
                int expectedVersion = quiz.getVersion();
                quiz.setVersion(expectedVersion + 1);
                //6)save quiz back to dynamoDB with updated ques
                //it will store whole quiz with nested ques in it.
                quizRepository.saveWithVersionCheck(quiz, expectedVersion);

                // quiz object already has updated question in memory
                // directly update cache with this updated quiz object
                lruCache.put(quizId, quiz);
                //exit method
                return;
            }
        }
        //if loop finished without finding question → throw error
        throw new RuntimeException("Question not found: " + updatedQuestion.getId());
    }

    //remove ques from quesList all its option should b deleted automatically
    public void deleteQuestion(String quizId, String questionId) {

        //1)fetch quiz from dynamoDb
        Quiz quiz = getQuizById(quizId);

        if (quiz == null) throw new RuntimeException("Quiz not found: " + quizId);

        //2)loop through each ques if id of ques to be removed matches curr quesId
        //then remove it and mark removed as true and stop loop

        boolean isRemoved = false; //set variable to false
        //loop through each question
        for (Question question : quiz.getQuestionList()) {
            //if id of ques to be removed matches curr quesId
            if (questionId.equals(question.getId())) {
                //remove ques from the quesList in quiz
                quiz.getQuestionList().remove(question);
                //mark isRemoved to true and stop loop
                isRemoved = true;
                break;
            }
        }
        //if loop finishes without match removed stays false and below error thrown
        if (!isRemoved) throw new RuntimeException("Question not found: " + questionId);
        //Get the current version, increment the version and save with current version
        //will either save to DDB if version matches or new version else rejects
        int expectedVersion = quiz.getVersion();
        quiz.setVersion(expectedVersion + 1);
        //3)Save quiz with ques removed back to dynamoDB
        //all option in ques will b removed automatically
        quizRepository.saveWithVersionCheck(quiz, expectedVersion);


        // quiz object already has updated question in memory
        // directly update cache with this updated quiz object
        lruCache.put(quizId, quiz);
    }

    //Option

    //return option list inside the ques
    public List<Option> getOptionListByQuestionId(String quizId, String questionId) {

        //1)calls getQuestionById() method above which fetches quiz from DynmoDB
        //then find ques inside quiz
        Question question = getQuestionById(quizId, questionId);

        //if ques not found return empty lst
        if (question == null) return new ArrayList<>();

        //ques already has option lst in it return it directly
        return question.getOptionList();
    }

    //find and return the option based on given ques ID
    public Option getOptionById(String quizId, String questionId, String optionId) {
        //1) fetch the quiz from dynamoDb then find ques inside quiz using getQuestionById()
        Question question = getQuestionById(quizId, questionId);

        //if qyes not found retun null & stop
        if (question == null) return null;

        //2) loop through each option in optionList to find matching option
        for (Option option : question.getOptionList()) {
            //if given optionId matches the option id inside ques then return option
            if (optionId.equals(option.getId())) {
                return option;
            }
        }
        //if loop finishes without match return null, option not found
        return null;
    }

    //add option to the question inside quiz
    //fetches quiz first then find ques inside then add options then saves quiz back
    public void addOption(String quizId, String questionId, Option option) {
        //1)Fetch quiz from dynamoDB
        Quiz quiz = getQuizById(quizId);

        //if quiz not found return error
        if (quiz == null) throw new RuntimeException("Quiz not found: " + quizId);

        //2)Loop through quesList to find matching ques
        Question foundQuestion = null;
        //check each ques in questList, if its id same as given quesId
        // then mark it as foundQuestion and stop the loop
        for (Question question : quiz.getQuestionList()) {
            if (questionId.equals(question.getId())) {
                foundQuestion = question;
                break;
            }
        }
        //if ques not found throw error
        if (foundQuestion == null) throw new RuntimeException("Question not found: " + questionId);

        //3)Generate unique Id for new option
        option.setId(IdGenerator.generateOptionId());

        //4)Link option to its ques
        option.setQuestionId(questionId);

        //5)add option to question's optionList in memory
        foundQuestion.getOptionList().add(option);
        //Get the current version, increment the version and save with current version
        //will either save to DDB if version matches or new version else rejects
        int expectedVersion = quiz.getVersion();
        quiz.setVersion(expectedVersion + 1);
        //6)save whole quiz with nested quest and option back to dynamoDB
        quizRepository.saveWithVersionCheck(quiz, expectedVersion);

        // quiz object already has updated question in memory
        // directly update cache with this updated quiz object
        lruCache.put(quizId, quiz);
    }

    //update option in quesList within the quiz
    public void updateOption(String quizId, String questionId, Option updatedOption) {
        //1)Fetch quiz from dynamoDB
        Quiz quiz = getQuizById(quizId);

        if (quiz == null) throw new RuntimeException("Quiz not found: " + quizId);

        //2)loop through quesList to find matching ques
        Question foundQuestion = null;
        for (Question question : quiz.getQuestionList()) {
            if (questionId.equals(question.getId())) {
                foundQuestion = question;
                break;
            }
        }
        //if ques not found throw error
        if (foundQuestion == null) throw new RuntimeException("Question not found: " + questionId);

        //3)Loop through optionList to find matching option
        //find optionlst separately:store the optionList of foundques in list
        List<Option> options = foundQuestion.getOptionList();
        //iterate through the option list
        for (int i = 0; i < options.size(); i++) {
            //if id of option at any position or index matches the id of given option
            if (options.get(i).getId().equals(updatedOption.getId())) {
                //then replace old option with updated one at same position
                options.set(i, updatedOption);
                //Get the current version, increment the version and save with current version
                //will either save to DDB if version matches or new version else rejects
                int expectedVersion = quiz.getVersion();
                quiz.setVersion(expectedVersion + 1);
                //save whole quiz back to dynamoDB
                quizRepository.saveWithVersionCheck(quiz, expectedVersion);

                // quiz object already has updated question in memory
                // directly update cache with this updated quiz object
                lruCache.put(quizId, quiz);
                //stop after saving
                return;
            }
        }
        // if loop finished without finding option then throw error
        throw new RuntimeException("Option not found: " + updatedOption.getId());
    }
    //delete option
    public void deleteOption(String quizId, String questionId, String optionId) {
        //1)fetch quiz from DynamoDB
        Quiz quiz = getQuizById(quizId);
        if (quiz == null) throw new RuntimeException("Quiz not found: " + quizId);

        //2)loop through questionList to find matching question
        Question foundQuestion = null;
        for (Question question : quiz.getQuestionList()) {
            if (questionId.equals(question.getId())) {
                foundQuestion = question;
                break;
            }
        }
        //if ques not found throw error
        if (foundQuestion == null) throw new RuntimeException("Question not found: " + questionId);

        //3)Loop through optionList to find and remove matching option
        boolean isRemoved = false;
        //loop through each option in option list
        for (Option option : foundQuestion.getOptionList()) {
            //if given option id matches the current optionId(in loop)
            if (optionId.equals(option.getId())) {
                //then option found so remove that option from optionlist
                foundQuestion.getOptionList().remove(option);
                //and set isRemoved flag to true
                isRemoved = true;
                break;
            }
        }
        //4) if nothing removed means option not found, so throw error
        if (!isRemoved) throw new RuntimeException("Option not found: " + optionId);
        //Get the current version, increment the version and save with current version
        //will either save to DDB if version matches or new version else rejects
        int expectedVersion = quiz.getVersion();
        quiz.setVersion(expectedVersion + 1);
        //Save whole quiz back to dynamoDB with removed option
        quizRepository.saveWithVersionCheck(quiz, expectedVersion);
        // quiz object already has updated question in memory
        // directly update cache with this updated quiz object
        lruCache.put(quizId, quiz);
    }
}


