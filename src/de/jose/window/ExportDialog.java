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
//import de.jose.devtools.Profiler;
import de.jose.export.ExportConfig;
import de.jose.export.ExportContext;
import de.jose.export.ExportList;
import de.jose.export.HtmlUtil;
import de.jose.image.ImgUtil;
import de.jose.task.GameSource;
import de.jose.util.StringUtil;
import de.jose.util.Units;
import de.jose.util.file.FileUtil;
import de.jose.util.print.NamedPaper;
import de.jose.util.print.PrintPreview;
import de.jose.util.print.PrintableDocument;
import de.jose.view.input.FileInput;
import de.jose.view.input.ValueHolder;
import de.jose.view.style.JoStyleContext;
import de.jose.view.style.StyleChooser;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.File;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

/**
 * @author Peter Sch�fer
 */

public class ExportDialog
        extends JoTabDialog
        implements ItemListener, DocumentListener, ChangeListener, ListSelectionListener
{
	public static final String PRINT    = "dialog.export.print";   //  identifier of print button
	public static final String PREVIEW  = "dialog.export.preview";
	public static final String BROWSER  = "dialog.export.browser";
	public static final String SAVE     = "dialog.export.save";
	public static final String SAVEAS   = "dialog.export.saveas";


	public static Color SELECTED = new Color(0xa0,0xa0,0xb0);
	public static Color BACKGROUND = new Color(0xee,0xee,0xef);

	protected ExportContext context;

	static class InsetsDouble {
		double top,bottom,left,right;
	}

	/** current paper margins   */
	private InsetsDouble margins; //  in pt !!

	/** page size & margin preview    */
	private PagePreview pagePreview;
	/** combo box with paper sizes  */
	private PaperList paperList;
	/** text fields for paper with & height */
	private JTextField tWidth,tHeight;
	/** text fields for paper margins  */
	private JTextField tTop,tBottom,tLeft,tRight;
	/** page orientation radio buttons */
	private AbstractButton rPortrait, rLandscape;
	/** measurement unit for paper size (Combo Box) */
	private Units.UnitPopup unit;

	/** list of XSL files   */
	private ExportList exportList;

	private JPanel exportPanel;
	private JLabel sourceInfo;
	/** panel with user info */
	private JLabel exportUserInfo;
	/** panel that contains export options  */
	private JPanel exportOptions;
	/** embedded file chooser   */
	private JoFileChooser fileChooser;
	/** style editor */
	private StyleChooser styleChooser;

	/** set during execution of listener methods (to avoid nasty recursions)    */
	private boolean enableListener;
	private int currentUIOutput=-1;

	/** preferred mode  */
	protected boolean preferExport;


	public ExportDialog(String name)
	{
		super(name,false);
		Dimension screensize = frame.getGraphicsConfiguration().getBounds().getSize();
		center(Math.min(screensize.width,640), Math.min(screensize.height,488));

//		SELECTED = UIManager.getColor("List.selectionBackground");
//		BACKGROUND = UIManager.getColor("Panel.background");

		preferExport = false;   //  prefer print
		context = new ExportContext();

		addTab(newGridPane());   // Output
		addTab(newGridPane());   // Page Setup
		addTab(newGridPane());   // Styles
		//  TODO rearrange: Input, Output, Styles, PageSetup !?

		addButton(PRINT);
		addButton(SAVE);
		addButton(SAVEAS);
		addButton(CANCEL);

		addSpacer(10);

		addButton(APPLY);
		addButton(REVERT);

		addSpacer(10);

		addButton(PREVIEW);
		addButton(BROWSER);

		addSpacer(10);

		addButton(HELP);

        if (!Version.mac) {
            getButton(PRINT).setIcon(ImgUtil.getMenuIcon("menu.file.print"));
            getButton(CANCEL).setIcon(ImgUtil.getMenuIcon("menu.edit.clear"));
            getButton(SAVE).setIcon(ImgUtil.getMenuIcon("menu.file.save"));
            getButton(SAVEAS).setIcon(ImgUtil.getMenuIcon("menu.file.save.as"));
            getButton(PREVIEW).setIcon(ImgUtil.getMenuIcon("menu.file.print.preview"));
            getButton(BROWSER).setIcon(ImgUtil.getMenuIcon("menu.help.web"));
        }

		JoDialog.rescaleFonts(getElementPane());
		JoDialog.rescaleFonts(getButtonPane());
	}

	public void forExport(GameSource src)
	{
		preferExport = true;
		context.source = src;
		context.styles = (JoStyleContext)context.profile.getStyleContext();
		//  ATTENTION: this refers to the user profile; clone() it before sending it to print
		if (fileChooser!=null)
			fileChooser.setFileName(getPreferredFileName(context.source,currentUIOutput));
	}

	public void forPrint(GameSource src)
	{
		preferExport = false;
		context.source = src;
		context.styles = (JoStyleContext)context.profile.getStyleContext();
		//  ATTENTION: this refers to the user profile; clone() it before sending it to print
		if (fileChooser!=null)
			fileChooser.setFileName(getPreferredFileName(context.source,currentUIOutput));
	}

	private NamedPaper paper()
	{
		return (NamedPaper)paperList.getValue();
	}

	protected static InsetsDouble calcMargins(PageFormat format, int unit)
	{
        InsetsDouble ins = new InsetsDouble();
		ins.top = Units.convert(format.getImageableY(), Units.POINT,unit);
		ins.left = Units.convert(format.getImageableX(), Units.POINT,unit);
		ins.bottom = Units.convert(format.getHeight()-format.getImageableY()-format.getImageableHeight(), Units.POINT,unit);
		ins.right = Units.convert(format.getWidth()-format.getImageableX()-format.getImageableWidth(), Units.POINT,unit);
		return ins;
	}

    protected static InsetsDouble defaultMargins(double value, int oldUnit, int newUnit)
    {
        InsetsDouble ins = new InsetsDouble();
        ins.top = ins.left = ins.bottom = ins.right =
                Units.convert(value,oldUnit,newUnit);
        return ins;
    }

	protected synchronized void activate(int idx)
	{
		super.activate(idx);
		adjustButtons(idx);
	}

	protected void readTab1()
	{
		PageFormat pageFormat = context.profile.getPageFormat(false);
        boolean isDefault = (pageFormat==null);
		pageFormat = PrintableDocument.validPageFormat(pageFormat);

		//  calc margins from pageFormat
		try {
			enableListener = false;
            paperList.setValue(pageFormat);

            if (isDefault)
                margins = defaultMargins(1.5,Units.CENTIMETER, paper().unit);
            else
			    margins = calcMargins(pageFormat,paper().unit);

			rPortrait.setSelected(pageFormat.getOrientation()==PageFormat.PORTRAIT);
			rLandscape.setSelected(pageFormat.getOrientation()!=PageFormat.PORTRAIT);
		} finally {
			enableListener = true;
		}
		updateFields();
	}

	public boolean confirmPrint(GameSource source, int max)
	{
		return confirmSource(source,max,false,false);
	}

	public boolean confirmPreview(GameSource source, int max)
	{
		return confirmSource(source,max,true,false);
	}

	public boolean confirmExport(GameSource source)
	{
		return confirmSource(source,Integer.MAX_VALUE,true,false);
	}

	protected boolean confirmSource(GameSource source, int max, boolean confirmUnknown, boolean acceptZero)
	{
		int count = (source==null) ? 0:source.size();
		if (count==0 && !acceptZero) {
			//  nothing selected
			JoDialog.showErrorDialog(frame, "dialog.export.games.0");
			return false;
		}
		if (count < 0 && !confirmUnknown) {
			//  unknown
			String message = Language.get("dialog.export.games.?");
			return doConfirm(message);
		}
		if (count > max) {
			//  confirm
			String message = Language.get("dialog.export.games.n");
			message = StringUtil.replace(message,"%n%", String.valueOf(count));
			return doConfirm(message);
		}
		//  else:
		return true;
	}

	private boolean doConfirm(String message)
	{
		String[] messages = {
			"<html>"+message,
			Language.get("dialog.export.confirm"),
		};
		String yesLabel = "dialog.export.yes";
		String noLabel = "dialog.button.cancel";

		String[] buttons = {
		    Language.get(yesLabel),
		    Language.get(noLabel),
		};
		int[] mnemos = JoMenuBar.getMnemonics(buttons);

		JoDialog.setOptions(buttons,mnemos);

        SplashScreen.close();
		int result = JOptionPane.showOptionDialog(frame,
		            messages, getTitle(),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,null,
		            null, buttons[JOptionPane.NO_OPTION]);
		return result == JOptionPane.YES_OPTION;
	}


	public boolean save()
	{
		if (isInited(0)) {
			ExportConfig expConfig = Application.theApplication.getExportConfig();
			String transform = (String)getValueByName("xsl.file");

			context.config = expConfig.getConfig(transform);

			java.util.List errors = save(0,context.profile.settings);
			if (errors!=null) {
				//  parse error ?
				Throwable ex = (Throwable)errors.get(0);
				JoDialog.showErrorDialog(ex.getLocalizedMessage());
				return false;
			}

			if (preferExport && ExportConfig.canExport(context.config))
				context.profile.set("transform.export",transform);
			else if (ExportConfig.canPrint(context.config))
				context.profile.set("transform.print",transform);
			else
				context.profile.set("transform.export",transform);
		}

		if (isInited(1)) {
			PageFormat pageFormat = new PageFormat();
			Paper p = (Paper)paper().clone();
			int unit = paper().unit;

			p.setImageableArea(
			        Units.convert(margins.left,unit,Units.POINT),
			        Units.convert(margins.top,unit,Units.POINT),
			        p.getWidth()-Units.convert(margins.left+margins.right,unit,Units.POINT),
			        p.getHeight()-Units.convert(margins.top+margins.bottom,unit,Units.POINT));

			pageFormat.setPaper(p);
			if (rPortrait.isSelected())
				pageFormat.setOrientation(PageFormat.PORTRAIT);
			else
				pageFormat.setOrientation(PageFormat.LANDSCAPE);

			context.profile.setPageFormat(pageFormat);
		}

		if (isInited(2)) {
			boolean styleDirty = styleChooser.isDirty();
			boolean moveDirty = (styleChooser.getMoveFormat()!=context.profile.getInt("doc.move.format"));

			if (styleDirty||moveDirty) {
				context.profile.set("doc.move.format",styleChooser.getMoveFormat());
				styleChooser.apply(context.styles,false);
				Application.theApplication.broadcast(new Command("styles.modified",null,ExportDialog.this,Boolean.TRUE));
			}
		}

		Application.theApplication.broadcast("print.settings.modified");
		return true;
	}

	protected void initTab1(Component comp1)
	{
		//  page setup
		JPanel tab1 = (JPanel)comp1;
		tab1.setLayout(new BorderLayout());

		//  page preview
		pagePreview = new PagePreview();
		pagePreview.setBorder(new BevelBorder(BevelBorder.LOWERED));
		add(tab1,pagePreview,BorderLayout.CENTER);

		JPanel p1 = newGridPane();
		add(tab1,p1,BorderLayout.EAST);

		JPanel pp = newGridBox("dialog.export.paper");
		JPanel po = newGridBox("dialog.export.orientation");
		JPanel pm = newGridBox("dialog.export.margins");

		add(p1,pp, ELEMENT_ROW);
		add(p1,po, ELEMENT_ROW);
		add(p1,pm, ELEMENT_ROW);

		paperList = new PaperList();
		paperList.setName("dialog.export.paper");
		paperList.addItemListener(this);

		tWidth = new JTextField("dialog.export.paper.width");
		tWidth.setColumns(6);

		tHeight = new JTextField("dialog.export.paper.height");
		tHeight.setColumns(6);

		unit = new Units.UnitPopup();

		add(pp,newLabel("dialog.export.paper.format"), LABEL_ONE);
		add(pp,paperList, ELEMENT_ROW);

		add(pp,newLabel("dialog.export.paper.size"), LABEL_ONE);

		Box box = Box.createHorizontalBox();
		box.add(tWidth);
		box.add(newLabel(" x "));
		box.add(tHeight);
		box.add(unit);

		add(pp, box, ELEMENT_ROW);

		tWidth.getDocument().addDocumentListener(this);
		tHeight.getDocument().addDocumentListener(this);
		unit.addItemListener(this);


		tTop = new JTextField("dialog.export.margin.top");
		tBottom = new JTextField("dialog.export.margin.bottom");
		tLeft = new JTextField("dialog.export.margin.left");
		tRight = new JTextField("dialog.export.margin.right");

		add(pm,newLabel("dialog.export.margin.top"), LABEL_ONE);
		add(pm,tTop, ELEMENT_TWO);

		add(pm,newLabel("dialog.export.margin.left"), LABEL_THREE);
		add(pm,tLeft, ELEMENT_FOUR);

		add(pm,newLabel("dialog.export.margin.bottom"), LABEL_ONE);
		add(pm,tBottom, ELEMENT_TWO);

		add(pm,newLabel("dialog.export.margin.right"), LABEL_THREE);
		add(pm,tRight, ELEMENT_FOUR);

		tTop.getDocument().addDocumentListener(this);
		tBottom.getDocument().addDocumentListener(this);
		tLeft.getDocument().addDocumentListener(this);
		tRight.getDocument().addDocumentListener(this);

		reg(rPortrait = newToggleButton("dialog.export.ori.port",ImgUtil.getMenuIcon("print.preview.port")));
		reg(rLandscape = newToggleButton("dialog.export.ori.land",ImgUtil.getMenuIcon("print.preview.land")));
		newButtonGroup("dialog.export.ori");

//		rPortrait.setVerticalTextPosition(JLabel.BOTTOM);
//		rLandscape.setVerticalTextPosition(JLabel.BOTTOM);

		add(po,rPortrait,ELEMENT_TWO);
		add(po,rLandscape,ELEMENT_FOUR);

		rPortrait.addChangeListener(this);
		rLandscape.addChangeListener(this);
	}

	Object mark;

	protected void initTab0(Component comp0)
	{
		//  XSL transformation
		JPanel tab0 = (JPanel)comp0;
		tab0.setLayout(new BorderLayout());
		JComponent leftComponent = createOutputTabLeft();
		JComponent rightComponent = createOutputTabRight();

		tab0.add(leftComponent,BorderLayout.WEST);
		tab0.add(rightComponent,BorderLayout.CENTER);
	}

	protected JComponent createOutputTabLeft()
	{
		exportList = new ExportList(JList.VERTICAL);
		exportList.setName("xsl.file");
		exportList.addListSelectionListener(this);

//		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		topPanel.add(newLabel("dialog.export.transform"));
//		topPanel.add(reg(exportList));
		reg(exportList);
		return new JScrollPane(exportList,
		            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		scroller.setMaximumSize(exportList.getMaximumSize());
//		scroller.setMinimumSize(exportList.getMinimumSize());
	}

	protected JComponent createOutputTabRight()
	{
		/** number of selected games    */
		sourceInfo = new JLabel();
//		reg(sourceInfo);
		sourceInfo.setOpaque(true);
		sourceInfo.setBackground(BACKGROUND);
		sourceInfo.setBorder(new CompoundBorder(
		                    new BevelBorder(BevelBorder.LOWERED),
		                    new EmptyBorder(4,8,4,4)));

		/** user info */
		exportUserInfo = new JLabel();
		exportUserInfo.setLayout(new BorderLayout());
		exportUserInfo.setBorder(new CompoundBorder(
		        new BevelBorder(BevelBorder.LOWERED),
		        new EmptyBorder(4,4,4,4)));
		exportUserInfo.setOpaque(true);
		exportUserInfo.setBackground(BACKGROUND);

		/** options */
		exportOptions = newGridBox(null);
		exportOptions.setBorder(new CompoundBorder(
		        new BevelBorder(BevelBorder.LOWERED),
		        new EmptyBorder(4,4,4,4)));

		/** export Panel (info + options) */
		exportPanel = new JPanel(new BorderLayout());
		exportPanel.add(exportUserInfo,BorderLayout.CENTER);
		exportPanel.add(exportOptions,BorderLayout.SOUTH);

		JScrollPane scroller = new JScrollPane(exportPanel,
		        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setBorder(null);   //  why ??

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(sourceInfo,BorderLayout.NORTH);
		rightPanel.add(exportPanel,BorderLayout.CENTER);

		return rightPanel;
	}

	protected void initTab2(Component comp2)
	{
		JPanel tab2 = (JPanel)comp2;

		styleChooser = new StyleChooser(false);

		add(tab2, styleChooser, ELEMENT_NEXTROW_REMAINDER);
	}

	protected void readTab0()
	{
        read(0,context.profile.settings);

        String transform;
        if (preferExport)
            transform = context.profile.getString("transform.export", ExportConfig.DEFAULT_EXPORT);
        else
            transform = context.profile.getString("transform.print", ExportConfig.DEFAULT_PRINT);

        context.config = context.theConfig.getConfig(transform);

        setValueByName("transform",transform);
        setValueByName("xsl.html.screenres",context.profile.getInt("xsl.html.screenres",72));
        setValueByName("xsl.pdf.embed",context.profile.get("xsl.pdf.embed",Boolean.TRUE));
        setOutputFormat(context.getOutput(),false); //  don't commit data
        exportList.setValue(transform);

        sourceInfo.setText("<html>"+getSourceInfo(context.source));
    }

	protected String getSourceInfo(GameSource src)
	{
		String explain;
		int count = (context.source==null) ? 0:context.source.size();
		if (count==0)
			explain = Language.get("dialog.export.games.0");
		else if (count < 0)
			explain = Language.get("dialog.export.games.?");
		else if (count == 1)
			explain = Language.get("dialog.export.games.1");
		else {
			explain = Language.get("dialog.export.games.n");
			explain = StringUtil.replace(explain,"%n%", String.valueOf(count));
		}

		return explain;
	}

	protected void readTab2()
	{
		read(2,context.profile.settings);

		if (styleChooser.revert(context.styles,context.profile.getInt("doc.move.format"),false)) {
			//  first time; expand some nodes
			styleChooser.expand("body");
			styleChooser.expand("body.line");
		}
	}

	protected void setOutputInfo(int output)
	{
		String text = "";
		switch (output) {
		case ExportConfig.OUTPUT_HTML:      text = Language.getTip("xsl.html");	break;
		case ExportConfig.OUTPUT_XSL_FO:	text = Language.getTip("xsl.pdf"); break;
		case ExportConfig.OUTPUT_AWT:		text = Language.getTip("print.awt"); break;
		}

		exportUserInfo.setText("<html>"+text);
	}

	protected boolean setOutputFormat(int output, boolean dosave)
	{
		if (output==currentUIOutput) return false; //   unmodified
		//  else:
		if (dosave) save(0,context.profile.settings);

		currentUIOutput = output;
		/** create elements */
		exportPanel.removeAll();
		exportOptions.removeAll();

		switch (output) {
		case ExportConfig.OUTPUT_HTML:
			setOutputInfo(output);
			createHtmlPanel(output);

			exportPanel.add(exportUserInfo,BorderLayout.NORTH);
			exportPanel.add(exportOptions,BorderLayout.CENTER);
			break;

		case ExportConfig.OUTPUT_PGN:
		case ExportConfig.OUTPUT_XML:
		case ExportConfig.OUTPUT_TEX:
		case ExportConfig.OUTPUT_ARCH:
			createFileChooserPanel(output);

			exportPanel.add(fileChooser,BorderLayout.CENTER);
			break;

		case ExportConfig.OUTPUT_AWT:
			setOutputInfo(output);

			exportPanel.add(exportUserInfo,BorderLayout.CENTER);
			break;

		case ExportConfig.OUTPUT_XSL_FO:
			setOutputInfo(output);
			createFOPanel();

			exportPanel.add(exportUserInfo,BorderLayout.NORTH);
			exportPanel.add(exportOptions,BorderLayout.CENTER);
			break;
		}

		int tab = getTabbedPane().getSelectedIndex();
		read(0,context.profile.settings);   //  read export options again

		adjustButtons(tab);
		getTab(tab).invalidate();
		/*getCurrentTab().*/frame.repaint();

		JoDialog.rescaleFonts(exportPanel);
		JoDialog.rescaleFonts(exportOptions);
		return true;
	}

	protected void createHtmlPanel(int output)
	{
		add(exportOptions, newRadioButton("xsl.html.figs.tt"), gridConstraint(ELEMENT_TWO_SMALL,1,1,3));   //  use TrueType fonts for figurines
		add(exportOptions, newRadioButton("xsl.html.figs.img"), gridConstraint(ELEMENT_TWO_SMALL,1,2,3));  //  use Images for figurines
		newButtonGroup("xsl.html.figs");
		//  target screen resolution
		Object[] dpis = { new Integer(72), new Integer(96), new Integer(120), };
		JComboBox screenRes = new JComboBox(dpis);
		screenRes.setEditable(true);
		screenRes.setName("xsl.html.screenres");

		Box box = Box.createHorizontalBox();
		box.add(reg(screenRes));
		box.add(new JLabel("dpi"));
		add(exportOptions, box, gridConstraint(ELEMENT_TWO_SMALL,1,3,3));

		//  create separate css file
		add(exportOptions, newCheckBox("xsl.css.standalone"), gridConstraint(ELEMENT_TWO_SMALL,1,4,3));
		//  image location
		FileInput dirInput = newFileInputField("xsl.html.img.dir");
		dirInput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		add(exportOptions,newLabel("xsl.html.img.dir"), gridConstraint(LABEL_ONE,0,5,1));
		add(exportOptions,dirInput, gridConstraint(ELEMENT_TWO,1,5,3));    		//  image & css directory (optional)
		 //  create images & css NOW
		add(exportOptions, newButton("xsl.create.images"), gridConstraint(ELEMENT_TWO_SMALL,1,6,3));

		add(exportOptions,newLabel(""),ELEMENT_REMAINDER);
	}

	protected void createFOPanel()
	{
		//  embed fonts in PDF
		add(exportOptions, newCheckBox("xsl.pdf.embed"), gridConstraint(ELEMENT_TWO_SMALL,1,1,3));
		//  additional font path (optional)
/*
		FileInput dirInput = newFileInputField("xsl.pdf.font.dir");
		dirInput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		add(exportOptions,newLabel("xsl.pdf.font.dir"), gridConstraint(LABEL_ONE,0,2,1));
		add(exportOptions,dirInput, gridConstraint(ELEMENT_TWO,1,2,3));    		//  image & css directory (optional)
*/

		add(exportOptions,newLabel(""),ELEMENT_REMAINDER);
	}

	protected JFileChooser getFileChooser(boolean embedded)
	{
		if (fileChooser==null) {
			fileChooser = new JoFileChooser(null, !embedded);
			fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		fileChooser.setControlButtonsAreShown(!embedded);
        fileChooser.showShellFolders(!embedded);
		return fileChooser;
	}

	protected String getPreferredFileName(GameSource src, int output)
	{
		if (src!=null && src.isCollection())
			try {
				int firstId = src.firstId();
				de.jose.pgn.Collection coll = de.jose.pgn.Collection.readCollection(firstId);
				String ext = ExportConfig.getFileExtension(output);
				if (FileUtil.hasExtension(coll.Name,ext))
					return coll.Name;
				else
					return coll.Name+"."+ext;
			} catch (Exception ex) {

			}

		return Language.get("default.file.name") +"."+ ExportConfig.getFileExtension(output);
	}

	protected void initFileChooser(int output)
	{
		File[] preferredDirs = new File[] {
		     (File)context.profile.get("filechooser.save.dir"),
//             new File(Application.theWorkingDirectory, "pgn"),
			 new File (Version.getSystemProperty("user.home")),
		     Application.theWorkingDirectory,
		 };
		fileChooser.setPreferredDir(preferredDirs);

		int[] availFilters = new int[] {
			0,0,0,0,
			JoFileChooser.ALL,
		};
		int[] preferredFilters = new int[] {
			context.profile.getInt("filechooser.save.filter"),
			0,0,0,0,0,
		};

		switch (output) {
		case ExportConfig.OUTPUT_PGN:
			preferredFilters[1] = availFilters[0] =  JoFileChooser.PGN;
//			availFilters[1] =  JoFileChooser.ARCH;
			break;
		case ExportConfig.OUTPUT_ARCH:
//			availFilters[0] =  JoFileChooser.PGN;
			preferredFilters[1] = availFilters[0] =  JoFileChooser.ARCH;
			break;
		case ExportConfig.OUTPUT_HTML:
			preferredFilters[1] = availFilters[0] =  JoFileChooser.HTML;
			break;
		case ExportConfig.OUTPUT_XSL_FO:
			preferredFilters[1] = availFilters[0] =  JoFileChooser.PDF;
			break;
		case ExportConfig.OUTPUT_XML:
			preferredFilters[1] = availFilters[0] =  JoFileChooser.XML;
			break;
		}

		FileFilter[] oldFilters = fileChooser.getChoosableFileFilters();
		fileChooser.removeChoosableFileFilters(oldFilters);

		fileChooser.setFilters(availFilters, JoFileChooser.getPreferredFilter(preferredFilters));

		fileChooser.setFileName(getPreferredFileName(context.source,output));
	}

	protected void createFileChooserPanel(int output)
	{
		//  show embedded file chooser

		getFileChooser(true);
		initFileChooser(output);
	}


	protected void adjustButtons(int tab)
	{
		boolean showApply       = false;
		boolean showRevert      = false;
		boolean showPrint       = false;
		boolean showPreview     = false;
		boolean showBrowser     = false;
		boolean showSave        = false;
		boolean showSaveAs      = false;

		switch (tab)
		{
		case 0:     //  Output
			showPrint = ExportConfig.canPrint(currentUIOutput);
			showPreview = ExportConfig.canPreview(currentUIOutput);
			showBrowser = ExportConfig.canBrowserPreview(currentUIOutput);
			showSaveAs = ExportConfig.canExport(currentUIOutput);

			switch (currentUIOutput) {
			case ExportConfig.OUTPUT_PGN:
			case ExportConfig.OUTPUT_ARCH:
			case ExportConfig.OUTPUT_XML:
			case ExportConfig.OUTPUT_TEX:
				showSave = true;
				showSaveAs = false;
				break;
			}
			break;

		case 1:     //  Page Setup
			showApply = showRevert = true;
			break;

		case 2:     //  Styles
			showApply = showRevert = true;
			break;
		}

		showButton(APPLY,showApply);
		showButton(REVERT,showRevert);
		showButton(PRINT,showPrint);
		showButton(PREVIEW,showPreview);
		showButton(BROWSER,showBrowser);
		showButton(SAVE,showSave);
		showButton(SAVEAS,showSaveAs);

		int count = (context.source==null) ? 0:context.source.size();

		enableButton(PRINT,count!=0);
		enableButton(PREVIEW,count!=0);
		enableButton(BROWSER,count!=0);
		enableButton(SAVE,count!=0);
		enableButton(SAVEAS,count!=0);

	}

	/**
	 * create an export context with the appropriate font scaling !
	 * don't mess up the user profile !
	 *
	 * @return
	 */
	protected ExportContext createPrintContext()
	{
		float screenRes = (float)Util.todouble(getValueByName("xsl.html.screenres"));

		ExportContext result = context.clone(true);
		switch (result.getOutput()) {
		case ExportConfig.OUTPUT_HTML:
				//  screen res set by user
				result.styles.setScreenResolution(screenRes);
				break;
		case ExportConfig.OUTPUT_AWT:
		case ExportConfig.OUTPUT_XSL_FO:
				//  AWT printing uses always 72 dpi
				result.styles.setScreenResolution(72f);
				break;
		default:
				//  screem res is irrelevant
				break;
		}
		return result;
	}

	protected File getSelectedFile(ActionEvent event)
	{
		/**
		 * when the File Chooser is embedded, it has no own Save button.
		 * We have to notify the File Chooser that the selection should be approved.
		 * Seems that JFileChooser.approveSelection() would do the job, but it doesn't !
		 * The following is a workaround for a notorious Swing bug (#4528663)
		 */
		if (fileChooser.getUI() instanceof BasicFileChooserUI) {
		     BasicFileChooserUI ui = (BasicFileChooserUI)fileChooser.getUI();
		     ui.getApproveSelectionAction().actionPerformed(event);
		 }
		fileChooser.approveSelection();
		File file = fileChooser.getSelectedFile();
		if (file==null) return null;    //  invalid,empty ?

		//  complete suffix
		context.profile.set("filechooser.save.dir",  fileChooser.getCurrentDirectory());
		context.profile.set("filechooser.save.filter",   fileChooser.getCurrentFilter());

		String defExt = JoFileChooser.getFileExtension(fileChooser.getCurrentFilter());
		if (defExt != null)
			file = FileUtil.appendExtension(file,defExt);
		else if (! FileUtil.hasExtension(file.getName())) {
			 //  append preferred extension
			 String newName = FileUtil.setExtension(file.getName(),
			                       ExportConfig.getFileExtension(context.getOutput()));
			 file = new File(file.getParentFile(), newName);
		 }

		//  overwrite ?
		 if (file.exists() && !JoFileChooser.confirmOverwrite(file))
		     return null; //  don't overwrite
		 else
		    return file;
	}

	public void setupActionMap(Map map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction()
		{
			public void Do(Command cmd) {
				save();
				if (!ExportConfig.canPrint(context.config)) throw new IllegalStateException();

				//  actually print (delegate to Application)
				cmd = new Command("export.print",null,createPrintContext());
				Application.theCommandDispatcher.forward(cmd,Application.theApplication);
			}
		};
		map.put("dialog.export.print",action);

		action = new CommandAction()
		{
			public void Do(Command cmd) {
				save();
				if (!ExportConfig.canExport(context.config)) throw new IllegalStateException();

				context.target = getSelectedFile((ActionEvent)cmd.event);
				if (context.target==null) return; //    user cancelled

				//  export to disk file
				cmd = new Command("export.disk",null,createPrintContext());
				Application.theCommandDispatcher.forward(cmd,Application.theApplication);
			}
		};
		map.put("dialog.export.save",action);

		action = new CommandAction()
		{
			public void Do(Command cmd) throws Exception {
				save();
				if (!ExportConfig.canExport(context.config)) throw new IllegalStateException();

				getFileChooser(false);
				initFileChooser(context.getOutput());

				if (fileChooser.showSaveDialog(frame)!=JFileChooser.APPROVE_OPTION)
					return; //  user cancelled

				context.target = getSelectedFile((ActionEvent)cmd.event);
				if (context.target==null) return;   //  user cancelled

				//  actually export (delegate to Application)
				//  export to disk file
				cmd = new Command("export.disk",null,createPrintContext());
				Application.theCommandDispatcher.forward(cmd,Application.theApplication);
			}
		};
		map.put("dialog.export.saveas",action);

		action = new CommandAction()
		{
			public void Do(Command cmd) {
				save();
				if (!ExportConfig.canPreview(context.config) &&
				    !ExportConfig.canBrowserPreview(context.config)) throw new IllegalStateException();

				//  internal print preview
				cmd = new Command("menu.file.print.preview",null,
				        createPrintContext(), Util.toBoolean(cmd.code.equals("dialog.export.preview")));
				Application.theCommandDispatcher.forward(cmd,Application.theApplication);
			}
		};
		map.put("dialog.export.preview",action);
		map.put("dialog.export.browser",action);

		action = new CommandAction()
		{
			public void Do(Command cmd) throws Exception
			{
//				boolean ttfigs = getBooleanValue("xsl.html.figs.tt");
//				boolean css = getBooleanValue("xsl.css.standalone");
				File dir = (File)getValueByName("xsl.html.img.dir");

				if ((dir==null) || (!dir.exists() && !dir.mkdirs()))
					JoDialog.showErrorDialog("bad directory");
				else {
					HtmlUtil.createImages(createPrintContext(),dir);
				}
			}
		};
		map.put("xsl.create.images",action);


		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				int orientation = Util.toint(cmd.data);
				if (isInited(1)) {
					rPortrait.setSelected(orientation==PageFormat.PORTRAIT);
					rLandscape.setSelected(orientation!=PageFormat.PORTRAIT);
				}
			}
		};
		map.put("print.orientation.modified",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				//  notification from style editor
				if (cmd.data==ExportDialog.this) return; //  was broadcasted by ourself

//				boolean sizeModified = Util.toboolean(cmd.data);
				if (styleChooser!=null)	styleChooser.revert(context.styles,context.profile.getInt("doc.move.format"),false);
			}
		};
		map.put("styles.modified",action);
	}

	class PagePreview extends JComponent
	{
		Rectangle paper = new Rectangle();
		Rectangle rmargin = new Rectangle();
		Random rand = new Random();

		public Dimension getPreferredSize()
		{
			return new Dimension(240,32768);
		}

		protected void paintComponent(Graphics g)
		{
			//  cerate a PageFormat to get the orientation right
			PageFormat p = new PageFormat();
			p.setPaper((Paper)paperList.getValue());
			if (rPortrait.isSelected())
				p.setOrientation(PageFormat.PORTRAIT);
			else
				p.setOrientation(PageFormat.LANDSCAPE);

			// calculate scale factor
			double scalex = (double)(Math.min(getWidth(),getHeight())-24)/p.getWidth();
			double scaley = (double)(Math.min(getWidth(),getHeight())-24)/p.getHeight();
			double scale = Math.min(scalex,scaley);

			paper.width = (int)Math.round(p.getWidth()*scale);
			paper.height = (int)Math.round(p.getHeight()*scale);
			paper.x = (getWidth()-paper.width)/2;
			paper.y = (getHeight()-paper.height)/2;

			int unit = paper().unit;
			rmargin.x = (int)Math.round(Units.convert(margins.left,unit,Units.POINT)*scale);
			rmargin.y = (int)Math.round(Units.convert(margins.top,unit,Units.POINT)*scale);
			rmargin.width = (int)Math.round((p.getWidth()-Units.convert(margins.left+margins.right,unit,Units.POINT))*scale);
			rmargin.height = (int)Math.round((p.getHeight()-Units.convert(margins.top+margins.bottom,unit,Units.POINT))*scale);

			PrintPreview.paintPaper(g,paper,rmargin,3);

			//  print "dummy content"
			g.setColor(Color.lightGray);
			rand.setSeed(0xfafafa);
			for (int y=0; y < rmargin.height; y+=4)
			{
				int width = rand.nextInt(Math.max(rmargin.width,4));
				g.drawLine(paper.x+rmargin.x, paper.y+rmargin.y+y,
				        paper.x+rmargin.x+width, paper.y+rmargin.y+y);
				g.drawLine(paper.x+rmargin.x, paper.y+rmargin.y+y+1,
				        paper.x+rmargin.x+width, paper.y+rmargin.y+y+1);
			}
		}
	}

	class PaperList extends JComboBox implements ValueHolder
	{
		Vector papers;

		PaperList(Vector list)
		{
			/** get default papers  */
			super(list);
			setEditable(false);
			list.add(new NamedPaper("dialog.print.custom.paper", 0,0,"cm"));
			papers = list;
		}

		public Object getValue()
		{
			return (NamedPaper)getSelectedItem();
		}

		public void setValue(Object value)
		{
			int current = getSelectedIndex();
			for (int i=0; i<papers.size()-2; i++) {
				if (papers.get(i).equals(value)) {
					if (i!=current) setSelectedIndex(i);
					return;
				}
			}

			NamedPaper customPaper = (NamedPaper)papers.get(papers.size()-1);
			customPaper.setSize(value);
			setSelectedIndex(papers.size()-1);
		}

		public void setValue(double width, double height, int unit)
		{
			int current = getSelectedIndex();
			for (int i=0; i<papers.size()-2; i++) {
				if (((NamedPaper)papers.get(i)).equals(width,height,unit)) {
					if (i!=current) setSelectedIndex(i);
					return;
				}
			}

			NamedPaper customPaper = (NamedPaper)papers.get(papers.size()-1);
			customPaper.setSize(width,height,unit);
			setSelectedIndex(papers.size()-1);
		}


		PaperList()
		{
			this(NamedPaper.getDefaultPaperFormats());
		}
	}

	protected void updateFields()
	{
		boolean wasenabled = enableListener;
		try {
            enableListener = false;

			String width_str = Units.toString(paper().width);
			if (!width_str.equals(tWidth.getText()))
				tWidth.setText(width_str);

			String height_str = Units.toString(paper().height);
			if (!height_str.equals(tHeight.getText()))
				tHeight.setText(height_str);

			if (paper().unit!=unit.getUnit())
				unit.setUnit(paper().unit);

			String top_str = Units.toString(margins.top);
			if (!top_str.equals(tTop.getText()))
				tTop.setText(top_str);

			String bottom_str = Units.toString(margins.bottom);
			if (!bottom_str.equals(tBottom.getText()))
				tBottom.setText(bottom_str);

			String left_str = Units.toString(margins.left);
			if (!left_str.equals(tLeft.getText()))
				tLeft.setText(left_str);

			String right_str = Units.toString(margins.right);
			if (!right_str.equals(tRight.getText()))
				tRight.setText(right_str);

			pagePreview.repaint();

		} finally {
			enableListener = wasenabled;
		}
	}

	protected void textChanged()
	{
		if (enableListener) {
			try {
				enableListener = false;
				//  parse width,height, etc.
				double width = Units.parse(tWidth.getText());
				double height = Units.parse(tHeight.getText());

				paperList.setValue(width,height, paper().unit);

				margins.top = Units.parse(tTop.getText());
				margins.bottom = Units.parse(tBottom.getText());
				margins.left = Units.parse(tLeft.getText());
				margins.right = Units.parse(tRight.getText());

				pagePreview.repaint();

			} finally {
				enableListener = true;
			}
		}
	}

	protected void changeUnit(int oldUnit, int newUnit, boolean forceUpdate, boolean setValue)
	{
		if (oldUnit!=newUnit) {
			//  convert values to new unit
			double width = Units.convert(paper().width,oldUnit,newUnit);
			double height = Units.convert(paper().height,oldUnit,newUnit);

            if (setValue)
			    paperList.setValue(width,height,newUnit);

			margins.top = Units.convert(margins.top,oldUnit,newUnit);
			margins.bottom = Units.convert(margins.bottom,oldUnit,newUnit);
			margins.left = Units.convert(margins.left,oldUnit,newUnit);
			margins.right = Units.convert(margins.right,oldUnit,newUnit);

			updateFields();
		}
		else if (forceUpdate)
			updateFields();
	}

	public void stateChanged(ChangeEvent e)
	{
		super.stateChanged(e);

		if (e.getSource()==rPortrait || e.getSource()==rLandscape)
			pagePreview.repaint();
	}

	public void itemStateChanged(ItemEvent e)
	{
		if (enableListener)
			try {
				enableListener = false;

				if (e.getSource()==paperList && e.getStateChange()==ItemEvent.SELECTED)
				{
					//  convert from old unit to new unit
					NamedPaper np = (NamedPaper)paperList.getValue();
					changeUnit(unit.getUnit(), np.unit, true,false);
				}

				if (e.getSource()==unit)
				{
					NamedPaper np = (NamedPaper)paperList.getValue();
					changeUnit(np.unit, unit.getUnit(), false,true);
				}
			} finally {
				enableListener = true;

			}
	}

	public void changedUpdate(DocumentEvent e)
	{
		textChanged();
	}

	public void insertUpdate(DocumentEvent e)
	{
		textChanged();
	}

	public void removeUpdate(DocumentEvent e)
	{
		textChanged();
	}

	public void valueChanged(ListSelectionEvent e)
	{
        Element elm = exportList.getExportConfig();
        setOutputFormat(ExportConfig.getOutput(elm),true);
	}
}
