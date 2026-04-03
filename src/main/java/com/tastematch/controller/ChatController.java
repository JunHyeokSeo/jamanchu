package com.tastematch.controller;

import com.tastematch.domain.AnonUser;
import com.tastematch.domain.ChatMessage;
import com.tastematch.domain.TasteMatch;
import com.tastematch.dto.ChatMessageResponse;
import com.tastematch.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/chat/{matchId}")
    public String chatPage(@PathVariable Long matchId,
                           HttpServletRequest request,
                           Model model) {
        AnonUser currentUser = (AnonUser) request.getAttribute("currentUser");

        TasteMatch match;
        try {
            match = chatService.getMatchForChat(currentUser, matchId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/matches";
        }

        List<ChatMessage> messages = chatService.getMessages(matchId);

        AnonUser partner = match.getUser1().getId().equals(currentUser.getId())
                ? match.getUser2()
                : match.getUser1();

        model.addAttribute("match", match);
        model.addAttribute("matchId", matchId);
        model.addAttribute("messages", messages);
        model.addAttribute("partnerNickname", partner.getNickname());
        model.addAttribute("currentUser", currentUser);

        return "chat";
    }

    @PostMapping("/chat/{matchId}/send")
    public String sendMessage(@PathVariable Long matchId,
                              @RequestParam String content,
                              HttpServletRequest request) {
        AnonUser currentUser = (AnonUser) request.getAttribute("currentUser");
        chatService.sendMessage(currentUser, matchId, content);
        return "redirect:/chat/" + matchId;
    }

    @GetMapping("/chat/{matchId}/messages")
    @ResponseBody
    public List<ChatMessageResponse> getMessages(@PathVariable Long matchId,
                                                  HttpServletRequest request) {
        AnonUser currentUser = (AnonUser) request.getAttribute("currentUser");
        chatService.getMatchForChat(currentUser, matchId);
        return chatService.getMessages(matchId).stream()
                .map(msg -> ChatMessageResponse.builder()
                        .id(msg.getId())
                        .senderNickname(msg.getSender().getNickname())
                        .senderId(msg.getSender().getId())
                        .content(msg.getContent())
                        .createdAt(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
