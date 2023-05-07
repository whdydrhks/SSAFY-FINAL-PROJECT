package com.ssafy.nanumi.db.entity;

import com.ssafy.nanumi.common.ChatMessageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "chat")
public class ChatMessageEntity {
    private ChatMessageDTO.MessageType type;
    private long roomId;
    private long sender;
    private String message;
    private String sendTime;
}