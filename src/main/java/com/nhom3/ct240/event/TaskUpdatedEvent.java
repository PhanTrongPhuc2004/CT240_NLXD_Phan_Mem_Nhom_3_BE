package com.nhom3.ct240.event;

import com.nhom3.ct240.entity.Task;
import lombok.Getter;

@Getter
public class TaskUpdatedEvent extends BaseEvent {
    private final Task task;
    private final String editorId;

    public TaskUpdatedEvent(Object source, Task task, String editorId) {
        super(source);
        this.task = task;
        this.editorId = editorId;
    }

}
