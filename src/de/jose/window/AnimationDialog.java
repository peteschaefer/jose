/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.window;

import de.jose.*;
import de.jose.comm.Command;
import de.jose.comm.CommandAction;
import de.jose.comm.CommandListener;
import de.jose.comm.msg.MessageListener;
import de.jose.profile.LayoutProfile;
import de.jose.util.ListUtil;
import de.jose.view.Animation;
import de.jose.view.JoToolBar;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

/**
 * contains Animation controls
 *
 * @author Peter Sch�fer
 */
public class AnimationDialog
        extends JoDialog
        implements ChangeListener, MessageListener
{
    private static String[] buttons = {
        "move.first", "move.backward", null,
		"move.forward", "move.last", null,
	    "move.animate", "engine.stop",
    };

    /** button panel    */
    private JoToolBar   buttonPanel;
    /** slider for speed control */
    private JSlider     slider;
	/** check box for animation hints   */
	private JCheckBox   hintCheckbox;
    /** the animation */
    private Animation   animation;


	private static Dimension PREFERRED_SIZE = Version.mac ?
              new Dimension(276,192)        //  optimized for Aqua L&F
            : new Dimension(276,172);

    public AnimationDialog(String name)
    {
        super(name,false);

        if (Application.theApplication.boardPanel() != null) {
            int x = Application.theApplication.boardPanel().getX() + Application.theApplication.boardPanel().getWidth();
            int y = Application.theApplication.boardPanel().getY();
            frame.setBounds(x,y,PREFERRED_SIZE.width,PREFERRED_SIZE.height);
        }
        else
            center(PREFERRED_SIZE.width,PREFERRED_SIZE.height);

        animation = Application.theApplication.getAnimation();
        animation.addMessageListener(this);
        buttonPanel = new JoToolBar(new LayoutProfile("animation.tool.bar"),
						ListUtil.toVector(buttons), FlowLayout.CENTER,
                        true,false);
        //  buttons report directly to the application
        getElementPane().add(buttonPanel,CENTER_ONE);

	    slider = createSlider("animation.speed");
	    slider.setValue((int)animation.getSpeed());
	    slider.addChangeListener(this);

        getElementPane().add(slider,CENTER_ONE);

	    //  show hints
	    hintCheckbox = newCheckBox("board.animation.hints");
		hintCheckbox.addChangeListener(this);
	    hintCheckbox.setSelected(Application.theUserProfile.getBoolean("board.animation.hints"));
	    getElementPane().add(hintCheckbox,ELEMENT_ONE);

        addButton("dialog.button.close");

		JoDialog.rescaleFonts(getButtonPane());
		JoDialog.rescaleFonts(getElementPane());
    }

	public static JSlider createSlider(String name)
	{
		JSlider slider = new JSlider(100,2500);
		slider.setName(name);
		slider.setInverted(true);

		Dictionary labels = new Hashtable();
		labels.put(new Integer(100), new JLabel(Language.get("animation.slider.fast")));
		labels.put(new Integer(2500), new JLabel(Language.get("animation.slider.slow")));
		slider.setLabelTable(labels);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setPaintTrack(true);
		return slider;
	}

    private void updateStatus()
    {
        buttonPanel.setSelected("engine.stop", ! animation.isRunning());
        buttonPanel.setSelected("move.animate", animation.isRunning());
    }

	public int numCommandChildren()
	{
			return 1;
	}

	public CommandListener getCommandChild(int i)
	{
		return buttonPanel;
	}

	public void setupActionMap(Map<String, CommandAction> map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				int speed = Util.toint(cmd.data);
				boolean hints = Util.toboolean(cmd.moreData);
				//  animation will be adjusted by the Application
				if (speed!=slider.getValue()) slider.setValue(speed);
				if (hints!=hintCheckbox.isSelected()) hintCheckbox.setSelected(hints);
			}
		};
		map.put("change.animation.settings",action);
	}

	public void stateChanged(ChangeEvent evt)
    {
        //  called by slider
	    if (evt.getSource()==slider)
			applyChanges();
	    if (evt.getSource()==hintCheckbox)
		    applyChanges();
    }

	private void applyChanges()
	{
		int speed = slider.getValue();
		boolean hints = hintCheckbox.isSelected();

		Application.theCommandDispatcher.broadcast(
				new Command("change.animation.settings",null,
				        new Integer(speed), Util.toBoolean(hints)),
				Application.theApplication);
		//  will be handled by the Application
	}

	public void handleMessage(Object who, int what, Object data)
    {
        //  sent by theAnimation
        updateStatus();
    }

}
