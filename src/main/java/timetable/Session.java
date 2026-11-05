package timetable;

import java.time.DayOfWeek;
import java.util.*;

final class Session {
    final Course course;
    final SessionType type;
    final int duration;
    final RoomType requiredRoomType;
    Session(Course course, SessionType type, int duration, RoomType requiredRoomType) {
        this.course=course; this.type=type; this.duration=duration; this.requiredRoomType=requiredRoomType;
    }
    String id() { return course.code + (type==SessionType.THEORY ? "-T" : "-L"); }
    @Override public String toString() { return id() + " · " + course.title; }
}
