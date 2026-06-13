package com.quiz.quizgame.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quiz.quizgame.enums.Symbol;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
@Getter
@Setter
public class Option {
    @Getter(onMethod_ = {@DynamoDbPartitionKey})
    private String id;
    @Getter(onMethod_ = {@DynamoDbSecondaryPartitionKey(indexNames = "questionId-index")})
    private String questionId;
    private String text;
    private Symbol symbol;

    @JsonProperty("isCorrect")
    private boolean correct;
}