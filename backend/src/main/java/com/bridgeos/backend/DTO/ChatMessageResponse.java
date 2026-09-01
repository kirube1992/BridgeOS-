package com.bridgeos.backend.DTO;

import com.bridgeos.backend.entity.ProjectChatMessage;
import com.bridgeos.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private ChatSenderResponse sender;
    private String message;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ProjectChatMessage entity) {
        User sender = entity.getSender();
        ChatSenderResponse senderDto = new ChatSenderResponse(
                sender.getId(),
                sender.getName(),
                sender.getEmail(),
                sender.getRole()
        );
        return new ChatMessageResponse(
                entity.getId(),
                senderDto,
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}
