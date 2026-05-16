package com.backend.notes.search;

import com.backend.notes.AbstractIntegrationTest;
import com.backend.notes.auth.dto.AuthResponse;
import com.backend.notes.auth.dto.RegisterRequest;
import com.backend.notes.note.dto.NoteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class SearchControllerIT extends AbstractIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String accessToken;
    
    @BeforeEach
    void setUp() throws Exception {
        // Register user and get token
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("searchtest@example.com")
            .password("SecurePassword123")
            .build();
        
        MvcResult result = mockMvc.perform(post("/register")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        
        AuthResponse authResponse = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            AuthResponse.class);
        
        accessToken = authResponse.getAccessToken();
        
        // Create test notes for search
        createNote("Java Programming", "Learn Java basics and advanced concepts");
        createNote("Python Tutorial", "Master Python for data science");
        createNote("Web Development", "HTML, CSS, JavaScript for web development");
        createNote("Database Design", "SQL and database fundamentals");
    }
    
    private void createNote(String title, String content) throws Exception {
        NoteRequest request = NoteRequest.builder()
            .title(title)
            .content(content)
            .build();
        
        mockMvc.perform(post("/notes")
            .header("Authorization", "Bearer " + accessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
    
    @Test
    void testSearchNotes_Success() throws Exception {
        mockMvc.perform(get("/search")
            .param("q", "Java")
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Java Programming"));
    }
    
    @Test
    void testSearchNotes_MultipleResults() throws Exception {
        mockMvc.perform(get("/search")
            .param("q", "development")
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
    
    @Test
    void testSearchNotes_NoResults() throws Exception {
        mockMvc.perform(get("/search")
            .param("q", "NonExistentKeyword")
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0));
    }
    
    @Test
    void testSearchNotes_EmptyQuery() throws Exception {
        mockMvc.perform(get("/search")
            .param("q", "")
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void testSearchNotes_TooLongQuery() throws Exception {
        String longQuery = "a".repeat(201);
        mockMvc.perform(get("/search")
            .param("q", longQuery)
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void testSearchNotes_Unauthorized() throws Exception {
        mockMvc.perform(get("/search")
            .param("q", "Java"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testSearchNotes_WithPagination() throws Exception {
        mockMvc.perform(get("/search")
            .param("q", "development")
            .param("page", "0")
            .param("size", "10")
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(10));
    }
}
