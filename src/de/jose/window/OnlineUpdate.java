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

package de.jose.window;

import de.jose.*;
import de.jose.task.io.FileDownload;
import de.jose.util.AWTUtil;
import de.jose.util.StringUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.file.FileUtil;
import de.jose.view.input.JoStyledLabel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;


/**
 * There is an XML file on the sourceforge Web server that describes the
 * current version and the update package:
 *
 * <pre>
    <download-info>
		<current-version>1.0.1</current-version>
		<update>
			<os>any</os>
			<url>http://switch.dl.sourceforge.net/sourceforge/jose-chess/jose-101-update.zip</url>
	       	<mirrors>http://prdownloads.sourceforge.net/jose-chess/jose-101-update.zip?download</mirrors>
			<type>zip</type>
			<size>1507516</size>
		</update>
	</download-info>
    </pre>
 *
 *
 * @author Peter Schäfer
 */
public class OnlineUpdate
{
	/**
	 * result from getDownloadMode: DON'T DOWNLOAD
	 */
	protected static final int CANCEL       = 0;
	/**
	 * result from getDownloadMode: DOWNLOAD, install later
	 */
	protected static final int DOWNLOAD     = 1;
	/**
	 * download from Mirror (open Web Browser)
	 */
	protected static final int MIRROR       = 2;
	/**
	 * result from getDownloadMode: DOWNLOAD & INSTALL immediately
	 */
	protected static final int INSTALL      = 3;



	static class PackageInfo
	{
		String version;
		String os;
		String url;
		String mirrorUrl;
		String fileType;
		int fileSize;
		String history;
		String notes;
	}


	protected static String paramMessage(String msg, String param)
	{
		String text = Language.get(msg);
		return StringUtil.replace(text,"%p%",param);
	}

	protected static Document getDownloadInfo()
	{
		//  get info
		URL url = null;
		String infoUrl = Application.theApplication.theConfig.getURL("online-update");
		try {
			url = new URL(infoUrl);

		} catch (MalformedURLException muex) {
			JoDialog.showErrorDialog(paramMessage("download.error.invalid.url",infoUrl));
			return null;
		}

		//  parse info
		try {

			InputStream info = url.openStream();
			Document doc = XMLUtil.parse(info);
			info.close();
			return doc;

		} catch (IOException ioex) {
			JoDialog.showErrorDialog(paramMessage("download.error.connect.fail",infoUrl));
			return null;
		} catch (Exception saxex) {
			JoDialog.showErrorDialog(paramMessage("download.error.parse.xml",infoUrl));
			return null;
		}
	}

	protected static PackageInfo getPackageInfo(Element root, String preferredOs)
	{
		//  else: look for download package
		PackageInfo result = new PackageInfo();
		result.history = XMLUtil.toString(XMLUtil.getChild(root,"history"));

		NodeList updates = root.getElementsByTagName("update");
		for (int i=0; i<updates.getLength(); i++)
		{
			Element update = (Element)updates.item(i);
			result.os = XMLUtil.getChildValue(update,"os");
			if (result.os==null)
				result.os = "any";

			if (result.os.equalsIgnoreCase("any") ||
			    result.os.equalsIgnoreCase(preferredOs))
			{
				//  that's it !
				result.url = XMLUtil.getChildValue(update,"url");
				if (result.url==null) continue; //  missing URL ?!?

				result.mirrorUrl = XMLUtil.getChildValue(update,"mirrors");
				result.fileType = XMLUtil.getChildValue(update,"type");
				result.fileSize = XMLUtil.getChildIntValue(update,"size");
				if (XMLUtil.existsChild(update,"notes"))
					result.notes = XMLUtil.toString(XMLUtil.getChild(update,"notes"));
				return result;
			}
		}
		return null;
	}

	public static boolean check()
	{
		//  read download info
		Document doc = getDownloadInfo();
		if (doc==null) return false;

		Element root = doc.getDocumentElement();

		//  compare to installed version
		String serverVersion = XMLUtil.getChildValue(root,"current-version");
		String infoUrl = Application.theApplication.theConfig.getURL("online-update");
		if (serverVersion==null) {
			JoDialog.showErrorDialog(paramMessage("download.error.version.missing",infoUrl));
			return false;
		}

		if (false/*StringUtil.compareVersion(serverVersion,Version.jose) <= 0*/) {
			JoDialog.showMessageDialog(paramMessage("download.message.up.to.date",null));
			return true;
		}

		//  get package info
		PackageInfo pack = getPackageInfo(root,Version.osDir);
		if (pack==null) {
			JoDialog.showErrorDialog(paramMessage("download.error.os.missing",infoUrl));
			return false;
		}

		//  ask download mode
		pack.version = serverVersion;
		int mode = getDownloadMode(pack);

		switch (mode) {
		default:
		case CANCEL:
			return false;

		case DOWNLOAD:
			//  download, don't install
			return doDownload(pack);

		case MIRROR:
			//  redirect to Web Browser
			try {
				BrowserWindow.showWindow(pack.mirrorUrl);
				return true;
			} catch (IOException ioex) {
				JoDialog.showErrorDialog(paramMessage("download.error.browser.fail",pack.mirrorUrl));
				return false;
			}

		case INSTALL:
			//  download an install immediately
			return doInstall(pack);
		}
	}

