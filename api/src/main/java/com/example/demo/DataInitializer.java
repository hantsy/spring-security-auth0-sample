package com.example.demo;

import com.example.demo.domain.Post;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    
    private final PostRepository posts;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.initPosts();
    }
    
    private void initPosts() {
        log.info(" start data initializing...");
        this.posts.deleteAll();
        Stream.of("Post one", "Post two").forEach(
                title -> this.posts.save(Post.builder().title(title).content("content of " + title).build())
        );
        log.info(" done data initialization...");
        log.info(" initialized data::");
        this.posts.findAll().forEach(p -> log.info(p.toString()));
    }
}