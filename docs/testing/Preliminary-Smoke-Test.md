# Preliminary Smoke Test (generated sample dataset)

**Scope:** compilation and scheduling-engine sanity check. This is not a substitute for the team's required manual test record.

- Java compiler used in the preparation environment: see the terminal validation performed for this bundle; target compatibility is Java 17+.
- Input files: the six-course sample dataset included in `data/`.
- Loaded: 6 courses, 6 instructors, 6 rooms, 5 special requests.
- Generated sessions: 11.
- Scheduled sessions: 11.
- Unscheduled sessions: 0.
- Hard-constraint violations found by re-checking every resulting assignment with `ConstraintChecker`: 0.
- Input warnings/errors: 0.

**Not yet verified here:** visual UI behavior on the user's Windows/IntelliJ setup, all 18 documented test cases, malformed-file edge cases, and the instructor-provided official sample dataset. Complete `Manual-Test-Plan.md` locally and update the actual results before submission.
