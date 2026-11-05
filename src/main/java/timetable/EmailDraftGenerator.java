package timetable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

final class EmailDraftGenerator {
    String generateDraft(SchedulingProblem problem,TimeSlot suggested) {
        Session s=problem.session;
        return "To: Student Affairs Office\nSubject: Classroom Request — "+s.course.code+"\n\n"+
                "Dear Student Affairs Office,\n\n"+
                "We kindly request your assistance in identifying an available room for "+s.course.code+" — "+s.course.title+".\n\n"+
                "Expected number of students: "+s.course.students+"\nRequired room type: "+s.requiredRoomType+"\n"+
                "Requested time: "+(suggested==null?"To be determined":suggested)+"\n\n"+
                "We would appreciate your support in arranging a suitable classroom.\n\nBest regards,\nDepartment Timetable Coordinator\n";
    }
    Path saveDraft(SchedulingProblem problem,String draft,Path output) throws IOException {
        Files.createDirectories(output); Path path=output.resolve("classroom_request_"+problem.session.course.code+".txt");
        Files.writeString(path,draft,StandardCharsets.UTF_8); return path;
    }
}
