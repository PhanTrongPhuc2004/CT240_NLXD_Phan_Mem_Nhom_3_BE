package com.nhom3.ct240.event;

import com.nhom3.ct240.entity.Task;
import lombok.Getter;

@Getter
public class TaskCreatedEvent extends BaseEvent {
    private final Task task;
    private final String creatorId;

    public TaskCreatedEvent(Object source, Task task, String creatorId) {
        super(source);
        this.task = task;
        this.creatorId = creatorId;
    }

}
