package timetable;

import java.util.*;

final class ConstraintChecker {
    private final DataStore store; private final Timetable timetable;
    ConstraintChecker(DataStore store, Timetable timetable) { this.store=store; this.timetable=timetable; }
    List<Violation> checkHard(Session session, TimeSlot slot, Room room, Assignment ignored) {
        List<Violation> v=new ArrayList<>();
        add(v,checkWorkingHours(slot)); add(v,checkFixedTime(session,slot)); add(v,checkInstructorAvailability(session,slot));
        add(v,checkInstructorConflict(session,slot,ignored)); add(v,checkYearGroupConflict(session,slot,ignored));
        add(v,checkRoomCapacity(session,room)); add(v,checkRoomType(session,room));
        add(v,checkMandatoryRoom(session,room)); add(v,checkRoomAvailability(room,slot)); add(v,checkRoomConflict(session,room,slot,ignored));
        return v;
    }
    private void add(List<Violation> list, Violation v) { if(v!=null) list.add(v); }
    Violation checkInstructorConflict(Session s,TimeSlot slot,Assignment ignored) {
        for(Assignment a:timetable.all()) if(a!=ignored && a.session.course.instructorId.equals(s.course.instructorId) && a.slot.overlaps(slot)) return new Violation("HC-01","Instructor is already teaching "+a.session.id()+" at this time"); return null;
    }
    Violation checkRoomConflict(Session s,Room room,TimeSlot slot,Assignment ignored) {
        for(Assignment a:timetable.all()) if(a!=ignored && a.room.id.equals(room.id) && a.slot.overlaps(slot)) return new Violation("HC-02","Room "+room.id+" is already occupied"); return null;
    }
    Violation checkYearGroupConflict(Session s,TimeSlot slot,Assignment ignored) {
        for(Assignment a:timetable.all()) if(a!=ignored && a.session.course.year==s.course.year && a.slot.overlaps(slot)) return new Violation("HC-03","Year group "+s.course.year+" already has "+a.session.id()); return null;
    }
    Violation checkWorkingHours(TimeSlot slot) { return slot.isWithinWorkingHours()?null:new Violation("HC-04","Session must be Monday-Friday, 09:00-17:00"); }
    Violation checkRoomCapacity(Session s,Room r) { return r.capacity>=s.course.students?null:new Violation("HC-05","Room "+r.id+" capacity "+r.capacity+" is below "+s.course.students); }
    Violation checkRoomType(Session s,Room r) { return r.type==s.requiredRoomType?null:new Violation("HC-06","Session requires "+s.requiredRoomType+", room "+r.id+" is "+r.type); }
    Violation checkInstructorAvailability(Session s,TimeSlot slot) { Instructor i=store.instructors.get(s.course.instructorId); return i!=null&&i.isUnavailable(slot)?new Violation("HC-07","Instructor "+i.name+" is unavailable at "+slot):null; }
    Violation checkFixedTime(Session s,TimeSlot slot) {
        SpecialRequest r=fixedRequest(s); if(r==null||!r.mandatory()) return null;
        try { return TimeSlot.parse(r.value).equalsSlot(slot)?null:new Violation("HC-08","Mandatory fixed time is "+r.value); }
        catch(Exception e) { return new Violation("HC-08","Invalid mandatory fixed time: "+r.value); }
    }
    Violation checkRoomAvailability(Room r,TimeSlot slot) { return r.isUnavailable(slot)?new Violation("HC-09","Room "+r.id+" is unavailable at "+slot):null; }
    private Violation checkMandatoryRoom(Session s,Room r) {
        SpecialRequest req=roomRequest(s); if(req!=null&&req.mandatory()&&!req.value.equalsIgnoreCase(r.id)) return new Violation("MR-01","Mandatory room is "+req.value); return null;
    }
    List<String> checkSoft(Session s,TimeSlot slot) {
        List<String> w=new ArrayList<>();
        boolean labMayPrecede=store.requestsFor(s.course.code,RequestType.LAB_BEFORE_THEORY).stream().anyMatch(x->x.priority==Priority.PREFERRED);
        if(s.type==SessionType.LABORATORY&&!labMayPrecede) {
            Assignment theory=timetable.find(s.course.code+"-T");
            if(theory!=null && (slot.day().getValue()<theory.slot.day().getValue() || (slot.day()==theory.slot.day()&&slot.startHour()<theory.slot.startHour()))) w.add("SC-01: Laboratory is before theory");
        }
        Instructor instructor=store.instructors.get(s.course.instructorId);
        if(instructor!=null&&!instructor.prefers(slot)) w.add("SC-02: Slot is outside instructor preferred times");
        if(instructor!=null&&instructor.prefersUnavailable(slot)) w.add("SC-02: Slot overlaps a preferred instructor-unavailability period");
        SpecialRequest preferredFixed=fixedRequest(s);
        if(preferredFixed!=null&&!preferredFixed.mandatory()) {
            try { if(!TimeSlot.parse(preferredFixed.value).equalsSlot(slot)) w.add("SC-02: Preferred session time was not used"); } catch(Exception ignored) {}
        }
        boolean gap=false;
        for(Assignment a:timetable.byYear(s.course.year)) if(a.slot.day()==slot.day() && !a.slot.overlaps(slot)) {
            if(slot.endHour()<a.slot.startHour() && a.slot.startHour()-slot.endHour()>0) gap=true;
            if(a.slot.endHour()<slot.startHour() && slot.startHour()-a.slot.endHour()>0) gap=true;
        }
        if(gap) w.add("SC-03: Slot creates a gap in year group "+s.course.year+" timetable");
        return w;
    }
    SpecialRequest fixedRequest(Session s) {
        SpecialRequest r=store.requestFor(s.id(),RequestType.FIXED_TIME); if(r==null) r=store.requestFor(s.course.code,RequestType.FIXED_TIME); return r;
    }
    SpecialRequest roomRequest(Session s) { SpecialRequest r=store.requestFor(s.id(),RequestType.SPECIFIC_ROOM); if(r==null) r=store.requestFor(s.course.code,RequestType.SPECIFIC_ROOM); return r; }
}
