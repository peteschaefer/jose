package de.jose.db;

import de.jose.Application;
import de.jose.Command;
import de.jose.CommandAction;
import de.jose.Language;
import de.jose.task.db.GameRepair;
import de.jose.util.file.TailOutputStream;
import de.jose.view.input.JoBigLabel;
import de.jose.window.JoDialog;
import de.jose.window.JoFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DBRepairTool
    extends JoDialog {
    private int stage = 0;
    private JLabel[] labels = new JLabel[8];
    private JTextArea tail;
    private PrintStream wasOut,wasErr,tailStream;
    private JButton nextButton;

    public DBRepairTool() {
        super(null, "menu.help.repair", true);
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
        JLabel title;
        comp.setLayout(layout);
        comp.add(title = newLabel("dialog.repair.title"),ELEMENT_ROW_SMALL);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16.0f));

        JoBigLabel explain = new JoBigLabel(Language.get("dialog.repair.explain"));
        comp.add(explain,ELEMENT_ROW);
        comp.add(Box.createVerticalStrut(20));

        for (stage=1; stage <= 7; stage++)
            comp.add(newLabel(stage,"dialog.repair."+stage),ELEMENT_ROW_SMALL);

        tail = newTextArea(120,40);
        JScrollPane scroll = new JScrollPane(tail);
        comp.add(scroll,ELEMENT_NEXTROW_REMAINDER);

        nextButton = addButton("dialog.button.next");

        JButton quit = addButton("dialog.button.quit");
        quit.setText(Language.get("menu.file.quit"));

        updateLabels(stage = 0, false);

        redirectStdIO();
    }

    private void redirectStdIO()
    {
        wasOut = System.out;
        wasErr = System.err;
        tailStream = new PrintStream(new TailOutputStream(tail));
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

    private class PipeThread extends Thread {
        private Process process;
        private PrintStream out;

        public PipeThread(Process process, PrintStream out) {
            this.process = process;
            this.out = out;
        }

        @Override
        public void run() {
            InputStream in1 = process.getInputStream();
            InputStream in2 = process.getErrorStream();
            BufferedReader bin1 = new BufferedReader(new InputStreamReader(in1));
            BufferedReader bin2 = new BufferedReader(new InputStreamReader(in2));

            while(process.isAlive()) {
                String line1 = null;
                String line2 = null;
                try {
                    line1 = bin1.readLine();
                    //line2 = bin2.readLine();
                } catch (IOException e) { }
                if (line1!=null)
                    this.out.println(line1);
                if (line2!=null)
                    this.out.println(line2);
                this.out.flush();
            }
            try {
                process.waitFor();
            } catch (InterruptedException e) { }
        }
    }

    protected Thread worker = null;
    protected Timer poll;

    protected void waitFor(Thread thread)
    {
        worker = thread;
        nextButton.setEnabled(false);
        poll = new Timer(500,this);
        poll.setRepeats(true);
        poll.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==poll) {
            if (!worker.isAlive()) {
                poll.stop();
                updateLabels(stage, false);
            }
        }
        super.actionPerformed(e);
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
            public void Do(Command cmd) throws Exception {
                if (++stage >= 8) {
                    hide();
                    return;
                }

                System.out.print("--- ");
                System.out.print(labels[stage].getText());
                System.out.println(" ---");
                updateLabels(stage, true);

                boolean done=false;
                try {
                    switch (stage) {
                        case 1: done=closeDBWindow(); break;
                        case 2: done=shutdownDB(); break;
                        case 3: done=checkIndexes(false); break;
                        case 4: done=checkIndexes(true); break;
                        case 5: done=launchDB(); break;
                        case 6: done=gameRepair(); break;
                        //case 7: done=openDBWindow(); break;
                        case 7: done=quitApplication(); break;
                    }
                } catch (Throwable e) {
                    System.err.println(e.getMessage());
                }

                updateLabels(stage, !done);
            }
        };
        map.put("dialog.button.next", action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
                quitApplication();
            }
        };
        map.put("dialog.button.quit", action);
    }

    protected boolean quitApplication()
    {
        Command cmd = new Command("menu.file.quit");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Application.theApplication.quit(cmd);
                } catch (Exception e) {
                    Application.error(e);
                }
            }
        });
        return true;
    }

    protected boolean closeDBWindow() {
        Application.theApplication.hidePanelFrame("window.collectionlist");
        Application.theApplication.hidePanelFrame("window.list");
        Application.theApplication.hidePanelFrame("window.gamelist");
        Application.theApplication.hidePanelFrame("window.query");
        Application.theApplication.hidePanelFrame("window.sqlquery");
        Application.theApplication.hidePanelFrame("window.sqllist");
        return true;
    }

    protected boolean shutdownDB()
    {
        DBAdapter ad = JoConnection.getAdapter(false);
        Thread shutdownThread = null;

        if (ad!=null && (ad.getServerMode()==DBAdapter.MODE_STANDALONE) && JoConnection.isConnected())
        {
            JoConnection conn = null;
            try {
                conn = JoConnection.get();
                shutdownThread = ad.shutDown(null);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        JoConnection.closeAll();

        if (shutdownThread!=null)
            waitFor(shutdownThread);
        return false;
    }

    protected boolean checkIndexes(boolean repair) throws IOException, InterruptedException {
        String[] tables = { "MetaInfo", "Collection", "Game", "MoreGame", "Player", "Event", "Site", "Opening" };
        Process proc = MySQLAdapter.repairIndexes(tables,repair);
        Thread pipeThread = new PipeThread(proc, tailStream);
        pipeThread.start();
        waitFor(pipeThread);
        //int result = proc.waitFor();
        return false;
    }

    protected boolean launchDB() throws IOException, InterruptedException {
        MySQLAdapter ad = (MySQLAdapter)JoConnection.getAdapter(true);
        Thread launcher = ad.launchProcess(false);
        waitFor(launcher);
        return false;
    }

    protected boolean gameRepair() throws Exception {
        GameRepair gameRepair = new GameRepair();
        gameRepair.start();
        waitFor(gameRepair);
        return false;
    }

    protected boolean openDBWindow() {
        Application.theApplication.showPanelFrame("window.collectionlist");
        return true;
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

        nextButton.setEnabled(!active);
        if (st==7 && !active)
            nextButton.setText(Language.get("dialog.button.close"));
    }

}