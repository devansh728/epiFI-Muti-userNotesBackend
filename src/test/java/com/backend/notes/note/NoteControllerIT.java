package com.backend.notes.note;

import com.backend.notes.AbstractIntegrationTest;
import com.backend.notes.auth.dto.AuthResponse;
import com.backend.notes.auth.dto.RegisterRequest;
import com.backend.notes.note.dto.NoteRequest;
import com.backend.notes.note.dto.NoteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class NoteControllerIT extends AbstractIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String accessToken;
    private UUID userId;
    
    @BeforeEach
    void setUp() throws Exception {
        // Register user and get token
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("notetest@example.com")
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
    }
    
    @Test
    void testCreateNote_Success() throws Exception {
        NoteRequest request = NoteRequest.builder()
            .title("Test Note")
            .content("This is test content")
            .build();
        
        MvcResult result = mockMvc.perform(post("/notes")
            .header("Authorization", "Bearer " + accessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isString())
            .andExpect(jsonPath("$.title").value("Test Note"))
            .andExpect(jsonPath("$.content").value("This is test content"))
            .andReturn();
        
        NoteResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            NoteResponse.class);
        assertThat(response.getId()).isNotNull();
    }
    
    @Test
    void testCreateNote_InvalidTitle() throws Exception {
        NoteRequest request = NoteRequest.builder()
            .title("")
            .content("This is test content")
            .build();
        
        mockMvc.perform(post("/notes")
            .header("Authorization", "Bearer " + accessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCreateNote_Unauthorized() throws Exception {
        NoteRequest request = NoteRequest.builder()
            .title("Test Note")
            .content("This is test content")
            .build();
        
        mockMvc.perform(post("/notes")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testGetNote_Success() throws Exception {
        // Create note first
        NoteRequest createRequest = NoteRequest.builder()
            .title("Get Test Note")
            .content("Get test content")
            .build();
        
        MvcResult createResult = mockMvc.perform(post("/notes")
            .header("Authorization", "Bearer " + accessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        
        NoteResponse createdNote = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            NoteResponse.class);
        
        // Get note
        mockMvc.perform(get("/notes/" + createdNote.getId())
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createdNote.getId().toString()))
            .andExpect(jsonPath("$.title").value("Get Test Note"));
    }
    
    @Test
    void testGetNote_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        mockMvc.perform(get("/notes/" + nonExistentId)
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void testListNotes_Success() throws Exception {
        // Create multiple notes
        for (int i = 1; i <= 3; i++) {
            NoteRequest request = NoteRequest.builder()
                .title("Note " + i)
                .content("Content " + i)
                .build();
            
            mockMvc.perform(post("/notes")
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        }
        
        // List notes
        mockMvc.perform(get("/notes")
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.totalElements").value(3));
    }
    
    @Test
    void testUpdateNote_Success() throws Exception {
        // Create note first
        NoteRequest createRequest = NoteRequest.builder()
            .title("Original Title")
            .content("Original Content")
            .build();
        
        MvcResult createResult = mockMvc.perform(post("/notes")
            .header("Authorization", "Bearer " + accessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        
        NoteResponse createdNote = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            NoteResponse.class);
        
        // Update note
        NoteRequest updateRequest = NoteRequest.builder()
            .title("Updated Title")
            .content("Updated Content")
            .build();
        
        mockMvc.perform(put("/notes/" + createdNote.getId())
            .header("Authorization", "Bearer " + accessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated Title"))
            .andExpect(jsonPath("$.content").value("Updated Content"));
    }
    
    @Test
    void testUpdateNote_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        NoteRequest request = NoteRequest.builder()
            .title("Updated Title")
            .content("Updated Content")
            .build();
        
        mockMvc.perform(put("/notes/" + nonExistentId)
            .header("Authorization", "Bearer " + accessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void testUpdateNote_Unauthorized() throws Exception {
        // Create note as first user
        NoteRequest createRequest = NoteRequest.builder()
            .title("Original Title")
            .content("Original Content")
            .build();
        
        MvcResult createResult = mockMvc.perform(post("/notes")
            .header("Authorization", "Bearer " + accessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        
        NoteResponse createdNote = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            NoteResponse.class);
        
        // Try to update as different user (no token = no auth)
        NoteRequest updateRequest = NoteRequest.builder()
            .title("Updated Title")
            .content("Updated Content")
            .build();
        
        mockMvc.perform(put("/notes/" + createdNote.getId())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testDeleteNote_Success() throws Exception {
        // Create note first
        NoteRequest createRequest = NoteRequest.builder()
            .title("Delete Test")
            .content("To be deleted")
            .build();
        
        MvcResult createResult = mockMvc.perform(post("/notes")
            .header("Authorization", "Bearer " + accessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        
        NoteResponse createdNote = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            NoteResponse.class);
        
        // Delete note
        mockMvc.perform(delete("/notes/" + createdNote.getId())
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());
    }
    
    @Test
    void testDeleteNote_Idempotent() throws Exception {
        // Create note first
        NoteRequest createRequest = NoteRequest.builder()
            .title("Idempotent Test")
            .content("To be deleted twice")
            .build();
        
        MvcResult createResult = mockMvc.perform(post("/notes")
            .header("Authorization", "Bearer " + accessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        
        NoteResponse createdNote = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            NoteResponse.class);
        
        // Delete note first time
        mockMvc.perform(delete("/notes/" + createdNote.getId())
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());
        
        // Delete note second time (should still return 204)
        mockMvc.perform(delete("/notes/" + createdNote.getId())
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNoContent());
    }
}
