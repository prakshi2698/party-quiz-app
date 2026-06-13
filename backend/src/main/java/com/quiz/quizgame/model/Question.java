package com.quiz.quizgame.model;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import java.util.ArrayList;
import java.util.List;

@DynamoDbBean
@Getter
@Setter
public class Question {

    @Getter(onMethod_ = {@DynamoDbPartitionKey})
    private String id;
    @Getter(onMethod_ = {@DynamoDbSecondaryPartitionKey(indexNames = "quizId-index")})
    private String quizId;
    private String text;
    private String type;
    private int timeLimit;
    private List<Option> optionList = new ArrayList<>();

}
