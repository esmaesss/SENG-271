package timetable;

import java.time.DayOfWeek;
import java.util.*;

final class Course {
    final String code, title, instructorId;
    final int year, theoryHours, labHours, students;
    final RoomType labRoomType;
    Course(String code, String title, int year, String instructorId, int theoryHours, int labHours, int students, RoomType labRoomType) {
        this.code=code; this.title=title; this.year=year; this.instructorId=instructorId;
        this.theoryHours=theoryHours; this.labHours=labHours; this.students=students; this.labRoomType=labRoomType;
    }
    List<Session> createSessions() {
        List<Session> result = new ArrayList<>();
        if (theoryHours > 0) result.add(new Session(this, SessionType.THEORY, theoryHours, RoomType.CLASSROOM));
        if (labHours > 0) result.add(new Session(this, SessionType.LABORATORY, labHours, labRoomType));
        return result;
    }
}
