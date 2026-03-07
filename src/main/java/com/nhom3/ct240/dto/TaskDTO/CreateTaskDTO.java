package com.nhom3.ct240.dto.TaskDTO;

import com.nhom3.ct240.entity.enums.TaskPriority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTaskDTO {

    @NotBlank(message = "Project ID is required")
    private String projectId;

    @NotBlank(message = "Title is required")
    @Size(min = 3, message = "Title must be at least 3 characters long")
    private String title;

    private String description;

    private String assigneeId;

    @Future(message = "Deadline must be in the future")
    private LocalDateTime deadline;

    private TaskPriority priority = TaskPriority.MEDIUM;
}
