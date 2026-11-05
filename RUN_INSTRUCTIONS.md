# Run instructions (Java 17+, no external libraries)

## IntelliJ IDEA
1. Open the `SENG-271` folder as a project.
2. Ensure Project SDK is set to JDK 17 or newer.
3. Mark `src/main/java` as **Sources Root** if IntelliJ does not detect it automatically.
4. Open `src/main/java/timetable/Main.java` and run `Main.main()`.
5. Keep the project root as the working directory so `data/` and `output/` resolve correctly.

## Windows PowerShell
From the project root:

```powershell
New-Item -ItemType Directory -Force build\classes | Out-Null
javac -encoding UTF-8 -d build\classes (Get-ChildItem -Recurse src\main\java\*.java).FullName
java -cp build\classes timetable.Main
```

## macOS / Linux
From the project root:

```bash
mkdir -p build/classes
javac -encoding UTF-8 -d build/classes $(find src/main/java -name '*.java')
java -cp build/classes timetable.Main
```

The application reads `data/courses.csv`, `data/instructors.txt`, `data/rooms.csv`, and `data/constraints.txt`.
Generated files are written to `output/` only when the user selects Export.
