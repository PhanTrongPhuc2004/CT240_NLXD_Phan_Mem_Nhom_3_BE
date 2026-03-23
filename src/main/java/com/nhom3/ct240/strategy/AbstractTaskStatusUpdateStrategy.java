package com.nhom3.ct240.strategy;

import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.User;
import java.time.LocalDateTime;

public abstract class AbstractTaskStatusUpdateStrategy implements TaskStatusUpdateStrategy {

    // TEMPLATE METHOD pattern
    @Override
    public final void update(Task task, User updater, String cancelReason) {
        validate(task, updater);
        performStatusChange(task, cancelReason);
        updateCommonFields(task);
        postUpdate(task, updater);
    }

    protected void validate(Task task, User updater) {
        // Default implementation: do nothing
    }

    protected abstract void performStatusChange(Task task, String cancelReason);

    private void updateCommonFields(Task task) {
        task.setUpdatedAt(LocalDateTime.now());
    }

    protected void postUpdate(Task task, User updater) {
        // Default implementation: do nothing
    }
}
