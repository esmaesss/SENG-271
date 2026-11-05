package timetable;

import java.time.DayOfWeek;
import java.util.*;

final class TimeSlot {
    private final DayOfWeek day;
    private final int startHour;
    private final int duration;

    TimeSlot(DayOfWeek day, int startHour, int duration) {
        this.day = Objects.requireNonNull(day);
        this.startHour = startHour;
        this.duration = duration;
    }
    DayOfWeek day() { return day; }
    int startHour() { return startHour; }
    int duration() { return duration; }
    int endHour() { return startHour + duration; }
    boolean isWithinWorkingHours() {
        return day.getValue() >= 1 && day.getValue() <= 5 && startHour >= 9 && duration > 0 && endHour() <= 17;
    }
    boolean overlaps(TimeSlot other) {
        return day == other.day && startHour < other.endHour() && other.startHour < endHour();
    }
    boolean contains(TimeSlot other) { return day == other.day && startHour <= other.startHour && endHour() >= other.endHour(); }
    boolean equalsSlot(TimeSlot other) { return other != null && day == other.day && startHour == other.startHour && duration == other.duration; }
    @Override public String toString() { return day.name().substring(0,3) + " " + fmt(startHour) + "-" + fmt(endHour()); }
    static String fmt(int hour) { return String.format(Locale.ROOT, "%02d:00", hour); }
    static DayOfWeek parseDay(String value) {
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "MON", "MONDAY" -> DayOfWeek.MONDAY; case "TUE", "TUESDAY" -> DayOfWeek.TUESDAY;
            case "WED", "WEDNESDAY" -> DayOfWeek.WEDNESDAY; case "THU", "THURSDAY" -> DayOfWeek.THURSDAY;
            case "FRI", "FRIDAY" -> DayOfWeek.FRIDAY; default -> throw new IllegalArgumentException("Unknown day: " + value);
        };
    }
    static TimeSlot parse(String value) {
        String[] p = value.trim().split("\\s+", 2);
        if (p.length != 2) throw new IllegalArgumentException("Expected DAY HH:MM-HH:MM");
        String[] t = p[1].split("-");
        if (t.length != 2) throw new IllegalArgumentException("Invalid time range: " + value);
        int start = parseHour(t[0]), end = parseHour(t[1]);
        TimeSlot slot = new TimeSlot(parseDay(p[0]), start, end-start);
        if (!slot.isWithinWorkingHours()) throw new IllegalArgumentException("Time must be within 09:00-17:00");
        return slot;
    }
    private static int parseHour(String s) {
        if (!s.matches("(?:09|1[0-6]):00|17:00")) throw new IllegalArgumentException("Time must be on whole hours 09:00-17:00");
        return Integer.parseInt(s.substring(0,2));
    }
}
