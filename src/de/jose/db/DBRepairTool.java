package de.jose.db;

import de.jose.Application;
import de.jose.Command;
import de.jose.CommandAction;
import de.jose.Language;
import de.jose.task.db.GameRepair;
import de.jose.util.file.TailOutputStream;
import de.jose.window.JoDialog;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.SQLException;
import java.util.Map;

public class DBRepairTool
    extends JoDialog {
    private int stage = 0;
    private JLabel[] labels = new JLabel[8];
    private JTextArea tail;
    private PrintStream wasOut,wasErr;

    public DBRepairTool() {
        super("menu.help.repair", true);
        center(600, 640);
    }

    public static void open() {
        DBRepairTool tool = new DBRepairTool();
        tool.init();
        tool.show();
    }

    private JLabel newLabel(int stage, String text)
    {
        text = Language.get(text);
        if (stage > 0)
            text = stage+". "+text;
        JLabel label = newLabel(text);
        labels[stage] = label;
        return label;
    }

    private void init()
    {
        JComponent comp = getElementPane();

        GridBagLayout layout = new GridBagLayout();
        comp.setLayout(layout);
        comp.add(newLabel(0,"Repair Database"),ELEMENT_ROW_SMALL);
        labels[0].setFont(labels[0].getFont().deriveFont(Font.BOLD, 16.0f));

        comp.add(Box.createVerticalStrut(20));

        comp.add(newLabel(1,"Close Database Window"),ELEMENT_ROW_SMALL);
        comp.add(newLabel(2,"Shutdown Database Process"),ELEMENT_ROW_SMALL);
        comp.add(newLabel(3,"Check Index Files"),ELEMENT_ROW_SMALL);
        comp.add(newLabel(4,"Repair Index Files"),ELEMENT_ROW_SMALL);
        comp.add(newLabel(5,"Launch Database Process"),ELEMENT_ROW_SMALL);
        comp.add(newLabel(6,"Recover Lost References"),ELEMENT_ROW_SMALL);
        comp.add(newLabel(7,"Open Database Window"),ELEMENT_ROW_SMALL);

        comp.add(tail = newTextArea(120,40),ELEMENT_NEXTROW_REMAINDER);

        addButton("dialog.button.next");
        addButton("dialog.button.close");
        addButton("menu.file.quit");

        updateLabels(stage = 0, false);

        redirectStdIO();
    }

    private void redirectStdIO()
    {
        wasOut = System.out;
        wasErr = System.err;
        PrintStream tailStream = new PrintStream(new TailOutputStream(tail));
        System.setOut(tailStream);
        System.setErr(tailStream);
        System.err.println("stderr redirected");
        System.err.println("stdout redirected");
    }

    private void restoreStdIO()
    {
        System.setOut(wasOut);
        System.setErr(wasErr);
    }

    private void pipeOutput(Process process) throws IOException {
        InputStream in1 = process.getInputStream();
        InputStream in2 = process.getErrorStream();
        BufferedReader bin = new BufferedReader(new InputStreamReader(in2));
        while(process.isAlive()) {
            String line = bin.readLine();
            if (line!=null)
                System.out.println(line);
        }
    }

    @Override
    public void hide() {
        super.hide();
        restoreStdIO();
    }

    @Override
    public void setupActionMap(Map map) {
        super.setupActionMap(map);

        CommandAction action;
        action = new CommandAction() {
            @Override
            public boolean isEnabled(String cmd) {
                return stage < 7;
            }

            public void Do(Command cmd) throws Exception {
                updateLabels(++stage, true);
                try {
                    switch (stage) {
                        case 1: closeDBWindow(); break;
                        case 2: shutdownDB(); break;
                        case 3: checkIndexes(false); break;
                        case 4: checkIndexes(true); break;
                        case 5: launchDB(); break;
                        case 6: gameRepair(); break;
                        case 7: openDBWindow(); break;
                        case 8: hide(); break;
                    }
                } catch (Throwable e) {
                    System.err.println(e.getMessage());
                }
                updateLabels(stage, false);
            }
        };
        map.put("dialog.button.next", action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
                Application.theApplication.quit(cmd);
            }
        };
        map.put("menu.file.quit", action);
    }

    protected void closeDBWindow() {
        Application.theApplication.hidePanelFrame("window.collectionlist");
        Application.theApplication.hidePanelFrame("window.list");
        Application.theApplication.hidePanelFrame("window.gamelist");
        Application.theApplication.hidePanelFrame("window.query");
        Application.theApplication.hidePanelFrame("window.sqlquery");
        Application.theApplication.hidePanelFrame("window.sqllist");
    }

    protected void shutdownDB()
    {
        DBAdapter ad = JoConnection.getAdapter(false);
        Thread shutdownThread = null;

        if (ad!=null && (ad.getServerMode()==DBAdapter.MODE_STANDALONE) && JoConnection.isConnected())
        {
            JoConnection conn = null;
            try {
                conn = JoConnection.get();
                shutdownThread = ad.shutDown(conn);
            } catch (SQLException e) {
                // ?
            }
        }

        JoConnection.closeAll();

        while (shutdownThread!=null && shutdownThread.isAlive()) {
            try {
                shutdownThread.wait();
            } catch (InterruptedException e) { }
        }
    }

    protected void checkIndexes(boolean repair) throws IOException, InterruptedException {
        String[] tables = { "MetaInfo", "Collection", "Game", "MoreGame", "Player", "Event", "Site", "Opening" };
        Process proc = MySQLAdapter.repairIndexes(tables,repair);
        pipeOutput(proc);
        int result = proc.waitFor();
    }

    protected void launchDB() {
        MySQLAdapter ad = (MySQLAdapter)JoConnection.getAdapter(true);
        Thread launcher = ad.launchProcess(false);
        while(launcher.isAlive())
            try {
                launcher.wait();
            } catch (InterruptedException e) { }
    }

    protected void gameRepair() throws Exception {
        GameRepair gameRepair = new GameRepair();
        gameRepair.start();
        while(gameRepair.isAlive())
            try {
                gameRepair.wait();
            } catch (InterruptedException e) { }
    }

    protected void openDBWindow() {
        Application.theApplication.showPanelFrame("window.collectionlist");
    }

    protected void updateLabels(int st, boolean active) {
        for (int i = 1; i < labels.length; i++) {
            JLabel label = labels[i];
            Font font = label.getFont();

            label.setEnabled(i <= st);

            if ((i == st) && active)
                font = font.deriveFont(Font.BOLD);
            else
                font = font.deriveFont(Font.PLAIN);
            label.setFont(font);
        }
        getButton("dialog.button.next").setEnabled(st < 7);
    }

}