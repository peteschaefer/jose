package de.jose;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.awt.Button;
import java.awt.TextArea;
import java.awt.Panel;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.event.WindowListener;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

/**
 * displays error message during launch.
 * The reason that we move it to a spearate class are:
 * - it should run with any VM, even Microsoft's
 * - we want to avoid class dependencies as much as possible in de.jose.Main
 */
public class BootError
{
    protected static String[] CMD_LINE_OPTIONS =
	{
		null,"Global Settings",null,
		"workdir",				"current directory",	"jose working directory",
		"db",					"database server",	"see config/defaults.xml",
		"datadir",				"database directory",	"workdir/database",
		"splash",				"show splash screen on startup",	"true",
		"show.errors",			"show exceptions to the user",	"true",
		"log",					"log errors to errors.log file",	"true",
		"3d",					"enable 3D view",	"true",
		"2d.double.buffer",		"use double buffering for board view (not recommended)",	"false",
		"framerate",			"log 3D frame rate",	"false",
		"native.skin",			"enable native skin functions",	"true",
		"unique.strings",		"avoid duplicate entries for indentical strings (players, events, etc.); recommended",	"true",
		"sys.collections",		"show all games in the list window, including those in the trash",	"true",
		"asynch.import",		"run insert statements in a separate thread (not recommended)",	"false",
		"pipe",					"use pipes for database connections","true",

		null,"User Preferences",null,
		"discard.profile",		"create a new user profile with factory settings",	"false",
		"discard.settings",		"use factory settings (colors, languages, etc.)",	"false",
		"discard.layout",		"use factory layout",	"false",
		"discard.styles",		"use factory text styles",		"false",
		"discard.history",		"discard document history",		"false",

		null,"Debug Options:",null,
		"debug.properties",		"log ystem properties",		"false",
		"debug.sql",			"logs database queries",	"false",
		"debug.commands",		"logs GUI commands",		"false",
	};

    public static void printHelp(PrintWriter out)
	{
		if (out==null) out = new PrintWriter(System.out,true);

		out.println("java -jar jose.jar [property=value]* ");
		out.println();
		out.println("Example:");
		out.println("\tjava -jar -jose.jar splash=false datadir=/home/tiger/jose-data");
		out.println();
		for (int i = 0; i < CMD_LINE_OPTIONS.length; i+=3)
		if (CMD_LINE_OPTIONS[i]==null) {
			out.println();
			out.println(CMD_LINE_OPTIONS[i+1]);
		}
		else {
			out.print("\t");
			out.print(CMD_LINE_OPTIONS[i]);
			for (int j=CMD_LINE_OPTIONS[i].length(); j<=24; j++) out.print(" ");
			out.print("\t");
			out.print(CMD_LINE_OPTIONS[i+1]);
			out.print(" (default=");
			out.print(CMD_LINE_OPTIONS[i+2]);
			out.println(")");
		}
	}

    public static void showError(String message)
	{
		System.err.println(message);

		CloseListener listener = new CloseListener();
		Button button = new Button("OK");
		button.addActionListener(listener);
/*
		Panel textPanel = new Panel(new GridLayout(0,1));
		int i=0, j;
		while (i < message.length()) {
			j = message.indexOf("\n",i);
			if (j < 0) j = message.length();
			textPanel.add(new Label(message.substring(i,j)));
			i = j+1;
		}
*/
		TextArea textPanel = new TextArea(message);

		Panel buttonPanel = new Panel(new FlowLayout());
		buttonPanel.add(button);

		Dialog dialog = new Dialog(new Frame());
		dialog.setLayout(new BorderLayout());
		dialog.setSize(400,250);
		dialog.setLocation(60,60);
		dialog.setTitle("Error");

		dialog.add(textPanel, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		dialog.show();
		dialog.addWindowListener(listener);

		for (;;)
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
	}

    public static void showError(Throwable ex)
    {
        StringWriter sout = new StringWriter();
        PrintWriter pout = new PrintWriter(sout);
        while (ex!=null) {
            ex.printStackTrace(pout);
            ex.printStackTrace();
//            ex = ex.getCause();	//	JDK 1.4
        }
        pout.flush();
        String message = sout.toString();
        showError(message);
    }

    static class CloseListener
			extends Thread
			implements WindowListener,ActionListener
 	{
		public void windowOpened(WindowEvent e) {}
		public void windowClosing(WindowEvent e) { System.exit(+1);	}
		public void windowClosed(WindowEvent e) {}
		public void windowIconified(WindowEvent e) {}
		public void windowDeiconified(WindowEvent e) {}
		public void windowActivated(WindowEvent e) {}
		public void windowDeactivated(WindowEvent e) {}
		public void actionPerformed(ActionEvent e) { System.exit(+1); }
	}
}
