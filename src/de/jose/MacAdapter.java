
package de.jose;

/**
 * MacAdapter
 * 
 * @author Peter Sch�fer
 */

import de.jose.comm.Command;
//	@deprecated last version that supported MRJAdapter was jdk1.8 for Mac
//import net.roydesign.mac.MRJAdapter;
//import net.roydesign.event.ApplicationEvent;
//	use java.awt.Desktop instead
import java.awt.*;
import java.awt.desktop.*;

public class MacAdapter implements AboutHandler, QuitHandler, PreferencesHandler, OpenFilesHandler, PrintFilesHandler
{

	public MacAdapter()
	{
		Desktop desktop = Desktop.getDesktop();
		desktop.setAboutHandler(this);
		desktop.setQuitHandler(this);
		desktop.setPreferencesHandler(this);
		desktop.setOpenFileHandler(this);
		desktop.setPrintFileHandler(this);
/*
		MRJAdapter.addAboutListener(this);
		MRJAdapter.addOpenApplicationListener(this);
		MRJAdapter.addReopenApplicationListener(this);
		MRJAdapter.addOpenDocumentListener(this);
		MRJAdapter.addPrintDocumentListener(this);
		MRJAdapter.addPreferencesListener(this);
		MRJAdapter.addQuitApplicationListener(this);
*/
	}



	@Override
	public void handleAbout(AboutEvent e) {
		Command cmd = new Command("menu.help.about");
		Application.theCommandDispatcher.handle(cmd,Application.theApplication);
	}

	@Override
	public void handlePreferences(PreferencesEvent e) {
		Command cmd = new Command("menu.edit.option");
		Application.theCommandDispatcher.handle(cmd,Application.theApplication);
	}

	@Override
	public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
		Command cmd = new Command("menu.file.quit");
		Application.theCommandDispatcher.handle(cmd,Application.theApplication);
	}

	@Override
	public void openFiles(OpenFilesEvent e) {
		String filePath = e.getFiles().get(0).getPath();
		Command cmd = new Command("menu.file.open.all",null,filePath);
		Application.theCommandDispatcher.handle (cmd,Application.theApplication);
	}

	@Override
	public void printFiles(PrintFilesEvent e) {
		Command cmd = new Command("menu.file.print");
		Application.theCommandDispatcher.handle(cmd,Application.theApplication);
	}
}