package com.example.foodstore.controller;

import com.example.foodstore.service.OpenAIService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatBotController {

    private final OpenAIService openAIService;

    public ChatBotController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping
    public ResponseEntity<String> askBot(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        String reply = openAIService.getChatBotReply(userMessage);
        return ResponseEntity.ok(reply);
    }
}
