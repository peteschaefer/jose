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
import de.jose.image.ImgUtil;
import de.jose.db.DBAdapter;
import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.jo3d.Util3D;
import de.jose.util.StringUtil;
import de.jose.util.file.FileUtil;
import de.jose.view.input.JoStyledLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.net.URL;

public class AboutDialog
		extends JoTabDialog
{
	public static final GridBagConstraints LOGO_ONE =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE, 1,1, 0,0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
							   INSETS_NORMAL, 0,0);
    public static final GridBagConstraints LOGO_BOTTOM =
        new GridBagConstraints(0,GridBagConstraints.RELATIVE, 1,GridBagConstraints.REMAINDER, 0,0,
                               GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                               INSETS_NORMAL, 0,0);

	public AboutDialog(String name)
	{
		super(name, false);
		center(500,400);

		addTab(newGridPane());  //  tab0
		addTab(newGridPane());  //  tab1
		addTab(newGridPane());  //  tab2
		addTab(newGridPane());  //  tab3
		addTab(newGridPane());  //  tab4
		addTab(newGridPane());  //  tab5

		addButton("dialog.button.close");

		JoMenuBar.assignMnemonics(getTabbedPane());
	}

	public void setupActionMap(Map map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) throws IOException
			{
				URL url = new URL("http",Version.projectURL,"");
				BrowserWindow.showWindow(url);
			}
		};
		map.put("dialog.about.link.home",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws IOException
			{
				String url = Application.theApplication.theConfig.getURL("online-donate");
				BrowserWindow.showWindow(url);
			}
		};
		map.put("dialog.about.link.donate",action);
	}

	private static final String MAIN_LOGO =
	        "<a href='http://%project-url%'><img src='file:///%imagedir%/jose128.png' border=0></a>";
	private static final String SPONSOR_LOGO = 
	        "<a href='%sponsor-url%'><img src='file:///%imagedir%/%sponsor-logo%' border=0></a>";
	private static final String JOSE_LOGO =
	        "<a href='http://%project-url%'><img src='file:///%imagedir%/logo.png' border=0></a>";
	private static final String JOSE_VERSION =
	        "Version %version%";
	private static final String JOSE_HOME = 
	        "<font color=black><a href='http://%project-url%'>%project-url%</a></font>";
	private static final String COPYRIGHT =
	        "<font size=-1>Copyright %year% %author%</font>";
	private static final String CUSTOM =
	        "<a href='%custom-url%'><img src='file:///%imagedir%/%custom-logo%' border=0></a>";
	private static final String GPL =
	        "<font size=-1>%gpl-hint%</font>";

	private static final String RIGHT_PANEL =
	        "<table>" +
	        "<tr><td valign=top>"+MAIN_LOGO+"</td></tr>"+
	        "<tr><td valign=bottom align=right>"+CUSTOM+"</td></tr>"+
	        "</table>";

	private static final String FIRST_ROW =
	        "<td colspan=2>"+JOSE_LOGO+"</td>";
	private static final String SECOND_ROW =
	        "<td colspan=2><br><br>"+JOSE_VERSION+"</td>";
	private static final String THIRD_ROW =
	        "<td colspan=2>"+COPYRIGHT+"<br></td>";
	private static final String FOURTH_ROW =
	        "<td colspan=2>"+JOSE_HOME+"<br><br>"+GPL+"</td>";
	
	private static final String LEFT_PANEL =
	        "<table>" +
	        "<tr>"+FIRST_ROW+"</tr>"+
	        "<tr>"+SECOND_ROW+"</tr>"+
	        "<tr>"+THIRD_ROW+"</tr>"+
	        "<tr>"+FOURTH_ROW+"</tr>"+
	        "</table>";

	private static final String ABOUT_HTML =
	        "<table><tr>" +
	        "<td valign=top>"+LEFT_PANEL+"</td>" +
	        "<td valign=top>"+RIGHT_PANEL+"</td>" +
	        "</tr></table>";
	
	public void initTab0(Component comp0)
	{
		JPanel tab0 = (JPanel)comp0;

		//	use a JLabel to determine display defaults
		String imageDir = Application.theWorkingDirectory.getAbsolutePath()
						  +File.separator+"images"+File.separator;

		JoStyledLabel label = new JoStyledLabel("dialog.about.info.1");
		tab0.add(reg(label), JoDialog.gridConstraint(ELEMENT_REMAINDER, 0,0,1));

		tab0.setBackground(Color.white);
		label.setBackground(Color.white);
		activateTab0(comp0);
	}

	public void activateTab0(Component comp0)
	{
        String imageDir = Application.theWorkingDirectory.getAbsolutePath()+"/images";
		String[] splogos = Application.theApplication.theConfig.getPaths("sponsor-logo");

		Map placeholders = new HashMap();
		placeholders.put("imagedir",imageDir);
		placeholders.put("application", Language.get("application.name"));
		placeholders.put("version", Version.jose);
		placeholders.put("year", Version.year);
		placeholders.put("author", Version.author);
		placeholders.put("contact", Version.contact);
		placeholders.put("project-url", Version.projectURL);
        if (splogos.length >= 1) {
            placeholders.put("custom-url", Application.theApplication.theConfig.getURL("sponsor"));
            placeholders.put("custom-logo", splogos[0]);
        }
        else {
			placeholders.put("custom-url", Application.theApplication.theConfig.getURL("online-donate"));
			placeholders.put("custom-logo", "project-support.jpg");
        }
        placeholders.put("gpl-hint", Language.get("dialog.about.gpl"));
/*
		placeholders.put("sponsor-url", Application.theApplication.theConfig.getURL("sponsor"));
		if (splogos.length > 0) {
            placeholders.put("sponsor-logo", splogos[0]);
			placeholders.put("sponsor-link", StringUtil.replace(SPONSOR_LOGO,placeholders));
        }
		else
			placeholders.put("sponsor-link","");
*/
		String html = StringUtil.replace(ABOUT_HTML,placeholders);
		setValueByName("dialog.about.info.1", html);
	}


	public void initTab1(Component comp2)
	{
		JPanel tab2 = (JPanel)comp2;

		String imageDir = Application.theWorkingDirectory.getAbsolutePath()
						  +File.separator+"images"+File.separator;
		String db = getDBIdentifier(Application.theApplication.theDatabaseId);
		tab2.add(new JLabel(new ImageIcon(imageDir+"db."+db+".gif")),
		        JoDialog.gridConstraint(LABEL_ONE, 0,0,1));

		JoStyledLabel label = new JoStyledLabel("dialog.about.info.db");
		tab2.add(reg(label), JoDialog.gridConstraint(ELEMENT_REMAINDER, 1,0,3));

		label.setBackground(Color.white);
		tab2.setBackground(Color.white);
	}

	public void activateTab1(Component comp2)
	{
		setValueByName("dialog.about.info.db",createDBInfoText());
	}

	public void initTab2(Component comp3)
	{
		JPanel tab3 = (JPanel)comp3;

		String imageDir = Application.theWorkingDirectory.getAbsolutePath()
						  +File.separator+"images"+File.separator;
		tab3.add(new JLabel(new ImageIcon(imageDir+"font_logo.gif")),
		        JoDialog.gridConstraint(LABEL_ONE, 0,0,1));

		JoStyledLabel label = new JoStyledLabel("dialog.about.3");
		tab3.add(reg(label), JoDialog.gridConstraint(ELEMENT_REMAINDER, 1,0,3));

		label.setBackground(Color.white);
		tab3.setBackground(Color.white);
		activateTab3(comp3);
	}

	protected void activateTab2(Component tab3)
	{
		setValueByName("dialog.about.3",Language.get("dialog.about.3"));
	}

	public void initTab3(Component comp4)
	{
		JPanel tab4 = (JPanel)comp4;

		String imageDir = Application.theWorkingDirectory.getAbsolutePath()
						  +File.separator+"images"+File.separator;
		tab4.add(new JLabel(new ImageIcon(imageDir+"java.logo2.gif")),
		        JoDialog.gridConstraint(LABEL_ONE, 0,0,1));

		JoStyledLabel label = new JoStyledLabel("dialog.about.info.sys");
		tab4.add(reg(label), JoDialog.gridConstraint(ELEMENT_REMAINDER, 1,0,3));

		label.setBackground(Color.white);
		tab4.setBackground(Color.white);
	}

	public void activateTab3(Component comp4)
	{
		setValueByName("dialog.about.info.sys",createSysInfoText());
	}

	public void initTab4(Component comp5)
	{
		JPanel tab5 = (JPanel)comp5;

		String imageDir = Application.theWorkingDirectory.getAbsolutePath()
						  +File.separator+"images"+File.separator;
		tab5.add(new JLabel(new ImageIcon(imageDir+"j3d.logo.gif")),
		        JoDialog.gridConstraint(LABEL_ONE, 0,0,1));

		JoStyledLabel label = new JoStyledLabel("dialog.about.info.j3d");
		tab5.add(reg(label), JoDialog.gridConstraint(ELEMENT_REMAINDER, 1,0,3));

		label.setBackground(Color.white);
		tab5.setBackground(Color.white);
	}

	public void activateTab4(Component comp5)
	{
		setValueByName("dialog.about.info.j3d",createJ3DInfoText());
	}

	public void initTab5(Component comp6)
	{
		JPanel tab6 = (JPanel)comp6;

		File licenseFile = new File(Application.theWorkingDirectory, "LICENSE");
		String text;
		try {
			text = FileUtil.readTextFile(licenseFile);
		} catch (IOException ex) {
			text = "LICENSE file not found !";
		}

		JTextArea txt = new JTextArea(text);
		txt.select(0,0);
		txt.setEditable(false);
		tab6.add(new JScrollPane(txt), ELEMENT_REMAINDER);
	}

	public static String getDBIdentifier(String db)
	{
		try {
			DBAdapter ad = DBAdapter.get(db,
									Application.theApplication.theConfig,
									Application.theWorkingDirectory,
									Application.theDatabaseDirectory);
			String className = ad.getClass().getName();
			int k2 = className.lastIndexOf("Adapter");
			int k1 = className.lastIndexOf(".",k2);
			return className.substring(k1+1,k2);
		} catch (Exception ex) {
			return db;
		}
	}

	private String createSysInfoText()
	{
		Map placeholders = (Map)System.getProperties().clone();
		placeholders.put("maxmem", byteString(Runtime.getRuntime().totalMemory()));
		placeholders.put("freemem", byteString(Runtime.getRuntime().freeMemory()));

		String text = Language.get("dialog.about.4");
		return StringUtil.replace(text, placeholders);
	}

	private String createDBInfoText()
	{
		Hashtable placeholders = new Hashtable();
		JoConnection conn = null;
		try {
			conn = JoConnection.get();
			placeholders.put("dbname", conn.getDatabaseProductName());
			placeholders.put("dbversion", conn.getDatabaseProductVersion());
			placeholders.put("dburl", JoConnection.getAdapter().getURL());

			JoPreparedStatement stm = conn.getPreparedStatement(
			        "SELECT * FROM MetaInfo WHERE BINARY TableName = BINARY ?");
			stm.setString(1,"Game");
			stm.execute();
			stm.closeResult();

		} catch (SQLException sqlex) {
			StringBuffer errors = new StringBuffer();
			while (sqlex != null) {
				if (errors.length() > 0) errors.append(", ");
				errors.append(sqlex.getLocalizedMessage());
				Application.error(sqlex);
				sqlex = sqlex.getNextException();
			}

			placeholders.put("dbname", errors.toString());
		} catch (Exception ex) {
			placeholders.put("dbname", "error:"+ex.getLocalizedMessage());
			Application.error(ex);
		} finally {
			conn.release();
		}

		String dbid = getDBIdentifier(Application.theApplication.theDatabaseId);
		String s1 = StringUtil.replace(Language.get("dialog.about.2"), placeholders);
		String s2 = Language.get("dialog.about."+dbid);
		return s1+" <br><br> <b>"+s2+"</b>";
	}

	private String createJ3DInfoText()
	{
		String s1;
		boolean preferOpenGL = Application.theUserProfile.getBoolean("board.3d.ogl");

		if (Version.hasJava3d(false,preferOpenGL))
		{
			s1 = "<b>"+Version.getJava3dImplementation(preferOpenGL)
					+" "+Version.getJava3dVersion(preferOpenGL)+"</b>";

			if (Version.hasJava3d(true,preferOpenGL)) {
				s1 += "<br><br>" + Language.get("dialog.about.5.native");
				String platform = Util3D.getNativeVersion(null);
				if (platform != null)
					s1 += platform;
				else
					s1 += Language.get("dialog.about.5.native.unknown");
				String graphicsCard = Util3D.getGraphicsCard(null);
				if (graphicsCard != null)
					s1 += "<br><br>"+graphicsCard;
			}
		}
		else
			s1 = Language.get("dialog.about.5.no3d");

		String s2 = Language.get("dialog.about.5.model");

		return s1+" <br><br> "+s2;
	}

	private String byteString(long bytes)
	{
		if (bytes < 2000)
			return bytes+" B";

		long kbytes = bytes / 1024;
		if (kbytes < 2000)
			return kbytes+" KB";

		long mbytes = kbytes/1024;
		if (mbytes > 16)
			return mbytes+" MB";
		else {
			long hbytes = (kbytes%1024)*10/1024;
			return mbytes+"."+hbytes+" MB";
		}
	}

	public void updateLanguage()
	{
		super.updateLanguage();

		for (int i=0; i<=5; i++)
			if (isInited(i)) activate(i);
	}

}
