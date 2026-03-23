package com.nhom3.ct240.event;

import org.springframework.context.ApplicationEvent;
import java.time.Clock;

public abstract class BaseEvent extends ApplicationEvent {
    public BaseEvent(Object source) {
        super(source, Clock.systemDefaultZone());
    }
}
