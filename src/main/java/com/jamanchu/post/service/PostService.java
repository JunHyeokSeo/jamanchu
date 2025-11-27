package com.jamanchu.post.service;

import com.jamanchu.post.dto.response.PostResponse;
import com.jamanchu.post.entity.Post;
import com.jamanchu.post.repository.PostRepository;
import com.jamanchu.user.entity.User;
import com.jamanchu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public Long createPost(Long userId, String content) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("잘못된 유저 정보입니다."));
        Post post = Post.builder().user(user).content(content).build();
        postRepository.save(post);

        return post.getId();
    }

    public void updatePost(Long postId, String content) {
        Post post = postRepository.findByIdAndIsDeletedIsFalse(postId).orElseThrow(() -> new IllegalArgumentException("잘못된 포스트 정보입니다."));
        post.update(content);
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findByIdAndIsDeletedIsFalse(postId).orElseThrow(() -> new IllegalArgumentException("잘못된 포스트 정보입니다."));
        post.delete();
    }

    public List<PostResponse> getPosts() {
        Sort sort = Sort.sort(Post.class).by(Post::getUpdate_at).descending();
        List<Post> posts = postRepository.findAllByIsDeletedIsFalse(sort);

        return posts.stream().map(PostResponse::from).collect(Collectors.toList());
    }
}
