package timetable;

import java.util.*;

final class DataStore {
    final Map<String, Course> courses = new LinkedHashMap<>();
    final Map<String, Instructor> instructors = new LinkedHashMap<>();
    final Map<String, Room> rooms = new LinkedHashMap<>();
    final List<SpecialRequest> requests = new ArrayList<>();
    final List<String> warnings = new ArrayList<>();
    void clear() { courses.clear(); instructors.clear(); rooms.clear(); requests.clear(); warnings.clear(); }
    List<Session> buildSessions() { List<Session> all=new ArrayList<>(); courses.values().forEach(c -> all.addAll(c.createSessions())); return all; }
    boolean isReady() { return !courses.isEmpty() && !instructors.isEmpty() && !rooms.isEmpty(); }
    List<SpecialRequest> requestsFor(String target, RequestType type) {
        return requests.stream().filter(r -> r.type==type && r.target.equalsIgnoreCase(target)).toList();
    }
    SpecialRequest requestFor(String target, RequestType type) {
        return requestsFor(target,type).stream().filter(SpecialRequest::mandatory).findFirst()
                .orElseGet(() -> requestsFor(target,type).stream().findFirst().orElse(null));
    }
}
