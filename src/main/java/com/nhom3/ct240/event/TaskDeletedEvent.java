package com.nhom3.ct240.event;

import com.nhom3.ct240.entity.Task;
import lombok.Getter;

@Getter
public class TaskDeletedEvent extends BaseEvent {
    private final Task task;
    private final String deleterId;

    public TaskDeletedEvent(Object source, Task task, String deleterId) {
        super(source);
        this.task = task;
        this.deleterId = deleterId;
    }

}
