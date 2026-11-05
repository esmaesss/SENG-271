package timetable;

import java.time.DayOfWeek;
import java.util.*;

final class Instructor {
    final String id, name;
    final List<String> assignedCourses = new ArrayList<>();
    final List<TimeSlot> unavailable = new ArrayList<>(), preferred = new ArrayList<>(), preferredUnavailable = new ArrayList<>();
    Instructor(String id, String name) { this.id=id; this.name=name; }
    boolean isUnavailable(TimeSlot slot) { return unavailable.stream().anyMatch(x -> x.overlaps(slot)); }
    boolean prefersUnavailable(TimeSlot slot) { return preferredUnavailable.stream().anyMatch(x -> x.overlaps(slot)); }
    boolean prefers(TimeSlot slot) {
        if (preferred.isEmpty()) return true;
        return preferred.stream().anyMatch(x -> x.day()==slot.day() && x.startHour() <= slot.startHour() && x.endHour() >= slot.endHour());
    }
}
