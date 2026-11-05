package timetable;

import java.time.DayOfWeek;
import java.util.*;

final class Violation {
    final String code, message;
    Violation(String code, String message) { this.code=code; this.message=message; }
    @Override public String toString() { return code + ": " + message; }
}
