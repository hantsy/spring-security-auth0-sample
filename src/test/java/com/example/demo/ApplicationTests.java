package com.example.demo;

import com.example.demo.domain.Post;
import com.example.demo.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
public class ApplicationTests {

    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext applicationContext;

    @MockBean
    PostRepository posts;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    JwtDecoder jwtDecoder;

    @Value("${auth0.audience}")
    private String audience;

    @BeforeEach
    public void setup() {
        this.mockMvc = webAppContextSetup(this.applicationContext)
            .apply(springSecurity())
            .build();
    }

    @Test
    public void testGetById() throws Exception {
        Post post = Post.builder().title("test").content("test content").build();
        post.setId(1L);
        given(this.posts.findById(anyLong())).willReturn(Optional.of(post));

        this.mockMvc
                .perform(
                        get("/posts/{id}", 1L)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test"));

        verify(this.posts, times(1)).findById(any(Long.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void testGetByIdNotFound() throws Exception {

        given(this.posts.findById(anyLong())).willReturn(Optional.empty());
        this.mockMvc
                .perform(
                        get("/posts/{id}", 2L)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(this.posts, times(1)).findById(any(Long.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void testSave() throws Exception {

        given(this.posts.save(any(Post.class)))
                .willReturn(Post.builder().title("test").content("test content").build());

        this.mockMvc
                .perform(
                        post("/posts")
                                .with(jwt().jwt(jwtBuilder().claim("scope", "write:posts").build()))
                                .content(this.objectMapper.writeValueAsBytes(Post.builder().title("test").content("test content").build()))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated());

        verify(this.posts, times(1)).save(any(Post.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void testUpdate() throws Exception {
        given(this.posts.findById(1L))
                .willReturn(Optional.of(Post.builder().title("test").content("test content").build()));

        given(this.posts.save(any(Post.class)))
                .willReturn(Post.builder().title("testUpdate").content("testUpdate content").build());

        this.mockMvc
                .perform(
                        put("/posts/1")
                                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_write:posts")))
                                .content(this.objectMapper.writeValueAsBytes(Post.builder().title("testUpdate").content("testUpdate content").build()))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        verify(this.posts, times(1)).findById(any(Long.class));
        verify(this.posts, times(1)).save(any(Post.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    public void testDelete() throws Exception {
        Post post = Post.builder().title("test").content("test content").build();
        post.setId(1L);
        given(this.posts.findById(anyLong())).willReturn(Optional.of(post));

        doNothing().when(this.posts).delete(any(Post.class));

        var jwt = jwtBuilder().claim("scope", "delete:posts");
        // when(jwtDecoder.decode(anyString())).thenReturn(jwt);

        this.mockMvc
                .perform(
                        delete("/posts/1").with(jwt().jwt(jwt.build()))
                )
                .andExpect(status().isNoContent());

        verify(this.posts, times(1)).findById(any(Long.class));
        verify(this.posts, times(1)).delete(any(Post.class));
        verifyNoMoreInteractions(this.posts);
    }

    private Jwt.Builder jwtBuilder() {
        return Jwt.withTokenValue("token").header("alg", "none").audience(List.of(audience));
    }


}
