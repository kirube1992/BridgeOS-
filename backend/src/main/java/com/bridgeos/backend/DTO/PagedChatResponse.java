package com.bridgeos.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedChatResponse {
    private List<ChatMessageResponse> content;
    private int totalPages;
    private long totalElements;
}
