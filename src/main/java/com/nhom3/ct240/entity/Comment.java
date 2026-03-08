package com.nhom3.ct240.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "comments")
public class Comment {
    @Id
    private String id;
    private String taskId;
    private String userId;
    private String content;
    private List<String> attachmentUrls; //c_n tao file
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private boolean edited = false;
    private boolean isDeleted = false; //phuc vu xoa mem
}