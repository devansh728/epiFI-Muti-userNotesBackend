package com.backend.notes.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    
    @Schema(description = "List of items in this page")
    private List<T> content;
    
    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int pageNumber;
    
    @Schema(description = "Number of items per page", example = "20")
    private int pageSize;
    
    @Schema(description = "Total number of items across all pages", example = "150")
    private long totalElements;
    
    @Schema(description = "Total number of pages", example = "8")
    private int totalPages;
    
    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;
}
