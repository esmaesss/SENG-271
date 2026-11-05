package timetable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

final class DataLoader {
    record LoadSummary(int courses, int instructors, int rooms, int requests, List<String> warnings, List<String> errors) {}
    LoadSummary loadAll(Path dataDir, DataStore store) {
        store.clear(); List<String> errors=new ArrayList<>();
        readInstructors(dataDir.resolve("instructors.txt"),store,errors);
        readCourses(dataDir.resolve("courses.csv"),store,errors);
        readRooms(dataDir.resolve("rooms.csv"),store,errors);
        for(Instructor instructor:store.instructors.values()) for(String code:instructor.assignedCourses) if(!store.courses.containsKey(code)) store.warnings.add("instructors.txt: course "+code+" listed for "+instructor.id+" was not found in courses.csv -> courses.csv takes precedence");
        readConstraints(dataDir.resolve("constraints.txt"),store,errors);
        return new LoadSummary(store.courses.size(),store.instructors.size(),store.rooms.size(),store.requests.size(),List.copyOf(store.warnings),List.copyOf(errors));
    }
    private List<String> lines(Path path, DataStore store, List<String> errors) {
        try { if (!Files.exists(path)) { errors.add("File " + path + " not found"); return List.of(); }
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch(IOException e) { errors.add("Cannot read " + path + ": " + e.getMessage()); return List.of(); }
    }
    private void readInstructors(Path p, DataStore s, List<String> errors) {
        List<String> lines=lines(p,s,errors);
        for(int i=0;i<lines.size();i++) { String line=lines.get(i).trim(); if(line.isEmpty()||line.startsWith("#")) continue;
            try { String[] f=line.split("\\|",-1); if(f.length!=5) throw new IllegalArgumentException("expected 5 fields, found "+f.length);
                String id=f[0].trim(), name=f[1].trim(); if(id.isEmpty()||name.isEmpty()) throw new IllegalArgumentException("id/name cannot be empty");
                if(s.instructors.containsKey(id)) throw new IllegalArgumentException("duplicate instructor id "+id);
                Instructor in=new Instructor(id,name);
                if(!f[2].trim().isBlank()&&!f[2].trim().equals("-")) for(String code:f[2].trim().split(",")) in.assignedCourses.add(code.trim());
                parseAvailability(f[3].trim(),in.unavailable);
                parsePreferred(f[4].trim(),in.preferred); s.instructors.put(id,in);
            } catch(Exception ex) { s.warnings.add("instructors.txt, line "+(i+1)+": "+ex.getMessage()+" -> record skipped"); }
        }
    }
    private void readCourses(Path p, DataStore s, List<String> errors) {
        List<String> lines=lines(p,s,errors);
        for(int i=0;i<lines.size();i++) { String line=lines.get(i).trim(); if(line.isEmpty()||i==0) continue;
            try { String[] f=line.split(",",-1); if(f.length!=8) throw new IllegalArgumentException("expected 8 fields, found "+f.length);
                String code=f[0].trim(), title=f[1].trim(), instructor=f[3].trim();
                int year=Integer.parseInt(f[2].trim()), theory=Integer.parseInt(f[4].trim()), lab=Integer.parseInt(f[5].trim()), students=Integer.parseInt(f[6].trim());
                if(code.isEmpty()||title.isEmpty()) throw new IllegalArgumentException("course code/title cannot be empty");
                if(year<1||year>4) throw new IllegalArgumentException("year must be 1-4");
                if(theory<0||theory>8||lab<0||lab>8||theory+lab<1) throw new IllegalArgumentException("hours must be 0-8 and total at least 1");
                if(students<=0) throw new IllegalArgumentException("students must be > 0");
                if(!s.instructors.containsKey(instructor)) throw new IllegalArgumentException("instructor "+instructor+" not found");
                RoomType type=RoomType.valueOf(f[7].trim().toUpperCase(Locale.ROOT));
                if(s.courses.containsKey(code)) throw new IllegalArgumentException("duplicate course code "+code);
                s.courses.put(code,new Course(code,title,year,instructor,theory,lab,students,type));
            } catch(Exception ex) { s.warnings.add("courses.csv, line "+(i+1)+": "+ex.getMessage()+" -> record skipped"); }
        }
    }
    private void readRooms(Path p, DataStore s, List<String> errors) {
        List<String> lines=lines(p,s,errors);
        for(int i=0;i<lines.size();i++) { String line=lines.get(i).trim(); if(line.isEmpty()||i==0) continue;
            try { String[] f=line.split(",",-1); if(f.length!=4) throw new IllegalArgumentException("expected 4 fields, found "+f.length);
                String id=f[0].trim(); RoomType type=RoomType.valueOf(f[1].trim().toUpperCase(Locale.ROOT)); int cap=Integer.parseInt(f[2].trim());
                if(id.isEmpty()||cap<=0) throw new IllegalArgumentException("room id/capacity invalid");
                if(s.rooms.containsKey(id)) throw new IllegalArgumentException("duplicate room id "+id);
                Room room=new Room(id,type,cap); parseRanges(f[3].trim(),room.unavailable); s.rooms.put(id,room);
            } catch(Exception ex) { s.warnings.add("rooms.csv, line "+(i+1)+": "+ex.getMessage()+" -> record skipped"); }
        }
    }
    private void readConstraints(Path p, DataStore s, List<String> errors) {
        List<String> lines=lines(p,s,errors);
        for(int i=0;i<lines.size();i++) { String line=lines.get(i).trim(); if(line.isEmpty()||line.startsWith("#")) continue;
            try { String[] f=line.split("\\|",-1); if(f.length!=4) throw new IllegalArgumentException("expected 4 fields, found "+f.length);
                RequestType type=RequestType.valueOf(f[0].trim().toUpperCase(Locale.ROOT)); String target=f[1].trim();
                Priority priority=Priority.valueOf(f[2].trim().toUpperCase(Locale.ROOT)); String value=f[3].trim();
                if(type==RequestType.INSTRUCTOR_UNAVAILABLE) { Course c=s.courses.get(target); if(c==null) throw new IllegalArgumentException("course "+target+" not found");
                    Instructor in=s.instructors.get(c.instructorId); if(priority==Priority.MANDATORY) parseAvailability(value,in.unavailable); else parseAvailability(value,in.preferredUnavailable);
                } else if(type==RequestType.FIXED_TIME) { String course=target.endsWith("-T")||target.endsWith("-L")?target.substring(0,target.length()-2):target; if(!s.courses.containsKey(course)) throw new IllegalArgumentException("course "+course+" not found"); }
                else if(type==RequestType.SPECIFIC_ROOM) { String course=target.replace("-T","").replace("-L",""); if(!s.courses.containsKey(course)) throw new IllegalArgumentException("course "+course+" not found"); if(!s.rooms.containsKey(value)) throw new IllegalArgumentException("room "+value+" not found"); }
                else if(type==RequestType.LAB_BEFORE_THEORY && !s.courses.containsKey(target)) throw new IllegalArgumentException("course "+target+" not found");
                s.requests.add(new SpecialRequest(type,target,priority,value));
            } catch(Exception ex) { s.warnings.add("constraints.txt, line "+(i+1)+": "+ex.getMessage()+" -> request ignored"); }
        }
    }
    private void parseAvailability(String text,List<TimeSlot> out) {
        if(text.isBlank()||text.equals("-")) return;
        if(text.toUpperCase(Locale.ROOT).startsWith("ONLY ")) {
            Set<String> allowed=new HashSet<>(Arrays.asList(text.substring(5).trim().toUpperCase(Locale.ROOT).split(",")));
            for(String d:List.of("MON","TUE","WED","THU","FRI")) if(!allowed.contains(d)) out.add(new TimeSlot(TimeSlot.parseDay(d),9,8));
            return;
        }
        parseRanges(text,out);
    }
    private void parsePreferred(String text,List<TimeSlot> out) {
        if(text.isBlank()||text.equals("-")) return;
        if(text.equalsIgnoreCase("MORNING")) { for(String d:List.of("MON","TUE","WED","THU","FRI")) out.add(new TimeSlot(TimeSlot.parseDay(d),9,3)); }
        else if(text.equalsIgnoreCase("AFTERNOON")) { for(String d:List.of("MON","TUE","WED","THU","FRI")) out.add(new TimeSlot(TimeSlot.parseDay(d),13,4)); }
        else parseRanges(text,out);
    }
    private void parseRanges(String text,List<TimeSlot> out) {
        if(text.isBlank()||text.equals("-")) return;
        for(String part:text.split(";")) { String v=part.trim(); if(v.matches("(?i)(MON|TUE|WED|THU|FRI)")) out.add(new TimeSlot(TimeSlot.parseDay(v),9,8)); else out.add(TimeSlot.parse(v)); }
    }
}
