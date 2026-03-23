package com.nhom3.ct240.event;

import com.nhom3.ct240.entity.Task;
import lombok.Getter;

@Getter
public class TaskAssignedEvent extends BaseEvent {
    private final Task task;
    private final String assignerId;

    public TaskAssignedEvent(Object source, Task task, String assignerId) {
        super(source);
        this.task = task;
        this.assignerId = assignerId;
    }

}
