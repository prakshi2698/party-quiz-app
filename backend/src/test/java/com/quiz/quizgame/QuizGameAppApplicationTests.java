package com.quiz.quizgame;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class QuizGameAppApplicationTests {

	@Test
	void contextLoads() {
        System.out.println(UUID.randomUUID().toString());
	}

}
