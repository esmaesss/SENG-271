package timetable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.*;
import java.util.List;

/** Java Swing desktop UI. Business rules remain in service/domain classes. */
@SuppressWarnings("serial")
final class MainFrame extends JFrame {
    private static final Color NAVY=new Color(18,31,56), BLUE=new Color(49,101,232), BG=new Color(245,247,251), INK=new Color(31,41,55), MUTED=new Color(111,123,143), WHITE=Color.WHITE;
    private final Path dataDir, outputDir;
    private final DataStore store=new DataStore(); private final Timetable timetable=new Timetable();
    private final DataLoader loader=new DataLoader(); private final TimetablePrinter printer=new TimetablePrinter();
    private final EmailDraftGenerator emailGenerator=new EmailDraftGenerator();
    private Scheduler scheduler; private SchedulingReport report=new SchedulingReport();
    private final CardLayout cards=new CardLayout(); private final JPanel content=new JPanel(cards);
    private final JLabel status=new JLabel("Ready · Load your input files to begin");
    private final JLabel coursesValue=new JLabel("—"), roomsValue=new JLabel("—"), scheduledValue=new JLabel("—"), unscheduledValue=new JLabel("—");
    private final JTextArea logArea=new JTextArea();
    private final JComboBox<String> yearFilter=new JComboBox<>(new String[]{"Year 1","Year 2","Year 3","Year 4"});
    private final DefaultTableModel scheduleModel=new DefaultTableModel(new String[]{"Session","Course","Year","Day","Time","Room","Instructor"},0) { public boolean isCellEditable(int r,int c){return false;} };
    private final JTable scheduleTable=new JTable(scheduleModel);
    private final DefaultTableModel calendarModel=new DefaultTableModel(new String[]{"TIME","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"},0) { public boolean isCellEditable(int r,int c){return false;} };
    private final JTable calendarTable=new JTable(calendarModel);
    private final DefaultTableModel issueModel=new DefaultTableModel(new String[]{"Session","Year","Type","Reason"},0) { public boolean isCellEditable(int r,int c){return false;} };
    private final JTable issueTable=new JTable(issueModel);

