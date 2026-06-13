package com.quiz.quizgame.cache;

import com.quiz.quizgame.model.Quiz;

//Node of doubly LL
//each node class stores key,val and ptr to prev & nxt node
public class Node {
    String key;
    Quiz value;
    // expiryTime: when this cache entry expires
    long expiryTime;
    Node prev;
    Node next;
    public Node(String key, Quiz value, long expiryTime) {
        this.key = key;
        this.value = value;
        this.expiryTime = expiryTime;
        this.prev = null;
        this.next = null;
    }

}
