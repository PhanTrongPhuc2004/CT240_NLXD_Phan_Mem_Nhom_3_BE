package com.nhom3.ct240.strategy;

import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.enums.TaskStatus;
import org.springframework.stereotype.Component;

@Component
public class ToDoStatusUpdateStrategy extends AbstractTaskStatusUpdateStrategy {

    @Override
    protected void performStatusChange(Task task, String cancelReason) {
        task.setStatus(TaskStatus.TO_DO);
    }
}
