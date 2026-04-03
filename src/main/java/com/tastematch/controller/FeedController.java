package com.tastematch.controller;

import com.tastematch.domain.AnonUser;
import com.tastematch.domain.Post;
import com.tastematch.domain.ReactionType;
import com.tastematch.dto.PostRequest;
import com.tastematch.dto.PostResponse;
import com.tastematch.dto.ReactionRequest;
import com.tastematch.service.MatchService;
import com.tastematch.service.PostService;
import com.tastematch.service.ReactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class FeedController {

    private final PostService postService;
    private final ReactionService reactionService;
    private final MatchService matchService;

    private static final List<String> CATEGORIES =
            Arrays.asList("음식", "음악", "영화", "패션", "장소", "취미", "기타");

    @GetMapping("/feed")
    public String feed(@RequestParam(required = false) String category,
                       HttpServletRequest request,
                       Model model) {
        AnonUser currentUser = (AnonUser) request.getAttribute("currentUser");

        List<Post> posts = (category != null && !category.isBlank())
                ? postService.getPostsByCategory(category)
                : postService.getAllPosts();

        List<PostResponse> postResponses = posts.stream()
                .map(post -> postService.getPostResponse(post, currentUser))
                .collect(Collectors.toList());

        model.addAttribute("posts", postResponses);
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("selectedCategory", category);

        return "feed";
    }

    @GetMapping("/posts/new")
    public String newPostForm(HttpServletRequest request, Model model) {
        AnonUser currentUser = (AnonUser) request.getAttribute("currentUser");
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("currentUser", currentUser);
        return "post-form";
    }

    @PostMapping("/posts")
    public String createPost(@Valid @ModelAttribute PostRequest req,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        AnonUser currentUser = (AnonUser) request.getAttribute("currentUser");
        postService.createPost(currentUser, req);
        redirectAttributes.addFlashAttribute("momentumMessage", "취향을 공유했어요. 공감이 쌓이면 자연스럽게 대화로 이어질 수 있어요.");
        return "redirect:/feed";
    }

    @PostMapping("/posts/{id}/react")
    public String react(@PathVariable Long id,
                         @ModelAttribute ReactionRequest req,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        AnonUser currentUser = (AnonUser) request.getAttribute("currentUser");
        ReactionType type;
        try {
            type = ReactionType.valueOf(req.getType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid reaction type: " + req.getType());
        }
        reactionService.toggleReaction(currentUser, id, type);
        matchService.calculateMatches(currentUser);
        redirectAttributes.addFlashAttribute("momentumMessage", getMomentumMessage(type));
        return "redirect:/feed";
    }

    private String getMomentumMessage(ReactionType type) {
        return switch (type) {
            case EMPATHY, SAME_HERE -> "좋아요. 진짜 공감 반응은 비슷한 취향의 사람을 더 선명하게 이어줘요.";
            case LIKE -> "좋아요. 마음에 든 글에 반응할수록 취향이 비슷한 사람이 더 잘 보여요.";
            case CURIOUS -> "좋아요. 궁금한 글을 따라가다 보면 대화가 쉬운 상대를 더 빨리 만날 수 있어요.";
        };
    }
}
