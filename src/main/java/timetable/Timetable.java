package timetable;

import java.util.*;

final class Timetable {
    private final List<Assignment> assignments = new ArrayList<>();
    void clear() { assignments.clear(); }
    List<Assignment> all() { return Collections.unmodifiableList(assignments); }
    void add(Assignment a) { assignments.add(a); }
    void remove(Assignment a) { assignments.remove(a); }
    Assignment find(String sessionId) { return assignments.stream().filter(a -> a.session.id().equalsIgnoreCase(sessionId)).findFirst().orElse(null); }
    List<Assignment> byYear(int year) { return assignments.stream().filter(a -> a.session.course.year==year).sorted(Comparator.comparing((Assignment a)->a.slot.day()).thenComparingInt(a->a.slot.startHour())).toList(); }
}