    MainFrame(Path dataDir,Path outputDir) {
        super("SENG 271 · Department Timetable"); this.dataDir=dataDir; this.outputDir=outputDir;
        setDefaultCloseOperation(EXIT_ON_CLOSE); setMinimumSize(new Dimension(1120,740)); setSize(1320,850); setLocationRelativeTo(null);
        getContentPane().setBackground(BG); setLayout(new BorderLayout()); add(buildSidebar(),BorderLayout.WEST); add(buildMain(),BorderLayout.CENTER);
        refreshDashboard(); showScreen("dashboard");
    }
    private JPanel buildSidebar() {
        JPanel nav=new JPanel(); nav.setBackground(NAVY); nav.setPreferredSize(new Dimension(238,0)); nav.setLayout(new BoxLayout(nav,BoxLayout.Y_AXIS)); nav.setBorder(new EmptyBorder(26,18,20,18));
        JLabel brand=new JLabel("◈  SENG 271"); brand.setForeground(WHITE); brand.setFont(new Font("SansSerif",Font.BOLD,22)); nav.add(brand);
        JLabel sub=new JLabel("DEPARTMENT TIMETABLE"); sub.setForeground(new Color(159,177,207)); sub.setFont(new Font("SansSerif",Font.BOLD,9)); sub.setBorder(new EmptyBorder(5,3,28,0)); nav.add(sub);
        addNav(nav,"▦   Overview","dashboard"); addNav(nav,"⇧   Load input data","load"); addNav(nav,"✦   Generate timetable","generate");
        addNav(nav,"▤   Weekly timetable","schedule"); addNav(nav,"⚑   Unscheduled & warnings","issues"); addNav(nav,"✎   Edit assignment","edit");
        addNav(nav,"✉   Classroom request","request"); addNav(nav,"⇩   Export files","export");
        nav.add(Box.createVerticalGlue()); JLabel foot=new JLabel("JAVA 17+  ·  SWING DESKTOP"); foot.setForeground(new Color(131,150,181)); foot.setFont(new Font("SansSerif",Font.PLAIN,9)); nav.add(foot); return nav;
    }
    private void addNav(JPanel nav,String label,String key) {
        JButton b=new JButton(label); b.setAlignmentX(Component.LEFT_ALIGNMENT); b.setMaximumSize(new Dimension(205,43)); b.setPreferredSize(new Dimension(205,43));
        b.setForeground(new Color(225,232,244)); b.setBackground(NAVY); b.setFocusPainted(false); b.setBorder(new EmptyBorder(0,12,0,8)); b.setHorizontalAlignment(SwingConstants.LEFT); b.setFont(new Font("SansSerif",Font.PLAIN,13)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e->showScreen(key)); nav.add(b); nav.add(Box.createVerticalStrut(5));
    }
    private JPanel buildMain() {
        JPanel root=new JPanel(new BorderLayout()); root.setBackground(BG);
        JPanel top=new JPanel(new BorderLayout()); top.setBackground(WHITE); top.setBorder(new EmptyBorder(17,28,17,28));
        JLabel title=new JLabel("Department Course Timetable Management"); title.setFont(new Font("SansSerif",Font.BOLD,19)); title.setForeground(INK); top.add(title,BorderLayout.WEST);
        JLabel tag=new JLabel("ACADEMIC PLANNING WORKSPACE"); tag.setForeground(MUTED); tag.setFont(new Font("SansSerif",Font.BOLD,9)); top.add(tag,BorderLayout.EAST); root.add(top,BorderLayout.NORTH);
        content.setBackground(BG); content.setBorder(new EmptyBorder(25,28,18,28));
        content.add(buildDashboard(),"dashboard"); content.add(buildLoad(),"load"); content.add(buildGenerate(),"generate"); content.add(buildSchedule(),"schedule"); content.add(buildIssues(),"issues"); content.add(buildEdit(),"edit"); content.add(buildRequest(),"request"); content.add(buildExport(),"export");
        root.add(content,BorderLayout.CENTER); JPanel bottom=new JPanel(new BorderLayout()); bottom.setBackground(WHITE); bottom.setBorder(new EmptyBorder(9,22,9,22)); status.setForeground(MUTED); status.setFont(new Font("SansSerif",Font.PLAIN,11)); bottom.add(status,BorderLayout.WEST); root.add(bottom,BorderLayout.SOUTH); return root;
    }
    private JPanel page(String title,String subtitle) { JPanel p=new JPanel(new BorderLayout(0,18)); p.setBackground(BG); JLabel h=new JLabel(title); h.setFont(new Font("SansSerif",Font.BOLD,25)); h.setForeground(INK); JLabel s=new JLabel(subtitle); s.setFont(new Font("SansSerif",Font.PLAIN,12)); s.setForeground(MUTED); JPanel head=new JPanel(new GridLayout(2,1,0,4)); head.setOpaque(false); head.add(h); head.add(s); p.add(head,BorderLayout.NORTH); return p; }
    private JPanel buildDashboard() {
        JPanel p=page("Good morning, Coordinator","Your department scheduling workspace at a glance."); JPanel body=new JPanel(new BorderLayout(0,18)); body.setOpaque(false);
        JPanel metrics=new JPanel(new GridLayout(1,4,14,0)); metrics.setOpaque(false); metrics.add(metric("COURSES",coursesValue,"Loaded from courses.csv",new Color(49,101,232))); metrics.add(metric("ROOMS",roomsValue,"Available inventory",new Color(16,153,129))); metrics.add(metric("SCHEDULED",scheduledValue,"Valid assignments",new Color(120,92,220))); metrics.add(metric("UNSCHEDULED",unscheduledValue,"Require attention",new Color(224,145,50)));
        JPanel actions=panelCard(); actions.setLayout(new BorderLayout(0,12)); JLabel at=new JLabel("Quick actions"); at.setFont(new Font("SansSerif",Font.BOLD,16)); actions.add(at,BorderLayout.NORTH);
        JPanel buttons=new JPanel(new GridLayout(1,3,12,0)); buttons.setOpaque(false); buttons.add(actionCard("01","Load source files","Import courses, instructors, rooms and requests.","load")); buttons.add(actionCard("02","Generate timetable","Run the rule-based scheduling engine.","generate")); buttons.add(actionCard("03","Review schedule","Explore sessions and unresolved conflicts.","schedule")); actions.add(buttons,BorderLayout.CENTER);
        JPanel recent=panelCard(); recent.setLayout(new BorderLayout()); JLabel rt=new JLabel("Activity & system messages"); rt.setFont(new Font("SansSerif",Font.BOLD,15)); recent.add(rt,BorderLayout.NORTH); logArea.setEditable(false); logArea.setFont(new Font(Font.MONOSPACED,Font.PLAIN,11)); logArea.setForeground(new Color(66,78,98)); logArea.setBackground(new Color(249,250,252)); logArea.setBorder(new EmptyBorder(12,12,12,12)); recent.add(new JScrollPane(logArea),BorderLayout.CENTER);
        body.add(metrics,BorderLayout.NORTH); JPanel lower=new JPanel(new GridLayout(2,1,0,14)); lower.setOpaque(false); lower.add(actions); lower.add(recent); body.add(lower,BorderLayout.CENTER); p.add(body,BorderLayout.CENTER); return p;
    }
    private JPanel metric(String label,JLabel value,String hint,Color accent) { JPanel c=panelCard(); c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS)); JLabel l=new JLabel(label); l.setForeground(MUTED); l.setFont(new Font("SansSerif",Font.BOLD,10)); value.setForeground(INK); value.setFont(new Font("SansSerif",Font.BOLD,29)); JLabel h=new JLabel(hint); h.setForeground(MUTED); h.setFont(new Font("SansSerif",Font.PLAIN,10)); c.add(l); c.add(Box.createVerticalStrut(10)); c.add(value); c.add(Box.createVerticalStrut(5)); c.add(h); c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3,0,0,0,accent),new EmptyBorder(16,17,15,17))); return c; }
    private JPanel actionCard(String n,String title,String desc,String target) { JPanel c=panelCard(); c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS)); JLabel no=new JLabel(n); no.setForeground(BLUE); no.setFont(new Font("SansSerif",Font.BOLD,11)); JLabel t=new JLabel(title); t.setForeground(INK); t.setFont(new Font("SansSerif",Font.BOLD,14)); JLabel d=new JLabel("<html><div style='width:190px'>"+desc+"</div></html>"); d.setForeground(MUTED); d.setFont(new Font("SansSerif",Font.PLAIN,11)); JButton b=button("Open →"); b.addActionListener(e->showScreen(target)); c.add(no); c.add(Box.createVerticalStrut(8)); c.add(t); c.add(Box.createVerticalStrut(6)); c.add(d); c.add(Box.createVerticalGlue()); c.add(b); return c; }
    private JPanel buildLoad() { JPanel p=page("Load input data","Import the four UTF-8 source files from the project's data folder."); JPanel c=panelCard(); c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS)); c.add(fileRow("courses.csv","Course curriculum, year, instructor, hours and capacity")); c.add(fileRow("instructors.txt","Instructor identity, availability and preferred times")); c.add(fileRow("rooms.csv","Room type, capacity and unavailable periods")); c.add(fileRow("constraints.txt","Mandatory and preferred scheduling requests")); c.add(Box.createVerticalStrut(20)); JButton b=button("Load data from ./data"); b.addActionListener(e->loadData()); c.add(b); p.add(c,BorderLayout.CENTER); return p; }
    private JPanel fileRow(String name,String desc) { JPanel r=new JPanel(new BorderLayout()); r.setOpaque(false); r.setBorder(new EmptyBorder(13,0,13,0)); JLabel n=new JLabel("▤  "+name); n.setFont(new Font("SansSerif",Font.BOLD,13)); JLabel d=new JLabel(desc); d.setFont(new Font("SansSerif",Font.PLAIN,11)); d.setForeground(MUTED); JPanel t=new JPanel(new GridLayout(2,1,0,4)); t.setOpaque(false); t.add(n); t.add(d); r.add(t,BorderLayout.CENTER); r.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(232,236,243))); return r; }
    private JPanel buildGenerate() { JPanel p=page("Generate timetable","Create a weekly timetable while enforcing all mandatory hard constraints."); JPanel c=panelCard(); c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS)); JLabel a=new JLabel("Scheduling strategy"); a.setFont(new Font("SansSerif",Font.BOLD,16)); c.add(a); c.add(Box.createVerticalStrut(10)); c.add(note("1  Fixed-time sessions are processed first.")); c.add(note("2  Remaining sessions: largest classes first, then course code, theory before lab.")); c.add(note("3  Candidate slots are ordered by soft-constraint preferences.")); c.add(note("4  Suitable rooms use best-fit capacity; alternate slots and rooms are tried.")); c.add(note("5  Assignments are recorded only when HC-01 to HC-09 are satisfied.")); c.add(Box.createVerticalStrut(18)); JButton run=button("✦  Generate weekly timetable"); run.addActionListener(e->generate()); c.add(run); p.add(c,BorderLayout.CENTER); return p; }
    private JLabel note(String text) { JLabel l=new JLabel("•  "+text); l.setFont(new Font("SansSerif",Font.PLAIN,13)); l.setForeground(new Color(71,84,105)); l.setBorder(new EmptyBorder(8,4,8,4)); return l; }
    private JPanel buildSchedule() { JPanel p=page("Weekly timetable","Browse the Monday–Friday calendar or select a session from the detailed list."); JPanel body=new JPanel(new BorderLayout(0,12)); body.setOpaque(false); JPanel tools=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); tools.setOpaque(false); yearFilter.addActionListener(e->refreshSchedule()); tools.add(new JLabel("  Filter: ")); tools.add(yearFilter); JButton all=button("Show all years"); all.addActionListener(e->{yearFilter.setSelectedIndex(-1);refreshSchedule();}); tools.add(all); body.add(tools,BorderLayout.NORTH); styleTable(scheduleTable); styleTable(calendarTable); calendarTable.setRowHeight(66); calendarTable.getColumnModel().getColumn(0).setPreferredWidth(82); for(int i=1;i<6;i++) calendarTable.getColumnModel().getColumn(i).setPreferredWidth(190); calendarTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS); JTabbedPane tabs=new JTabbedPane(); tabs.addTab("Weekly calendar",new JScrollPane(calendarTable)); tabs.addTab("Session list · select to edit",new JScrollPane(scheduleTable)); body.add(tabs,BorderLayout.CENTER); p.add(body,BorderLayout.CENTER); return p; }
    private JPanel buildIssues() { JPanel p=page("Unscheduled sessions & warnings","Understand why a session could not be placed and what needs attention."); styleTable(issueTable); p.add(new JScrollPane(issueTable),BorderLayout.CENTER); return p; }
    private JPanel buildEdit() { JPanel p=page("Edit timetable assignment","Move or remove an assignment. Hard constraints are checked before changes are applied."); JPanel c=panelCard(); c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS)); c.add(note("Select a scheduled session from the Weekly timetable screen, then return here.")); JButton edit=button("Edit selected session"); edit.addActionListener(e->editSelected()); JButton remove=button("Remove selected assignment"); remove.addActionListener(e->removeSelected()); JButton assign=button("Assign selected unscheduled session"); assign.addActionListener(e->assignSelectedUnscheduled()); c.add(edit); c.add(Box.createVerticalStrut(10)); c.add(remove); c.add(Box.createVerticalStrut(10)); c.add(assign); p.add(c,BorderLayout.CENTER); return p; }
    private JPanel buildRequest() { JPanel p=page("Classroom request","Prepare a formal email draft for sessions classified as NO ROOM."); JPanel c=panelCard(); c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS)); JButton create=button("Create classroom request draft"); create.addActionListener(e->createRequest()); c.add(note("Only NO ROOM sessions are eligible. No email is sent by the application.")); c.add(create); p.add(c,BorderLayout.CENTER); return p; }
    private JPanel buildExport() { JPanel p=page("Export timetable","Save the full weekly timetable, session list, unscheduled sessions and warnings."); JPanel c=panelCard(); c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS)); c.add(note("The application creates the output folder if it does not exist.")); c.add(note("timetable.txt — year-group views, all sessions, unscheduled sessions and warnings.")); c.add(note("timetable.csv — all scheduled sessions in structured form.")); JButton save=button("⇩  Save timetable files"); save.addActionListener(e->saveFiles()); c.add(Box.createVerticalStrut(18)); c.add(save); p.add(c,BorderLayout.CENTER); return p; }
    private JPanel panelCard() { JPanel p=new JPanel(); p.setBackground(WHITE); p.setBorder(new EmptyBorder(20,22,20,22)); return p; }
    private JButton button(String text) { JButton b=new JButton(text); b.setBackground(BLUE); b.setForeground(WHITE); b.setFocusPainted(false); b.setBorder(new EmptyBorder(11,17,11,17)); b.setFont(new Font("SansSerif",Font.BOLD,12)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b; }
    private void styleTable(JTable t) { t.setRowHeight(34); t.setFont(new Font("SansSerif",Font.PLAIN,12)); t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,11)); t.getTableHeader().setBackground(new Color(238,242,248)); t.setGridColor(new Color(235,238,244)); t.setSelectionBackground(new Color(224,234,255)); t.setFillsViewportHeight(true); }
    private void showScreen(String key) { cards.show(content,key); if(key.equals("schedule")) refreshSchedule(); if(key.equals("issues")) refreshIssues(); }
    private void loadData() { DataLoader.LoadSummary r=loader.loadAll(dataDir,store); timetable.clear(); scheduler=new Scheduler(store,timetable); report=new SchedulingReport(); report.warnings.addAll(r.warnings()); refreshDashboard();
        String msg="Loaded courses: "+r.courses()+" · instructors: "+r.instructors()+" · rooms: "+r.rooms()+" · requests: "+r.requests()+"\nWarnings: "+r.warnings().size();
        if(!r.errors().isEmpty()) msg+="\nErrors:\n"+String.join("\n",r.errors()); log(msg); status.setText("Data loaded · "+r.warnings().size()+" warning(s)"); JOptionPane.showMessageDialog(this,msg,"Input processing",r.errors().isEmpty()?JOptionPane.INFORMATION_MESSAGE:JOptionPane.WARNING_MESSAGE); }
    private void generate() { if(!store.isReady()) { JOptionPane.showMessageDialog(this,"Load valid courses, instructors and rooms first.","Data required",JOptionPane.WARNING_MESSAGE); return; }
        scheduler=new Scheduler(store,timetable); report=scheduler.generate(); refreshDashboard(); refreshSchedule(); refreshIssues(); log(String.join("\n",report.log)); status.setText("Generation complete · "+timetable.all().size()+" scheduled · "+report.problems.size()+" unscheduled"); JOptionPane.showMessageDialog(this,report.log.isEmpty()?"No result":report.log.get(report.log.size()-1),"Scheduling complete",JOptionPane.INFORMATION_MESSAGE); }
    private void refreshDashboard() { coursesValue.setText(String.valueOf(store.courses.size())); roomsValue.setText(String.valueOf(store.rooms.size())); scheduledValue.setText(String.valueOf(timetable.all().size())); unscheduledValue.setText(String.valueOf(report.problems.size())); }
    private void refreshSchedule() { scheduleModel.setRowCount(0); calendarModel.setRowCount(0); int selected=yearFilter.getSelectedIndex()+1; Object[][] cells=new Object[8][5]; for(int h=0;h<8;h++) calendarModel.addRow(new Object[]{TimeSlot.fmt(9+h),"","","","",""}); for(Assignment a:timetable.all()) if(yearFilter.getSelectedIndex()<0||a.session.course.year==selected) { scheduleModel.addRow(new Object[]{a.session.id(),a.session.course.title,a.session.course.year,a.slot.day().name(),TimeSlot.fmt(a.slot.startHour())+"–"+TimeSlot.fmt(a.slot.endHour()),a.room.id,a.session.course.instructorId}); int col=a.slot.day().getValue(); int row=a.slot.startHour()-9; if(row>=0&&row<8&&col>=1&&col<=5) { String label="<html><b>"+a.session.id()+"</b><br>"+a.session.course.title+"<br><span style='color:#64748b'>"+TimeSlot.fmt(a.slot.startHour())+"–"+TimeSlot.fmt(a.slot.endHour())+" · "+a.room.id+"</span></html>"; calendarModel.setValueAt(label,row,col); for(int h=1;h<a.slot.duration()&&row+h<8;h++) calendarModel.setValueAt("<html><span style='color:#64748b'>↳ "+a.session.id()+" (cont.)</span></html>",row+h,col); } } }
    private void refreshIssues() { issueModel.setRowCount(0); for(SchedulingProblem p:report.problems) issueModel.addRow(new Object[]{p.session.id(),p.session.course.year,p.type,p.reason}); }
    private void log(String text) { logArea.append("\n"+text+"\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }
    private Assignment selectedAssignment() { int row=scheduleTable.getSelectedRow(); if(row<0) return null; String id=String.valueOf(scheduleModel.getValueAt(row,0)); return timetable.find(id); }
    private void editSelected() { Assignment current=selectedAssignment(); if(current==null) { JOptionPane.showMessageDialog(this,"Select a session in Weekly timetable first."); return; }
        JComboBox<String> day=new JComboBox<>(new String[]{"MON","TUE","WED","THU","FRI"}); day.setSelectedItem(current.slot.day().name().substring(0,3)); JSpinner start=new JSpinner(new SpinnerNumberModel(current.slot.startHour(),9,17-current.slot.duration(),1)); JComboBox<Room> room=new JComboBox<>(store.rooms.values().stream().filter(r->r.isSuitableFor(current.session)).toArray(Room[]::new)); room.setRenderer(new DefaultListCellRenderer(){public Component getListCellRendererComponent(JList<?>l,Object v,int i,boolean s,boolean f){super.getListCellRendererComponent(l,v,i,s,f); if(v instanceof Room r)setText(r.id+" · "+r.type+" · cap "+r.capacity);return this;}}); room.setSelectedItem(current.room);
        JPanel form=new JPanel(new GridLayout(3,2,8,8)); form.add(new JLabel("Day"));form.add(day);form.add(new JLabel("Start hour"));form.add(start);form.add(new JLabel("Room"));form.add(room);
        if(JOptionPane.showConfirmDialog(this,form,"Edit "+current.session.id(),JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        TimeSlot proposed=new TimeSlot(TimeSlot.parseDay(String.valueOf(day.getSelectedItem())),(Integer)start.getValue(),current.slot.duration()); Room selected=(Room)room.getSelectedItem(); TimetableEditor editor=new TimetableEditor(store,timetable); TimetableEditor.Validation v=editor.validateChange(current,proposed,selected);
        if(!v.accepted()){JOptionPane.showMessageDialog(this,String.join("\n",v.hard().stream().map(Object::toString).toList()),"Change rejected",JOptionPane.ERROR_MESSAGE);return;}
        boolean accept=v.soft().isEmpty()||JOptionPane.showConfirmDialog(this,String.join("\n",v.soft())+"\nApply anyway?","Soft-constraint warning",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;
        if(accept){editor.applyChange(current,proposed,selected,true);refreshDashboard();refreshSchedule();log("Manual edit: "+current.session.id()+" → "+proposed);}
    }
    private void assignSelectedUnscheduled() {
        int row=issueTable.getSelectedRow();
        if(row<0) { JOptionPane.showMessageDialog(this,"Select a row in Unscheduled & warnings first."); return; }
        String id=String.valueOf(issueModel.getValueAt(row,0));
        SchedulingProblem problem=report.problems.stream().filter(x->x.session.id().equals(id)).findFirst().orElse(null);
        if(problem==null) { JOptionPane.showMessageDialog(this,"Select an unscheduled session, not a warning."); return; }
        JComboBox<String> day=new JComboBox<>(new String[]{"MON","TUE","WED","THU","FRI"});
        JSpinner start=new JSpinner(new SpinnerNumberModel(9,9,17-problem.session.duration,1));
        Room[] candidates=store.rooms.values().stream().filter(r->r.isSuitableFor(problem.session)).toArray(Room[]::new);
        JComboBox<Room> room=new JComboBox<>(candidates);
        JPanel form=new JPanel(new GridLayout(3,2,8,8)); form.add(new JLabel("Day"));form.add(day);form.add(new JLabel("Start hour"));form.add(start);form.add(new JLabel("Room"));form.add(room);
        if(candidates.length==0) { JOptionPane.showMessageDialog(this,"No room has the required type and capacity."); return; }
        if(JOptionPane.showConfirmDialog(this,form,"Assign "+id,JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        TimeSlot slot=new TimeSlot(TimeSlot.parseDay(String.valueOf(day.getSelectedItem())),(Integer)start.getValue(),problem.session.duration);
        Room selected=(Room)room.getSelectedItem(); ConstraintChecker checker=new ConstraintChecker(store,timetable);
        List<Violation> hard=checker.checkHard(problem.session,slot,selected,null);
        if(!hard.isEmpty()) { JOptionPane.showMessageDialog(this,String.join("\n",hard.stream().map(Object::toString).toList()),"Assignment rejected",JOptionPane.ERROR_MESSAGE); return; }
        List<String> soft=checker.checkSoft(problem.session,slot);
        if(!soft.isEmpty()&&JOptionPane.showConfirmDialog(this,String.join("\n",soft)+"\nAssign anyway?","Soft-constraint warning",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        timetable.add(new Assignment(problem.session,slot,selected)); report.problems.remove(problem); refreshDashboard(); refreshSchedule(); refreshIssues(); log("Manually assigned "+id+" to "+slot+" · "+selected.id);
    }
    private void removeSelected() { Assignment a=selectedAssignment(); if(a==null){JOptionPane.showMessageDialog(this,"Select a session in Weekly timetable first.");return;} if(JOptionPane.showConfirmDialog(this,"Remove "+a.session.id()+" from the timetable?","Confirm removal",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){new TimetableEditor(store,timetable).removeAssignment(a);refreshDashboard();refreshSchedule();log("Removed assignment: "+a.session.id());} }
    private void createRequest() { if(report.byType(ProblemType.NO_ROOM).isEmpty()){JOptionPane.showMessageDialog(this,"There are no NO ROOM sessions.");return;} List<SchedulingProblem> ps=report.byType(ProblemType.NO_ROOM); SchedulingProblem p=(SchedulingProblem)JOptionPane.showInputDialog(this,"Choose a session:","Classroom request",JOptionPane.QUESTION_MESSAGE,null,ps.toArray(),ps.get(0)); if(p==null)return;
        TimeSlot suggestion=scheduler==null?null:scheduler.suggestSlot(p.session); String draft=emailGenerator.generateDraft(p,suggestion); JTextArea area=new JTextArea(draft,17,58); area.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12)); if(JOptionPane.showConfirmDialog(this,new JScrollPane(area),"Review email draft",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) try {Path file=emailGenerator.saveDraft(p,area.getText(),outputDir);log("Saved classroom request: "+file);JOptionPane.showMessageDialog(this,"Draft saved to "+file);}catch(IOException ex){showError(ex);} }
    private void saveFiles() { try {printer.save(timetable,report,outputDir);status.setText("Saved to "+outputDir);log("Saved timetable.txt and timetable.csv to "+outputDir);JOptionPane.showMessageDialog(this,"Files saved to "+outputDir);}catch(IOException ex){showError(ex);} }
    private void showError(Exception ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"Operation failed",JOptionPane.ERROR_MESSAGE); }
}
