package timetable;

import java.time.DayOfWeek;
import java.util.*;

final class SpecialRequest {
    final RequestType type;
    final String target;
    final Priority priority;
    final String value;
    SpecialRequest(RequestType type, String target, Priority priority, String value) {
        this.type=type; this.target=target; this.priority=priority; this.value=value;
    }
    boolean mandatory() { return priority==Priority.MANDATORY; }
}
