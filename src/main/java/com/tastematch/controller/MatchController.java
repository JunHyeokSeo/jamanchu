package com.tastematch.controller;

import com.tastematch.domain.AnonUser;
import com.tastematch.dto.MatchResponse;
import com.tastematch.service.MatchService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/matches")
    public String matches(HttpServletRequest request, Model model) {
        AnonUser currentUser = (AnonUser) request.getAttribute("currentUser");
        matchService.calculateMatches(currentUser);
        List<MatchResponse> myMatches = matchService.getMyMatches(currentUser);

        model.addAttribute("matches", myMatches);
        model.addAttribute("currentUser", currentUser);

        return "matches";
    }

    @PostMapping("/matches/{id}/accept")
    public String accept(@PathVariable Long id, HttpServletRequest request) {
        AnonUser currentUser = (AnonUser) request.getAttribute("currentUser");
        matchService.acceptMatch(currentUser, id);
        return "redirect:/matches";
    }

    @PostMapping("/matches/{id}/decline")
    public String decline(@PathVariable Long id, HttpServletRequest request) {
        AnonUser currentUser = (AnonUser) request.getAttribute("currentUser");
        matchService.declineMatch(currentUser, id);
        return "redirect:/matches";
    }
}
