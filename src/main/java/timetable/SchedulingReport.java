package timetable;

import java.util.*;

final class SchedulingReport {
    final List<SchedulingProblem> problems = new ArrayList<>();
    final List<String> warnings = new ArrayList<>(), log = new ArrayList<>();
    void clear() { problems.clear(); warnings.clear(); log.clear(); }
    void addProblem(SchedulingProblem p) { problems.add(p); }
    List<SchedulingProblem> byType(ProblemType type) { return problems.stream().filter(p->p.type==type).toList(); }
    int scheduledCount() { return log.stream().mapToInt(s -> s.startsWith("[OK]") ? 1 : 0).sum(); }
}
