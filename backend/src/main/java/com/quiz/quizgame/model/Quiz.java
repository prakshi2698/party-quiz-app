package com.quiz.quizgame.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.ArrayList;
import java.util.List;

@DynamoDbBean
@Getter
@Setter
public class Quiz {

    @Getter(onMethod_ = {@DynamoDbPartitionKey})
    private String id;

    private String name;
    private int maxPlayers;
    private int version;
    private List<Question> questionList = new ArrayList<>();  // stored as JSON array in DynamoDB
}