package de.jose.eboard;

import de.jose.Application;
import de.jose.Language;
import de.jose.image.ImgUtil;
import de.jose.window.JoDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DialogComponent extends JComponent implements ActionListener
{
    private EBoardConnector eboard;

    private JRadioButton synchLead,synchFollow;
    private ButtonGroup synchGroup;
    private JLabel status;
    private JButton connect;
    private JComboBox ori;

    public DialogComponent(boolean with_synch)
    {
        eboard = Application.theApplication.getEBoardConnector();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        if (with_synch) {
            JPanel synchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            synchPanel.add(synchLead = JoDialog.newRadioButton("eboard.synch.leads"), 	JoDialog.gridConstraint(JoDialog.LABEL_ONE_LEFT, 0,0,1));
            synchPanel.add(synchFollow = JoDialog.newRadioButton("eboard.synch.follows"), 	JoDialog.gridConstraint(JoDialog.LABEL_ONE_LEFT, 1,0,1));
            synchGroup = new ButtonGroup();
            synchGroup.add(synchLead);
            synchGroup.add(synchFollow);
            this.add(synchPanel);
        }

        JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectPanel.add(status = JoDialog.newLabel("eboard.na"));
        connectPanel.add(connect = JoDialog.newButton("eboard.connect", ImgUtil.getMenuIcon("eboard.connect"),this));

        ori = new JComboBox<String>();
        ori.addItem(Language.get("eboard.orientation.auto"));
        ori.addItem(Language.get("eboard.orientation.white"));
        ori.addItem(Language.get("eboard.orientation.black"));

        connectPanel.add(JoDialog.newLabel("eboard.orientation"));
        connectPanel.add(ori);
        this.add(connectPanel);

        updateStatus();
    }

    protected void updateStatus()
    {
        if (eboard==null || !eboard.isAvailable()) {
            status.setText(Language.get("eboard.na"));
            status.setForeground(Color.BLACK);

            connect.setText(Language.get("eboard.connect"));
            connect.setEnabled(false);
        }
        else if (!eboard.isConnected()) {
            status.setText(Language.get("eboard.off"));
            status.setForeground(Color.RED.darker());

            connect.setText(Language.get("eboard.connect"));
            connect.setEnabled(true);
            connect.setActionCommand("eboard.connect");
        }
        else {
            status.setText(Language.get("eboard.on"));
            status.setForeground(Color.GREEN.darker());

            connect.setText(Language.get("eboard.disconnect"));
            connect.setEnabled(true);
            connect.setActionCommand("eboard.disconnect");
        }

        ori.setSelectedIndex(eboard.inputOri.ordinal());
        if (synchLead!=null)
            synchLead.setSelected(eboard.mode==EBoardConnector.Mode.SETUP_LEAD);
        if (synchFollow!=null)
            synchFollow.setSelected(eboard.mode==EBoardConnector.Mode.SETUP_FOLLOW);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        Application.theCommandDispatcher.handle(e,Application.theApplication);
        updateStatus();
    }
}
