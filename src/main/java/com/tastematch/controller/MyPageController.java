package com.tastematch.controller;

import com.tastematch.domain.AnonUser;
import com.tastematch.dto.MatchResponse;
import com.tastematch.dto.MomentumSummary;
import com.tastematch.dto.PostResponse;
import com.tastematch.service.MatchService;
import com.tastematch.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final PostService postService;
    private final MatchService matchService;

    @GetMapping("/my")
    public String myPage(HttpServletRequest request, Model model) {
        AnonUser currentUser = (AnonUser) request.getAttribute("currentUser");

        List<PostResponse> myPostResponses = postService.getMyPosts(currentUser).stream()
                .map(post -> postService.getPostResponse(post, currentUser))
                .collect(Collectors.toList());

        long totalReactionsReceived = myPostResponses.stream()
                .mapToLong(PostResponse::getReactionCount)
                .sum();

        List<MatchResponse> myMatches = matchService.getMyMatches(currentUser);
        long matchesCount = myMatches.size();
        MomentumSummary momentumSummary = buildMomentumSummary(myPostResponses.size(), totalReactionsReceived, matchesCount);

        model.addAttribute("myPosts", myPostResponses);
        model.addAttribute("totalReactions", totalReactionsReceived);
        model.addAttribute("matchCount", matchesCount);
        model.addAttribute("momentumSummary", momentumSummary);
        model.addAttribute("currentUser", currentUser);

        return "my";
    }

    private MomentumSummary buildMomentumSummary(int myPostCount, long totalReactionsReceived, long matchesCount) {
        if (matchesCount > 0) {
            return MomentumSummary.builder()
                    .title("이제 대화를 시작해볼 만해요")
                    .body("공통 취향이 보이는 연결이 생겼어요. 마음 가는 사람의 글을 다시 살펴보고 가볍게 한마디 건네보세요.")
                    .actionLabel("매칭 보러 가기")
                    .actionHref("/matches")
                    .build();
        }

        if (totalReactionsReceived > 0) {
            return MomentumSummary.builder()
                    .title("반응이 차곡차곡 쌓이고 있어요")
                    .body("내 글에 공감한 사람들을 살펴보고, 비슷한 글에 진짜 공감 반응을 남기면 대화로 이어질 가능성이 더 커져요.")
                    .actionLabel("피드에서 더 둘러보기")
                    .actionHref("/feed")
                    .build();
        }

        if (myPostCount > 0) {
            return MomentumSummary.builder()
                    .title("첫 반응을 기다리는 중이에요")
                    .body("지금 올린 취향이 연결의 시작점이 될 수 있어요. 다른 사람 글에도 공감해두면 비슷한 사람들이 더 빨리 드러나요.")
                    .actionLabel("비슷한 글에 반응하기")
                    .actionHref("/feed")
                    .build();
        }

        return MomentumSummary.builder()
                .title("취향을 보여줄수록 연결이 쉬워져요")
                .body("짧은 글 하나만 올려도 비슷한 사람이 반응할 수 있어요. 먼저 취향을 남기고, 마음 가는 글에 공감해보세요.")
                .actionLabel("새 취향 공유하기")
                .actionHref("/posts/new")
                .build();
    }
}
