
package de.jose;

/**
 * MacAdapter
 * 
 * @author Peter Sch�fer
 */

import de.jose.comm.Command;
import net.roydesign.mac.MRJAdapter;
import net.roydesign.event.ApplicationEvent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

import de.jose.window.JoFrame;
        
public class MacAdapter implements ActionListener
{

	public MacAdapter()
	{
		MRJAdapter.addAboutListener(this);
		MRJAdapter.addOpenApplicationListener(this);
		MRJAdapter.addReopenApplicationListener(this);
		MRJAdapter.addOpenDocumentListener(this);
		MRJAdapter.addPrintDocumentListener(this);
		MRJAdapter.addPreferencesListener(this);
		MRJAdapter.addQuitApplicationListener(this);
	}

	public void actionPerformed(ActionEvent e)
	{
		ApplicationEvent ae = (ApplicationEvent)e;
		switch (ae.getType())
		{
		case ApplicationEvent.ABOUT:
			/** show about dialog */
			Command cmd = new Command("menu.help.about");
			Application.theCommandDispatcher.handle(cmd,Application.theApplication);
			break;

		case ApplicationEvent.OPEN_APPLICATION:
            /** ignored */
            break;
		case ApplicationEvent.OPEN_DOCUMENT:
            String filePath = ae.getFile().getAbsolutePath();
            cmd = new Command("menu.file.open.all",null,filePath);
            Application.theCommandDispatcher.handle (cmd,Application.theApplication);
            break;
		case ApplicationEvent.REOPEN_APPLICATION:
			/** bring to front  */
			if (JoFrame.getActiveFrame()!=null)
				JoFrame.getActiveFrame().toFront();
			break;

		case ApplicationEvent.PRINT_DOCUMENT:
			/** open print dialog (doesn't make very much sense...) */
			cmd = new Command("menu.file.print");
			Application.theCommandDispatcher.handle(cmd,Application.theApplication);
			break;

		case ApplicationEvent.PREFERENCES:
			/** open options dialog */
			cmd = new Command("menu.edit.option");
			Application.theCommandDispatcher.handle(cmd,Application.theApplication);
			break;

		case ApplicationEvent.QUIT_APPLICATION:
			/** quit gracefully */
			cmd = new Command("menu.file.quit");
			Application.theCommandDispatcher.handle(cmd,Application.theApplication);
			break;
		}
	}

	public static void openBrowser(URL url) throws IOException
	{
		MRJAdapter.openURL(url.toExternalForm());
	}
}