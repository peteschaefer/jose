package de.jose.eboard;

import javax.swing.*;

public class DialogComponent extends JComponent
{
    public DialogComponent()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(new JLabel("eboard.connected"));
        add(new JButton("eboard.connect"));

        JComboBox<String> oriCombo = new JComboBox<String>();
        oriCombo.addItem("Auto Detect");
        oriCombo.addItem("White Up");
        oriCombo.addItem("Black Up");

        add(new JLabel("eboard.orientation"));
        add(oriCombo);
    }
}
