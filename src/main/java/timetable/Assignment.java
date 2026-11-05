package timetable;

import java.time.DayOfWeek;
import java.util.*;

final class Assignment {
    final Session session;
    final TimeSlot slot;
    final Room room;
    Assignment(Session session, TimeSlot slot, Room room) { this.session=session; this.slot=slot; this.room=room; }
}
