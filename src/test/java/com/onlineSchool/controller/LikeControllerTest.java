package com.onlineSchool.controller;

import com.onlineSchool.config.TestSecurityConfig;
import com.onlineSchool.model.EntityType;
import com.onlineSchool.model.Like;
import com.onlineSchool.model.User;
import com.onlineSchool.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LikeController.class)
@Import(TestSecurityConfig.class)
public class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LikeService likeService;

    private Like testLike;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testLike = Like.builder()
                .id(1L)
                .user(testUser)
                .entityType(EntityType.COURSE)
                .entityId(1L)
                .build();
    }

    @Test
    @WithMockUser
    public void createLike_WhenValidInput_ShouldReturnCreatedLike() throws Exception {
        when(likeService.createLike(any(Like.class))).thenReturn(testLike);

        mockMvc.perform(post("/api/likes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"user\":{\"id\":1},\"entityType\":\"COURSE\",\"entityId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.entityType").value("COURSE"))
                .andExpect(jsonPath("$.entityId").value(1));
    }

    @Test
    @WithMockUser
    public void deleteLike_WhenLikeExists_ShouldReturnNoContent() throws Exception {
        doNothing().when(likeService).deleteLike(anyLong());

        mockMvc.perform(delete("/api/likes/1"))
                .andExpect(status().isNoContent());

        verify(likeService, times(1)).deleteLike(1L);
    }

    @Test
    public void getLikesByEntity_ShouldReturnLikesList() throws Exception {
        List<Like> likes = Arrays.asList(testLike);
        when(likeService.getLikesByEntity(any(EntityType.class), anyLong())).thenReturn(likes);

        mockMvc.perform(get("/api/likes/entity/COURSE/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].entityType").value("COURSE"))
                .andExpect(jsonPath("$[0].entityId").value(1));
    }

    @Test
    @WithMockUser
    public void getLikesByUser_ShouldReturnLikesList() throws Exception {
        List<Like> likes = Arrays.asList(testLike);
        when(likeService.getLikesByUser(anyLong())).thenReturn(likes);

        mockMvc.perform(get("/api/likes/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].user.id").value(1));
    }

    @Test
    @WithMockUser
    public void getLikesByUserAndEntity_ShouldReturnLikesList() throws Exception {
        List<Like> likes = Arrays.asList(testLike);
        when(likeService.getLikesByUserAndEntity(anyLong(), any(EntityType.class), anyLong())).thenReturn(likes);

        mockMvc.perform(get("/api/likes/user/1/entity/COURSE/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].user.id").value(1))
                .andExpect(jsonPath("$[0].entityType").value("COURSE"))
                .andExpect(jsonPath("$[0].entityId").value(1));
    }

    @Test
    public void getLikeById_WhenLikeExists_ShouldReturnLike() throws Exception {
        when(likeService.getLikeById(anyLong())).thenReturn(testLike);

        mockMvc.perform(get("/api/likes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.entityType").value("COURSE"))
                .andExpect(jsonPath("$.entityId").value(1));
    }

    @Test
    public void getLikeById_WhenLikeDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(likeService.getLikeById(anyLong())).thenThrow(new RuntimeException("Like not found"));

        mockMvc.perform(get("/api/likes/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void countLikes_ShouldReturnCount() throws Exception {
        when(likeService.countLikes(anyLong(), any(EntityType.class))).thenReturn(5L);

        mockMvc.perform(get("/api/likes/count/COURSE/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }

    @Test
    @WithMockUser
    public void hasLiked_WhenUserLiked_ShouldReturnTrue() throws Exception {
        when(likeService.hasLiked(anyLong(), anyLong(), any(EntityType.class))).thenReturn(true);

        mockMvc.perform(get("/api/likes/has-liked/1/COURSE/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @WithMockUser
    public void hasLiked_WhenUserNotLiked_ShouldReturnFalse() throws Exception {
        when(likeService.hasLiked(anyLong(), anyLong(), any(EntityType.class))).thenReturn(false);

        mockMvc.perform(get("/api/likes/has-liked/1/COURSE/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
} 