/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Schäfer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.devtools;

import de.jose.Application;
import de.jose.Command;
import de.jose.CommandAction;
import de.jose.chess.Move;
import de.jose.jo3d.SplineKeyFrame;
import de.jose.jo3d.interpolators.KBRotPosScaleSplinePathInterpolator;
import de.jose.view.BoardPanel;

import javax.media.j3d.Alpha;
import javax.media.j3d.Interpolator;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

public class FlightRecorder
        extends FlyBy
        implements ActionListener
{

	/**	output	*/
	private String outputFileName;
    /** input */
    private String inputFileName;
	/**	list of SplineFrames	*/
	private ArrayList frames;
	/**	increment step	*/
	private double step;
	/**	the control panel	*/
	private Panel controlPanel;
	/**	interpolator for replay	*/
	private Interpolator interpolator;
    /** duration in seconds */
    private double duration;

	public FlightRecorder(String[] args)
		throws Exception
	{
		super(args);
		frames = new ArrayList();
		frames.add(new SplineKeyFrame());
		step = 0.1;
        duration = 5.0;
		interpolator = null;
	}

	public void setupActionMap(Map map)
	{
		CommandAction action;
		action = new CommandAction() {
			public void Do(Command cmd) {
				//	make a move
				Move mv = theGame.forward();
				if (mv != null) {
					boardPanel().move(mv, getAnimation().getSpeed());
					boardPanel().get3dView().refresh(true);
				}
			}
		};
		map.put("move >", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				//	undo move
				Move mv = theGame.backward();
				if (mv != null) boardPanel().get3dView().refresh(true);
			}
		};
		map.put("< move", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				//	apply edits
				read();
				synch();
			}
		};
		map.put("apply", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				//	save a frame
				frames.add(currentFrame().clone());
				synch();
			}
		};
		map.put("save", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				//  read from disk
				try {
					ArrayList input = new ArrayList();
					SplineKeyFrame.read(inputFileName,input);
					frames = input;
					frames.add(currentFrame().clone());  //  edit frame
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				synch();
			}
		};
		map.put("read", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				try {	//	write to disk
					FileWriter fout = new FileWriter(outputFileName,true);
					PrintWriter out = new PrintWriter(fout);
					SplineKeyFrame.print(out, frames,0,frames.size()-1);
					out.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		map.put("write", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				//	delete last frame
				if (frames.size() > 1) frames.remove(frames.size()-1);
				synch();
			}
		};
		map.put("undo", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				stopInterpolator();	//	play animation
				Alpha alpha = new Alpha(1, (long)(1000*duration));
				interpolator = new KBRotPosScaleSplinePathInterpolator(alpha,
							        boardPanel().get3dView().getViewTransformGroup(),
									new Transform3D(),
							        SplineKeyFrame.toKeyFrames(frames,0,frames.size()-1));

				boardPanel().get3dView().addBehavior(interpolator);
				alpha.setStartTime(System.currentTimeMillis());
			}
		};
		map.put("play", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				stopInterpolator();
				synch();
			}
		};
		map.put("stop", action);
	}

	private void stopInterpolator()
	{
		if (interpolator != null) {
			interpolator.setEnable(false);
/*			Group bg = (Group)interpolator.getParent();
			Group g = (Group)bg.getParent();
			g.removeChild(bg);
*/			interpolator = null;
		}
	}

	public SplineKeyFrame currentFrame()
	{
		return (SplineKeyFrame)frames.get(frames.size()-1);
	}

	public void open()
		throws Exception
	{
		super.open();
        setupPane(theWindow, boardPanel());

        boardPanel().get3dView().setMinimumFrameCycleTime(1000/20);
		getAnimation().stop();
		currentFrame().setPolarLocation(0.0, 2*Math.PI/8, 3.0);
		currentFrame().setCameraAngle(0.0, -2*Math.PI/8, 0.0);	//	 looking directly to 0,0,0

        synch();
	}

	//-------------------------------------------------------------------------------
	//	interface ActionListener
	//-------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		/*	forward menu events to CommandListener	*/
		theCommandDispatcher.handle(e, this);
	}

	public void eventDispatched(AWTEvent evt)
	{
		if (evt.getID() == KeyEvent.KEY_PRESSED)
		{
			SplineKeyFrame frm = currentFrame();

			switch (((KeyEvent)evt).getKeyCode())
			{

			case KeyEvent.VK_Q:	frm.incrementLocation(+step,0.0,0.0); break;		//	+ x
			case KeyEvent.VK_A:	frm.incrementLocation(-step,0.0,0.0); break;		//	- x

			case KeyEvent.VK_W:	frm.incrementLocation(0.0,+step,0.0); break;		//	+ y
			case KeyEvent.VK_S:	frm.incrementLocation(0.0,-step,0.0); break;		//	- y

			case KeyEvent.VK_E:	frm.incrementLocation(0.0,0.0,+step); break;		//	+ z
			case KeyEvent.VK_D:	frm.incrementLocation(0.0,0.0,-step); break;		//	- z

			case KeyEvent.VK_R:	frm.rotatePolarLocation(+step,0.0,0.0); break;		//	+ longitude
			case KeyEvent.VK_F:	frm.rotatePolarLocation(-step,0.0,0.0); break;		//	- longitude

			case KeyEvent.VK_T:	frm.rotatePolarLocation(0.0,+step,0.0); break;		//	+ latitude
			case KeyEvent.VK_G:	frm.rotatePolarLocation(0.0,-step,0.0); break;		//	- latitude

			case KeyEvent.VK_Z:	frm.rotatePolarLocation(0.0,0.0,+step); break;		//	+ radius
			case KeyEvent.VK_H:	frm.rotatePolarLocation(0.0,0.0,-step); break;		//	- radius

			case KeyEvent.VK_U:	frm.incrementCameraAngle(+step,0.0,0.0); break;		//	+ heading
			case KeyEvent.VK_J:	frm.incrementCameraAngle(-step,0.0,0.0); break;		//	- heading

			case KeyEvent.VK_MULTIPLY:	step *= 10.0; break;								//	step * 10
			case KeyEvent.VK_DIVIDE:	step /= 10.0; break;								//	step / 10

            case KeyEvent.VK_NUMPAD1:	frm.incrementCameraAngle(0.0,+step,+step); break;
            case KeyEvent.VK_NUMPAD2:	frm.incrementCameraAngle(0.0,+step,0.0); break;		//	+ pitch
            case KeyEvent.VK_NUMPAD3:	frm.incrementCameraAngle(0.0,+step,-step); break;
            case KeyEvent.VK_NUMPAD4:	frm.incrementCameraAngle(0.0,0.0,+step); break;		//	+ bank
            case KeyEvent.VK_NUMPAD6:	frm.incrementCameraAngle(0.0,0.0,-step); break;		//	- bank
            case KeyEvent.VK_NUMPAD7:	frm.incrementCameraAngle(0.0,-step,+step); break;
			case KeyEvent.VK_NUMPAD8:	frm.incrementCameraAngle(0.0,-step,0.0); break;		//	- pitch
            case KeyEvent.VK_NUMPAD9:	frm.incrementCameraAngle(0.0,-step,-step); break;

			case KeyEvent.VK_NUMPAD5:		//	+ forward
                        Vector3d v = frm.getCameraVector();
						frm.incrementLocation(+v.x*step,+v.y*step,+v.z*step);
						break;

			case KeyEvent.VK_NUMPAD0:		//	backwards
                        v = frm.getCameraVector();
                        frm.incrementLocation(-v.x*step,-v.y*step,-v.z*step);
						break;

			default: return;
			}

			synch();
		}
	}

	public void processArgs(String[] args, int from, int to)
		throws Exception
	{
        inputFileName = "flight.csv";
		outputFileName = "flight_log.csv";

		for (int i=from; i<=to; i++)
		{
			if (args[i].equalsIgnoreCase("-out"))
			{
				outputFileName = args[i+1];
				args[i] = args[++i] = null;		//	hide from upper class
			}
            if (args[i].equalsIgnoreCase("-in"))
			{
				inputFileName = args[i+1];
				args[i] = args[++i] = null;		//	hide from upper class
			}
		}

		super.processArgs(args, from,to);
	}

	public void synch()
	{
		SplineKeyFrame frm = currentFrame();

		set("x","y","z", frm.getLocation());
		set("longitude","latitude","radius", frm.getPolarLocation());
		set("heading","pitch","bank", frm.getCameraAngle());
		set("step", step);
        set("duration", duration);
		set("frames", frames.size()-1);

		//	apply view transform
		TransformGroup tg = boardPanel().get3dView().getViewTransformGroup();
		Transform3D tf = frm.applyTransform(null);
		tg.setTransform(tf);
	}

	public void read()
	{
		SplineKeyFrame frm = currentFrame();

//		frm.setLocation(read("x","y","z"));
		frm.setPolarLocation(read("longitude","latitude","radius"));
		frm.setCameraAngle(read("heading","pitch","bank"));
		step = read("step");
        duration = read("duration");
	}

	protected void setupPane(RootPaneContainer cont, BoardPanel boardPanel)
	{
		Container cnt = cont.getContentPane();

		cnt.setLayout(new BorderLayout());
		cnt.add(boardPanel.get3dView(), BorderLayout.CENTER);

		controlPanel = new Panel();
		controlPanel.setLayout(new GridLayout(24,2));

        addField(controlPanel, "x", "Q A");
		addField(controlPanel, "y", "W S");
		addField(controlPanel, "z", "E D");
		addSpacer(controlPanel, 2);

		addField(controlPanel, "longitude", "R F");
		addField(controlPanel, "latitude", "T G");
		addField(controlPanel, "radius", "Z H");
		addSpacer(controlPanel, 2);

		addField(controlPanel, "heading", "U J");
		addField(controlPanel, "pitch", "I K");
		addField(controlPanel, "bank", "O L");
		addSpacer(controlPanel, 2);

		addField(controlPanel, "step", "Ü Ä");
		addSpacer(controlPanel, 2);

        addField(controlPanel, "duration", "");
		addLabel(controlPanel, "frames", "");

        addButton(controlPanel, "apply");
		addButton(controlPanel, "undo");

		addButton(controlPanel, "play");
		addButton(controlPanel, "stop");

		addButton(controlPanel, "< move");
		addButton(controlPanel, "move >");

        addButton(controlPanel, "read");
        addButton(controlPanel, "save");

        addButton(controlPanel, "write");

		cnt.add(controlPanel, BorderLayout.WEST);
	}

	private void set(String name, double value)
	{
		JTextField field = (JTextField)getComponentByName(controlPanel,name);
		field.setText(Double.toString(value));
	}

	private void set(String name, int value)
	{
		JTextComponent field = (JTextComponent)getComponentByName(controlPanel,name);
		field.setText(Integer.toString(value));
	}

	private void set(String n1, String n2, String n3, Tuple3d values)
	{
		set(n1,values.x);
		set(n2,values.y);
		set(n3,values.z);
	}

	private double read(String name)
	{
		JTextField field = (JTextField)getComponentByName(controlPanel,name);
		String text = field.getText();
		try {
			return Double.parseDouble(text);
		} catch (Throwable ex) {
			return 0.0;
		}
	}

	private Point3d read(String n1, String n2, String n3)
	{
		return new Point3d(read(n1),read(n2),read(n3));
	}

	private Component getComponentByName(Container parent,  String name)
	{
		for (int i=0; i < parent.getComponentCount(); i++)
		{
			Component comp = parent.getComponent(i);
			if (name.equals(comp.getName()))
				return comp;
		}
		return null;
	}

	private JTextField addField(Panel panel, String name, String tooltip)
	{
		JLabel label = new JLabel(name);
		label.setHorizontalAlignment(JLabel.RIGHT);
		label.setToolTipText(tooltip);
		panel.add(label);

		JTextField field = new JTextField(10);
//		field.setHorizontalAlignment(JTextField.RIGHT);
        field.setName(name);
		field.setToolTipText(tooltip);
		field.setActionCommand(name);
        field.addActionListener(this);
		panel.add(field);
		return field;
	}

	private JTextField addLabel(Panel panel, String name, String tooltip)
	{
		JTextField field = addField(panel,name,tooltip);
		field.setEditable(false);
		return field;
	}

	private JButton addButton(Panel panel, String name)
	{
		JButton button = new JButton(name);
		button.setName(name);
		button.setActionCommand(name);
		button.addActionListener(this);
		panel.add(button);
		return button;
	}

	private void addSpacer(Panel panel, int columns)
	{
		while (columns-- > 0)
			panel.add(new JLabel(""));
	}

	public static void main(String[] args) {
		try {
			logErrors = false;

			new FlightRecorder(args).open();

		} catch (Throwable ex) {
			Application.fatalError(ex,-1);
		}
	}
}
