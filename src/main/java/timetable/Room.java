package timetable;

import java.time.DayOfWeek;
import java.util.*;

final class Room {
    final String id;
    final RoomType type;
    final int capacity;
    final List<TimeSlot> unavailable = new ArrayList<>();
    Room(String id, RoomType type, int capacity) { this.id=id; this.type=type; this.capacity=capacity; }
    boolean isSuitableFor(Session s) { return type==s.requiredRoomType && capacity>=s.course.students; }
    boolean isUnavailable(TimeSlot slot) { return unavailable.stream().anyMatch(x -> x.overlaps(slot)); }
}
