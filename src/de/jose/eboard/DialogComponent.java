package de.jose.eboard;

import de.jose.Application;
import de.jose.Language;
import de.jose.image.ImgUtil;
import de.jose.view.IBoardAdapter;
import de.jose.window.JoDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DialogComponent extends JComponent implements ActionListener
{
    public EBoardConnector eboard;

    private JRadioButton synchLead,synchFollow;
    private ButtonGroup synchGroup;
    private JLabel status;
    private JButton connect;
    private JComboBox ori;

    public DialogComponent(boolean with_synch)
    {
        eboard = Application.theApplication.getEBoardConnector();

        setLayout(new FlowLayout(FlowLayout.LEFT));

        if (with_synch) {
            add(synchLead = JoDialog.newRadioButton("eboard.synch.leads"), 	JoDialog.gridConstraint(JoDialog.LABEL_ONE_LEFT, 0,0,1));
            add(synchFollow = JoDialog.newRadioButton("eboard.synch.follows"), 	JoDialog.gridConstraint(JoDialog.LABEL_ONE_LEFT, 1,0,1));
            synchGroup = new ButtonGroup();
            synchGroup.add(synchLead);
            synchGroup.add(synchFollow);
            add(Box.createHorizontalStrut(12));
        }

        add(status = JoDialog.newLabel("eboard.na"));
        add(connect = JoDialog.newButton("eboard.connect", ImgUtil.getMenuIcon("eboard.connect"),this));

        ori = new JComboBox<String>();
        ori.addItem(Language.get("eboard.orientation.auto"));
        ori.addItem(Language.get("eboard.orientation.white"));
        ori.addItem(Language.get("eboard.orientation.black"));

        add(Box.createHorizontalStrut(12));
        add(JoDialog.newLabel("eboard.orientation"));
        add(ori);

        updateStatus();
    }


    protected void updateStatus()
    {
        setEnabled(eboard!=null && eboard.isAvailable());

        if (eboard==null || !eboard.isAvailable()) {
            status.setText(Language.get("eboard.na"));
            status.setForeground(Color.BLACK);

            connect.setText(Language.get("eboard.connect"));
            connect.setEnabled(false);
        }
        else if (eboard.connected) {
            status.setText(Language.get("eboard.on"));
            status.setForeground(Color.GREEN.darker());

            connect.setText(Language.get("eboard.disconnect"));
            connect.setEnabled(true);
            connect.setActionCommand("eboard.disconnect");

            ori.setSelectedIndex(eboard.inputOri.ordinal());
            ori.setEnabled(true);
        }
        else {
            status.setText(Language.get("eboard.off"));
            status.setForeground(Color.RED.darker());

            connect.setText(Language.get("eboard.connect"));
            connect.setEnabled(true);
            connect.setActionCommand("eboard.connect");

            ori.setSelectedIndex(0);
            ori.setEnabled(false);
        }

        if (synchLead!=null)
            synchLead.setSelected(eboard.connected && eboard.mode==EBoardConnector.Mode.SETUP_LEAD);
        if (synchFollow!=null)
            synchFollow.setSelected(eboard.connected && eboard.mode==EBoardConnector.Mode.SETUP_FOLLOW);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        Application.theCommandDispatcher.handle(e,Application.theApplication);
        updateStatus();
    }
}
