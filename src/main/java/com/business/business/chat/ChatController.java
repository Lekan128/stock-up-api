package com.business.business.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.lekan128.aiagent.api.ChatMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping()
    public ResponseEntity<String> chat(
            @RequestBody String userMessage
    ) throws JsonProcessingException {
        String response = chatService.chat(userMessage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory() {
        return ResponseEntity.ok(chatService.getChatHistory());
    }

    @DeleteMapping()
    public ResponseEntity<Void> clearChat() {
        chatService.clearChat();
        return ResponseEntity.noContent().build();
    }
}

