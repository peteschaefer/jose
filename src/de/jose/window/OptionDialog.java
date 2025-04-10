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
import de.jose.book.OpeningLibrary;
import de.jose.chess.TimeControl;
import de.jose.comm.Command;
import de.jose.comm.CommandAction;
import de.jose.comm.msg.DeferredMessageListener;
import de.jose.jo3d.Util3D;
import de.jose.plugin.EnginePlugin;
import de.jose.plugin.UciPlugin;
import de.jose.plugin.Plugin;
import de.jose.plugin.EngineOptionReader;
import de.jose.profile.UserProfile;
import de.jose.util.*;
import de.jose.util.file.ExecutableFileFilter;
import de.jose.util.file.FileUtil;
import de.jose.util.file.ImageFileFilter;
import de.jose.util.map.IntHashSet;
import de.jose.util.style.StyleUtil;
import de.jose.view.BoardPanel;
import de.jose.view.BoardView2D;
import de.jose.view.BoardView3D;
import de.jose.view.JoToolBar;
import de.jose.view.colorchooser.JoChessSurfaceButton;
import de.jose.view.colorchooser.JoSurfaceButton;
import de.jose.view.input.*;
import de.jose.view.style.FontList;
import de.jose.view.style.StyleChooser;
import de.jose.plugin.EnginePlugin.SearchType;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.text.BreakIterator;


public class OptionDialog
		extends JoTabDialog
		implements ItemListener, DocumentListener, ListSelectionListener, ChangeListener, DeferredMessageListener
