package timetable;

import java.time.DayOfWeek;
import java.util.*;

final class Scheduler {
    private final DataStore store; private final Timetable timetable; private final ConstraintChecker checker;
    private final SchedulingReport report=new SchedulingReport();
    Scheduler(DataStore store,Timetable timetable) { this.store=store; this.timetable=timetable; this.checker=new ConstraintChecker(store,timetable); }
    SchedulingReport generate() {
        timetable.clear(); report.clear();
        if(!store.isReady()) { report.log.add("[FAIL] Data is incomplete. Load courses, instructors and rooms first."); return report; }
        List<Session> sessions=store.buildSessions();
        List<Session> fixed=new ArrayList<>(), remaining=new ArrayList<>();
        for(Session s:sessions) { SpecialRequest r=checker.fixedRequest(s); if(r!=null&&r.mandatory()) fixed.add(s); else remaining.add(s); }
        fixed.sort(Comparator.comparing(x->x.id()));
        for(Session s:fixed) schedule(s,true);
        remaining.sort(Comparator.comparingInt((Session x)->x.course.students).reversed().thenComparing(x->x.course.code).thenComparingInt(x->x.type==SessionType.THEORY?0:1));
        for(Session s:remaining) schedule(s,false);
        report.warnings.addAll(store.warnings);
        report.log.add("Summary: "+sessions.size()+" total · "+timetable.all().size()+" scheduled · "+report.problems.size()+" unscheduled · "+report.warnings.size()+" warnings");
        return report;
    }
    private void schedule(Session s,boolean fixedOnly) {
        List<Room> suitable=store.rooms.values().stream().filter(r->r.isSuitableFor(s)).sorted(Comparator.comparingInt(r->r.capacity)).toList();
        SpecialRequest roomReq=checker.roomRequest(s);
        if(roomReq!=null&&roomReq.mandatory()) suitable=suitable.stream().filter(r->r.id.equalsIgnoreCase(roomReq.value)).toList();
        if(suitable.isEmpty()) { fail(s,ProblemType.NO_ROOM,"HC-05/HC-06: no room of required type and capacity (or mandatory room "+(roomReq==null?"":roomReq.value)+")"); return; }
        List<TimeSlot> slots=candidates(s,fixedOnly);
        Map<String,Integer> rejected=new LinkedHashMap<>(); boolean slotPassed=false, roomBlocked=false;
        for(TimeSlot slot:slots) {
            List<Violation> slotViolations=new ArrayList<>();
            add(slotViolations,checker.checkWorkingHours(slot)); add(slotViolations,checker.checkFixedTime(s,slot));
            add(slotViolations,checker.checkInstructorAvailability(s,slot)); add(slotViolations,checker.checkInstructorConflict(s,slot,null));
            add(slotViolations,checker.checkYearGroupConflict(s,slot,null));
            if(!slotViolations.isEmpty()) { slotViolations.forEach(v->rejected.merge(v.code,1,Integer::sum)); continue; }
            slotPassed=true;
            List<Room> ordered=new ArrayList<>(suitable);
            if(roomReq!=null&&!roomReq.mandatory()) ordered.sort(Comparator.comparing((Room r)->!r.id.equalsIgnoreCase(roomReq.value)).thenComparingInt(r->r.capacity));
            for(Room room:ordered) {
                List<Violation> roomViolations=new ArrayList<>(); add(roomViolations,checker.checkRoomCapacity(s,room)); add(roomViolations,checker.checkRoomType(s,room));
                add(roomViolations,checker.checkRoomAvailability(room,slot)); add(roomViolations,checker.checkRoomConflict(s,room,slot,null));
                if(roomReq!=null&&roomReq.mandatory()&&!roomReq.value.equalsIgnoreCase(room.id)) add(roomViolations,new Violation("MR-01","Mandatory room is "+roomReq.value));
                if(roomViolations.isEmpty()) {
                    Assignment a=new Assignment(s,slot,room); timetable.add(a);
                    List<String> soft=checker.checkSoft(s,slot); report.warnings.addAll(soft);
                    report.log.add((soft.isEmpty()?"[OK] ":"[WARN] ")+s.id()+" → "+slot+" · "+room.id+(soft.isEmpty()?"":" · "+String.join("; ",soft)));
                    return;
                }
                roomBlocked=true; roomViolations.forEach(v->rejected.merge(v.code,1,Integer::sum));
            }
            if(fixedOnly) break;
        }
        if(slotPassed&&roomBlocked) fail(s,ProblemType.NO_ROOM,"HC-02/HC-09: all suitable rooms are occupied or unavailable");
        else { String code=rejected.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("HC-08");
            String reason=code+": no valid time slot found"+(fixedOnly?" for mandatory fixed time":""); fail(s,ProblemType.CONSTRAINT,reason); }
    }
    private void add(List<Violation> v,Violation x) { if(x!=null) v.add(x); }
    private List<TimeSlot> candidates(Session s,boolean fixedOnly) {
        SpecialRequest req=checker.fixedRequest(s); List<TimeSlot> result=new ArrayList<>();
        if(req!=null) { try { TimeSlot preferred=TimeSlot.parse(req.value); if(preferred.duration()!=s.duration) report.warnings.add("HC-08: "+s.id()+" fixed-time duration differs from session duration");
                result.add(new TimeSlot(preferred.day(),preferred.startHour(),s.duration)); } catch(Exception e) { report.warnings.add("HC-08: invalid fixed time for "+s.id()+" — "+req.value); }
        }
        if(!fixedOnly && (req==null || !req.mandatory())) for(DayOfWeek day:List.of(DayOfWeek.MONDAY,DayOfWeek.TUESDAY,DayOfWeek.WEDNESDAY,DayOfWeek.THURSDAY,DayOfWeek.FRIDAY))
            for(int h=9;h+s.duration<=17;h++) { TimeSlot t=new TimeSlot(day,h,s.duration); if(result.stream().noneMatch(x->x.equalsSlot(t))) result.add(t); }
        List<TimeSlot> clean=new ArrayList<>(), soft=new ArrayList<>();
        for(TimeSlot t:result) { List<String> warnings=checker.checkSoft(s,t); if(warnings.isEmpty()) clean.add(t); else soft.add(t); }
        clean.addAll(soft); return clean;
    }
    private void fail(Session s,ProblemType type,String reason) { report.addProblem(new SchedulingProblem(s,type,reason)); report.log.add("[FAIL] "+s.id()+" · "+type+" · "+reason); }
    TimeSlot suggestSlot(Session s) {
        for(DayOfWeek d:List.of(DayOfWeek.MONDAY,DayOfWeek.TUESDAY,DayOfWeek.WEDNESDAY,DayOfWeek.THURSDAY,DayOfWeek.FRIDAY)) for(int h=9;h+s.duration<=17;h++) {
            TimeSlot t=new TimeSlot(d,h,s.duration); Instructor in=store.instructors.get(s.course.instructorId);
            if(!in.isUnavailable(t) && timetable.byYear(s.course.year).stream().noneMatch(a->a.slot.overlaps(t))) return t;
        } return null;
    }
    SchedulingReport report() { return report; }
}
