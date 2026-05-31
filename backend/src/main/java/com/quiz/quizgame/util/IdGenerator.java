package com.quiz.quizgame.util;

import java.security.SecureRandom;

public class IdGenerator {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateId(int length) {
        if(length <= 0){
            throw new IllegalArgumentException("Length must be greater than 0");
        }
        StringBuilder id = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            id.append(CHARACTERS.charAt(randomIndex));
        }
        return id.toString();
    }
    public static String generateQuizId() {
        return generateId(4);
    }
    public static String generateQuestionId() {
        return generateId(5);
    }
    public static String generateOptionId() {
        return generateId(4);
    }
}
