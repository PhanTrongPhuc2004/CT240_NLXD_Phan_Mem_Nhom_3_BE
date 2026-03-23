package com.nhom3.ct240.event;

import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.enums.TaskStatus;
import lombok.Getter;

@Getter
public class TaskStatusChangedEvent extends BaseEvent {
    private final Task task;
    private final TaskStatus oldStatus;
    private final String updaterId;
    private final String projectId;
    private final String cancelReason;

    public TaskStatusChangedEvent(Object source, Task task, TaskStatus oldStatus, String updaterId, String cancelReason) {
        super(source);
        this.task = task;
        this.oldStatus = oldStatus;
        this.updaterId = updaterId;
        this.projectId = task.getProjectId();
        this.cancelReason = cancelReason;
    }

}
