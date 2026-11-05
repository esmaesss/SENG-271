package timetable;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public final class Main {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings","on");
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored) {}
            MainFrame frame=new MainFrame(Path.of("data"),Path.of("output"));
            frame.setVisible(true);
        });
    }
}
