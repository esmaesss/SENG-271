<div align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=3165E8&height=150&section=header&text=Timetable%20Management&fontSize=45&fontColor=ffffff&desc=Department%20Course%20Scheduling%20System&descSize=20" width="100%" />

  # 📅 Department Course Timetable Management System

  [![Java Version](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)]()
  [![UI Framework](https://img.shields.io/badge/UI-Java_Swing-3165E8?style=for-the-badge)]()
  [![Version](https://img.shields.io/badge/Version-Code_v1-success?style=for-the-badge)]()
  [![License](https://img.shields.io/badge/License-Academic-lightgrey?style=for-the-badge)]()

  *A robust, Java-based desktop application with a modern Swing UI that automatically generates weekly university course timetables while enforcing strict scheduling constraints and handling resource conflicts.*
  
  ---
</div>

## 👥 Team

| Name | GitHub Profile | 
| :--- | :--- | 
| **Esma Çalış** | [@esmaesss](https://github.com/esmaesss) | 
| **Çağatay Temel** | [@Cagatackle](https://github.com/Cagatackle) | 

## ⚙️ Requirements

* **Java:** Version `17` or later
* **External libraries:** `[none]` *(Java Swing is fully built into the JDK, ensuring zero external dependencies)*

---

## 🚀 How to Compile and Run

### Option A: From the Terminal (Repository Root)

```bash
# 1. Compile the source code into an 'out' directory
javac -d out src/main/java/timetable/*.java

# 2. Run the application
java -cp out timetable.Main
```

<details>
<summary><b>Option B: Using an IDE (IntelliJ IDEA)</b> <i>(Click to expand)</i></summary>
<br>

1. Open the repository root folder (`Seng-271`) in IntelliJ.
2. Ensure the JDK is set to version 17 or higher in **Project Structure**.
3. Locate `src/main/java/timetable/Main.java` in the Project Explorer.
4. Right-click `Main.java` and select **"Run 'Main.main()'"**.
</details>

---

## 📂 Input Files

The system reads initialization data from the `data/` directory. *(File formats are described in Section 5 of the SRS).*

| File | Format | Content Description | 
| :--- | :---: | :--- | 
| 📄 `data/courses.csv` | **CSV** | Courses, curriculum, hours, and capacity | 
| 📄 `data/instructors.txt` | **TXT** | Instructor availability and preferences | 
| 📄 `data/rooms.csv` | **CSV** | Rooms, laboratories, and unavailable periods | 
| 📄 `data/constraints.txt` | **TXT** | Special mandatory & preferred scheduling requests | 

---

## 💻 Using the Application

Navigate through the modern sidebar to manage the scheduling lifecycle:

| Screen / Option | Description | 
| :--- | :--- | 
| 📥 **Load input data** | Reads files from the `data/` folder and dynamically checks for errors. | 
| ⚙️ **Generate timetable** | Runs the automatic rule-based constraint-satisfaction scheduling algorithm. | 
| 📅 **Weekly timetable** | Explores the calendar grid per year group and reviews scheduled sessions. | 
| ⚠️ **Unscheduled & warnings** | Lists sessions that could not be scheduled and soft-constraint warnings. | 
| ✏️ **Edit assignment** | Allows manual moving or assigning of sessions with real-time hard-constraint validation. | 
| ✉️ **Classroom request** | Prepares a formal email draft for sessions lacking a suitable room (`NO ROOM`). | 
| 💾 **Export files** | Saves current timetables and reports to the output folder. | 

---

## 📤 Outputs

All generated files are safely stored in the `output/` directory:
* 🗂️ **Timetables:** `timetable.txt`, `timetable.csv`
* 📊 **Reports:** Unscheduled sessions report
* 📧 **Drafts:** Classroom request emails (e.g., `classroom_request_<course_code>.txt`)

---

## 🏗️ Repository Structure

```text
📦 Seng-271
 ┣ 📂 docs/
 ┃ ┣ 📂 requirements/   # SRS versions
 ┃ ┣ 📂 design/         # SDD versions and diagram source files
 ┃ ┣ 📂 testing/        # Test results
 ┃ ┗ 📂 checkpoints/    # Checkpoint reports
 ┣ 📂 data/             # Input files (.csv, .txt)
 ┣ 📂 src/              # Java source code
 ┗ 📂 output/           # Generated timetables and emails
```

---

## 📌 Versions & Milestones

| Version | Tag | Date | Summary | 
| :--- | :---: | :--- | :--- | 
| **Code v1** | `code-v1` | 05.11.2026 | Project skeleton, modern UI layout, data loading and model classes | 
| **Code v2** | `code-v2` | - | - | 
| **Code v3** | `code-v3` | - | - | 
| **Code v4** | `code-v4` | - | - | 
| **Code v5** | `code-v5` | - | - | 
| **Code v6** | `code-v6` | - | - | 
| **Final** | `final` | - | - | 

---

## 🚧 Known Limitations

> **Note:** These are expected limitations based on the current algorithm architecture.

* 🔍 **No Backtracking:** The algorithm uses a first-fit approach. Early placements cannot be moved automatically to resolve later conflicts.
* 📏 **Best-Fit Room Selection:** Room selection uses a best-fit strategy based on capacity, which may occasionally occupy a room needed by a subsequent session.
* 🧱 **Indivisible Blocks:** A teaching block (theory or laboratory) is treated as a single consecutive session and cannot be split across multiple days.

---

## 📚 Documents

* 📋 **SRS:** [`docs/requirements/`](docs/requirements/)
* 📐 **SDD:** [`docs/design/`](docs/design/)
* ✅ **Test results:** [`docs/testing/Test-Results.pdf`](docs/testing/Test-Results.pdf)

<div align="center">
  <i>Developed for SENG 271 Software Project Assignment</i>
</div>