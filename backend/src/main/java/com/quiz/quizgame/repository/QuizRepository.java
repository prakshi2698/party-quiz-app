package com.quiz.quizgame.repository;

import com.quiz.quizgame.model.Quiz;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import com.quiz.quizgame.exception.VersionConflictException;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class QuizRepository {

    //1)table reference
    private final DynamoDbTable<Quiz> quizTable;

    //2)connecting to table: quiz in dynamo db
    //EnhancedClient is inserted by spring(bean we created in DynamoDbConfig)
    //quiz is table name in dynamo db
    //TableSchema.fromBean(Quiz.class) reads @DynamoDbBean annotations to understand the table structure
    //this replaces what JPA/Hibernate did automatically from @Entity
    public QuizRepository(DynamoDbEnhancedClient enhancedClient) {
        this.quizTable = enhancedClient.table("quiz", TableSchema.fromBean(Quiz.class));
    }

    //3)Methods to fetch data from table in DB
    //scan whole table
    //.scan()-fetches every item in the table,same as SELECT * FROM quiz
    public List<Quiz> findAll() {
        return quizTable.scan().items().stream().collect(Collectors.toList());
    }

    //fetch ONE quiz by its id
    public Optional<Quiz> findById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        Quiz quiz = quizTable.getItem(key);
        return Optional.ofNullable(quiz);
    }

    //Check if quiz exists
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    //create OR update quiz
    //if id exists: update quiz
    //if id new: creates it
    public void save(Quiz quiz) {
        quizTable.putItem(quiz);
    }

    //Versioning:Method to remove race condition
    //save to DB only if version in DB matches expectedVersion
    //quiz            : the quiz object we want to save
    //expectedVersion : version number we fetched from DB
    public void saveWithVersionCheck(Quiz quiz, int expectedVersion) {
        //condition: save only if DB version = expectedVersion
        //Expression: is condition we send to DDB telling it when to allow save
        //Expression.builder() = start building the condition
        Expression condition = Expression.builder()
                //condition in DDB lang. save only if below condition met
                .expression(
                        //save if version in DB equals our expectedVersion
                        //:expectedVersion → placeholder, actual value set below like ? in SQL queries
                        "version = :expectedVersion " +
                                //OR save if version field doesn't exist at all
                                "OR attribute_not_exists(version)"
                )
                //expressionValues: replace placeholders with actual value like filling in the ? in SQL
                .expressionValues(Map.of(
                        ":expectedVersion", //map placeholder name to actual value
                        //DynamoDB stores values in its own format,we cant pass int directly to DDB
                        //wrap it in AttributeValue object
                        AttributeValue.builder()
                                .n(String.valueOf(expectedVersion))//n() means NUMBER type in DynamoDB
                                //String.valueOf(1) converts int 1 to "1"
                                //DynamoDB takes number as string internally(hence we first take int then
                                // convert it to string)
                                .build()
                        //so if expectedVersion = 3: ":expectedVersion" → AttributeValue(n="3")
                        // condition becomes: "version = 3 OR attribute_not_exists(version)"
                ))
                .build();//finishes building the condition expression its now ready to use
        try{
            //save item to DynamoDB table with condition
            quizTable.putItem(
                    // PutItemEnhancedRequest: a request object that holds:
                    //-what to save (the quiz)
                    //-under what condition (version check)
                    PutItemEnhancedRequest.builder(Quiz.class)
                            //this is the quiz we want to save with incremented version inside it
                            .item(quiz)
                            //attach our version condition to save request
                            //tells DynamoDB: "only save if this condition is true"
                            .conditionExpression(condition)
                            //finish building the save request
                            //request is now ready to send to DynamoDB
                            .build()
            );
            //DynamoDB processes the request:
            //checks condition and
            //→ if condition true  → saves quiz
            //→ if condition false → throws ConditionalCheckFailedException

        }catch (ConditionalCheckFailedException e){
            //version mismatch, someone else updated quiz
            //version in DB != expectedVersion
            throw new VersionConflictException(quiz.getId());
        }


    }

    //delete quiz by id
    public void deleteById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        quizTable.deleteItem(key);
    }
}