/**
 * implements DeferredMessageListener makes sure that message from EngineOptionReader are handle
 * within the AWT event handling thread
 */
{
	private UserProfile profile;
	private Map oldValues;
	private StyleChooser theStyleChooser;
	private JComboBox timeControls;
	private PluginList pluginList;
	private JPanel pluginInfo;
	private JPanel pluginOptions;
	private JLabel optionWaitMessage;
	private JScrollPane pluginScroller;
	private TimeControl selectedControl;
	private Element pluginConfig;
	private OpeningBookList bookList;
	private EngineOptionReader engOptionReader;
	private de.jose.eboard.DialogComponent eboardCtrl;
	private Date lnfTouched;

	protected static String yellowIcon = ":#dddd00:#444444";
	protected static String greenIcon = ":#22cc00:#444444";
	//
 	//	Sort options by *Importance*
 	//
	private static String[] IMPORTANCE = {
				//	hidden options
				"UCI_ShowCurrLine", //  always disabled (TODO)
				"UCI_ShowRefutations",  //  always disabled (TODO)
				"UCI_AnalyseMode",   //  is automatically set when entering analysis mode
				"UCI_Opponent", //  not in use (TODO)
				"UCI_Chess960", //  FRC
				//	topmost options
				null,
				"UCI_EngineAbout",
				"Ponder", "OwnBook",
				"Random", "MultiPV",
				//"UCI_LimitStrength", "UCI_Elo",
				//	Scores and Info
				null,
				"ScoreType", "UCI_ShowWDL", "UCI_ShowMovesLeft",
				"VerboseMoveStats", "LogLiveStats",
				//	Leela settings
				null,
				"WeightsFile", "Threads", "Backend", "BackendOptions", "Hash", "ConfigFile",
				//	Contempt Settings
				null,
				"ContemptMode", "Contempt",
				"WDLCalibrationElo", "WDLEvalObjectivity", "WDLDrawRateReference",
				"DrawScore",
				//	Endgame Bases
				null,
				"SyzygyPath", "SyzygyProbeDepth", "Syzygy50MoveRule", "SyzygyProbeLimit",
				null,
	};

	private static String[] searchRadioValues = { "time.control", "time.fixed", "depth", "nodes" };

	private static int importance(String option, int inputOrder)
	{
		for(int i = 0; i < IMPORTANCE.length; i++)
			if (option.equals(IMPORTANCE[i]))
				return i;
		return 1000 + inputOrder;
	}

	private static int importanceGroup(String option)
	{
		int group = 0;
		for(int i = 0; i < IMPORTANCE.length; i++)
			if (IMPORTANCE[i]==null)
				group++;
			else if (option.equals(IMPORTANCE[i]))
				break;
		return group;
	}


	public OptionDialog(String name)
	{
		super(name, false);
		Dimension screensize = frame.getGraphicsConfiguration().getBounds().getSize();
		center(Math.min(screensize.width,760), Math.min(screensize.height,620));
        profile = AbstractApplication.theUserProfile;
/*
		ImageIcon user = ImgUtil.getIcon("nav","user.regular");
		ImageIcon board = ImgUtil.getIcon("nav","chess.board");
		ImageIcon palette = ImgUtil.getIcon("nav","palette.solid");
		ImageIcon clock = ImgUtil.getIcon("nav","chess.clock");
		ImageIcon book = ImgUtil.getIcon("nav","book.solid");
		ImageIcon gears = ImgUtil.getIcon("nav","gears.solid");
		ImageIcon font = ImgUtil.getIcon("nav","font.solid");
*/
		int iconSize = 20;
		Color iconColor= Color.lightGray;

		Icon user = FontUtil.awesomeIcon(BoardView2D.cUser,iconSize,iconColor);
		Icon board = FontUtil.awesomeIcon('\uf43c',iconSize,iconColor);
		Icon palette = FontUtil.awesomeIcon('\uf5c3',iconSize,iconColor); // swatchbook :)
		Icon clock = FontUtil.awesomeIcon(/*'\uf2f2'*/'\ue47e',iconSize,iconColor);
		//	todo alternative: FigurineSymbol \u201e
		Icon book = FontUtil.awesomeIcon(BoardView2D.cBook,iconSize,iconColor);
		Icon gears = FontUtil.awesomeIcon(BoardView2D.cGears,iconSize,iconColor);
		Icon cubes = FontUtil.awesomeIcon('\uf1b3',iconSize,iconColor);
		Icon font = FontUtil.awesomeIcon('\uf031',iconSize,iconColor);

		addTab(newGridPane(),user);   // tab0
		addTab(newGridPane(),board);   // tab1
		addTab(newGridPane(), palette);   // tab2
		addTab(newGridPane(),clock);   // tab3
        addTab(newGridPane(),gears);   // tab4
        addTab(newGridPane(),book);   // tab5
		addTab(newGridPane(),cubes);   // tab6
		addTab(newGridPane(),font);  //  tab7

		addButtons(OK_CANCEL);
		addSpacer(10);
		addButtons(APPLY_REVERT);
		addSpacer(10);
		addButton(HELP);

		if (!Version.hasJava3d(true,false))
			getTabbedPane().setEnabledAt(6,false);

		IntHashSet mnemonicsInUse = new IntHashSet();
		JoMenuBar.assignMnemonics(buttonPane,mnemonicsInUse);
		JoMenuBar.assignMnemonics(getTabbedPane(),mnemonicsInUse);

		Font fnt = getTabbedPane().getFont();
		fnt = fnt.deriveFont(Font.BOLD,13);
		getTabbedPane().setFont(fnt);

		rescaleFonts(elementPane);
		rescaleFonts(buttonPane);
	}

	protected void initTab0(Component comp0)
	{
		JPanel tab0 = (JPanel)comp0;
		JPanel sbox = newGridBox("dialog.option.user.title");

		addWithLabel(sbox,0,0,4, "user.name", new JTextField(24));
		addWithLabel(sbox,0,1,4, "user.language", new LanguageList(
						Language.getAvailableLanguages(Application.theApplication.theLanguageDirectory)));

		//  load recent games
		addWithLabel(sbox, 0,2,4, null, newCheckBox("doc.load.history"));
		//  classify by ECO
		addWithLabel(sbox, 0,3,4, null, newCheckBox("doc.classify.eco"));
		//  associate PGN files with jose
        if (Version.windows)
		    addWithLabel(sbox, 0,4,4, null, newCheckBox("doc.associate.pgn"));
        /**
         * hardcoded on Mac OS X (in Contents/Info.plist)
         * don't know how to do on Linux ?! 
         */
		addBox(tab0, 0,0,4, sbox);

		//  sound
		sbox = newGridBox("dialog.option.sound");

		FileInput sinput = newFileInputField("sound.moves.dir");
		sinput.setBaseDirectory(new File(Application.theWorkingDirectory,"sounds"));
		sinput.setDialogType(JFileChooser.OPEN_DIALOG);
		sinput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		addWithLabel(sbox, 0,0,4, "sound.moves.dir",sinput);

		addWithLabel(sbox, 0,1,4, null, newCheckBox("sound.moves.engine"));   //  speak engine moves
		addWithLabel(sbox, 0,2,4, null, newCheckBox("sound.moves.ack.user"));   //  acknowledge user moves
		addWithLabel(sbox, 0,3,4, null, newCheckBox("sound.moves.user"));   //  speak user moves

		addBox(tab0, 0,1,4, sbox);

		tab0.add(new JLabel(""), ELEMENT_REMAINDER);
	}

	protected void initTab1(Component comp1)
	{
		JPanel tab1 = (JPanel)comp1;
		JPanel abox = newGridBox("dialog.option.notation");

		FontList fontList = FontList.createDiagramFontList(20,true);
		fontList.setVisibleRowCount(9);
		fontList.setFixedCellHeight(21);

		JScrollPane fontScroller = new JScrollPane(fontList,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		fontScroller.setMinimumSize(new Dimension(80,5*21));
		fontScroller.setPreferredSize(new Dimension(80,9*21));
		//	Important: for FontList to fill available space, we need to set sizes it at the enclosing JScrollPane !!

		GridBagConstraints constr = (GridBagConstraints) ELEMENT_TWO.clone();
		constr.fill = GridBagConstraints.BOTH;
		constr.weighty = 4;
		abox.add(fontScroller, constr);
		fontList.setName("font.diagram");
		reg(fontList);

		//  write mode
		addWithLabel(abox, 0,1,4, "doc.write.mode", new WriteModeList());    //  insert move mode

		addBox(tab1, 0, 0, 4, abox);

		//  Animation
		JSlider aslider = AnimationDialog.createSlider("animation.speed");
/*
		aslider.setBorder(new CompoundBorder(
			new BevelBorder(BevelBorder.LOWERED),
		    new EmptyBorder(4,4,4,4)));
*/

		JPanel sbox = newGridBox("dialog.option.animation");

		addWithLabel(sbox, 0,0,4, null, aslider);
		addWithLabel(sbox, 1,1,3, "", newCheckBox("board.animation.hints"));    //  show hints

		addBox(tab1, 0,2,4, sbox);

		// 	Board options
		JPanel bbox = newGridBox("dialog.option.board");

		addWithLabel(bbox, 0,0,2, "", newCheckBox("board.flip", "menu.game.flip", null));
		addWithLabel(bbox, 2,0, 2, null, newCheckBox("board.evalbar", "menu.game.evalbar", null));

		addWithLabel(bbox, 0,1,2, "", newCheckBox("board.coords", "menu.game.coords", null));
		addWithLabel(bbox, 2,1,2, null, newCheckBox("board.suggestions", "menu.game.suggestions", null));

		addBox(tab1, 0,3,4,bbox);

		//	E-Board

		JPanel ebox = newGridBox("dialog.option.eboard");

		//addWithLabel(ebox, 0,0,1, null, );
		//addWithLabel(ebox, 1,1,1, null, newButton("eboard.connect"));

		ebox.add( newCheckBox("eboard.chessnut", "eboard.chessnut", null), 	gridConstraint(ELEMENT_ROW,0,0,3));

		//ebox.add( new JLabel("         "), 												gridConstraint(ELEMENT_ROW_SMALL,0,1,1));
//		ebox.add( newLabel("eboard.connected"), 											gridConstraint(LABEL_INDENTED,1,1,1));
//		ebox.add( newButton("eboard.connect","eboard.connect"), 				gridConstraint(LABEL_ONE_LEFT,2,1,1));
		eboardCtrl = new de.jose.eboard.DialogComponent(false);
		ebox.add(eboardCtrl, gridConstraint(ELEMENT_ROW_SMALL,0,1,1));


//		ebox.add( newLabel("eboard.orientation"), 										gridConstraint(LABEL_INDENTED,1,2,1));
//		ebox.add( oriCombo,																		gridConstraint(LABEL_ONE_LEFT, 2,2,2));
		if (eboardCtrl.eboard.isAvailable())
			addBox(tab1, 0,4,4, ebox);


		tab1.add(new JLabel(""), ELEMENT_REMAINDER);
	}

	protected void initTab2(Component comp2)
	{
		JPanel tab2 = (JPanel)comp2;

		JPanel sbox = newGridBox("dialog.option.board.title");

		addWithLabel(sbox,1, "board.surface.light", newChessSurfaceButton("board.surface.light",null));
		addWithLabel(sbox,1, "board.surface.dark", newChessSurfaceButton("board.surface.dark",null));
		addWithLabel(sbox,1, "board.surface.white", newChessSurfaceButton("board.surface.white",null));
		addWithLabel(sbox,1, "board.surface.black", newChessSurfaceButton("board.surface.black",null));
		addWithLabel(sbox,1, "board.surface.background", newChessSurfaceButton("board.surface.background",null));
		addWithLabel(sbox,1, "board.surface.coords", newChessSurfaceButton("board.surface.coords",null));

		addBox(tab2, 0,0,4, sbox);

		sbox = newGridBox("dialog.option.lnf.title");

		LookAndFeelList lookAndFeelList = new LookAndFeelList();
		JLabel label = (JLabel)addWithLabel(sbox,0,0,4, "ui.look.and.feel2", lookAndFeelList);
		label.setText(Language.get("dialog.option.ui.look.and.feel"));

		JoSurfaceButton sbutton1 = newChessSurfaceButton("lnf.select.color", null);
		sbutton1.setTexturesEnabled(false);
		sbutton1.setGradientsEnabled(false);

		JoSurfaceButton sbutton2 = newChessSurfaceButton("lnf.accent.color",null);
		sbutton2.setTexturesEnabled(false);
		sbutton2.setGradientsEnabled(false);

		JRadioButton radSystem = newRadioButton("accent.color.system");
		JRadioButton radUser = newRadioButton("accent.color.user");
		JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		radioPanel.add(reg(radSystem));
		radioPanel.add(reg(radUser));
		radSystem.addActionListener(this);
		radUser.addActionListener(this);

		add(sbox, radioPanel, ELEMENT_TWO);
		newButtonGroup("accent.color");

		radioPanel.setVisible(StyleUtil.supportsSystemAccentColors());

		add(sbox, sbutton1, ELEMENT_TWO);
		add(sbox, sbutton2, ELEMENT_TWO);
		add(sbox, newButton("lnf.theme.editor",null,null,this), ELEMENT_TWO);
		//sbox.add(newButton("lnf.theme.reload",null,null,this), ELEMENT_TWO);

		addBox(tab2, 0,1,4, sbox);

		tab2.add(new JLabel(""), ELEMENT_REMAINDER);
	}

	protected void initTab3(Component comp3)
	{
		JPanel tab3 = (JPanel)getTabbedPane().getComponentAt(3);

		timeControls = new JComboBox();
		timeControls.setEditable(true);
		timeControls.setSelectedIndex(-1);	//	no value selected
		timeControls.addItemListener(this);

		JTextComponent editor = (JTextComponent)timeControls.getEditor().getEditorComponent();
		editor.getDocument().addDocumentListener(this);

		JPanel sbox = newGridBox("dialog.option.time.control");
		add(sbox, "time.control.popup", timeControls, ELEMENT_ONE);
		add(sbox, null, new JLabel(""), ELEMENT_TWO);
		addBox(tab3, 0,0,4, sbox);

		Box box;
		for (int phase=0; phase < 3; phase++) {
			String prefix = "time.control."+phase;
			box = Box.createHorizontalBox();
			box.setBorder(new TitledBorder(Language.get("dialog.option.phase."+(phase+1))));
			JIntegerField ifield = newIntegerField(prefix+".moves");
			ifield.setTextValue(0, Language.get("dialog.option.all.moves"));
			ifield.setColumns(6);
			add(box, ifield, null);
			box.add(Box.createHorizontalStrut(8));
			box.add(newLabel("dialog.option.moves.in"));
			box.add(Box.createHorizontalStrut(8));
			add(box, newTimeField(prefix+".time"), null);
			box.add(Box.createHorizontalStrut(8));
			box.add(newLabel("dialog.option.increment"));
			box.add(Box.createHorizontalStrut(8));
			add(box, newTimeField(prefix+".increment"), null);
			box.add(Box.createHorizontalStrut(8));
			box.add(newLabel("dialog.option.increment.label"));
			box.add(Box.createHorizontalGlue());
			add(tab3, box, gridConstraint(ELEMENT_ROW,0,1+phase,2));
		}

		float iconSize = 22f;
		Icon addIcon 		= JoToolBar.create1AwesomeIcon("\uf0fe"+yellowIcon,iconSize);
		Icon removeIcon 	= JoToolBar.create1AwesomeIcon("\uf146"+yellowIcon,iconSize);

		box = Box.createHorizontalBox();
		add(box,newButton("time.control.new",addIcon,(String)null),null);
		add(box,newButton("time.control.delete",removeIcon,(String)null),null);
		add(tab3, box, gridConstraint(ELEMENT_ROW,0,4,2));

		tab3.add(new JLabel(""), ELEMENT_REMAINDER);
	}

	protected void initTab4(Component comp4)
	{
		JPanel tab4 = (JPanel)comp4;
        tab4.setLayout(new BoxLayout(tab4,BoxLayout.X_AXIS));

		JComponent box = createPluginListPanel();
		tab4.add(box);

		JPanel p1 = createPluginInfoPanel();
		JPanel p2 = createPluginSearchPanel();
		JPanel p4 = createPluginOptionsPanel();
		JPanel p3 = createMorePluginOptionsPanel();

		pluginInfo.add(p1, ELEMENT_ONE_ROW);
		pluginInfo.add(p2, ELEMENT_ONE_ROW);
		pluginInfo.add(p4, ELEMENT_ONE_ROW);
		pluginInfo.add(p3, ELEMENT_ONE_ROW);

		pluginScroller = new JScrollPane(pluginInfo,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		pluginScroller.getVerticalScrollBar().setUnitIncrement(20);

		tab4.add(pluginScroller);
	}

	private JPanel createPluginSearchPanel()
	{
		JPanel p2 = newGridBox("plugin.search.title");

		addWithLabel(p2, 0,0,2, null,
				reg(newRadioButton("plugin.search",searchRadioValues[SearchType.TIME_CONTROL.ordinal()])));

		JButton button = JoDialog.newLinkButton("time.control.link",  null, this);
		p2.add(button, ELEMENT_THREE_SMALL);

		JTimeField timeField = newTimeField("plugin.search.time.fixed.value");
		JIntegerField pliesField = newIntegerField("plugin.search.depth.value");
		JIntegerField nodesField = newIntegerField("plugin.search.nodes.value");

		//pliesField.setMinimumSize(timeField.getMinimumSize());
		//nodesField.setMinimumSize(timeField.getMinimumSize());

		addWithLabel(p2, 0,1,2, null,
				reg(newRadioButton("plugin.search",searchRadioValues[SearchType.TIME.ordinal()])));
		p2.add(reg(timeField), ELEMENT_THREE);

		GridBagConstraints suffix = (GridBagConstraints) ELEMENT_FOUR_SMALL.clone();

		addWithLabel(p2, 0,2,2, null,
				reg(newRadioButton("plugin.search",searchRadioValues[SearchType.DEPTH.ordinal()])));
		p2.add(reg(pliesField), ELEMENT_THREE);
		suffix.gridy=2;
		p2.add(newLabel("plugin.search.plies.suffix"), suffix);

		addWithLabel(p2, 0,3,2, null,
				reg(newRadioButton("plugin.search",searchRadioValues[SearchType.NODES.ordinal()])));

		p2.add(reg(nodesField), ELEMENT_THREE);
		suffix.gridy=3;
		p2.add(newLabel("plugin.search.nodes.suffix"), suffix);

		newButtonGroup("plugin.search");
		return p2;
	}

	private JPanel createPluginOptionsPanel() {
		pluginOptions = newGridPane();
		optionWaitMessage = newLabel("plugin.options.wait", JLabel.CENTER);
		optionWaitMessage.setVisible(false);

		JPanel p4 = newGridBox("plugin.options");
		p4.add(optionWaitMessage, ELEMENT_ONE_ROW);
		p4.add(pluginOptions, ELEMENT_REMAINDER);

		return p4;
	}

	private JPanel createMorePluginOptionsPanel()
	{
		//  filled dynamically
		JPanel p3 = new JPanel(new BorderLayout());
		p3.setBorder(new TitledBorder(Language.get("plugin.startup")));

		JTextArea moreOptions = newTextArea(4,20);
		moreOptions.setName("plugin.startup");
		reg(moreOptions);
		p3.add(moreOptions, BorderLayout.CENTER);

		return p3;
	}


	private JComponent createPluginListPanel()
	{
		//		pluginList = new JComboBox(EnginePlugin.getEngineNames(Version.osDir));
		pluginList = new PluginList();
		pluginList.addListSelectionListener(this);
		pluginList.setName("plugin.1");

//        addWithLabel(tab4,1, "plugin.1", pluginList);
		JScrollPane scroller = new JScrollPane(pluginList,
//		                                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
								        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		reg(pluginList);

		Box vbox = Box.createVerticalBox();
		Box hbox = Box.createHorizontalBox();

		float iconSize = 24f;
		Icon addIcon 		= JoToolBar.create1AwesomeIcon("\uf0fe"+yellowIcon,iconSize);
		Icon duplicateIcon 	= JoToolBar.create1AwesomeIcon("\uf24d"+greenIcon,iconSize);
		Icon removeIcon 	= JoToolBar.create1AwesomeIcon("\uf146"+yellowIcon,iconSize);
		JButton addButton 	= newButton("plugin.add",addIcon,(String)null);
		JButton duplButton 	= newButton("plugin.duplicate",duplicateIcon,(String)null);
		JButton delButton 	= newButton("plugin.delete",removeIcon,(String)null);

		Border border = new EmptyBorder(4,8,4,8);
		addButton.setBorder(border);
		duplButton.setBorder(border);
		delButton.setBorder(border);

		hbox.add(reg(addButton));
		hbox.add(reg(duplButton));
		hbox.add(reg(delButton));

		vbox.setMaximumSize(new Dimension(pluginList.getMaximumSize().width+20, Integer.MAX_VALUE));

		JCheckBox logoCheckbox = newCheckBox("plugin.show.logos");
		logoCheckbox.addChangeListener(this);

		vbox.add(hbox);
		vbox.add(scroller);

		hbox = Box.createHorizontalBox();
		hbox.add(reg(logoCheckbox));
		vbox.add(hbox);
		return vbox;
	}

	private JPanel createPluginInfoPanel()
	{
		pluginInfo = newGridPane();

		JPanel p1 = newGridBox("plugin.info");

		//  Engine Name, Version, Author
		addWithLabel(p1,1, "plugin.name", newTextField("plugin.name"));
		addWithLabel(p1,1, "plugin.version", newTextField("plugin.version"));
		addWithLabel(p1,1, "plugin.author", newTextField("plugin.author"));

		FileInput dirInput = newFileInputField("plugin.dir");
		FileInput exeInput = newFileInputField("plugin.exe");

		FileInput logoInput = newFileInputField("plugin.logo");

		dirInput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		exeInput.addFilter(new ExecutableFileFilter());
		logoInput.addFilter(new ImageFileFilter());

		addWithLabel(p1,1, "plugin.dir", dirInput);
		addWithLabel(p1,1, "plugin.exe", exeInput);
		addWithLabel(p1,1, "plugin.args", newTextField("plugin.args"));
		addWithLabel(p1,1, "plugin.logo", logoInput);

		//  protocol type (xboard or UCI)
		JRadioButton radUci = newRadioButton("plugin.protocol.xboard");
		JRadioButton radXboard = newRadioButton("plugin.protocol.uci");

		Box box1 = Box.createHorizontalBox();
		box1.add(reg(radUci));
		box1.add(reg(radXboard));
		p1.add(box1, ELEMENT_TWO);
		newButtonGroup("plugin.protocol");
		radUci.addItemListener(this);
		radXboard.addItemListener(this);
		return p1;
	}

	protected void initTab6(Component comp6)
	{
		if (!Version.hasJava3d(false,false)) {
			getTabbedPane().setEnabledAt(6,false);
			return;
		}

		JPanel tab6 = (JPanel)comp6;
		JPanel sbox = newGridBox("board.3d.title");

        File dir3d = new File(Application.theWorkingDirectory, "3d");
		addWithLabel(sbox, 0,0,2, "board.3d.model", new Model3dList(dir3d,Language.theLanguage.langCode,false));

        JCheckBox chkbox1, chkbox2;

		addWithLabel(sbox, 0,1,2, "board.3d.surface.frame", newSurfaceButton("board.3d.surface.frame",null));
		addWithLabel(sbox, 0,2,2, "board.3d.light.ambient", newColorButton("board.3d.light.ambient",null));
		addWithLabel(sbox, 0,3,2, "board.3d.light.directional", newColorButton("board.3d.light.directional",null));

		addWithLabel(sbox, 2,0,2, null, newCheckBox("board.3d.clock"));
        addWithLabel(sbox, 2,1,2, null, newCheckBox("board.hilite.squares"));

		addWithLabel(sbox, 2,2,2, null, newCheckBox("board.3d.shadow"));
		addWithLabel(sbox, 2,3,2, null, newCheckBox("board.3d.reflection"));

        addWithLabel(sbox, 0,4,2, null, chkbox2 = newCheckBox("board.3d.fsaa"));
        addWithLabel(sbox, 2,4,2, null, chkbox1 = newCheckBox("board.3d.anisotropic"));

		chkbox1.setEnabled(false);
		chkbox2.setEnabled(false);

		if (Version.hasJava3d(true, Application.theUserProfile.getBoolean("board.3d.ogl"))) {
			Object canvas3d = getCanvas3D();
			chkbox1.setEnabled(Util3D.hasAnisotropicFiltering(canvas3d));
			chkbox2.setEnabled(Util3D.hasFullScreenAntiAliasing(canvas3d));
		}

		//  Knight angle
//		tab5.add(tab5,1,newLabel("board.3d.knight.angle"));
		JSlider slider = createAngleSlider("board.3d.knight.angle");
		slider.addChangeListener(this);
		addWithLabel(sbox, 0,5,4, "board.3d.knight.angle", slider);

		if (Version.windows) {
			//  choose OpenGL on Windows. Default is DirectX
			//  there is no choice on Linux
			addWithLabel(sbox, 0,6,2, null, newCheckBox("board.3d.ogl"));
		}

		addBox(tab6,0,0,4,sbox);
        tab6.add(new JLabel(""), ELEMENT_REMAINDER);
	}


	private static JSlider createAngleSlider(String name)
	{
		JSlider slider = new JSlider(-180,+180);
		slider.setName(name);
		slider.setInverted(true);

		Dictionary labels = new Hashtable();
		labels.put(-180, new JLabel("-180\u00b0"));
		labels.put( -90, new JLabel("-90\u00b0"));
		labels.put(   0, new JLabel("0\u00b0"));
		labels.put( +90, new JLabel("+90\u00b0"));
		labels.put(+180, new JLabel("+180\u00b0"));

		slider.setLabelTable(labels);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setPaintTrack(true);
		slider.setMajorTickSpacing(45);
		slider.setMinorTickSpacing(15);
		return slider;
	}


	protected void initTab7(Component comp7)
	{
		JPanel tab7 = (JPanel)comp7;

		theStyleChooser = new StyleChooser();
		add(tab7, theStyleChooser, ELEMENT_NEXTROW_REMAINDER);
	}

	protected void initTab5(Component comp5)
	{
		JPanel tab5 = (JPanel)comp5;

		bookList = new OpeningBookList(Application.theApplication.theOpeningLibrary);
		bookList.addListSelectionListener(this);

		//  list of opening books
		JPanel sbox = newGridBox("book.list.title");
		add(sbox, "book.list", new JScrollPane(bookList),
		        gridConstraint(ELEMENT_ONE, 0,0,1, GridBagConstraints.BOTH));

		float iconSize = 24f;
		Icon addIcon 		= JoToolBar.create1AwesomeIcon("\uf0fe"+yellowIcon,iconSize);
		Icon removeIcon 	= JoToolBar.create1AwesomeIcon("\uf146"+yellowIcon,iconSize);
		Icon upIcon 		= JoToolBar.create1AwesomeIcon("\uf151"+yellowIcon,iconSize);
		Icon downIcon 		= JoToolBar.create1AwesomeIcon("\uf150"+yellowIcon,iconSize);

		//  buttons:
		JPanel buttonBox = newGridPane();
		buttonBox.add(reg(newButton("book.list.add",addIcon,(String)null)), ELEMENT_ONE_ROW);
		buttonBox.add(reg(newButton("book.list.remove",removeIcon,(String)null)), ELEMENT_ONE_ROW);
//		buttonBox.add(Box.createVerticalGlue());
//		buttonBox.add(reg(newButton("book.list.enable")), ELEMENT_ONE_ROW);
//		buttonBox.add(reg(newButton("book.list.disable")), ELEMENT_ONE_ROW);
		buttonBox.add(Box.createVerticalGlue());
		buttonBox.add(reg(newButton("book.list.up",upIcon,(String)null)), ELEMENT_ONE_ROW);
		buttonBox.add(reg(newButton("book.list.down",downIcon,(String)null)), ELEMENT_ONE_ROW);
		buttonBox.add(Box.createVerticalGlue());
		JButton downloadButton = newButton("book.list.download", "download");
		buttonBox.add(reg(downloadButton), ELEMENT_ONE_ROW);
		downloadButton.setVisible(false);

		add(sbox, "book.buttons", buttonBox,
				gridConstraint(LABEL_ONE, 1,0,1));
		addBox(tab5, 0,0, 4, sbox);

		//  engine play options
		Box engineBox = Box.createVerticalBox();
		engineBox.setBorder(
				new CompoundBorder(
					new TitledBorder(Language.get("book.engine.options")),
						new EmptyBorder(8,8,8,8)));
		//  When playing against a chess engine:
		engineBox.add(newLabel("book.engine.options.tip")); //, gridConstraint(ELEMENT_ONE,0,0,2));

		//  + radio button: OpeningLibrary.PREFER_GUI_BOOK
		engineBox.add(reg(newRadioButton("book.engine.prefer.gui"))); //, gridConstraint(ELEMENT_ONE_ROW,0,1,2));
		//          use GUI books, engine book as fall back
//		engineBox.add(newLabel("book.engine.prefer.gui.tip"),
//							gridConstraint(LABEL_TWO,1,1,2));

		//  + radio button: OpeningLibrary.PREFER_ENGINE_BOOK
		engineBox.add(reg(newRadioButton("book.engine.prefer.engine")));//, gridConstraint(ELEMENT_ONE_ROW,0,2,2));
		//          use engine book, if there is one. Otherwise use GUI books
//		engineBox.add(newLabel("book.engine.prefer.engine.tip"),
//							gridConstraint(LABEL_TWO,1,2,2));

		//  + radio button: OpeningLibrary.GUI_BOOK_ONLY
		engineBox.add(reg(newRadioButton("book.engine.gui.only")));//, gridConstraint(ELEMENT_ONE_ROW,0,3,2));
		//          only use GUI book, disable engine book
//		engineBox.add(newLabel("book.engine.gui.only.tip"),
//							gridConstraint(LABEL_TWO,1,3,2));

		//  + radio button: OpeningLibrary.NO_BOOK
		engineBox.add(reg(newRadioButton("book.engine.no.book")));//, gridConstraint(ELEMENT_ONE_ROW,0,4,2));
		//          use neither GUI book, nor engine book
//		engineBox.add(newLabel("book.engine.no.book.tip"),
//							gridConstraint(LABEL_TWO,1,4,2));

		newButtonGroup("book.engine");

		add(tab5, "book.engine.box", engineBox,
				gridConstraint(ELEMENT_ROW, 0,1,2));

		//  collect mode:
		//  +   radio button: OpeningLibrary.COLLECT_FIRST
		//          collect moves from one book
		//  +   radio button: OpeningLibrary.COLLECT_ALL
		//          collect moves from all books

		//  when selecting an engine move from the book:
		//  +   radio button: OpeningLibrary.SELECT_IMPLEMENTATION
		//          let book implementation select a move
		//  +   radio button: OpeningLibrary.SELECT_GAME_COUNT
		//          probability based on number of played games
		//  +   radio button: OpeningLibrary.SELECT_RESULT_RATIO
		//          probability based on results
		//  +   radio button: OpeningLibrary.SELECT_DRAW_RATIO
		//          probability base on number of draws
		//  +   radio button: OpeningLibrary.SELECT_EQUAL
		//          select each move with equal probability

		tab5.add(new JLabel(""), ELEMENT_REMAINDER);
	}

	public void setupActionMap(Map<String, CommandAction> map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				addTimeControl();
			}
		};
		map.put("time.control.new", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				deleteTimeControl();
			}
		};
		map.put("time.control.delete", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				int speed = Util.toint(cmd.data);
				boolean hints = Util.toboolean(cmd.moreData);
				//  animation will be adjust by Application
				setValue("animation.speed",speed);
				setValue("board.animation.hints",hints);
			}
		};
		map.put("change.animation.settings",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				//  create new plugin configuration
				//  ask user for path to executable
				File[] dirs = {
					(File)Application.theUserProfile.get("filechooser.exe.dir"),
					new File(Application.theWorkingDirectory,"engines")
				};
				JoFileChooser chooser = JoFileChooser.forExecutable(dirs);
				if (chooser.showOpenDialog(JoFrame.getActiveFrame()) != JFileChooser.APPROVE_OPTION)
		            return; //  cancelled

		        File exeFile = chooser.getSelectedFile();
		        Application.theUserProfile.set("filechooser.exe.dir", chooser.getCurrentDirectory());

				//  LENGHTY OPERATION: MOVE TO SEPARATE THREAD
				if (engOptionReader!=null) engOptionReader.stop();
				engOptionReader = new EngineOptionReader(OptionDialog.this);
				optionWaitMessage.setVisible(true);
				engOptionReader.createNewConfig(exeFile);
				//  will call back to handleMessage !!
			}
		};
		map.put("plugin.add",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				getTabbedPane().setSelectedIndex(3);
			}
		};
		map.put("time.control.link",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				try {
					//  duplicate plugin configuration
					Element cfg = EnginePlugin.duplicateConfig(pluginConfig);
					//  mark dirty
					Config.setDirtyElement(cfg,true);
					//  assign a unique name/Id and insert in list
					pluginList.addNewConfig(cfg);
				} catch (IOException ioex) {
					showErrorDialog(ioex.getLocalizedMessage());
				}
			}
		};
		map.put("plugin.duplicate",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				//  delete plugin configuration
				if (pluginList.countElements() > 1)
					pluginList.deleteSelected();
			}
		};
		map.put("plugin.delete",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				//  add new book file
				File[] preferredDirs = new File[] {
				    (File) Application.theUserProfile.get("filechooser.book.dir"),
				    new File(Application.theWorkingDirectory, "books"),
				    Application.theWorkingDirectory,
				};

				int[] preferredFilters = new int[] {
						Application.theUserProfile.getInt("filechooser.book.filter"),
						JoFileChooser.BOOK,
						JoFileChooser.POLYGLOT_BOOK,
						JoFileChooser.CRAFTY_BOOK,
//						JoFileChooser.SHREDDER_BOOK,
				};

				JoFileChooser chooser = JoFileChooser.forOpenBook(preferredDirs, preferredFilters);
				if (chooser.showOpenDialog(JoFrame.getActiveFrame()) != JFileChooser.APPROVE_OPTION)
		            return; //  cancelled

		        File[] files = chooser.getSelectedFiles();
		        Application.theUserProfile.set("filechooser.book.dir", chooser.getCurrentDirectory());
				Application.theUserProfile.set("filechooser.book.filter", chooser.getCurrentFilter());

				addBooks(files);
			}
		};
		map.put("book.list.add",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				bookList.removeSelected();
			}
		};
		map.put("book.list.remove",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				bookList.moveSelected(-1);
			}
		};
		map.put("book.list.up",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				bookList.moveSelected(+1);
			}
		};
		map.put("book.list.down",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				//  reset all UCI options to their default values
				if (pluginConfig!=null)
				try {
					//  LENGHTY OPERATION - MOVE TO SEPARATE THREAD
					if (engOptionReader!=null) engOptionReader.stop();
					engOptionReader = new EngineOptionReader(OptionDialog.this);
					optionWaitMessage.setVisible(true);
					engOptionReader.setDefaultConfig(pluginConfig);
					//  will call back to handleMessage !!
				} catch (IllegalArgumentException isex) {
		            JoDialog.showErrorDialog(frame, Language.get("error.bad.uci"));
	            }
			}
		};
		map.put("dialog.option.plugin.default",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				openThemeEditor();
			}
		};
		map.put("lnf.theme.editor",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				//  notification from style editor
				if (cmd.data==OptionDialog.this) return; //  was broadcasted by ourself

//				boolean sizeModified = Util.toboolean(cmd.data);
				if (theStyleChooser!=null)
					theStyleChooser.revert(profile.getStyleContext(), profile.getInt("doc.move.format"), false);
			}
		};
		map.put("styles.modified",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				boolean dark = (Boolean)cmd.moreData;
				if (theStyleChooser!=null)
					theStyleChooser.updateUI(dark);
				//	forward to JoDialog
				SwingUtilities.updateComponentTreeUI(OptionDialog.this.frame);
			}
		};
		map.put("update.ui",action);




		action = new CommandAction() {
			@Override
			public void Do(Command cmd) throws Exception {
				OptionDialog.super.getTabbedPane().setSelectedIndex(4);
			}
		};
		map.put("plugin.search.time.control.link",action);
	}

	private void openThemeEditor()
	{
		lnfTouched = new Date();
		String[] args = new String[] {
				"jre/bin/java",
				"-jar", "lib/plaf/flatlaf-theme-editor.jar",
				"config/themes" };
        try {
            Runtime.getRuntime().exec(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //com.formdev.flatlaf.themeeditor.FlatLafThemeEditor.main( args );
		//	would work too, but hangs up sooner or later
	}

	public void addBooks(File[] files)
	{
		for (int i=0; i < files.length; i++)
			bookList.add(files[i]);
	}

	/**
	 * @return the current Canvas3D (declared as object so that we need not import it !)
	 */
    protected Object getCanvas3D()
    {
        BoardPanel bp = Application.theApplication.boardPanel();
        if (bp==null) return null;
        BoardView3D view = bp.get3dView();
        if (view==null)
            return null;
        else
            return view.getCanvas();
    }

    public final JoSurfaceButton newChessSurfaceButton(String name, Icon icon)
    {
        JoSurfaceButton button = new JoChessSurfaceButton(profile.settings,name);
        button.setName(name);
        button.setActionCommand(name);
        button.addActionListener(this);
        button.setText(Language.get(name));
        button.setToolTipText(Language.getTip(name));
        button.setIcon(icon);
        reg(button);
        return button;
    }

	public void readTab0()
    {
        read(0,profile.settings);
    }

	public void readTab1()  {
		read(1,profile.settings);

		setValueByName("font.diagram", profile.getString("font.diagram"));
		setValueByName("eboard.chessnut", profile.getBoolean("eboard.chessnut",false));
		//eboardCtrl.eboard.readProfile(profile);
	}

	public void readTab2()  {
		read(2,profile.settings);

		if (StyleUtil.supportsSystemAccentColors()) {
			String accent = profile.getString("accent.color", "system");
			useSystemAccentColors(accent.equals("system"));
		}
		else {
			useSystemAccentColors(false);
		}
	}

	private void useSystemAccentColors(boolean system)
	{
		JoSurfaceButton button1 = (JoSurfaceButton) getElement("lnf.select.color");
		JoSurfaceButton button2 = (JoSurfaceButton) getElement("lnf.accent.color");

		if (system) {
			button1.setColor(StyleUtil.getSystemSelectionColor());
			button2.setColor(StyleUtil.getSystemAccentColor());
		}
		//	else: retrieve from profile
		button1.setEnabled(!system);
		button2.setEnabled(!system);

		JRadioButton radsys = (JRadioButton)getElement("accent.color.system");
		JRadioButton radusr = (JRadioButton)getElement("accent.color.user");

		if (radsys!=null) radsys.setSelected(  system);
		if (radusr!=null) radusr.setSelected(! system);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("accent.color.system"))
			useSystemAccentColors(true);
		if (e.getActionCommand().equals("accent.color.user"))
			useSystemAccentColors(false);

		super.actionPerformed(e);
	}

	public void readTab3()  {
		read(3,profile.settings);

		Vector<TimeControl> controls = profile.getTimeControls();
		controls = (Vector<TimeControl>) ListUtil.deepClone(controls);
		timeControls.setModel(new DefaultComboBoxModel(controls));
		int idx = profile.getTimeControlIdx();
		selectedControl = profile.getTimeControl();
		timeControls.setSelectedIndex(idx);
		setTimeControl(selectedControl);
		//	eventually trigger itemStateChanged
		enableButton("time.control.delete",controls.size() > 1);
	}

	public void readTab4()
	{
		read(4,profile.settings);

		/** explicit setting of plugin.show.logos, becuase the DEFAULT vaue is TRUE */
		boolean showLogos = profile.getBoolean("plugin.show.logos",true);
		JCheckBox cb = (JCheckBox)getElement("plugin.show.logos");
		if (showLogos != cb.isSelected()) cb.setSelected(showLogos);

		pluginList.showLogo(showLogos);
		pluginList.setPlugins(EnginePlugin.getPlugins());
		pluginConfig = pluginList.getSelectedConfig();

		if (pluginConfig != null)
			try {
				readPluginInfo(pluginConfig,false);
			} catch (FileNotFoundException fnex) {
				//  ignore for now
				//  JoDialog.showErrorDialog(frame,"file not found: "+fnex.getMessage());
			}

		String current = EnginePlugin.getPluginId(profile.getString("plugin.1"), Version.osDir, true);
		if (current==null) current = EnginePlugin.getPluginId(profile.getString("plugin.1"), Version.osDir, false);
		if (current==null) current = Plugin.getDefaultPluginName(Version.osDir);

		if (!pluginList.setPlugin(current)) {
			//  plugin not available anymore (deleted?, wrong platform?)
			String defaultValue = EnginePlugin.getDefaultPluginName(Version.osDir);
			if (!pluginList.setPlugin(defaultValue)) {
				//  still not available ?? select first
				pluginList.setSelectedIndex(0);
			}
		}

		adjustFileInputs((File)getValueByName("plugin.dir"));

		/*
		Element searchConfig = EnginePlugin.getSearchControls(pluginConfig);
		SearchType searchType = EnginePlugin.getSelectedSearchControl(searchConfig);

		setValueByName("plugin.search.time.control", searchType==SearchType.TIME_CONTROL);
		setValueByName("plugin.search.time.fixed", searchType==SearchType.TIME);
		setValueByName("plugin.search.depth", searchType==SearchType.DEPTH);
		setValueByName("plugin.search.nodes", searchType==SearchType.NODES);

		setValueByName("plugin.search.time.fixed.value", EnginePlugin.getSearchControlArgument(searchConfig,SearchType.TIME));
		setValueByName("plugin.search.depth.value", EnginePlugin.getSearchControlArgument(searchConfig,SearchType.DEPTH));
		setValueByName("plugin.search.nodes.value", EnginePlugin.getSearchControlArgument(searchConfig,SearchType.NODES));
		 */
	}

    protected void adjustFileInputs(File dir)
    {
        FileInput exeInput = (FileInput)getElement("plugin.exe");
        FileInput logoInput = (FileInput)getElement("plugin.logo");

        exeInput.setBaseDirectory(dir);
        logoInput.setBaseDirectory(dir);
    }

	public void readTab6()  {
		read(6,profile.settings);
		//  TODO scroll selected entry to visible
	}

	public void readTab7()  {
		read(7,profile.settings);

		if (theStyleChooser.revert(profile.getStyleContext(),profile.getInt("doc.move.format"),false)) {
			//  first time; expand some nodes
			theStyleChooser.expand("body");
			theStyleChooser.expand("body.line");
		}
	}

	public void readTab5()
	{
		read(5,profile.settings);

		bookList.updateListData(Application.theApplication.theOpeningLibrary);

		switch (Application.theApplication.theOpeningLibrary.engineMode)
		{
		default:
		case OpeningLibrary.PREFER_GUI_BOOK:
			setValueByName("book.engine","prefer.gui");
			break;
		case OpeningLibrary.PREFER_ENGINE_BOOK:
			setValueByName("book.engine","prefer.engine");
			break;
		case OpeningLibrary.GUI_BOOK_ONLY:
			setValueByName("book.engine","gui.only");
			break;
		case OpeningLibrary.NO_BOOK:
			setValueByName("book.engine","no.book");
			break;
		}

		adjustBookButtons();
	}

	public void read() throws Exception
	{
		super.read();

		oldValues = (Map)profile.settings.clone();
	}


	public boolean save() throws Exception
	{
		boolean styleDirty = false;
		byte engineDirty = EnginePlugin.OPTIONS_CLEAN;       //  1 = options dirty, 2 = restart required, 3 = new plugin
        int currentTimeControl = profile.getTimeControlIdx();

		List errors = save(profile.settings);
		if (errors!=null) {
			//  parse error ?
			Throwable ex = (Throwable)errors.get(0);
			JoDialog.showErrorDialog(ex.getLocalizedMessage());
			return false;
		}

       	if (isInited(1))
		{
			profile.set("font.diagram", getValueByName("font.diagram"));
			profile.set("eboard.chessnut", getBooleanValue("eboard.chessnut"));
			eboardCtrl.eboard.storeProfile(profile);
		}

		if (isInited(3))
		{
			profile.set("time.controls", getTimeControls());
			currentTimeControl = timeControls.getSelectedIndex();
		}

		if (isInited(4))
		{
			//  store plugin config in XML files
			if (pluginConfig!=null)
				try {
					readPluginInfo(pluginConfig,true);
				} catch (FileNotFoundException fnfex) {
			        JoDialog.showErrorDialog(frame,"file not found: "+fnfex.getMessage());
					return false;
		        }

            engineDirty = 0;
			if (profile.changed("plugin.1",oldValues))
				engineDirty |= EnginePlugin.OPTIONS_NEW_ENGINE;    //  new plugin

			pluginConfig = pluginList.getSelectedConfig();    //  might be invalidated, now

			//	write changes to disk
			engineDirty |= EnginePlugin.updateDirtyElements((PluginListModel)pluginList.getModel(),
					Application.theApplication.getEnginePlugin());
		}

		if (isInited(7))
		{
			styleDirty = styleDirty || theStyleChooser.isDirty();
			profile.set("doc.panel.antialias", true);	//	@deprecated
			profile.set("doc.move.format",theStyleChooser.getMoveFormat());
		}

		if (isInited(5))
		{
//			Application.theApplication.theOpeningLibrary.open(profile);
			String engineMode = (String)getValueByName("book.engine");
			if ("prefer.gui".equalsIgnoreCase(engineMode))
				Application.theApplication.theOpeningLibrary.engineMode = OpeningLibrary.PREFER_GUI_BOOK;
			else if ("prefer.engine".equalsIgnoreCase(engineMode))
				Application.theApplication.theOpeningLibrary.engineMode = OpeningLibrary.PREFER_ENGINE_BOOK;
			else if ("gui.only".equalsIgnoreCase(engineMode))
				Application.theApplication.theOpeningLibrary.engineMode = OpeningLibrary.GUI_BOOK_ONLY;
			else if ("no.book".equalsIgnoreCase(engineMode))
				Application.theApplication.theOpeningLibrary.engineMode = OpeningLibrary.NO_BOOK;

			Application.theApplication.theOpeningLibrary.setEntries(bookList.getEntries());
			Application.theApplication.theOpeningLibrary.store(profile);
		}

		//  apply to application
		try {
			if (profile.changed("user.language", oldValues))
				Application.theApplication.setLanguage(profile.getString("user.language"));
			if (profile.changed("ui.look.and.feel2", oldValues))
				Application.theApplication.setLookAndFeel(profile.getString("ui.look.and.feel2"));
			if (profile.changed("lnf.accent.color", oldValues) ||
				profile.changed("lnf.select.color", oldValues) ||
				profile.changed("accent.color", oldValues) ||
				(lnfTouched!=null) && FileUtil.wasFileTouched(new File("config/themes"),lnfTouched))
				Application.theApplication.resetLookAndFeel();
			if (profile.changed("font.diagram",oldValues) ||
			    profile.changed("board.surface.light",oldValues) ||
				profile.changed("board.surface.dark",oldValues) ||
				profile.changed("board.surface.white",oldValues) ||
				profile.changed("board.surface.black",oldValues) ||
				profile.changed("board.surface.background",oldValues) ||
				profile.changed("board.flip",oldValues) ||
				profile.changed("board.coords",oldValues) ||
				profile.changed("board.evalbar",oldValues) ||
				profile.changed("board.suggestions",oldValues) ||
                profile.changed("board.3d.clock",oldValues) ||
				profile.changed("board.3d.surface.frame",oldValues) ||
				profile.changed("board.3d.shadow",oldValues) ||
				profile.changed("board.3d.reflection",oldValues) ||
                profile.changed("board.3d.anisotropic",oldValues) ||
                profile.changed("board.3d.fsaa",oldValues) ||
				profile.changed("board.3d.light.ambient",oldValues) ||
				profile.changed("board.3d.light.directional",oldValues) ||
			    profile.changed("board.3d.knight.angle",oldValues) ||
                profile.changed("board.hilite.squares",oldValues) ||
				profile.changed("board.3d.model",oldValues) ||
			    profile.changed("board.animation.hints",oldValues) ||
			    styleDirty)
				if (Application.theApplication.boardPanel() != null)
					Application.theApplication.boardPanel().updateProfile(profile);
			if (profile.changed("board.surface.background",oldValues))
				if (Application.theApplication.clockPanel() != null)
					Application.theApplication.clockPanel().updateColors();
			if (profile.changed("doc.panel.tab.placement",oldValues) ||
				profile.changed("doc.pabel.tab.layout",oldValues))
				if (Application.theApplication.docPanel() != null)
					Application.theApplication.docPanel().updateFromProfile(profile);
			if (profile.changed("doc.associate.pgn",oldValues))
			{
				Object oldAssoc = profile.get("doc.associate.old");
				oldAssoc = WinUtils.associateFileExtension("pgn","jose.exe");
				profile.set("doc.associate.old",oldAssoc);
				//  keep info about existing file association, so that we can restore it later
			}
			if (profile.changed("doc.move.format",oldValues) || styleDirty)
			{
				if (theStyleChooser!=null) {
					theStyleChooser.apply(profile.getStyleContext(),false);
					Application.theApplication.broadcast(new Command("styles.modified",null,OptionDialog.this,Boolean.TRUE));
				}
			}

            if (Util.allOf(engineDirty,EnginePlugin.OPTIONS_DIRTY)) {
				//  modify engine options w/out restarting the engine
				EnginePlugin plugin = Application.theApplication.getEnginePlugin();
				if (plugin!=null) {
					if (plugin.restartRequired())
						engineDirty |= EnginePlugin.OPTIONS_RESTART;
					else {
						plugin.setOptions(true);
						Application.theApplication.broadcast("change.engine.settings");
						//  notify engine panel (and other interested parties)
					}
				}
            }

            if (Util.allOf(engineDirty,EnginePlugin.OPTIONS_NEW_ENGINE))    //  ask user before starting new engine
                Application.theApplication.askSwitchPlugin();
            else if (Util.allOf(engineDirty,EnginePlugin.OPTIONS_RESTART))  //  engine restart is required (or is it ?)
				Application.theApplication.askRestartPlugin();

			if (currentTimeControl!=profile.getTimeControlIdx()) {
				Command cmd = new Command("menu.game.time.control",null,currentTimeControl);
				Application.theCommandDispatcher.forward(cmd,Application.theApplication);
			}
			if (profile.changed("sound.moves.dir",oldValues))
			try {
				Application.theApplication.updateSoundFormatter(profile);
			} catch (IOException ioex) {
				JoDialog.showErrorDialog(ioex.getLocalizedMessage());
			}

			if (profile.changed("animation.speed",oldValues) ||
			    profile.changed("board.animation.hints",oldValues))
				Application.theApplication.broadcast(
				                    new Command("change.animation.settings",null,
				                    profile.get("animation.speed"), profile.get("board.animation.hints")));


		} catch (Exception ex) {
			Application.error(ex);
			return false;
		}

		return true;
	}

	public void itemStateChanged(ItemEvent e)
	{
		Component src = (Component)e.getSource();
		//	called when am item is selected in the time controls ComboBox
		if (src==timeControls)
		{
			TimeControl control = (TimeControl)e.getItem();
			if (control!=null)
				switch (e.getStateChange()) {
				case ItemEvent.SELECTED:
					//	update input fields
					if (selectedControl!=null) saveTimeControl(selectedControl);
					setTimeControl(selectedControl=control);
					break;
				case ItemEvent.DESELECTED:
					//	commit changes
//					saveTimeControl(control);
					break;
				}
		}

		if (src.getName()!=null && src.getName().startsWith("plugin.protocol"))
		try {
			/**
			 * toggling a radio button will fire two change events; one for deselection
			 * and one for selection. we need to listen to just one of them
			 */

			if ((e.getStateChange()==ItemEvent.DESELECTED) && (pluginConfig!=null))
			{
				String oldProt = EnginePlugin.getProtocol(pluginConfig);
				String newProt = (String)getValueByName("plugin.protocol");

				if (!Util.equals(oldProt,newProt))
				{
					try {
						readPluginInfo(pluginConfig,oldProt,false);   //  retrieve current values before switching
					} catch (FileNotFoundException e1) {
						//  ignore for now; check again on save()
					}

					if (newProt.equals("uci"))
						EnginePlugin.setUci(pluginConfig);
					else
						EnginePlugin.setXBoard(pluginConfig);

					createPluginInfo(pluginConfig);
					setPluginInfo(pluginConfig);

					Config.setDirtyElement(pluginConfig,true);
				}
			}

		} catch (IOException e1) {
			Application.error(e1);
		}
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getSource()==pluginList)
        try {
			Element cfg = pluginList.getSelectedConfig();
            if (cfg!=null) showPluginInfo(cfg);
        } catch (IOException e1) {
            Application.error(e1);
       }

		if (e.getSource()==bookList)
			adjustBookButtons();
	}

	private void adjustBookButtons()
		{
			boolean hasSelection = ! bookList.getSelectionModel().isSelectionEmpty();
			enableButton("book.list.add",true);
		enableButton("book.list.download",true);
			enableButton("book.list.remove",hasSelection);
			enableButton("book.list.up",hasSelection);
			enableButton("book.list.down",hasSelection);
		}

	public void stateChanged(ChangeEvent evt)
	{
		super.stateChanged(evt);    //  essential for Tab Dialog

		JCheckBox cb = (JCheckBox)getElement("plugin.show.logos");
		if (evt.getSource()==cb)
		{
			pluginList.showLogo(cb.isSelected());
			pluginList.repaint();
		}
		JSlider slider = (JSlider)getElement("board.3d.knight.angle");
		if (evt.getSource()==slider)
		{
			BoardPanel bpanel = Application.theApplication.boardPanel();
			if (bpanel!=null) {
				BoardView3D view3d = bpanel.get3dView();
				if (view3d != null) {
					int angle = slider.getValue();
					view3d.rotateKnights(angle);
					view3d.refresh();
				}
			}
		}
	}

	protected void setTimeControl(TimeControl control)
	{
//		TimeControl control = (TimeControl)timeControls.getModel().getElementAt(idx);
		for (int phase=0; phase < 3; phase++) {
			String prefix = "time.control."+phase;
			if (phase < control.countPhases()) {
				setValueByName(prefix+".moves", 	control.getMoves(phase));
				setValueByName(prefix+".time", 		control.getTime(phase));
				setValueByName(prefix+".increment",	control.getIncrement(phase));
			}
			else {
				setValueByName(prefix+".moves", 	null);
				setValueByName(prefix+".time", 		null);
				setValueByName(prefix+".increment",	null);
			}
		}
	}

    protected void createPluginInfo(Element cfg)
    {
		pluginOptions.removeAll();

        if (EnginePlugin.isXBoard(cfg)) {
	        //  pondering
	        add(pluginOptions,1, "plugin.option.Ponder", newCheckBox("plugin.option.Ponder"));
            add(pluginOptions,1, "plugin.option.Random", newCheckBox("plugin.option.Random"));

	        pluginOptions.revalidate();
        }
        if (EnginePlugin.isUci(cfg)) {
	        /** Vector<UciPlugin.Option>    */
	        AWTUtil.setWaitCursor(frame);
	        try {
		        //  LENGTHY OPERATION: MOVE TO SEPARATE THREAD
		        if (engOptionReader!=null) engOptionReader.stop();
		        engOptionReader = new EngineOptionReader(this);
		        optionWaitMessage.setVisible(true);
		        engOptionReader.readOptions(cfg);
		        //  will call back to handleMessage !!!
	        } catch (IllegalArgumentException isex) {
		        JoDialog.showErrorDialog(frame, Language.get("error.bad.uci"));
	        }
        }
    }

	protected void updatePluginInfo()
	{
		//  update option labels when language changes
		for (int i=pluginOptions.getComponentCount()-1; i >= 0; i--)
		{
			Component comp = pluginOptions.getComponent(i);
			String name = comp.getName();
			if (name!=null && name.startsWith("plugin.option."))
				Language.update(comp, name.substring(14));
		}
	}

	protected String makeHtmlLabel(String text)
	{
		StringBuffer buf = new StringBuffer(text);
		//  insert some "reasonable" line breaks
		BreakIterator it = BreakIterator.getLineInstance();
		it.setText(text);
		int j = text.length();
		for(int i = it.last(); i > 0; i = it.previous())
		{
			if (i > 12 && i < (j-12))
				buf.insert(j=i,"<br>");
		}
		buf.insert(0,"<html>");
		return buf.toString();
	}

	protected void createUciOption(UciPlugin.Option option)
	{
		String compName = "plugin.option."+option.name;
		String compTitle = Language.get(compName, option.name);
//		compTitle = makeHtmlLabel(compTitle);
		JComponent component = null;

		switch (option.type)
		{
		case UciPlugin.CHECKBOX:
			JCheckBox checkbox = newCheckBox(compName);
			checkbox.setSelected(option.defaultBooleanValue());
			checkbox.setText(compTitle);
			component = checkbox;
			compTitle = "";
			break;

		case UciPlugin.SPIN:
			component = new UciSpinner(option.minValue, option.maxValue, option.defaultIntValue());
			break;

		case UciPlugin.COMBO:
			JComboBox<String> combo = new JComboBox<String>(option.values);
			combo.setSelectedItem(option.defaultValue);

			component = combo;
			break;

		case UciPlugin.BUTTON:
			/** buttons are not shown ! */
			return;

		default:
		case UciPlugin.STRING:
			if (UciPlugin.isFileOption(option.name)) {
				FileInput file = new FileInput(null,JFileChooser.OPEN_DIALOG,JFileChooser.FILES_ONLY);
				file.setColumns(Math.max(option.maxValue,16));
				file.setFile(option.defaultValue);

				component = file;
			}
			else if (UciPlugin.isDirectoryOption(option.name)) {
				FileInput file = new FileInput(null,JFileChooser.OPEN_DIALOG,JFileChooser.DIRECTORIES_ONLY);
				file.setColumns(Math.max(option.maxValue,16));
				file.setFile(option.defaultValue);

				component = file;
			}
            else if(UciPlugin.isReadOnly(option.name)) {
                JoStyledLabel label = new JoStyledLabel(option.defaultValue);
                label.setFixedWidth(200);

                component = label;
            }
			else {
				JTextField text = newTextField(compName);
				text.setColumns(Math.max(option.maxValue,16));
				text.setText(option.defaultValue);

				component = text;
			}
			component.setMinimumSize(new Dimension(40,20));
			component.setPreferredSize(new Dimension(80,20));
			break;
		}

		component.setName(compName);
		reg(component);

		if (compTitle!=null) {
			JoStyledLabel label = new JoStyledLabel(compTitle);
            label.setFixedWidth(100);

			if (compTitle.length() > 0) label.setName(component.getName());
			pluginOptions.add(label, LABEL_ONE);
			pluginOptions.add(component, ELEMENT_TWO_SMALL);
		}
		else {
			pluginOptions.add(component, ELEMENT_ONE_ROW);
		}
	}

    protected void setPluginInfo(Element cfg) throws IOException
    {
        /** fill in values  */
        String os = Version.osDir;

        setValueByName("plugin.name", EnginePlugin.getName(cfg));

        setValueByName("plugin.version", EnginePlugin.getVersion(cfg,os));
        setValueByName("plugin.author", EnginePlugin.getAuthor(cfg));

        File dir = EnginePlugin.getDir(cfg,os);
        File exe = EnginePlugin.getExecutable(cfg,os,dir);
        File logo = EnginePlugin.getLogo(cfg);
	    String args = EnginePlugin.getArgs(cfg);

	    if (dir==null)
		    throw new IllegalStateException("must not happen!");

        adjustFileInputs(dir);

        setValueByName("plugin.dir", dir);
        setValueByName("plugin.exe", exe);
        setValueByName("plugin.logo", logo);

	    setValueByName("plugin.args", args);
        setValueByName("plugin.protocol", EnginePlugin.isXBoard(cfg) ? "xboard":"uci");

		//	Search Control
		Element search = EnginePlugin.getSearchControls(cfg);
		EnginePlugin.SearchType selected = EnginePlugin.getSelectedSearchControl(search);

		setValueByName("plugin.search", searchRadioValues[selected.ordinal()]);
		setValueByName("plugin.search.time.fixed.value",
				new Date(EnginePlugin.getSearchControlArgument(search,SearchType.TIME)));
		setValueByName("plugin.search.depth.value",
				EnginePlugin.getSearchControlArgument(search, SearchType.DEPTH));
		setValueByName("plugin.search.nodes.value",
				EnginePlugin.getSearchControlArgument(search, SearchType.NODES));

		setPluginOptions(cfg);

	    String[] startup = StringUtil.separateLines(EnginePlugin.getStartup(cfg,os));
        StringBuffer buf = new StringBuffer();
        if (startup!=null) {
            for (int i=0; i<startup.length; i++)
            {
                if (i > 0) buf.append("\n");
                buf.append(startup[i]);
            }
        }

//	    setValueByName("plugin.startup", buf.toString());

	    JTextArea text = (JTextArea)getElement("plugin.startup");
	    AWTUtil.setTextSafe(text,buf.toString());
	    /**
	     * WORKAROUND
	     * modyfing the text of this JTextArea would move the caret
	     * which would in turn cause the viewport to scroll
	     *
	     * to avoid that, we temporarily disable the caret
	     */

        //  update list (logo)
	    pluginList.repaint();
    }

	private void setPluginOptions(Element cfg)
	{
		if (EnginePlugin.isXBoard(cfg)) {
			setValueByName("plugin.option.Ponder", EnginePlugin.getBooleanValue(cfg,"Ponder"));
		    setValueByName("plugin.option.Random", EnginePlugin.getBooleanValue(cfg,"Random"));
		}

		File baseDir = EnginePlugin.getDir(cfg,Version.osDir);
		if (EnginePlugin.isUci(cfg))
		{
			String[] options = EnginePlugin.getOptions(cfg,false);    //  Name-Value pairs
			for (int i=0; i < options.length; i += 2)
			{
				String name = options[i];
				String value = options[i+1];

				Object inputElement = getElement("plugin.option."+name);
				if (inputElement==null)
				    /* what can we do ? */ ;
				else {
					if (inputElement instanceof FileInput)
						((FileInput)inputElement).setBaseDirectory(baseDir);
                    try {
                        setValue(inputElement,value);   //  can handle string values
                    } catch (RuntimeException e) {
                        /** this can happen if the data type of an UCI option changed
                         *  (e.g. after the engine was upgraded, or registered)
                         *  if this happens, reset to the default value
                         *  */
                    }
                }
			}
		}
		//pluginScroller.getVerticalScrollBar().setValue(0);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pluginScroller.getViewport().setViewPosition(new Point(0,0));
			}
		});
		//System.out.println("engine options read");
	}

	protected void readPluginInfo(Element cfg, boolean strictErrors) throws FileNotFoundException
	{
		readPluginInfo(cfg, (String)getValueByName("plugin.protocol"), strictErrors);
	}

	protected void readPluginInfo(Element cfg, String protocol, boolean strictErrors)
	        throws FileNotFoundException
	{
		boolean dirty = false;
		boolean optionDirty = false;

		if (EnginePlugin.setName(cfg, (String)getValueByName("plugin.name"))) dirty = true;
		if (EnginePlugin.setVersion(cfg, (String)getValueByName("plugin.version"))) dirty = true;
		if (EnginePlugin.setAuthor(cfg, (String)getValueByName("plugin.author"))) dirty = true;

//System.out.println(cfg.toString());

		File dir = (File)getValueByName("plugin.dir");
		File exe = (File)getValueByName("plugin.exe");
		File logo =(File)getValueByName("plugin.logo");

		try {
			if (EnginePlugin.setPaths(cfg, Version.osDir, dir,exe, Application.theWorkingDirectory))
				dirty = true;
			if (EnginePlugin.setLogoPath(cfg, Version.osDir, dir,logo))
				dirty = true;

		} catch (FileNotFoundException e) {
			if (strictErrors)
				JoDialog.showErrorDialog(frame, "File not found: "+e.getMessage());
		} catch (Exception e) {
			if (strictErrors)
				JoDialog.showErrorDialog(frame,e.getMessage());
		}

		String args = (String)getValueByName("plugin.args");
		if (EnginePlugin.setArgs(cfg,args)) dirty = true;

		if (protocol.equals("uci")) {
			if (!EnginePlugin.isUci(cfg)) { EnginePlugin.setUci(cfg); dirty = true; }
		}
		else {
			if (!EnginePlugin.isXBoard(cfg)) { EnginePlugin.setXBoard(cfg); dirty = true; }
		}

		Element optionSet = EnginePlugin.getOptionSet(cfg);
		Config.cleanAll(optionSet);

		if (EnginePlugin.isXBoard(cfg)) {
			if (EnginePlugin.setOptionValue(cfg, "Ponder", getValueByName("plugin.option.Ponder"))) optionDirty = dirty = true;
			if (EnginePlugin.setOptionValue(cfg, "random", getValueByName("plugin.option.Random"))) optionDirty = dirty = true;
		}

		if (EnginePlugin.isUci(cfg))
			for(int i=0; i < pluginOptions.getComponentCount(); i++)
			{
				Component comp = pluginOptions.getComponent(i);
				if ((comp instanceof JLabel) || (comp instanceof JoStyledLabel))
					continue;
				else {
					String name = comp.getName();
					if (name!=null && name.startsWith("plugin.option."))
                    {
                        String optionname = name.substring(14);
                        if (EnginePlugin.setOptionValue(cfg, optionname, getValue(comp))) optionDirty = dirty = true;
                    }
				}
			}

		String startup = (String)getValueByName("plugin.startup");
		//  throw in some line breaks for nice formatting ...
//		if (!startup.startsWith("\n")) startup = "\n"+startup;
//		if (!startup.endsWith("\n")) startup = startup+"\n";
		if (startup!=null) startup = startup.trim();

		if (EnginePlugin.setStartup(cfg, startup)) dirty = true;

		String radioString = (String)getValueByName("plugin.search");
		SearchType selected = SearchType.values() [ListUtil.indexOf(searchRadioValues, radioString)];

		Date timeValue = (Date)getValueByName("plugin.search.time.fixed.value");
		long ltime = (timeValue==null) ? 0:timeValue.getTime();
		long pliesValue = getIntValue("plugin.search.depth.value");
		long nodesValue = getIntValue("plugin.search.nodes.value");

		Element searchConfig = EnginePlugin.getSearchControls(pluginConfig);

		dirty |= EnginePlugin.setSearchControlArgument(searchConfig, SearchType.TIME_CONTROL, selected, null);
		dirty |= EnginePlugin.setSearchControlArgument(searchConfig, SearchType.TIME, selected, ltime);
		dirty |= EnginePlugin.setSearchControlArgument(searchConfig, SearchType.DEPTH, selected, pliesValue);
		dirty |= EnginePlugin.setSearchControlArgument(searchConfig, SearchType.NODES, selected, nodesValue);

		if (dirty) {
			Config.setDirtyElement(cfg,true);
			//  update list (logo)
			pluginList.repaint();
//			System.out.println(cfg.toString());
		}
		if (optionDirty)
			Config.setDirtyElement(optionSet,true);
	}

	protected void showPluginInfo(Element cfg) throws IOException
    {
	    if (cfg != pluginConfig)
	    {
		    if (pluginConfig != null)
		        try {
			        //  save current settings
			        //  but since the settings are about to get disabled,
			        //  don't be too picky wiht errors
			        readPluginInfo(pluginConfig,false);
		        } catch (Exception fnfex) {
			        //  ignore for now
			        // JoDialog.showErrorDialog(frame,"file not found: "+fnfex.getMessage());
		        }

		    pluginConfig = cfg;

            createPluginInfo(pluginConfig);
            setPluginInfo(pluginConfig);
		    //  adjust button states
		    setEnabled("plugin.add",true);
		    setEnabled("plugin.duplicate",pluginConfig!=null);  //  kind of paradoxical, but what the heck...
		    setEnabled("plugin.delete",pluginList.countElements() > 1);  //  leave at least one entry
	    }
	}

	protected void saveTimeControl(TimeControl control)
	{
		for (int phase=0; phase < 3; phase++) {
			String prefix = "time.control."+phase;
			Integer moves = (Integer)getValueByName(prefix+".moves");
			Date time = (Date)getValueByName(prefix+".time");
			Date increment = (Date)getValueByName(prefix+".increment");

			if (moves==null && phase > 0) {
				control.setPhaseCount(phase);
				break;
			}
			else if (phase >= control.countPhases())
				control.setPhaseCount(phase+1);

			control.setMoves(phase, (moves==null) ? TimeControl.ALL_MOVES:moves.intValue());
			control.setMillis(phase, (time==null) ? 0L:time.getTime());
			control.setIncrementMillis(phase, (increment==null) ? 0L:increment.getTime());
		}
	}

	protected void addTimeControl()
	{
		Vector controls = getTimeControls();
		TimeControl newControl;
		if (selectedControl!=null) {
			newControl = (TimeControl)selectedControl.clone();
			newControl.setName("time.control.new");
			newControl.setUserName(null);
		}
		else
			newControl = new TimeControl("time.control.new",0,5*TimeControl.MINUTE);
		controls.add(newControl);
		timeControls.setModel(new DefaultComboBoxModel(controls));
		timeControls.setSelectedItem(newControl);

		enableButton("time.control.delete",controls.size() > 1);
	}

	protected void deleteTimeControl()
	{
		int idx = timeControls.getSelectedIndex();
		Vector controls = getTimeControls();
		controls.remove(selectedControl);
		timeControls.setModel(new DefaultComboBoxModel(controls));
		if (idx >= controls.size()) idx = controls.size()-1;
		timeControls.setSelectedIndex(idx);
		enableButton("time.control.delete",controls.size() > 1);
	}

	/**
	 * @return Vector<TimeControl>
	 */
	protected Vector getTimeControls()
	{
		TimeControl control = (TimeControl)timeControls.getSelectedItem();
		if (control!=null) saveTimeControl(control); // commit changes

		Vector result = new Vector();
		for (int i=0; i < timeControls.getModel().getSize(); i++)
			result.add(timeControls.getModel().getElementAt(i));
		return result;
	}

	public void insertUpdate(DocumentEvent e) {
		//	timeControl name edited
		updateTimeControlName();
	}

	public void removeUpdate(DocumentEvent e) {
		updateTimeControlName();
	}

	public void changedUpdate(DocumentEvent e) {
		updateTimeControlName();
	}

	protected void updateTimeControlName()
	{
		JTextComponent editor = (JTextComponent)timeControls.getEditor().getEditorComponent();
		String name = editor.getText();
		TimeControl control = (TimeControl)timeControls.getSelectedItem();
		if (control!=null) {
			if (name.equals(control.getDefaultDisplayName()))
				control.setUserName(null);
			else
				control.setUserName(name);
		}
	}


	public void updateLanguage()
	{
		super.updateLanguage();
		//  special handling for plugin option labels
		if (isInited(4))
			updatePluginInfo();
	}

	public void handleMessage(Object source, int what, Object data)
	{
		if (source==engOptionReader)
		{
			//  message from Engine Options
			switch (what)
			{
			//  newly added engine, create configuration and add to list
			case EngineOptionReader.NEW_CONFIG:
				Element cfg = (Element) data;
				Config.setDirtyElement(cfg,true);
				pluginList.addNewConfig(cfg);
				break;

			case EngineOptionReader.READ_CONFIG:
				ArrayList<UciPlugin.Option> optionList = (ArrayList<UciPlugin.Option>)data;
				displayOptions(optionList);
				//  fill with current settings
				setPluginOptions(pluginConfig);
				break;

			case EngineOptionReader.DEFAULT_CONFIG:
				//  Pondering is enabled by default
				EnginePlugin.setOptionValue(pluginConfig,"Ponder","true");
				setPluginOptions(pluginConfig);
				break;

			case EngineOptionReader.ERROR:
				Throwable ex = (Throwable) data;
				ex.printStackTrace();
				showErrorDialog(ex.getLocalizedMessage());
				break;
}

			AWTUtil.setDefaultCursor(frame);
			optionWaitMessage.setVisible(false);
			pluginOptions.revalidate();
		}
	}

	private void displayOptions(ArrayList<UciPlugin.Option> optionList)
	{
		for(int i=optionList.size()-1; i >= 0; i--)
		{
			UciPlugin.Option option = optionList.get(i);
			if (importanceGroup(option.name)==0) {
				//	hidden
				optionList.remove(i);
				continue;
			}
			option.importance = importance(option.name,i+1);
		}

		optionList.sort(new Comparator<UciPlugin.Option>() {
			@Override
			public int compare(UciPlugin.Option a, UciPlugin.Option b) {
				return a.importance-b.importance;
			}
		});

		for(int i=0; i<optionList.size(); i++)
		{
			UciPlugin.Option option = optionList.get(i);
			int g = importanceGroup(option.name);
			if (i > 0 && g!=importanceGroup(optionList.get(i-1).name))
			{
				//	named separator
				String text = Language.get("plugin.option.group."+g);
				JLabel label = new JLabel(text);
				label.setFont(label.getFont().deriveFont(Font.BOLD));
				pluginOptions.add(label, LABEL_ONE_LEFT);
				pluginOptions.add(new JSeparator(), ELEMENT_ONE_ROW);
			}

			createUciOption(option);
		}

		pluginOptions.add(newButton("dialog.option.plugin.default"), CENTER_ROW);
	}
}
