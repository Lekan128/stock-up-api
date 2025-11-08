package com.business.business.chat;

import com.business.business.auth.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.lekan128.aiagent.api.Agent;
import io.github.lekan128.aiagent.api.ChatMessage;
import io.github.lekan128.aiagent.api.llm.Gemini;
import io.github.lekan128.aiagent.core.AgentProvider;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

    private final Cache<UUID, List<ChatMessage>> chatCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30)) // auto-expire after 4 hours of inactivity
            .maximumSize(10000)                     // up to 10k user chats
            .build();

    private final Agent agent = AgentProvider.get();
    private final Gemini llm = new Gemini();

    public String chat(String userQuery) throws JsonProcessingException {
        UUID userId = AuthService.getCurrentAuthenticatedUser().getId();
        String aiPersona = String.format("""
                You are an expert inventory management assistant chatting with a client.
                If you need more information from the client ask for it.
                Ask questions after a response if appropriate.
                The currency is naira.
                The user's id = %s
                NOTE: The client does not know about id's, so don't ask them about that.
                """, userId);

        List<ChatMessage> history = chatCache.get(userId, id -> new ArrayList<>());

        // Add user query
        history.add(ChatMessage.newInstance(ChatMessage.Role.USER, userQuery));

        // Call AI
        String response = agent.useChatAgent(userQuery, aiPersona, llm, String.class, history);

        // Add AI response
        history.add(ChatMessage.newInstance(ChatMessage.Role.AGENT, response));

        // Update cache
        chatCache.put(userId, history);

        return response;
    }

    public void clearChat() {
        UUID userId = AuthService.getCurrentAuthenticatedUser().getId();
        chatCache.invalidate(userId);
    }

    public List<ChatMessage> getChatHistory() {
        UUID userId = AuthService.getCurrentAuthenticatedUser().getId();
        List<ChatMessage> chatHistory = chatCache.getIfPresent(userId);
        return chatHistory != null ? chatHistory : new ArrayList<>();
    }
}
