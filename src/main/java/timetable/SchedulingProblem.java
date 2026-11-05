package timetable;

import java.time.DayOfWeek;
import java.util.*;

final class SchedulingProblem {
    final Session session;
    final ProblemType type;
    final String reason;
    SchedulingProblem(Session session, ProblemType type, String reason) { this.session=session; this.type=type; this.reason=reason; }
}