	public static boolean update(File zipFile, String newVersion)
	{
		try {
			FileUtil.unzip(zipFile, Application.theWorkingDirectory);

			JoDialog.showMessageDialog(paramMessage("download.message.success",newVersion));

			zipFile.delete();

			return true;

		} catch (IOException ioex) {

			JoDialog.showErrorDialog(paramMessage("download.error.update",newVersion));
			return false;
		}
	}


	protected static boolean doDownload(PackageInfo pack)
	{
		URL url = null;
		try {
			url = new URL(pack.url);
		} catch (MalformedURLException muex) {
			JoDialog.showErrorDialog(paramMessage("download.error.invalid.url",pack.url));
			return false;
		}

		File target = showSaveDialog(FileUtil.getFileName(url));
		if (target==null)
			return false;

		new FileDownload(url,target,pack.fileSize).start();
		return true;
	}

	protected static boolean doInstall(PackageInfo pack)
	{
		URL url = null;
		try {
			url = new URL(pack.url);
		} catch (MalformedURLException muex) {
			JoDialog.showErrorDialog(paramMessage("download.error.invalid.url",pack.url));
			return false;
		}

		File target = new File(Application.theWorkingDirectory, FileUtil.getFileName(url));
		Command cmd = new Command("system.update",null, target, pack.version);
		cmd.target = Application.theApplication;
		
		FileDownload down = new FileDownload(url,target,pack.fileSize);
		down.setOnSuccess(cmd);
		down.start();
		return true;
	}


	static class DownloadDialog extends JoTabDialog
	{
		PackageInfo pack;

		DownloadDialog(PackageInfo pack)
		{
			super("online.update",true);
			this.pack = pack;

			addTab(newGridPane());
			addTab(newGridPane());

			addButtons(JoDialog.OK_CANCEL);

			JoMenuBar.assignMnemonics(getTabbedPane());
		}

		protected void initTab0(Component comp0)
		{
			JPanel tab0 = (JPanel)comp0;
			String message = Language.get("download.message");
			HashMap params = new HashMap();
			params.put("version", pack.version);
			params.put("url", pack.url);
			params.put("size", FileUtil.formatFileSize(pack.fileSize));
			message = StringUtil.replace(message,params);

			JoStyledLabel panel1 = new JoStyledLabel(message);
			JPanel panel2 = newGridPane();

			Border border = new CompoundBorder(
					new BevelBorder(BevelBorder.RAISED),
					new EmptyBorder(8,8,8,8));
			panel1.setBorder(border);
			panel2.setBorder(border);

			add(panel2,1, JoDialog.newRadioButton("update.install"));
			add(panel2,1, JoDialog.newRadioButton("update.download"));

			if (pack.mirrorUrl!=null)
				add(panel2,1, JoDialog.newRadioButton("update.mirror"));

			tab0.add(panel1, JoDialog.ELEMENT_ROW);
			tab0.add(panel2, JoDialog.ELEMENT_ROW);

			newButtonGroup("update");
			setValue("update","install");
		}

		protected void initTab1(Component comp1)
		{
			JPanel tab1 = (JPanel)comp1;

			JoStyledLabel label = null;
			JScrollPane scroller = null;

			label = new JoStyledLabel(((pack.history!=null)?pack.history:"---"));
			scroller = new JScrollPane(label,
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			tab1.add(scroller, JoDialog.ELEMENT_REMAINDER);

			AWTUtil.scrollRectToVisible(label,new Rectangle(0,0,0,0));

			if (pack.notes != null) {
				JPanel tab3 = JoDialog.newGridPane();
				addTab(tab3);
				label = new JoStyledLabel(pack.notes);
				scroller = new JScrollPane(label,
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				tab3.add(scroller, JoDialog.ELEMENT_REMAINDER);
			}
		}

	}

	protected static int getDownloadMode(PackageInfo pack)
	{
		DownloadDialog dialog = new DownloadDialog(pack);

		dialog.center(400,300);
		dialog.show();

		if (dialog.wasCancelled())
			return CANCEL;

		String result = (String)dialog.getValue("update");

		if (result.equals("install"))
			return INSTALL;
		if (result.equals("download"))
			return DOWNLOAD;
		if (result.equals("mirror"))
			return MIRROR;
		//  else
		return CANCEL;
	}



	protected static File showSaveDialog(String fileName)
	{
	    File[] preferredDirs = { Application.theWorkingDirectory };

	    JoFileChooser chooser = JoFileChooser.forSave(preferredDirs, null, fileName);

	    if (chooser.showSaveDialog(JoFrame.theActiveFrame) != JFileChooser.APPROVE_OPTION)
	        return null; //  cancelled

	    File file = chooser.getSelectedFile();

	    String defExt = JoFileChooser.getFileExtension(chooser.getCurrentFilter());
	    if (defExt != null)
	         file = FileUtil.appendExtension(file,defExt);

	    if (file.exists() && !JoFileChooser.confirmOverwrite(file))
	        return null; //  don't overwrite
	    else
	        return file;
	}

}
