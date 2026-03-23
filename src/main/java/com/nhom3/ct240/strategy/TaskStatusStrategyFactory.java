package com.nhom3.ct240.strategy;

import com.nhom3.ct240.entity.enums.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TaskStatusStrategyFactory {

    private final Map<TaskStatus, TaskStatusUpdateStrategy> strategyMap;

    @Autowired
    public TaskStatusStrategyFactory(
            ToDoStatusUpdateStrategy toDo,
            InProgressStatusUpdateStrategy inProgress,
            DoneStatusUpdateStrategy done,
            CancelledStatusUpdateStrategy cancelled) {
        
        strategyMap = Map.of(
            TaskStatus.TO_DO, toDo,
            TaskStatus.IN_PROGRESS, inProgress,
            TaskStatus.DONE, done,
            TaskStatus.CANCELLED, cancelled
        );
    }

    public TaskStatusUpdateStrategy getStrategy(TaskStatus status) {
        TaskStatusUpdateStrategy strategy = strategyMap.get(status);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for status: " + status);
        }
        return strategy;
    }
}
