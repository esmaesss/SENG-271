# Manual Test Plan — Department Course Timetable Management System

Record **Actual result**, **Pass/Fail**, tester and date during the team's own verification. Do not mark a case as passed until it has been run locally.

| ID | Scenario | Expected result | Actual result | Status |
| --- | --- | --- | --- | --- |
| TC-01 | Load sample files and generate | Sessions are assigned where a valid slot/room exists; summary shown | | Not run |
| TC-02 | Same instructor, overlapping sessions | Second conflicting assignment is rejected (HC-01) | | Not run |
| TC-03 | Same room, overlapping sessions | Room is not double-booked (HC-02) | | Not run |
| TC-04 | Same year group, overlapping sessions | Year-group overlap rejected (HC-03) | | Not run |
| TC-05 | Room capacity too small | Room rejected; NO_ROOM if no fitting room (HC-05) | | Not run |
| TC-06 | Instructor unavailable | Slot rejected (HC-07) | | Not run |
| TC-07 | Mandatory fixed time | Session uses only fixed slot or remains unscheduled (HC-08) | | Not run |
| TC-08 | Theory before laboratory | Lab-after-theory preference is attempted; warning if not satisfied (SC-01) | | Not run |
| TC-09 | First room/slot blocked | Alternative room or time slot is attempted | | Not run |
| TC-10 | No valid assignment | Session reported with type and reason | | Not run |
| TC-11 | Classroom request | Only NO_ROOM sessions are offered; draft is saved on confirmation | | Not run |
| TC-12 | Invalid/missing input file | Understandable warning/error; app remains open | | Not run |
| TC-13 | Manual edit | Invalid edit rejected without changing previous assignment | | Not run |
| TC-14 | Multiple constraints | No recorded assignment violates a hard constraint | | Not run |
| TC-15 | Room unavailable | Room not used during blocked period (HC-09) | | Not run |
| TC-16 | Outside working hours | No assignment outside 09:00–17:00 (HC-04) | | Not run |
| TC-17 | Room type mismatch | Classroom/lab mismatch rejected (HC-06) | | Not run |
| TC-18 | Invalid UI input/navigation | Clear feedback; application remains usable | | Not run |
