package com.backend.notes.note;

import com.backend.notes.AbstractIntegrationTest;
import com.backend.notes.auth.dto.AuthResponse;
import com.backend.notes.auth.dto.RegisterRequest;
import com.backend.notes.note.dto.NoteRequest;
import com.backend.notes.note.dto.NoteResponse;
import com.backend.notes.note.dto.ShareRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ShareControllerIT extends AbstractIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String ownerAccessToken;
    private String collaboratorAccessToken;
    private UUID noteId;
    
    @BeforeEach
    void setUp() throws Exception {
        // Register owner
        RegisterRequest ownerRequest = RegisterRequest.builder()
            .email("owner@example.com")
            .password("SecurePassword123")
            .build();
        
        MvcResult ownerResult = mockMvc.perform(post("/register")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(ownerRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        
        AuthResponse ownerAuth = objectMapper.readValue(
            ownerResult.getResponse().getContentAsString(),
            AuthResponse.class);
        
        ownerAccessToken = ownerAuth.getAccessToken();
        
        // Register collaborator
        RegisterRequest collaboratorRequest = RegisterRequest.builder()
            .email("collaborator@example.com")
            .password("SecurePassword123")
            .build();
        
        MvcResult collaboratorResult = mockMvc.perform(post("/register")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(collaboratorRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        
        AuthResponse collaboratorAuth = objectMapper.readValue(
            collaboratorResult.getResponse().getContentAsString(),
            AuthResponse.class);
        
        collaboratorAccessToken = collaboratorAuth.getAccessToken();
        
        // Create a note
        NoteRequest noteRequest = NoteRequest.builder()
            .title("Shared Note")
            .content("This note will be shared")
            .build();
        
        MvcResult noteResult = mockMvc.perform(post("/notes")
            .header("Authorization", "Bearer " + ownerAccessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(noteRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        
        NoteResponse noteResponse = objectMapper.readValue(
            noteResult.getResponse().getContentAsString(),
            NoteResponse.class);
        
        noteId = noteResponse.getId();
    }
    
    @Test
    void testShareNote_Success() throws Exception {
        ShareRequest request = ShareRequest.builder()
            .email("collaborator@example.com")
            .permission(SharePermission.READ)
            .build();
        
        mockMvc.perform(post("/notes/" + noteId + "/share")
            .header("Authorization", "Bearer " + ownerAccessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.noteId").value(noteId.toString()))
            .andExpect(jsonPath("$.sharedWithEmail").value("collaborator@example.com"))
            .andExpect(jsonPath("$.permission").value("READ"));
    }
    
    @Test
    void testShareNote_WithWritePermission() throws Exception {
        ShareRequest request = ShareRequest.builder()
            .email("collaborator@example.com")
            .permission(SharePermission.WRITE)
            .build();
        
        mockMvc.perform(post("/notes/" + noteId + "/share")
            .header("Authorization", "Bearer " + ownerAccessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.permission").value("WRITE"));
    }
    
    @Test
    void testShareNote_WithSelf() throws Exception {
        ShareRequest request = ShareRequest.builder()
            .email("owner@example.com")
            .permission(SharePermission.READ)
            .build();
        
        mockMvc.perform(post("/notes/" + noteId + "/share")
            .header("Authorization", "Bearer " + ownerAccessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void testShareNote_UnknownUser() throws Exception {
        ShareRequest request = ShareRequest.builder()
            .email("nonexistent@example.com")
            .permission(SharePermission.READ)
            .build();
        
        mockMvc.perform(post("/notes/" + noteId + "/share")
            .header("Authorization", "Bearer " + ownerAccessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void testShareNote_Duplicate_Upsert() throws Exception {
        // Share with READ first
        ShareRequest readRequest = ShareRequest.builder()
            .email("collaborator@example.com")
            .permission(SharePermission.READ)
            .build();
        
        mockMvc.perform(post("/notes/" + noteId + "/share")
            .header("Authorization", "Bearer " + ownerAccessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(readRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.permission").value("READ"));
        
        // Upsert to WRITE
        ShareRequest writeRequest = ShareRequest.builder()
            .email("collaborator@example.com")
            .permission(SharePermission.WRITE)
            .build();
        
        mockMvc.perform(post("/notes/" + noteId + "/share")
            .header("Authorization", "Bearer " + ownerAccessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(writeRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.permission").value("WRITE"));
    }
    
    @Test
    void testShareNote_NonOwner() throws Exception {
        ShareRequest request = ShareRequest.builder()
            .email("collaborator@example.com")
            .permission(SharePermission.READ)
            .build();
        
        mockMvc.perform(post("/notes/" + noteId + "/share")
            .header("Authorization", "Bearer " + collaboratorAccessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void testShareNote_InvalidEmail() throws Exception {
        ShareRequest request = ShareRequest.builder()
            .email("invalid-email")
            .permission(SharePermission.READ)
            .build();
        
        mockMvc.perform(post("/notes/" + noteId + "/share")
            .header("Authorization", "Bearer " + ownerAccessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void testShareNote_NoteNotFound() throws Exception {
        UUID nonExistentNoteId = UUID.randomUUID();
        
        ShareRequest request = ShareRequest.builder()
            .email("collaborator@example.com")
            .permission(SharePermission.READ)
            .build();
        
        mockMvc.perform(post("/notes/" + nonExistentNoteId + "/share")
            .header("Authorization", "Bearer " + ownerAccessToken)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
}
