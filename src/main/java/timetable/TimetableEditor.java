package timetable;

import java.time.DayOfWeek;
import java.util.*;

final class TimetableEditor {
    record Validation(boolean accepted,List<Violation> hard,List<String> soft) {}
    private final DataStore store; private final Timetable timetable; private final ConstraintChecker checker;
    TimetableEditor(DataStore store,Timetable timetable) { this.store=store; this.timetable=timetable; checker=new ConstraintChecker(store,timetable); }
    Validation validateChange(Assignment current,TimeSlot proposed,Room room) {
        List<Violation> hard=checker.checkHard(current.session,proposed,room,current);
        List<String> soft=checker.checkSoft(current.session,proposed);
        return new Validation(hard.isEmpty(),hard,soft);
    }
    Validation applyChange(Assignment current,TimeSlot proposed,Room room,boolean acceptSoft) {
        Validation v=validateChange(current,proposed,room);
        if(!v.accepted()||(!v.soft().isEmpty()&&!acceptSoft)) return v;
        timetable.remove(current); timetable.add(new Assignment(current.session,proposed,room)); return v;
    }
    void removeAssignment(Assignment assignment) { timetable.remove(assignment); }
    boolean assignUnscheduled(Session session,TimeSlot slot,Room room,boolean acceptSoft) {
        List<Violation> hard=checker.checkHard(session,slot,room,null); List<String> soft=checker.checkSoft(session,slot);
        if(!hard.isEmpty()||(!soft.isEmpty()&&!acceptSoft)) return false;
        timetable.add(new Assignment(session,slot,room)); return true;
    }
}
