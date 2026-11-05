# SDD v2 — Swing UI Design Update

**Project:** Department Course Timetable Management System  
**Related baseline:** SRS v1, SDD v1  
**Implementation language:** Java 17+  
**UI decision:** Java Swing, standard library only

## 1. Reason for this update

SRS v1 and SDD v1 describe a console interface and list a graphical interface as optional. The team selected the optional Swing interface to improve coordinator usability. The scheduling scope, data formats, constraint IDs, file paths, and in-memory operation remain unchanged. This document is an implementation design update; the original SDD v1 file is preserved unchanged.

## 2. Updated component map

| Layer | Classes | Responsibility |
| --- | --- | --- |
| UI | `Main`, `MainFrame` | Launch Swing, navigation, dashboard, tables, dialogs, user feedback |
| Input | `DataLoader`, `DataStore` | Parse UTF-8 CSV/TXT files, validate records, retain loaded objects and warnings |
| Domain | `Course`, `Instructor`, `Room`, `Session`, `TimeSlot`, `Assignment`, `SpecialRequest`, `Timetable`, enums | Represent scheduling entities and state |
| Scheduling | `Scheduler`, `ConstraintChecker`, `Violation`, `SchedulingProblem`, `SchedulingReport`, `TimetableEditor` | Generate schedule, enforce HC rules, record failures, validate manual changes |
| Output | `TimetablePrinter`, `EmailDraftGenerator` | Export TXT/CSV and classroom request draft |

The `MainFrame` class contains presentation and interaction logic only. It delegates file processing, scheduling, constraint validation, and output generation to the corresponding service classes.

## 3. Swing screens

- Overview dashboard: loaded courses/rooms, scheduled/unscheduled counters, quick actions and activity messages.
- Load input data: lists the four required files and reports record counts, errors and warnings.
- Generate timetable: explains the rule-based algorithm and runs the scheduler.
- Weekly timetable: tabular schedule view with year filter and all-years view.
- Unscheduled & warnings: separates `NO_ROOM` and `CONSTRAINT` problems and shows soft/input warnings.
- Edit assignment: allows moving or removing a selected assignment; hard constraints are checked before mutation and soft warnings require confirmation.
- Classroom request: selects only `NO_ROOM` sessions and saves a text draft; it never sends email.
- Export: writes `output/timetable.txt` and `output/timetable.csv`.

## 4. Design rules retained

1. Java 17 or newer; Java standard library only; relative paths.
2. All hard checks are centralized in `ConstraintChecker`.
3. Automatic scheduling and manual editing use the same hard-check methods.
4. A hard-constraint failure prevents an assignment.
5. A rejected manual change leaves the existing assignment unchanged.
6. Soft constraints SC-01–SC-03 are preferences; SC-04 is satisfied by construction because each session is a consecutive block.
7. Unscheduled sessions are classified as `NO_ROOM` or `CONSTRAINT`; only `NO_ROOM` is eligible for a classroom request.

## 5. Current implementation notes / items to verify

This is a first implementation baseline and must be reviewed against the team's approved SDD v2 class diagram before submission. The current implementation supports the required CSV/TXT formats and core workflows. The team should verify the exact sample dataset, all constraint edge cases, preferred-request behavior, and the instructor's expected class diagram. The SDD v2 diagram and final flowchart must be updated to match the final implementation.
