# Code Version Plan (Code v1–v6) — Template

Each code version receives a Git tag and a short GitHub Release note. Keep the release notes short, specific, and truthful to the code that existed at that tag.

| Version | Tag | Expected focus |
| --- | --- | --- |
| Code v1 | `code-v1` | Java project skeleton matching the SDD v2 class diagram; Main class; main menu runs |
| Code v2 | `code-v2` | Input file processing and object management |
| Code v3 | `code-v3` | Initial automatic scheduling algorithm and conflict detection |
| Code v4 | `code-v4` | Working core scheduling functionality (Checkpoint 2) |
| Code v5 | `code-v5` | Additional constraints, special requests, timetable modification, algorithm improvements |
| Code v6 | `code-v6` | Output, classroom requests, manual testing, error correction |
| Final | `final` | Final release (Checkpoint 3) |

Use the matching `*-release-note.md` file as the release description form. Fill the commit hash, date, test results, contributions and known issues after that version is actually committed. Do not publish a release note claiming work that was not present at the tagged commit.
