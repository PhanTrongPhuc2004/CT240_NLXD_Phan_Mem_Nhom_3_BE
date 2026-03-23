package com.nhom3.ct240.strategy;

import com.nhom3.ct240.entity.Task;
import com.nhom3.ct240.entity.User;

public interface TaskStatusUpdateStrategy {
    void update(Task task, User updater, String cancelReason);
}
