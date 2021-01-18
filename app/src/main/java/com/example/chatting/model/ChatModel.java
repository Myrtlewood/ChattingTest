package com.example.chatting.model;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {
    public Map<String,String> users = new HashMap<>(); //채팅방 유저
    public Map<String,Comment> comments = new HashMap<>(); //채팅방의 대화내용

    public static class Comment{
        public String uid;
        public String message;
        public Object timestamp;
        public Map<String,Object> readUsers = new HashMap<>();
    }
}
