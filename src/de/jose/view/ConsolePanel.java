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

package de.jose.view;

import de.jose.*;
import de.jose.window.JoFileChooser;
import de.jose.window.JoFrame;
import de.jose.plugin.InputListener;
import de.jose.plugin.OutputListener;
import de.jose.plugin.Plugin;
import de.jose.profile.LayoutProfile;
import de.jose.profile.UserProfile;
import de.jose.util.AWTUtil;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Collection;
import java.util.Map;

public class ConsolePanel
		extends JoPanel
		implements InputListener, OutputListener, MessageListener
{
	/** one instance */
	public static ConsolePanel theConsole = null;

	/**	output text area	 */
	protected JTextPane output;
	protected JScrollPane scroller;
	/** output file writer (optional) */
	protected Writer logWriter;
	/**	input line	 */
	protected JTextField input;
	/** checkbox for hiding INFO messages   */
	protected JCheckBox showInfo;
	/**	associated plugin	 */
	protected Plugin plugin;

	/** current style for output    */
	protected AttributeSet currentStyle;


	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------
	
	public ConsolePanel(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
	{
		super(profile,withContextMenu,withBorder);
		
		setLayout(new BorderLayout());
		output = new JTextPane();
		output.setEditable(false);
//		output.setLineWrap(true);
//		output.setWrapStyleWord(true);
		
		scroller = new JScrollPane(output,
									JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scroller, BorderLayout.CENTER);
		
		input = new JTextField();
		input.addActionListener(this);

		showInfo = new JCheckBox(Language.get("plugin.show.info"));

		Box hbox = Box.createHorizontalBox();
		hbox.add(input);
		hbox.add(showInfo);
		add(hbox, BorderLayout.SOUTH);

		logWriter = null;
	}
	
	/**	called when first shown	 */
	public void init()
	{
		if (Application.theApplication.getEnginePlugin() != null)
			connectTo(Application.theApplication.getEnginePlugin());

		StyleContext styles = new StyleContext();

		Style base = styles.addStyle("base", null);
		StyleConstants.setFontFamily(base,"monospaced");
		StyleConstants.setFontSize(base,12);

		Style style = styles.addStyle("output",base);
		StyleConstants.setForeground(style,new Color(0,0,128));

		style = styles.addStyle("input",base);
		StyleConstants.setForeground(style,new Color(0,128,0));

		style = styles.addStyle("info",base);
		StyleConstants.setForeground(style,new Color(64,64,64));

		style = styles.addStyle("error",base);
		StyleConstants.setForeground(style,new Color(128,0,0));

		DefaultStyledDocument doc = new DefaultStyledDocument(styles);
		output.setDocument(doc);

		showInfo.setSelected(Application.theUserProfile.getBoolean("plugin.show.info",true));

		setStyle("output");

		theConsole = this;
	}
	
	public void connectTo(Plugin plug)
	{
		plugin = plug;
		plugin.addInputListener(this,0);
		plugin.addOutputListener(this,0);
		plugin.addMessageListener(this);
	}
	
	public void handleMessage(Object who, int what, Object data)
	{
		//	currently ignored
	}

	private void setStyle(String styleName)
	{
		currentStyle = ((DefaultStyledDocument)output.getDocument()).getStyle(styleName);
	}

/*
	public void print(String s)
	{
		output.append(s);
	}
	
	public final void println(String s)
	{
		print(s);
		println();
	}
	
	public void println()
	{
		output.append("\n");
	}
*/	
	public void clear()
	{
		output.setText("");

		if (logWriter!=null) {
			try {
				logWriter.close();
			} catch (IOException e) {
				//  can't help it
			}
			logWriter = null;
		}
	}
	
	public void setupActionMap(Map map)
	{
        super.setupActionMap(map);

		CommandAction action;

		action = new CommandAction() {
			public void Do(Command cmd) {
				connectTo((Plugin)cmd.data);
			}
		};
		map.put("new.plugin", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
/*			Position pos = Application.theApplication.boardPanel().position;
			if (pos.isMate())
				println("Mate !");
			else if (pos.isStalemate())
				println("Stalemate");
			else if (pos.isCheck())
				println("check");
*/			}
		};
		map.put("move.user.notify", action);

        action = new CommandAction() {
            public void Do(Command cmd) {
                output.setText("");      
            }
        };
        map.put("menu.edit.clear", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws IOException {
				//  log to file
				saveToFile();
			}
		};
		map.put("plugin.log.file", action);


		action = new CommandAction() {
			public void Do(Command cmd) {
				//  toggle
				showInfo.setSelected(!showInfo.isSelected());
			}
		};
		map.put("plugin.show.info", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (showInfo!=null) {
					UserProfile profile = (UserProfile)cmd.data;
					profile.set("plugin.show.info",showInfo.isSelected());
				}
			}
		};
		map.put("update.user.profile",action);
	}
	
	public void adjustContextMenu(Collection list, MouseEvent event)
	{
		super.adjustContextMenu(list,event);
		list.add(ContextMenu.SEPARATOR);
		list.add("menu.edit.clear");

		list.add(Util.toBoolean(showInfo.isSelected()));
		list.add("plugin.show.info");

		list.add("plugin.log.file");

		list.add(null);
		list.add("restart.plugin");
	}
	
	protected void scrollDown()
	{
		//  don't scroll if the user is currently holding the scroll bar !
		AWTUtil.scrollDown(scroller,output);
	}


	public void saveToFile() throws IOException
	{
		File[] preferredDirs = {
			(File)Application.theUserProfile.get("filechooser.log.dir"),
			Application.theWorkingDirectory,
		};

		JoFileChooser chooser = JoFileChooser.forSave(preferredDirs, null, "plugin-log.txt");
		if (chooser.showSaveDialog(JoFrame.theActiveFrame) != JFileChooser.APPROVE_OPTION)
            return; //  cancelled

		File file = chooser.getSelectedFile();
		Application.theUserProfile.set("filechooser.log.dir",  chooser.getCurrentDirectory());

		saveToFile(file);
	}

	public void saveToFile(File file) throws IOException
	{
		FileWriter out = new FileWriter(file);

		javax.swing.text.Document doc = output.getDocument();
		Segment seg = new Segment(new char[4096],0,4096);
		seg.setPartialReturn(true);

		int offset = 0;
		while (offset < doc.getLength())
		try {
			int chunkSize = Math.min(doc.getLength()-offset, seg.array.length);
		    doc.getText(offset, chunkSize, seg);

			out.write(seg.array, seg.offset, seg.count);
		    offset += seg.count;

		} catch (BadLocationException blex) {
			//  can't help it
			break;
		}

		logWriter = out;
		out.flush();
	}


	//-------------------------------------------------------------------------------
	//	Interface InputListener
	//-------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e) 
	{
		if (/*e.getActionCommand().equals(JTextField.notifyAction) &&*/
			e.getSource()==input)
		{
			String s = input.getText();
			input.setText("");
			
/*			print(">");
			println(s);
			scrollDown();
*/
            if (plugin==null)
                try {
                    Application.theApplication.openEnginePlugin();
                } catch (IOException e1) {
                    Application.error(e1);
                }

        /*  hack
            if (s.startsWith("sig")) {
                int sig = Integer.parseInt(StringUtil.rest(s).trim());
                de.jose.util.UnixUtils.kill(plugin.getNativeProcess(),sig);
            }
        */
			if (plugin != null) {
				plugin.print().println(s);
				/*	this will eventually trigger a "writeLine" call back */
			}
		}
		else
			super.actionPerformed(e);
	}

	//	public void nextChar(char c)	{ }
	
	//	interface InputListener
	public void readLine(char[] chars, int offset, int len) throws IOException
    {
        synchronized(output) {
            String s = String.valueOf(chars,offset,len);
            if (s.startsWith("info")) {
                if (!showInfo.isSelected()) return;  //  hide info lines
                doprintln("info",s);
            }
            else
                doprintln("input",s);
		}
	}
	
	public void readError(Throwable ex) throws IOException
	{
		doprintln("error","> "+ex.getClass().getName()+" "+ex.getLocalizedMessage());
		}

	public void readEOF()					{ }

	public void println(String style, String text) throws IOException
	{
		doprintln(style, "> "+text);
		}

	public void writeLine(char[] chars, int offset, int len) throws IOException
	{
		doprintln("output","> "+String.valueOf(chars,offset,len));
		}

	private void doprintln(final String style, final String text) throws IOException
	{
		Runnable print1line = new Runnable() {
			public void run()
			{
				synchronized (output)
				{
					setStyle(style);

		try {
			javax.swing.text.Document doc = output.getDocument();
						doc.insertString(doc.getLength(),text+"\n",currentStyle);
		} catch (BadLocationException e) {
			//  must not happen
			Application.error(e);
		}

		scrollDown();

					if (logWriter!=null)
						try {
			logWriter.write("\n");
			logWriter.flush();
						} catch (IOException e) {
							Application.error(e);
		}
	}
			}
		};

		SwingUtilities.invokeLater(print1line);
	}


	public void writeEOF()					{ /* can not happen */ }
	
	public void writeError(Exception ex) throws IOException
	{
		readError(ex);
	}

}
