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
import de.jose.SplashScreen;
import de.jose.comm.Command;
import de.jose.comm.CommandAction;
import de.jose.comm.CommandListener;
import de.jose.image.ImgUtil;
import de.jose.util.StringUtil;
import de.jose.util.AWTUtil;
import de.jose.util.WinUtils;
import de.jose.util.icon.TextIcon;
import de.jose.view.JoToolBar;
import de.jose.view.colorchooser.JoColorButton;
import de.jose.view.colorchooser.JoSurfaceButton;
import de.jose.view.input.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.util.*;
import java.util.List;

public class JoDialog
//		extends JDialog
		implements ActionListener, CommandListener, WindowListener
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	/**	commonly used button sets	 */

	public static final String OK		= "dialog.button.ok";
	public static final String CANCEL	= "dialog.button.cancel";
	public static final String YES		= "dialog.button.yes";
	public static final String NO		= "dialog.button.no";
	public static final String APPLY	= "dialog.button.apply";
	public static final String REVERT	= "dialog.button.revert";
	public static final String HELP		= "dialog.button.help";

	public static final String[] OK_CANCEL	= { OK, CANCEL };

	public static final String[] YES_NO	= { YES, NO };

	public static final String[] APPLY_REVERT	= { APPLY, REVERT };


	/**	default insets	 */
	public static final Insets INSETS_NORMAL = new Insets(4,4,4,4);
	public static final Insets INSETS_GAP = new Insets(4,24,4,4);
	public static final Insets INSETS_INDENTED = new Insets(4,64,4,4);

	/**	grid bag constraints: label on column one	 */
	public static final GridBagConstraints LABEL_ONE =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE, 1,1, 0,0,
							   GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
							   INSETS_NORMAL, 90,0);
	//	todo why such a large padding? who needs it?
	public static final GridBagConstraints LABEL_ONE_NOPAD =
			new GridBagConstraints(0,GridBagConstraints.RELATIVE, 1,1, 0,0,
					GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
					INSETS_NORMAL, 0,0);

	public static final GridBagConstraints LABEL_INDENTED =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE, 1,1, 0,0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
							   INSETS_INDENTED, 0,0);

	/**	grid bag constraints: label on column one, left aligned	 */
	public static final GridBagConstraints LABEL_ONE_LEFT =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE, 1,1, 0,0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: dialog element on column one	 */
	public static final GridBagConstraints ELEMENT_ONE =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE, 1,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: dialog element spanning 1 row	 */
	public static final GridBagConstraints ELEMENT_ONE_ROW =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: dialog element spanning 1 row	 */
	public static final GridBagConstraints BOX =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: dialog element spanning the rest of the current row	 */
	public static final GridBagConstraints ELEMENT_ROW =
		new GridBagConstraints(GridBagConstraints.RELATIVE,GridBagConstraints.RELATIVE,
								GridBagConstraints.REMAINDER,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
							   INSETS_NORMAL, 0,0);

	public static final GridBagConstraints ELEMENT_ROW_SMALL =
		new GridBagConstraints(GridBagConstraints.RELATIVE,GridBagConstraints.RELATIVE,
								GridBagConstraints.REMAINDER,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: dialog element spanning the rest of the current row	 */
	public static final GridBagConstraints FILL_ROW =
		new GridBagConstraints(4,GridBagConstraints.RELATIVE,
								GridBagConstraints.REMAINDER,1, 1000.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
							   INSETS_NORMAL, 0,0);

    /**	grid bag constraints: dialog element centered on column one	 */
	public static final GridBagConstraints CENTER_ONE =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE, 1,1, 1.0,0.0,
							   GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: dialog element centered on row	 */
	public static final GridBagConstraints CENTER_ROW =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE,
		                       GridBagConstraints.REMAINDER,1, 1.0,0.0,
							   GridBagConstraints.CENTER, GridBagConstraints.NONE,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: dialog element on column two	 */
	public static final GridBagConstraints ELEMENT_TWO =
		new GridBagConstraints(1,GridBagConstraints.RELATIVE, 1,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
							   INSETS_NORMAL, 0,0);

	public static final GridBagConstraints ELEMENT_TWO_SMALL =
		new GridBagConstraints(1,GridBagConstraints.RELATIVE, 1,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: label on column two	 */
	public static final GridBagConstraints LABEL_TWO =
		new GridBagConstraints(1,GridBagConstraints.RELATIVE, 1,1, 0,0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: label on column three	 */
	public static final GridBagConstraints LABEL_THREE =
		new GridBagConstraints(2,GridBagConstraints.RELATIVE, 1,1, 0,0,
							   GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
							   INSETS_GAP, 0,0);

	/**	grid bag constraints: dialog element on column three	 */
	public static final GridBagConstraints ELEMENT_THREE =
		new GridBagConstraints(2,GridBagConstraints.RELATIVE, 1,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
							   INSETS_NORMAL, 0,0);

	public static final GridBagConstraints ELEMENT_THREE_SMALL =
		new GridBagConstraints(2,GridBagConstraints.RELATIVE, 1,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: dialog element on column three	 */
	public static final GridBagConstraints ELEMENT_FOUR =
		new GridBagConstraints(3,GridBagConstraints.RELATIVE, 1,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
							   INSETS_NORMAL, 0,0);

	public static final GridBagConstraints ELEMENT_FOUR_SMALL =
		new GridBagConstraints(3,GridBagConstraints.RELATIVE, 1,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: dialog element on column three	 */
	public static final GridBagConstraints ELEMENT_WIDTH100 =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER,1, 1.0,1.0,
							   GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: dialog element filling the rest of the dialog	 */
	public static final GridBagConstraints ELEMENT_REMAINDER =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE,
							   GridBagConstraints.REMAINDER,GridBagConstraints.REMAINDER,
							   1.0,1.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: dialog element filling the rest of the dialog	 */
	public static final GridBagConstraints ELEMENT_FILLBOTH =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE,
							   GridBagConstraints.REMAINDER,GridBagConstraints.REMAINDER,
							   1.0,1.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
							   INSETS_NORMAL, 0,0);

	/**	grid bag constraints: dialog element filling the rest of the dialog; starting from the next line	 */
	public static final GridBagConstraints ELEMENT_NEXTROW_REMAINDER =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE,
							   GridBagConstraints.REMAINDER,GridBagConstraints.REMAINDER,
							   1.0,1.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
							   INSETS_NORMAL, 0,0);

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

    /** associated window frame
     *  this is a JDialog for modal dialogs
     *  or a JFrame for modeless dialogs
     *
     *  we don't use modeless JDialogs cause they have several disadvantages, among which are:
     *  - modeless dialogs "stick" to their parent frame
     *  - they can't have a menu bar on Mac OS X
     */
    protected Window frame;

    protected boolean isModal;

	/**	the element pane	 */
	protected JComponent elementPane;

	/**	the button pane	*/
	protected JPanel buttonPane;

	/**	contains all active elements	 */
	protected HashMap elements;
	/**	contains all active buttons	 */
	protected HashMap buttons;

    /** currently activ dialog (or null)    */
    protected static JoDialog theActiveDialog = null;

	/**
	 * after a failed save, read data anew ?
	 */
	protected boolean readOnFailedSave = true;

	/**
	 * was the cancel button pressed
	 */
	protected boolean wasCancelled;

//    protected static Frame sharedOwnerFrame = null;

	/**
	 * static map containing all dialogs
	 */
	public static HashMap theDialogs = new HashMap();

	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	public JoDialog(String name, boolean modal)
	{
		this(JoFrame.getActiveFrame(),name,modal);
	}

	public JoDialog(JFrame parent, String name, boolean modal)
	{
        isModal = modal;
        if (isModal) {
	        frame = new JDialog(parent,true);
        }
        else {
	        JFrame jframe;
            frame = jframe = new JFrame();
	        jframe.setIconImage(Application.theApplication.theIconImage);
	        if (Version.mac)
                jframe.setJMenuBar(new JoMenuBar(this));
        }

		frame.addWindowListener(this);
        setTitle(Language.get(name,Language.get("application.name")));

        if (parent!=null)
            AWTUtil.centerOn(frame,parent);
        else
            AWTUtil.centerOnScreen(frame);

		frame.addWindowListener(this);
		frame.setName(name);
		theDialogs.put(name,this);
		elements = new HashMap();
		buttons = new HashMap();

		elementPane = createElementPane();
		buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));


		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hide();
				wasCancelled = true;
			}
		};
		KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		elementPane.getActionMap().put(CANCEL,action);
		elementPane.getInputMap().put(keystroke,CANCEL);

        Container contentPane = ((RootPaneContainer)frame).getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(elementPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.SOUTH);

		AbstractApplication.theCommandDispatcher.addCommandListener(this);
	}

    public static JoDialog getActiveDialog()
    {
        return theActiveDialog;
    }

    public boolean isModal()
    {
        return isModal;
    }

	public static boolean isModalActive()
	{
		return getActiveDialog()!=null && getActiveDialog().isModal();
	}

    // ---------------------------------------------------
    //  delegates to Frame
    // ---------------------------------------------------

    public String getName()                         { return frame.getName(); }

    public void setName(String name)                { frame.setName(name); }

	public void toFront()                           { frame.toFront(); }

    public boolean isShowing()                      { return frame.isShowing(); }

    public void show()
    {
	    frame.setVisible(true);
//	    if (!isModal) WinUtils.setTopMost((Frame)frame);
	    //  do/don't force topmost position for modeless dialogs
    }

    public void hide()                              { frame.setVisible(false); }

    public void dispose()                           { frame.dispose(); }

    public int getWidth()                           { return frame.getWidth(); }

    public int getHeight()                          { return frame.getHeight(); }

    public void setLocation(Point p)                { frame.setLocation(p); }

    public String getTitle()
    {
        if (frame instanceof Frame)
            return ((Frame)frame).getTitle();
        if (frame instanceof Dialog)
            return ((Dialog)frame).getTitle();
        return frame.getName();
    }

    public void setTitle(String title)
    {
        if (frame instanceof Frame)
            ((Frame)frame).setTitle(title);
        if (frame instanceof Dialog)
            ((Dialog)frame).setTitle(title);
    }


    /**
     * dialog factory method
     */
	public static JoDialog create(String name)
	{
		if (name.equals("dialog.option"))
			return new OptionDialog(name);
		if (name.equals("dialog.about"))
			return new AboutDialog(name);
        if (name.equals("dialog.animate"))
            return new AnimationDialog(name);
		if (name.equals("dialog.setup"))
			return new SetupDialog(name);
		if (name.equals("dialog.game"))
			return new GameDetailsDialog(name);
		if (name.equals("dialog.write.mode"))
			return new WriteModeDialog(name);
		if (name.equals("dialog.export"))
			return new ExportDialog(name);
		//	else:
		return new JoDialog(name,true);
	}


	public static JoDialog getDialog(String name)
	{
		return (JoDialog)theDialogs.get(name);
	}


	public static int countAllDialogs()
	{
		return theDialogs.size();
	}

	public static JoDialog[] getAllDialogs()
	{
		Collection values = theDialogs.values();
		JoDialog[] result = new JoDialog[values.size()];
		return (JoDialog[])values.toArray(result);
	}

	public static void closeAll()
	{
		for (Iterator i = theDialogs.values().iterator(); i.hasNext(); )
		{
			((JoDialog)i.next()).hide();
		}
	}

	//-------------------------------------------------------------------------------
	//	Basic Access
	//-------------------------------------------------------------------------------

	public final JComponent getElementPane()
	{
		return elementPane;
	}

	public final JPanel getButtonPane()
	{
		return buttonPane;
	}

	public void read() throws Exception
	{
		/*	overwrite !	*/
	}

	public boolean save() throws Exception
	{
		/*	overwrite ! */
		return true;
	}

	public void clear()
	{
		/*	overwrite ! */
	}

	public void addButton(JButton button)
	{
		button.addActionListener(this);
		getButtonPane().add(button);
	}


	public JButton addButton(String name)
	{
		return addButton(name,false);
	}

	public JButton addButton(String name, boolean withIcon)
	{
		JButton button;
		if (withIcon)
			button = newButton(name,ImgUtil.getMenuIcon(name),"icon");
		else
			button = newButton(name,(ImageIcon)null,(String)null);

		getButtonPane().add(button);
		return button;
	}

	public final JButton newButton(String name, Icon icon, String style)
	{
		JButton button = newButton(name,icon,style,this);
		buttons.put(name,button);
		return button;
	}

	public static final JButton newButton(String name, ActionListener listener)
	{
		return newButton(name,null,listener);
	}

    public static final JButton newButton(String name, Icon icon, ActionListener listener)
    {
        return newButton(name, icon, null, listener);
    }

    public static final JButton newButton(String name, Icon icon, String style, ActionListener listener)
	{
		JButton button = new JButton();
		button.setName(name);
		button.setActionCommand(name);
		button.addActionListener(listener);
        String text = null;
		if (name!=null) {
            text = Language.get(name);
            if (text!=null && text.length() > 0)
                button.setText(text);
        }
		button.setToolTipText(Language.getTip(name));
        if (Version.mac)
        {
            /** Aqua speciality: Icon buttons are painted different from text buttons
             *  however, we want a consistent look. Icons are removed from plain text buttons.
             */
            if (text!=null && text.length()>0
                    && (style==null || "text".equalsIgnoreCase(style)))
                icon = null;
            if (style!=null)
                button.putClientProperty("JButton.buttonType",style);
        }
        button.setIcon(icon);
		if (icon!=null && (icon instanceof ImageIcon))
			button.setDisabledIcon(ImgUtil.createDisabledIcon((ImageIcon)icon));
		if (icon!=null && (icon instanceof TextIcon))
			button.setDisabledIcon(TextIcon.clone((TextIcon)icon).disabled());
		return button;
	}

	public final JButton newButton(String name)
	{
		return newButton(name,(ImageIcon)null,(String)null);
	}

    public final JButton newButton(String name, String iconName)
    {
        if (iconName != null)
            return newButton(name,ImgUtil.getMenuIcon(iconName),"icon");
        else
            return newButton(name,(Icon)null,(String)null);
    }

	public final JButton getButton(String name)
	{
		return (JButton)buttons.get(name);
	}


	public void enableButton(String name, boolean enabled)
	{
		getButton(name).setEnabled(enabled);
	}

	public final void showButton(String name, boolean visible)
	{
		getButton(name).setVisible(visible);
	}

	public static final JToggleButton newToggleButton(String name, Icon icon)
	{
		JToggleButton button = new JToggleButton();
		button.setName(name);
		button.setActionCommand(name);
		button.setText(Language.get(name));
		button.setToolTipText(Language.getTip(name));
		button.setIcon(icon);
        if (Version.mac)
            button.putClientProperty("JButton.buttonType","toggle");
		return button;
	}

	public static final JToggleButton newToggleButton(String name)
	{
		return newToggleButton(name,null);
	}

	public final JoSurfaceButton newSurfaceButton(String name, Icon icon)
	{
		JoSurfaceButton button = new JoSurfaceButton();
		button.setName(name);
		button.setActionCommand(name);
		button.addActionListener(this);
		button.setText(Language.get(name));
		button.setToolTipText(Language.getTip(name));
		button.setIcon(icon);
		elements.put(name,button);
		return button;
	}

	public final JoColorButton newColorButton(String name, Icon icon)
	{
		JoColorButton button = new JoColorButton();
		button.setName(name);
		button.setActionCommand(name);
		button.addActionListener(this);
		button.setText(Language.get(name));
		button.setToolTipText(Language.getTip(name));
		button.setIcon(icon);
		elements.put(name,button);
		return button;
	}

	public void addSpacer(int width)
	{
		JPanel spacer = new JPanel();
		spacer.setPreferredSize(new Dimension(width,1));
		getButtonPane().add(spacer);
	}

	public static final JLabel newLabel(String name, Icon icon, int alignment)
	{
        return newLabel(name,icon,alignment,null);
    }

    public static final JLabel newLabel(String name, Icon icon, int alignment, Insets insets)
    {
		JLabel label = new JLabel();
		label.setName(name);
		label.setText(Language.get(name));
		label.setToolTipText(Language.getTip(name));
		label.setIcon(icon);
		label.setHorizontalAlignment(alignment);
        if (insets!=null) label.setBorder(new EmptyBorder(insets));
		return label;
	}

	public static final JLabel newLabel(String name, Icon icon)
	{
		return newLabel(name,icon, JLabel.RIGHT);
	}

    public static final JLabel newLabel(String name, int alignment)
    {
        return newLabel(name,null,alignment);
    }

	public static final JLabel newLabel(String name)
	{
		return newLabel(name,null, JLabel.RIGHT);
	}


	public static final JCheckBox newCheckBox(String name, Icon icon)
	{
		return newCheckBox(name,name,icon);
	}

	public static final JCheckBox newCheckBox(String name, String label, Icon icon)
	{
		JCheckBox box = new JCheckBox();
		box.setName(name);
		box.setActionCommand(name);
		box.setText(Language.get(label));
		box.setToolTipText(Language.getTip(label));
		box.setIcon(icon);
		return box;
	}

    public static final JCheckBox newCheckBox(String name, ChangeListener listener)
    {
        return newCheckBox(name,null,listener);
    }

	public static final JCheckBox newCheckBox(String name, Icon icon, ChangeListener listener)
	{
		JCheckBox box = newCheckBox(name,icon);
		box.addChangeListener(listener);
		return box; 
	}

	public static final JTextField newTextField(String name)
	{
		JTextField text = new JTextField();
		text.setName(name);
		text.setToolTipText(Language.getTip(name));
		return text;
	}

    public static final JTextField newTextField(DocumentListener listener)
    {
        JTextField field = new JTextField();
        field.getDocument().addDocumentListener(listener);
        return field;
    }

    public static final JTextField newTextField(Dimension size, DocumentListener listener)
    {
        JTextField field = newTextField(listener);
        field.setMinimumSize(size);
        field.setMaximumSize(size);
        return field;
    }


	public final FileInput newFileInputField(String name)
	{
		FileInput file = new FileInput();
		file.setName(name);
		file.setToolTipText(Language.getTip(name));
		file.addActionListener(this);
		return file;
	}

	public static final JTextArea newTextArea(int rows, int cols)
	{
		JTextArea text = new JTextArea(rows,cols);
		text.setBorder(new LineBorder(Color.black));
		return text;
	}

    public static final JTextArea newTextArea(DocumentListener listener)
    {
        JTextArea text = new JTextArea();
        text.getDocument().addDocumentListener(listener);
        return text;
    }

	public static final JDateField newDateField(String name)
	{
		JDateField text = new JDateField();
		text.setName(name);
		text.setToolTipText(Language.getTip(name));
		return text;
	}

	public static final JTimeField newTimeField(String name)
	{
		JTimeField text = new JTimeField();
		text.setName(name);
		text.setToolTipText(Language.getTip(name));
		return text;
	}

	public static final JDateField newPgnDateField(String name)
	{
		JDateField text = new PgnDateField();
		text.setName(name);
		text.setToolTipText(Language.getTip(name));
		setColumns(text,'0',10);
		return text;
	}

	public static final JIntegerField newIntegerField(String name)
	{
		JIntegerField text = new JIntegerField();
		text.setName(name);
		text.setToolTipText(Language.getTip(name));
		return text;
	}

	public static final JSpinner newSpinner(String name)
	{
		JSpinner text = new JSpinner();
		text.setName(name);
		text.setToolTipText(Language.getTip(name));
		return text;
	}

	public static final JCheckBox newCheckBox(String name)
	{
		return newCheckBox(name,name,null);
	}


	public static final JRadioButton newRadioButton(String name, Icon icon)
	{
		JRadioButton radio = new JRadioButton();
		radio.setName(name);
		radio.setActionCommand(name);
		radio.setText(Language.get(name));
		radio.setToolTipText(Language.getTip(name));
		radio.setIcon(icon);
		return radio;
	}

	public static final JRadioButton newRadioButton(String name)
	{
		return newRadioButton(name,(Icon)null);
	}

	public static final JRadioButton newRadioButton(String groupName, String value)
	{
		return newRadioButton(groupName+"."+value,(Icon)null);
	}

	public static final JPanel newGridPane()
	{
		return new JPanel(new GridBagLayout());
	}


	public static final JPanel newGridBox(String title)
	{
		JPanel panel = newGridPane();
		panel.setName(title);
		if (title!=null) {
			TitledBorder border = new TitledBorder(Language.get(title));
			Font font = border.getTitleFont();
			font = font.deriveFont(Font.BOLD);
			border.setTitleFont(font);
			panel.setBorder(border);
		}
		return panel;
	}

	public final void add(Container cont, int column,
							Component component)
	{
		Object constraint = null;
		switch (column) {
		case 1:
			constraint = ELEMENT_ONE;
			break;
		case 2:
			constraint = ELEMENT_TWO;
			break;
		case 3:
			constraint = ELEMENT_THREE;
			break;
		}

		add(cont,component,constraint);
	}

	public final void add(Container cont, Component component, Object constraint)
	{
		cont.add(component, constraint);
		reg (component);
	}

	public final void add(Container cont, int column,
							String compName, Component component)
	{
		component.setName(compName);
		add(cont,column,component);
	}


	public final void add(Container cont,
							String compName, Component component,
						    Object constraint)
	{
		component.setName(compName);
		add(cont,component,constraint);
	}

	public final JLabel addWithLabel(Container cont, int column,
										  String compName,
										  Component component)
	{
		return addWithLabel(cont,column,compName,component,-1,-1);
	}

	public final JLabel addWithLabel(Container cont, int column,
										  String compName,
										  Component component,
	                                      int vsbPolicy, int hsbPolicy)
	{
		JLabel label = newLabel(getName()+"."+compName);	//	<dialog name>.<component name>
		label.setLabelFor(component);
		component.setName(compName);
		addWith(cont,column, label, component, vsbPolicy,hsbPolicy);
		return label;
	}

	public final Component addWithLabel(Container cont,
	                               int x, int y, int colspan,
	                               Object labelName, Component component)
	{
		return addWithLabel(cont, x,y,colspan, 0.0,0.0, labelName,component, -1,-1);
	}

	public final Component addWithLabel(Container cont,
	                               int x, int y, int colspan, double weightx, double weighty,
	                               Object labelName, Component component,
	                               int vsbPolicy, int hsbPolicy)
	{
		GridBagConstraints lconst = (GridBagConstraints)LABEL_ONE.clone();
		GridBagConstraints cconst = (GridBagConstraints)ELEMENT_TWO.clone();
		Component labelComponent = null;

		if (labelName!=null)
		{
			if (labelName instanceof String) {
				JLabel label;
				if (((String) labelName).isEmpty())
					label = newLabel("");
				else
					label = newLabel(getName()+"."+labelName);
				if (component!=null) {
					label.setLabelFor(component);
					component.setName((String)labelName);
				}
				labelComponent = label;
			}
			else if (labelName instanceof Component) {
				labelComponent = (Component)labelName;
			}
			else
				throw new IllegalArgumentException();

			lconst.gridx = x;
			lconst.gridy = y;
			lconst.gridwidth = 1;
			cont.add(labelComponent,lconst);
		}

		if (component != null) {
			cconst.gridx = x+1;
			cconst.gridy = y;
			cconst.gridwidth = colspan-1;
			if (weightx > 0.0) cconst.weightx = weightx;
			if (weighty > 0.0) cconst.weighty = weighty;

			reg(component);

			if (vsbPolicy >= 0 && hsbPolicy >= 0)
				component = new JScrollPane(component,vsbPolicy,hsbPolicy);

			cont.add(component,cconst);
		}
		return labelComponent;
	}

	public final void addBox(Container cont,
	                         int x, int y, int colspan,
	                         Component box)
	{
		GridBagConstraints cconst = (GridBagConstraints)BOX.clone();
		cconst.gridx = x;
		cconst.gridy = y;
		cconst.gridwidth = colspan;
		cont.add(box,cconst);
	}
	
	public final void add(Container cont,
	                      int x, int y, int colspan,
	                      Component component,
	                      GridBagConstraints cconst)
	{
		cconst = (GridBagConstraints)cconst.clone();
		cconst.gridx = x;
		cconst.gridy = y;
		cconst.gridwidth = colspan;
		cont.add(component,cconst);
	}

	public final void addWith(Container cont, int column,
							Component label, Component component)
	{
		addWith(cont,column,label,component,-1,-1);
	}

	public final void addWith(Container cont, int column,
							Component label, Component component,
	                        int vsbPolicy, int hsbPolicy)
	{
//		reg (label);
		reg (component);

		if (vsbPolicy >= 0 && hsbPolicy >= 0)
			component = new JScrollPane(component,vsbPolicy,hsbPolicy);

		switch (column) {
		case 1:
			cont.add(label, LABEL_ONE);
			cont.add(component, ELEMENT_TWO);
			break;
		case 2:
			cont.add(label, LABEL_TWO);
			cont.add(component, ELEMENT_THREE);
			break;
		case 3:
			cont.add(label, LABEL_THREE);
			cont.add(component, ELEMENT_FOUR);
			break;
		}
	}

	/**
	 * register a named component with the elements map
	 */
	public final Component reg(Component comp)
	{
		return (Component)reg(comp.getName(),comp);
	}

	public Object reg(String name, Object comp)
	{
		if (name != null)
			elements.put(name, comp);
		return comp;
	}

	public final void addWith(Container cont, int column,
							Component label, String compName, Component component)
	{
		component.setName(compName);
		addWith(cont, column, label, component);
	}

	//-------------------------------------------------------------------------------
	//	Protected Access
	//-------------------------------------------------------------------------------

	/**	called by the ctor
	 *	override, if you like
	 */
	protected JComponent createElementPane()
	{
		return newGridPane();
	}

	/**	called by the ctor
	 */
	protected void addButtons(String[] names)
	{
		for (int i=0; i<names.length; i++)
			if (names[i]==null)
				addSpacer(20);
			else
				addButton(names[i]);
	}

	public JoButtonGroup newButtonGroup(String prefix)
	{
		JoButtonGroup bg = new JoButtonGroup(prefix);
		bg.addAll(elements.values());
		reg(prefix,bg);
		return bg;
	}

	public void setValue(String name, String value)
	{
		JoButtonGroup bgroup = (JoButtonGroup)getElement(name);
		bgroup.setValue(value);
	}

	public final void setValue(String name, int value)
	{
		setValue(name, Integer.valueOf(value));
	}

	public final void setValue(String name, boolean value)
	{
		setValue(name, Boolean.valueOf(value));
	}

	public boolean setEnabled(String name, boolean enabled)
	{
		Component comp = (Component)getElement(name);
		comp.setEnabled(enabled);
		return enabled;
	}

	public boolean isEnabled(String name)
	{
		Component comp = (Component)getElement(name);
		return comp.isEnabled();
	}

	public Object getElement(String name)
	{
		return elements.get(name);
	}

	public void read(HashMap values)
	{
		Iterator i = elements.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry ety = (Map.Entry)i.next();
			String name = (String)ety.getKey();
			Object comp = ety.getValue();

			Object value = values.get(name);
			setValue(comp,value);
		}
	}

	public List save(HashMap values)
	{
		Vector errors = null;
		Iterator i = elements.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry ety = (Map.Entry)i.next();
			String name = (String)ety.getKey();
			Object comp = ety.getValue();

			try {
				Object value = getValue(comp);
				values.put(name,value);
			} catch (Throwable ex) {
				//  parse errors
				if (errors==null) errors = new Vector();
				errors.add(ex);
			}
		}
		return errors;
	}

	public boolean wasCancelled()
	{
		return wasCancelled;
	}


	public Object getValue(Object comp)
	{
		if (comp instanceof ValueHolder)
			return ((ValueHolder)comp).getValue();
		else if (comp instanceof JTextComponent)
			return ((JTextComponent)comp).getText();
		else if (comp instanceof JLabel)
			return ((JLabel)comp).getText();
		else if (comp instanceof JComboBox)
			return ((JComboBox)comp).getSelectedItem();
		else if (comp instanceof JCheckBox)
			return ((JCheckBox)comp).isSelected();
		else if (comp instanceof JRadioButton)
			return ((JRadioButton)comp).isSelected();
		else if (comp instanceof JColorChooser)
			return ((JColorChooser)comp).getColor();
		else if (comp instanceof JSpinner)
			return ((JSpinner)comp).getValue();
		else if (comp instanceof JSlider)
			return ((JSlider)comp).getValue();
		else if (comp instanceof JTimeField)
			return ((JTimeField)comp).getValue();
		else if (comp instanceof JIntegerField)
			return ((JIntegerField)comp).getValue();
		else if (comp instanceof String)
			return getValueByName((String)comp);
		else
			return null;
	}

	public final Object getValueByName(String name)
	{
		return getValue(getElement(name));
	}

	public final int getIntValue(String name)
	{
		Number num = (Number)getValueByName(name);
		return (num!=null) ? num.intValue() : 0;
	}

	public final boolean getBooleanValue(String name)
	{
		Object obj = getValueByName(name);
		if (obj==null) return false;
		if (obj instanceof Boolean)
			return ((Boolean)obj).booleanValue();
		if (obj instanceof String)
			return Boolean.valueOf((String)obj).booleanValue();
		throw new IllegalArgumentException();
	}

	public void setValue(Object comp, Object value)
	{
		if (comp instanceof ValueHolder)
			((ValueHolder)comp).setValue(value);
		else if (comp instanceof JTextComponent)
			((JTextComponent)comp).setText(StringUtil.nullValueOf(value));
		else if (comp instanceof JLabel)
			((JLabel)comp).setText(StringUtil.nullValueOf(value));
		else if (comp instanceof JComboBox)
			((JComboBox)comp).setSelectedItem(value);
		else if (comp instanceof JCheckBox)
			((JCheckBox)comp).setSelected(Util.toboolean(value));
		else if (comp instanceof JRadioButton)
			((JRadioButton)comp).setSelected(Util.toboolean(value));
		else if (comp instanceof JColorChooser)
			((JColorChooser)comp).setColor(Util.toColor(value));
		else if (comp instanceof JSpinner)
			((JSpinner)comp).setValue(Util.toNumber(value));
		else if (comp instanceof JSlider)
			((JSlider)comp).setValue(Util.toint(value));
		else if (comp instanceof String)
			setValueByName((String)comp, value);
	}

	public final void setValueByName(String name, Object value)
	{
		setValue(getElement(name), value);
	}

	public final void setValueByName(String name, int value)
	{
		setValue(getElement(name), value);
	}


	//-------------------------------------------------------------------------------
	//	Interface ActionListener
	//-------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		/*	forwar menu events to CommandListener	*/
		AbstractApplication.theCommandDispatcher.handle(new Command(e.getActionCommand(), e, e.getSource()), this);
	}

	//-------------------------------------------------------------------------------
	//	Interface CommandListener
	//-------------------------------------------------------------------------------

	public CommandListener getCommandParent()
    {
		return Application.theApplication;
    }

	public int numCommandChildren()
	{
		/**	broadcast to children ? not yet needed ...	*/
		return 0;
	}

	public CommandListener getCommandChild(int i)
	{
		return null;
	}

	public void setupActionMap(Map<String, CommandAction> map)
	{
		CommandAction action;
		action = new CommandAction() {
			public void Do(Command cmd) {
				updateLanguage();
			}
		};
		map.put("update.language", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				SwingUtilities.updateComponentTreeUI(JoDialog.this.frame);
			}
		};
		map.put("update.ui",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				if (save())
					hide();
				else if (readOnFailedSave)
					read();
				wasCancelled = false;
			}
		};
		map.put("dialog.button.ok",action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				hide();
				wasCancelled = true;
			}
		};
		map.put("dialog.button.cancel",action);
		map.put("dialog.button.close",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				save();
				read();
			}
		};
		map.put("dialog.button.apply",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				read();
				frame.repaint();
			}
		};
		map.put("dialog.button.revert",action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				clear();
			}
		};
		map.put("dialog.button.clear",action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				cmd.code = "menu.help.context";
				cmd.data = getHelpFocus();
				Application.theCommandDispatcher.forward(cmd,Application.theApplication);
			}
		};
		map.put("dialog.button.help",action);
	}


	/**
	 * @return the appropriate component when the HELP button is pressed
	 */
	protected Component getHelpFocus()
	{
		return elementPane;
	}

	public void stagger(Component owner, int x, int y)
	{
		stagger(owner,x,y, getWidth(),getHeight());
	}

	public void stagger(Component owner, int x, int y, int width, int height)
	{
		if (owner!=null) {
			Point p = owner.getLocationOnScreen();
			stagger(p.x,p.y, x,y, width,height);
		}
		else
			stagger(0,0, x,y,width,height);
	}

	public static Point staggerOn(Component owner, int x, int y)
	{
		if (owner!=null) {
			Point p = owner.getLocationOnScreen();
			return new Point(p.x+x,p.y+y);
		}
		else
			return new Point(x,y);
	}

	public void stagger(int x0, int y0, int x, int y, int width, int height)
	{
		Rectangle r = new Rectangle(x0+x, y0+y, width, height);
		r = JoFrame.adjustBounds(r,false);
		frame.setBounds(r);
	}

	public final void center(int width, int height)
	{
		center(frame.getOwner(),width,height);
	}

	public final void center(Component owner, int width, int height)
	{
		if (owner==null || !owner.isShowing() ) owner = JoFrame.getActiveFrame();
		if (owner!=null) {
			Rectangle bounds = owner.getBounds();
			if (bounds.width>0 && bounds.height>0) {
				center(bounds,width,height);
				return;
			}
		}
		AWTUtil.centerOnScreen(frame,width,height);
	}

    public void center(Rectangle r, int width, int height)
	{
		int x,y;
		if (r != null) {
			x = r.x + (r.width - width)/2;
			y = r.y + (r.height - height)/2;
		}
		else
			x = y = 10;

		frame.setBounds(x,y,width,height);
	}

	public void updateLanguage()
	{
		String title = Language.get("application.name");
		if (getName() != null)
			title += " - "+Language.get(getName());
		setTitle(title);

		Language.update(getElementPane());
		Language.update(getButtonPane());

		if (frame instanceof JFrame)
			Language.update(((JFrame)frame).getJMenuBar());
	}

	/**
	 * displays an error dialog
	 */
	public static void showErrorDialog(Component parent, String message)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					showErrorDialog(parent,message);
				}
			});
		}
		else
		{
			JOptionPane opane = new JOptionPane(
					Language.get(message!=null ? message:""),
					JOptionPane.ERROR_MESSAGE);
			JDialog dlg;
			if (parent==null)
				dlg = opane.createDialog(JoFrame.getActiveFrame(), Language.get("dialog.error.title"));
			else
				dlg = opane.createDialog(parent, Language.get("dialog.error.title"));
			SplashScreen.close();
			dlg.setVisible(true);
			WinUtils.setTopMost(dlg);
		}
	}

	public static void showErrorDialog(String message)
	{
		showErrorDialog(null,message);
	}

	public static void showErrorDialog(Component parent, String message, Map params)
	{
		if (message!=null) {
			message = Language.get(message);
			message = StringUtil.replace(message,params);
		}
		showErrorDialog(parent,message);
	}

	public static void showErrorDialog(Dialog parent, String message, String param1, Object value1)
	{
		Map map = new HashMap();
		map.put(param1, value1);
		showErrorDialog(parent, message,map);
	}

	public static void showErrorDialog(Dialog parent, String message,
									   String param1, Object value1,
									   String param2, Object value2)
	{
		Map map = new HashMap();
		map.put(param1, value1);
		map.put(param2, value2);
		showErrorDialog(parent,message,map);
	}

	public static void showMessageDialog(String message)
	{
		SplashScreen.close();
        JOptionPane.showMessageDialog(JoFrame.getActiveFrame(), Language.get(message),
									Language.get("dialog.message.title"),
									JOptionPane.DEFAULT_OPTION);
	}

	public static Dialog createMessageDialog(String title, String message, boolean modal)
	{
		JOptionPane pane = new JOptionPane(message,
		        JOptionPane.INFORMATION_MESSAGE,
		        JOptionPane.DEFAULT_OPTION);
        SplashScreen.close();
		Dialog dlg = pane.createDialog(JoFrame.getActiveFrame(), title);
		dlg.setModal(modal);
		return dlg;
	}

	/**
	 * displays an error dialog
	 */
	public static void showErrorDialog(String[] message)
	{
		StringBuffer buf = new StringBuffer(Language.get(message[0]));
		for (int i=1; i< message.length; i++) {
			buf.append("\n");
			buf.append(Language.get(message[i]));
		}
		JOptionPane opane = new JOptionPane(buf.toString(), JOptionPane.ERROR_MESSAGE);
		JDialog dlg = opane.createDialog(JoFrame.getActiveFrame(), Language.get("dialog.error.title"));
        SplashScreen.close();
		dlg.setVisible(true);
		WinUtils.setTopMost(dlg);
	}

	public static int showOKCancelDialog(String[] message)
	 {
		StringBuffer buf = new StringBuffer(Language.get(message[0]));
		for (int i=1; i< message.length; i++) {
			buf.append("\n");
			buf.append(Language.get(message[i]));
		}

        SplashScreen.close();
		return JOptionPane.showConfirmDialog(JoFrame.getActiveFrame(), buf.toString(),
									 Language.get("dialog.confirm.title"),
		                             JOptionPane.OK_CANCEL_OPTION);

	 }


	public static void setOptions(String[] options, int[] mnemos)
	{
		UIDefaults def = UIManager.getDefaults();

		if (options.length > 0)
			def.put("OptionPane.yesButtonText",options[0]);
		if (options.length > 1)
			def.put("OptionPane.noButtonText",options[1]);
		if (options.length > 2)
			def.put("OptionPane.cancelButtonText",options[2]);

        if (mnemos.length > 0)
            def.put("OptionPane.yesButtonMnemonic", String.valueOf(mnemos[0]));
        if (mnemos.length > 1)
            def.put("OptionPane.noButtonMnemonic", String.valueOf(mnemos[1]));
        if (mnemos.length > 2)
            def.put("OptionPane.cancelButtonMnemonic", String.valueOf(mnemos[2]));
	}


    public static int showYesNoCancelDialog(String message, String title,
                                            String yesLabel, String noLabel,
                                            int initialOption)
    {
        if (yesLabel==null) yesLabel = "dialog.button.yes";
        if (noLabel==null) noLabel = "dialog.button.no";
        String cancelLabel = "dialog.button.cancel";

	    /**
	     * we could pass the text of the buttons as "options" parameter,
	     * however, we can't set mnemonics ;-(
	     *
	     * we could create the buttons, set the mnemonics and then pass as paramters,
	     * however, the dialog won't listen to the buttons ;-(
	     *
	     * that's why we use UIManager.getDefaults()
	     */

	    String[] buttons = {
		    Language.get(yesLabel),
		    Language.get(noLabel),
		    Language.get(cancelLabel),
	    };
	    int[] mnemos = JoMenuBar.getMnemonics(buttons);

        if (initialOption<=0 || initialOption>=buttons.length) initialOption = 0;
		setOptions(buttons,mnemos);

        SplashScreen.close();
        return JOptionPane.showOptionDialog(JoFrame.getActiveFrame(),
                    Language.get(message), Language.get(title),
			        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,null,
                    null, buttons[initialOption]);
    }

    public static int showYesNoDialog(String message, String title,
                                            String yesLabel, String noLabel,
                                            int initialOption)
    {
        if (yesLabel==null) yesLabel = "dialog.button.yes";
        if (noLabel==null) noLabel = "dialog.button.no";

        String[] buttons = {
            Language.get(yesLabel),
            Language.get(noLabel),
        };
	    int[] mnemos = JoMenuBar.getMnemonics(buttons);

        if (initialOption<=0 || initialOption>=buttons.length) initialOption = 0;
	    setOptions(buttons,mnemos);

        SplashScreen.close();
        return JOptionPane.showOptionDialog(JoFrame.getActiveFrame(),
                    Language.get(message), Language.get(title),
			        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,null,
                    null,buttons[initialOption]);
    }

	public static GridBagConstraints gridConstraint(GridBagConstraints constr, int x, int y, int colspan)
	{
		constr = (GridBagConstraints)constr.clone();
		constr.gridx = x;
		constr.gridy = y;
		constr.gridwidth = colspan;
		return constr;
	}

	public static GridBagConstraints gridConstraint(GridBagConstraints constr, int x, int y, int colspan, int fill)
	{
		constr = (GridBagConstraints)constr.clone();
		constr.gridx = x;
		constr.gridy = y;
		constr.gridwidth = colspan;
		constr.fill = fill;
		return constr;
	}

	//-------------------------------------------------------------------------------
	//	interfac WindowListener
	//-------------------------------------------------------------------------------

	public void windowOpened(WindowEvent e)
	{ }

	public void windowClosing(WindowEvent e)
	{
		/*	forward to CommandListener	*/
		if (AbstractApplication.theCommandDispatcher != null)
        {
            CommandListener target;
            if (e.getWindow() instanceof CommandListener)
                target = (CommandListener)e.getWindow();
            else if (e.getWindow()==frame)
                target = this;
            else
                target = Application.theApplication;

            AbstractApplication.theCommandDispatcher.handle(new Command("dialog.button.cancel", e,
                                                            e.getWindow()), target);
        }
	}

	public void windowClosed(WindowEvent e)
	{
        if (e.getWindow()==frame && theActiveDialog==this) theActiveDialog = null;
    }

	public void windowIconified(WindowEvent e)
	{ }

	public void windowDeiconified(WindowEvent e)
	{ }


    public void windowActivated(WindowEvent e)
    {
        if (e.getWindow()==frame)
            theActiveDialog = this;
    }

    public void windowDeactivated(WindowEvent e)
    {
        if (e.getWindow()==frame && theActiveDialog==this) theActiveDialog = null;
    }

	public static void setColumns(JTextField field, int columns)
	{
		setColumns(field,'m',10);
	}

	public static void setColumns(JTextField field, char c, int columns)
	{
		field.setColumns(columns);

		FontMetrics metrix = field.getFontMetrics(field.getFont());
		int columnWidth = metrix.charWidth(c);

		Dimension size = field.getPreferredSize();
		size.width = columnWidth*columns;
		field.setMinimumSize(size);
	}


    protected JButton newSmallButton(String command, String icon)
    {
        JButton button = newButton(command,icon);
        Font font = new Font("dialog",Font.PLAIN,10);
        button.setFont(font);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(2,8,2,8));
        button.setPreferredSize(new Dimension(80,20));
        return button;
    }

	protected JButton newIconButton(String command, Icon icon)
	{
	    JButton button = newButton(command,icon,(String)null);
	    Font font = new Font("dialog",Font.PLAIN,10);
	    button.setFont(font);
	    button.setBorderPainted(false);
	    button.setBorder(new EmptyBorder(2,2,2,2));
	    button.setPreferredSize(new Dimension(20,20));
	    return button;
	}

    public static JButton newLinkButton(String name, String iconName, ActionListener listener)
    {
		Icon icon = null;
		String iconSpec = JoMenuBar.ICON_SPECS.get(iconName);
		if (iconSpec!=null)
			icon = JoToolBar.create1AwesomeIcon(iconSpec,20);
		if (icon==null)
			icon = ImgUtil.getMenuIcon(iconName);

        JButton button = newButton(name, icon, listener);
		button.setBorderPainted(false);
		button.setBorder(new EmptyBorder(2,8,2,8));
		button.setPreferredSize(new Dimension(92,22));

		Font font = new Font("dialog",Font.PLAIN,10);
		Map attrs = font.getAttributes();
		attrs.put(TextAttribute.UNDERLINE,TextAttribute.UNDERLINE_ON);
		font = font.deriveFont(attrs);
		button.setFont(font);
		button.setForeground(Color.BLUE);
		return button;
    }

	public static Font rescaleFont(Font f)
	{
		/*	this is used to scale Fonts on HiDPI displays.
			some LnFs can do this out-of-the-box, some don't

			setting a font explicitly should have the desired effect,
			but some Components have funny default font sizes.
			let's fix them
		 */
		if (Version.windows)
		{
			int screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
			if (screenResolution <= 96) return f;

			float scaleFactor = screenResolution/96.f;
			return f.deriveFont(f.getSize()*scaleFactor);
		}
		return f;
	}

	public static void rescaleFonts(Component root)
	{
		/*	this is used to scale Fonts on HiDPI displays.
			some LnFs can do this out-of-the-box, some don't

			setting a font explicitly should have the desired effect,
			but some Components have funny default font sizes.
			let's fix them
		 */
		if (Version.windows)
		{
			int screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
			if (screenResolution <= 96) return;

			float scaleFactor = screenResolution/96.f;
			rescaleFonts(root,9,scaleFactor);
		}
	}

	private static void rescaleFonts(Component comp, int minSize, float scaleFactor)
	{
		Font f = comp.getFont();
		if (f!=null && f.getSize() < minSize) {
			f = f.deriveFont(f.getSize()*scaleFactor);
			comp.setFont(f);
		}

		if (comp instanceof JComponent) {
			Border b = ((JComponent)comp).getBorder();
			if (b instanceof TitledBorder) {
				TitledBorder tb = (TitledBorder)b;
				rescaleFont(tb, minSize, scaleFactor);
			}
		}

		if (comp instanceof JSlider) {
			rescaleSliderFonts((JSlider)comp, minSize, scaleFactor);
		}

		if (comp instanceof Container) {
			Container cont = (Container)comp;
			for(int i=0; i<cont.getComponentCount(); i++)
				rescaleFonts(cont.getComponent(i),minSize,scaleFactor);
		}
	}

	private static void rescaleSliderFonts(JSlider slider, int minSize, float scaleFactor)
	{
		Dictionary<Integer,JComponent> labels = slider.getLabelTable();
		for(Enumeration<Integer> i = labels.keys(); i.hasMoreElements(); ) {
			JComponent label = labels.get(i.nextElement());
			if (label!=null)
				rescaleFonts(label,minSize,scaleFactor);
		}
	}

	private static void rescaleFont(TitledBorder border, int minSize, float scaleFactor)
	{
		Font f = border.getTitleFont();
		if (f!=null && f.getSize() < minSize) {
			f = f.deriveFont(f.getSize()*scaleFactor);
			border.setTitleFont(f);
		}
	}
}
