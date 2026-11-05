package timetable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

final class TimetablePrinter {
    String allSessions(Timetable timetable) {
        StringBuilder b=new StringBuilder("SCHEDULED SESSIONS\n");
        b.append(String.format("%-12s %-34s %-5s %-5s %-16s %-8s%n","SESSION","COURSE","YEAR","DAY","TIME","ROOM"));
        for(Assignment a:timetable.all()) b.append(String.format("%-12s %-34s %-5d %-5s %-16s %-8s%n",a.session.id(),a.session.course.title,a.session.course.year,a.slot.day().name().substring(0,3),TimeSlot.fmt(a.slot.startHour())+"-"+TimeSlot.fmt(a.slot.endHour()),a.room.id));
        return b.toString();
    }
    String year(Timetable timetable,SchedulingReport report,int year) {
        StringBuilder b=new StringBuilder("YEAR "+year+" TIMETABLE\n");
        b.append(String.format("%-8s %-12s %-12s %-13s %-30s %-8s %-25s%n","DAY","START","END","SESSION","COURSE TITLE","ROOM","INSTRUCTOR"));
        for(Assignment a:timetable.byYear(year)) b.append(String.format("%-8s %-12s %-12s %-13s %-30s %-8s %-25s%n",a.slot.day().name().substring(0,3),TimeSlot.fmt(a.slot.startHour()),TimeSlot.fmt(a.slot.endHour()),a.session.id(),a.session.course.title,a.room.id, a.session.course.instructorId));
        b.append("\nUNSCHEDULED\n");
        for(SchedulingProblem p:report.problems) if(p.session.course.year==year) b.append(p.session.id()+" · "+p.type+" · "+p.reason+"\n");
        return b.toString();
    }
    String unscheduled(SchedulingReport report) {
        StringBuilder b=new StringBuilder("UNSCHEDULED SESSIONS\n");
        for(ProblemType t:ProblemType.values()) { b.append("\n").append(t).append("\n"); for(SchedulingProblem p:report.byType(t)) b.append(p.session.id()+" · Year "+p.session.course.year+" · "+p.session.duration+"h · "+p.session.course.students+" students · "+p.reason+"\n"); }
        b.append("\nWARNINGS\n"); report.warnings.forEach(w->b.append(w).append('\n')); return b.toString();
    }
    void save(Timetable timetable,SchedulingReport report,Path output) throws IOException {
        Files.createDirectories(output); String text="DEPARTMENT COURSE TIMETABLE\n\n"+allSessions(timetable)+"\n"+unscheduled(report);
        for(int y=1;y<=4;y++) text+="\n"+year(timetable,report,y);
        Files.writeString(output.resolve("timetable.txt"),text,StandardCharsets.UTF_8);
        StringBuilder csv=new StringBuilder("session,courseTitle,year,instructor,day,start,end,room\n");
        for(Assignment a:timetable.all()) csv.append(csv(a.session.id())).append(',').append(csv(a.session.course.title)).append(',').append(a.session.course.year).append(',').append(csv(a.session.course.instructorId)).append(',').append(a.slot.day()).append(',').append(TimeSlot.fmt(a.slot.startHour())).append(',').append(TimeSlot.fmt(a.slot.endHour())).append(',').append(csv(a.room.id)).append('\n');
        Files.writeString(output.resolve("timetable.csv"),csv,StandardCharsets.UTF_8);
    }
    private String csv(String s) { return "\""+s.replace("\"","\"\"")+"\""; }
}
