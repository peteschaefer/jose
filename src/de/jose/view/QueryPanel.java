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

package de.jose.view;

import de.jose.*;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.comm.Command;
import de.jose.comm.CommandAction;
import de.jose.image.ImgUtil;
import de.jose.image.Surface;
import de.jose.pgn.SearchRecord;
import de.jose.profile.LayoutProfile;
import de.jose.profile.UserProfile;
import de.jose.util.FontUtil;
import de.jose.util.StringUtil;
import de.jose.util.icon.TextIcon;
import de.jose.util.icon.TextShapeIcon;
import de.jose.view.list.IDBTableModel;
import de.jose.window.JoDialog;
import de.jose.window.JoMenuBar;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;

public class QueryPanel
		extends JoPanel
        implements IDBPanel, TableModelListener, ChangeListener, DocumentListener
{

    //-------------------------------------------------------------------------------
    //	Constants
    //-------------------------------------------------------------------------------

    protected ImageIcon ON_ICON  = ImgUtil.getMenuIcon("menu.game.resign"); //  a STOP sign

	protected static Dimension DATE_FIELD_SIZE	= new Dimension(80,24);
	protected static Dimension ECO_FIELD_SIZE	= new Dimension(32,24);

	protected static final Insets INSETS_NORMAL = new Insets(1,4,1,4);
	protected static final Insets INSETS_TO = new Insets(0,8,0,8);

    protected static SearchRecord tempSearch = new SearchRecord();
    protected static Icon dirtyIcon = null;
	protected static Icon[] tabIcon = new Icon[3];

	protected static final GridBagConstraints LABEL_ONE =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE, 1,1, 0,0,
							   GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
							   INSETS_NORMAL, 0,0);

	public static final GridBagConstraints LABEL_THREE =
		new GridBagConstraints(2,GridBagConstraints.RELATIVE, 1,1, 0,0,
							   GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
							   INSETS_NORMAL, 0,0);

	protected static final GridBagConstraints ELEMENT_ONE_ROW =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
							   INSETS_NORMAL, 0,0);

	protected static final GridBagConstraints ELEMENT_TWO =
		new GridBagConstraints(1,GridBagConstraints.RELATIVE, 1,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
							   INSETS_NORMAL, 0,0);

	protected static final GridBagConstraints ELEMENT_FOUR =
		new GridBagConstraints(3,GridBagConstraints.RELATIVE, 1,1, 1.0,0.0,
						   GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
						   INSETS_NORMAL, 0,0);

	protected static final GridBagConstraints ELEMENT_ROW =
		new GridBagConstraints(GridBagConstraints.RELATIVE,GridBagConstraints.RELATIVE,
								GridBagConstraints.REMAINDER,1, 1.0,0.0,
							   GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
							   INSETS_NORMAL, 0,0);

	protected static final GridBagConstraints BOX0_CONSTRAINTS =
		new GridBagConstraints(0,GridBagConstraints.RELATIVE, 1,1, 1000.0,0.0,
						   GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
						   INSETS_NORMAL, 0,0);

	protected static final GridBagConstraints BOX1_CONSTRAINTS =
		new GridBagConstraints(1,GridBagConstraints.RELATIVE, 1,1, 0,0,
						   GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						   INSETS_NORMAL, 0,0);

	protected static final GridBagConstraints BOX2_CONSTRAINTS =
		new GridBagConstraints(2,GridBagConstraints.RELATIVE, 1,1, 0,0,
						   GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						   INSETS_NORMAL, 0,0);


	class PosAdapter extends SetupBoardAdapter
	{
		PosAdapter (Position pos) { super(pos); }

		public void userMove(Move mv)
		{
			super.userMove(mv);
			activate(posEditor);
		}
	}

	class PosEditor extends BoardEditView
	{
		PosEditor(Position pos) { super(new PosAdapter(pos)); }

		public void updateProfile(UserProfile prf)
		{
			super.updateProfile(prf);
			//	fix colors
			currentWhite = Surface.newColor(Color.white);
			currentBlack = Surface.newColor(Color.black);
			currentLight = Surface.newColor(Color.lightGray);
			currentDark = Surface.newColor(Color.lightGray.darker());
		}

		public Dimension getMaximumSize()
		{
			Dimension sz = getSize();
			if (sz.width > sz.height*PREFERRED_RATIO) sz.width = (int)Math.round(sz.height*PREFERRED_RATIO);
			if (sz.height > sz.width/PREFERRED_RATIO) sz.height = (int)Math.round(sz.width/PREFERRED_RATIO);
			return sz;
		}
	}

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	tabbed panel	*/
	protected JTabbedPane cardPanel;
	/**	button panel	*/
	protected JPanel buttonPanel;

	/**	first tab: game info	*/
	protected JPanel infoPanel;
	/**	second tab: comments	*/
	protected JPanel commentPanel;
	/**	this tab: position search	*/
	protected JPanel posPanel;

	/**		Info Panel	*/
	/**	player name labels	*/
	protected JLabel whiteLabel,blackLabel;
	/**	input field for player names	*/
	protected JTextField whiteName,blackName;
	/**	inpurt field for event	*/
	protected JTextField eventName,siteName;
	/**	input fields for ECO code (from .. to)	*/
	protected JTextField eco1,eco2;
	/**	input field for opening	*/
	protected JTextField openingName;
	/**	input fields for date (from .. to)	*/
	protected JTextField date1,date2;
	/**	input fields for move count (from..to)	*/
    protected JTextField count1,count2;
	/**	check boxes	*/
	protected JCheckBox colorSens, caseSens, soundSens;
	protected JCheckBox win,draw,lose,unknown;

	/**		Comment Panel	*/
	/**	input field for annotator	*/
	protected JTextField annotatorName;
	/**	input field for comments	*/
	protected JTextArea commentText;
	/** falg: has comments  */
	protected JCheckBox flagComments;
	protected JCheckBox flagVars;

	/**		Position Panel	*/
	/** position editor */
	protected PosEditor posEditor;
	/** checkbox for searching positions with reversed color    */
	protected JCheckBox reversePosition;
	/** search variations ? */
	protected JCheckBox searchVariations;


	/**		Button Panel		*/
	/**	shows number of rows	*/
    protected JLabel rowCount;
    protected JButton searchButton,clearButton,stopButton;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public QueryPanel(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
	{
		super(profile, withContextMenu,withBorder);
        focusPriority = 1;
		titlePriority = 5;
		setOpaque(false);
	}

	public void init()
	{
		int iconSize = 16;
		Color iconColor = Color.lightGray;
		tabIcon[0] = FontUtil.awesomeIcon('\uf05a',iconSize,iconColor);	//	info
		tabIcon[1] = FontUtil.awesomeIcon('\uf086',iconSize,iconColor);
		tabIcon[2] = FontUtil.awesomeIcon('\uf43c',iconSize,iconColor);

		//	set up layout
		cardPanel = new JTabbedPane();

		initInfoPanel();
		initCommentPanel();
		initPosPanel();
		initButtonPanel();

		//	dialog layout
		setLayout(new BorderLayout());
		add(cardPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		JoMenuBar.assignMnemonics(buttonPanel);
		setInitialValues();

		JoDialog.rescaleFonts(this);
	}

/*
	public void startContinuousResize()
	{
		if (posEditor!=null) posEditor.startContinuousResize();
	}

	public void finishContinuousResize()
	{
		if (posEditor!=null) posEditor.finishContinuousResize();
	}
*/

	protected void setInitialValues()
	{
		//	initial values
		clear();
		colorSens.setSelected(true);
		caseSens.setSelected(false);
		soundSens.setSelected(false);

        searchButton.setEnabled(false);
        clearButton.setEnabled(false);
	}

	protected void initInfoPanel()
	{
		JPanel p1,p2;
		Box box1;

		//	first panel: info
		infoPanel = new JPanel(new GridBagLayout());
		//	"White" "Black"
		p1 = new JPanel(new BorderLayout());
		infoPanel.add(p1, JoDialog.gridConstraint(ELEMENT_ONE_ROW,0,0,3));

		p2 = new JPanel(new GridLayout(2,2));
		p2.add(whiteLabel = JoDialog.newLabel("dialog.query.white",JLabel.LEFT));
		p2.add(blackLabel = JoDialog.newLabel("dialog.query.black",JLabel.LEFT));

		p2.add(whiteName = JoDialog.newTextField(this));
		p2.add(blackName = JoDialog.newTextField(this));

		p1.add(p2,BorderLayout.CENTER);

		JButton swap = JoDialog.newButton("dialog.query.swap.colors",ImgUtil.getMenuIcon("swap.colors"),this);
        swap.setBorderPainted(true);
        swap.setContentAreaFilled(false);
		swap.setBorder(new EmptyBorder(4,4,4,4));
		p1.add(swap,BorderLayout.EAST);

		p1 = new JPanel(new GridBagLayout());
		//	"Event"
		p1.add(JoDialog.newLabel("dialog.query.event"), LABEL_ONE);
		p1.add(eventName = JoDialog.newTextField(this), JoDialog.gridConstraint(ELEMENT_ROW,1,0,1));
		//	"Site"
		p1.add(JoDialog.newLabel("dialog.query.site"), LABEL_ONE);
		p1.add(siteName = JoDialog.newTextField(this), JoDialog.gridConstraint(ELEMENT_ROW,1,1,1));
		//	"Opening"
		p1.add(JoDialog.newLabel("dialog.query.opening"), LABEL_ONE);
		p1.add(openingName = JoDialog.newTextField(this), JoDialog.gridConstraint(ELEMENT_ROW,1,2,1));
		//	"ECO"
		p1.add(JoDialog.newLabel("dialog.query.eco"), LABEL_ONE);
		box1 = Box.createHorizontalBox();
		box1.add(eco1 = JoDialog.newTextField(ECO_FIELD_SIZE,this));
		box1.add(JoDialog.newLabel("dialog.query.to",null,JLabel.CENTER,INSETS_TO));
		box1.add(eco2 = JoDialog.newTextField(ECO_FIELD_SIZE,this));
		p1.add(box1, JoDialog.gridConstraint(ELEMENT_ROW,1,3,1));
		eco1.setColumns(3);
		eco2.setColumns(3);
		//	"Move" count
		p1.add(JoDialog.newLabel("dialog.query.movecount"), LABEL_ONE);
		box1 = Box.createHorizontalBox();
		box1.add(count1 = JoDialog.newTextField(ECO_FIELD_SIZE,this));
		box1.add(JoDialog.newLabel("dialog.query.to",null,JLabel.CENTER,INSETS_TO));
		box1.add(count2 = JoDialog.newTextField(ECO_FIELD_SIZE,this));
		p1.add(box1, JoDialog.gridConstraint(ELEMENT_ROW,1,4,1));
		//	"Date"
		p1.add(JoDialog.newLabel("dialog.query.date"), LABEL_ONE);
		box1 = Box.createHorizontalBox();
		box1.add(date1 = JoDialog.newTextField(DATE_FIELD_SIZE,this));
		box1.add(JoDialog.newLabel("dialog.query.to",null,JLabel.CENTER,INSETS_TO));
		box1.add(date2 = JoDialog.newTextField(DATE_FIELD_SIZE,this));
		p1.add(box1, JoDialog.gridConstraint(ELEMENT_ROW,1,5,1));
        infoPanel.add(p1, JoDialog.gridConstraint(BOX0_CONSTRAINTS,0,1,1));

		//	"Result"
        p1 = new JPanel(new GridLayout(4,1));
		p1.setBorder(new TitledBorder(Language.get("dialog.query.result")));
		p1.add(win=JoDialog.newCheckBox("Result.1-0",this));
		p1.add(draw=JoDialog.newCheckBox("Result.1/2",this));
		p1.add(lose=JoDialog.newCheckBox("Result.0-1",this));
		p1.add(unknown=JoDialog.newCheckBox("Result.*",this));
//		box1.add(Box.createVerticalGlue());
        infoPanel.add(p1, JoDialog.gridConstraint(BOX1_CONSTRAINTS,1,1,1));

		//	"Flags"
        p1 = new JPanel(new GridLayout(4,1));
        p1.setBorder(new TitledBorder(Language.get("dialog.query.flags")));
		p1.add(colorSens = JoDialog.newCheckBox("dialog.query.color.sensitive",this));

		p1.add(caseSens = JoDialog.newCheckBox("dialog.query.case.sensitive",this));
		p1.add(soundSens = JoDialog.newCheckBox("dialog.query.soundex",this));
//		box2.add(Box.createVerticalGlue());
		infoPanel.add(p1, JoDialog.gridConstraint(BOX2_CONSTRAINTS,2,1,1));

		infoPanel.add(new JLabel(""), JoDialog.gridConstraint(JoDialog.ELEMENT_REMAINDER,0,2,3));

		//	add info panel to tabbed pane
		addTab("dialog.query.info", infoPanel, tabIcon[0],  true);
	}

	protected void initCommentPanel()
	{
		//	second panel: comments
		commentPanel = new JPanel(new GridBagLayout());

		//  Comments Flag, Variation Flag
		Box box = Box.createHorizontalBox();
		box.add(flagComments = JoDialog.newCheckBox("dialog.query.com.flag",this));
		box.add(flagVars = JoDialog.newCheckBox("dialog.query.var.flag",this));
		commentPanel.add(box, JoDialog.ELEMENT_ONE);

		//	"Annotator"
		commentPanel.add(JoDialog.newLabel("dialog.query.annotator",JLabel.LEFT), JoDialog.LABEL_ONE_LEFT);
		commentPanel.add(annotatorName = JoDialog.newTextField(this), JoDialog.ELEMENT_ONE);
		//	"Comment Text"
		commentPanel.add(JoDialog.newLabel("dialog.query.commenttext",JLabel.LEFT), JoDialog.LABEL_ONE_LEFT);
		commentPanel.add(commentText = JoDialog.newTextArea(this), JoDialog.ELEMENT_NEXTROW_REMAINDER);
		commentText.setBorder(new LineBorder(Color.black));

		//	add comments panel to tabbed pane
		addTab("dialog.query.comments", commentPanel, tabIcon[1], true);
	}

	protected void initPosPanel()
	{
		//  third panel: position
		posPanel = new JPanel(new BorderLayout());

		posEditor = new PosEditor(new Position(Position.EMPTY_POSITION));
		posEditor.init();

		Box controls = Box.createVerticalBox();
		/** editor controls */

        controls.add(searchVariations = JoDialog.newCheckBox("query.setup.var",this));
		controls.add(reversePosition = JoDialog.newCheckBox("query.setup.reversed",this));

        controls.add(Box.createVerticalStrut(10));

        //  Edit buttons
		createEditButtons(controls,this);

		posPanel.add(posEditor, BorderLayout.CENTER);
		posPanel.add(controls, BorderLayout.WEST);

		addTab("dialog.query.position", posPanel, tabIcon[2],true);
	}

	public static void createEditButtons(JComponent controls, ActionListener list)
	{
		controls.add( JoDialog.newLinkButton("dialog.setup.clear",     "menu.edit.clear",list) );
		controls.add( JoDialog.newLinkButton("dialog.setup.initial",   "menu.web.home", list) );
		controls.add( JoDialog.newLinkButton("dialog.setup.copy",      "menu.edit.copy", list) );
		controls.add( JoDialog.newLinkButton("menu.edit.copy.fen",     "menu.edit.copy", list) );
		controls.add( JoDialog.newLinkButton("menu.edit.paste",        "menu.edit.paste", list) );
	}

	protected void addTab(String id, JComponent component, Icon icon, boolean scrollable)
	{
		if (scrollable) {
			JScrollPane scroller = new JScrollPane(component,
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroller.setBorder(null);
			component = scroller;
		}
        component.setName(id);
		cardPanel.addTab(Language.get(id), icon, component, Language.getTip(id));
	}

	protected void initButtonPanel()
	{
		//	bottom panel: buttons
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		//	"Search"
		Icon searchIcon = new TextShapeIcon("\uf002",FontUtil.fontAwesome(),18,Color.black,Color.white);
		Icon clearIcon = new TextShapeIcon("\uf057",FontUtil.fontAwesome(),18,Color.red.darker(),Color.white);
		Icon stopIcon = new TextShapeIcon("\uf28d",FontUtil.fontAwesome(),18,Color.red.darker(),Color.white);

		searchButton = JoDialog.newButton("dialog.query.search",searchIcon,"icon",this);
		searchButton.setDefaultCapable(true);
		buttonPanel.add(searchButton);
		//	"Clear"
        clearButton = JoDialog.newButton("dialog.query.clear",clearIcon,"icon",this);
        buttonPanel.add(clearButton);

		buttonPanel.add(Box.createHorizontalGlue());
		//	"Row Count"
        rowCount = new JLabel();
        rowCount.setBorder(new CompoundBorder(
                new BevelBorder(BevelBorder.LOWERED),
                new EmptyBorder(2,8,2,8)));
        buttonPanel.add(rowCount);

        stopButton = JoDialog.newButton("dialog.query.stop.results", stopIcon,this);
        stopButton.setText(null);
		stopButton.setVisible(false);
        buttonPanel.add(stopButton);

		rowCount.setText("   ");
//		rowCount.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
		rowCount.setMinimumSize(new Dimension(160,20));
		rowCount.setHorizontalAlignment(JLabel.RIGHT);
//		rowCount.setHorizontalTextPosition(JLabel.LEADING);
  		rowCount.setIconTextGap(8);
	}


	public void adjustContextMenu(Collection list, MouseEvent event)
	{
		super.adjustContextMenu(list, event);

		if (cardPanel.getSelectedIndex()==2)
        {
            list.add(null);
            list.add("dialog.setup.clear");
            list.add("dialog.setup.initial");
            list.add("dialog.setup.copy");
            list.add(null);
			//  position search
			list.add("menu.edit.copy.fen");     //  copy FEN to clipboard
			list.add("menu.edit.paste");    //  copy FEN from clipboard
		}
	}

	public void setupActionMap(Map<String, CommandAction> map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				ListPanel lpanel = (ListPanel)JoPanel.get("window.gamelist");
				lpanel.stopResult();
			}
		};
		map.put("dialog.query.stop.results",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
                search();
			}
		};
		map.put("dialog.query.search",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				clear();
                search();
			}
		};
		map.put("dialog.query.clear",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				String swap = whiteName.getText();
				whiteName.setText(blackName.getText());
				blackName.setText(swap);

				boolean bswap = win.isSelected();
				win.setSelected(lose.isSelected());
				lose.setSelected(bswap);
			}
		};
		map.put("dialog.query.swap.colors",action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
                posEditor.clearPosition();
            }
        };
        map.put("dialog.setup.clear",action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
                posEditor.initialPosition();
            }
        };
        map.put("dialog.setup.initial",action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
                posEditor.defaultPosition();
            }
        };
        map.put("dialog.setup.copy",action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				posEditor.copyToClipboard();
			}
		};
		map.put("menu.edit.copy.fen",action);
        map.put("menu.edit.copy",action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (!posEditor.pasteFromClipboard())
					Sound.play("sound.error");
			}
		};
		map.put("menu.edit.paste",action);
	}

    protected void search()
    {
        ListPanel lpanel = (ListPanel)JoPanel.get("window.gamelist");
        if (lpanel==null) return;   //  invalid state, actually

        SearchRecord search = lpanel.getSearchRecord();
        if (search==null) return;   //  invalid state, actually

        java.util.List errors = setSearchFields(search);
        if (errors==null || errors.isEmpty())
            lpanel.model.refresh(true);
        else
            showErrors(errors);
	    activate(null);
    }

	protected boolean isEmptySearch()
	{
		ListPanel lpanel = (ListPanel)JoPanel.get("window.gamelist");
		if (lpanel==null) return true;

		SearchRecord search = lpanel.getSearchRecord();
		if (search==null) return true;

		return !search.hasFilter();
	}

	protected void clear()
	{
		whiteName.setText("");
		blackName.setText("");
		eventName.setText("");
		siteName.setText("");
		eco1.setText("");
		eco2.setText("");
		openingName.setText("");
        annotatorName.setText("");
        count1.setText("");
        count2.setText("");
		date1.setText("");
		date2.setText("");

		flagComments.setSelected(false);
		flagVars.setSelected(false);
		commentText.setText("");

		win.setSelected(true);
		draw.setSelected(true);
		lose.setSelected(true);
		unknown.setSelected(true);

		posEditor.clearPosition();
	}

	public java.util.List setSearchFields(SearchRecord rec)
	{
		java.util.List errors = new ArrayList();

		setSearchFields(rec,errors);

		if (errors.isEmpty())
			return null;
		else
			return errors;
	}

	protected void setSearchFields(SearchRecord rec, java.util.List errors)
	{
		rec.firstPlayerName = whiteName.getText();
		rec.secondPlayerName = blackName.getText();
		rec.eventName = eventName.getText();
		rec.siteName = siteName.getText();
		rec.eco1 = eco1.getText();
		rec.eco2 = eco2.getText();
		rec.openingName = openingName.getText();

        rec.moveCount1 = SearchRecord.parseInt(count1.getText(),errors);
        rec.moveCount2 = SearchRecord.parseInt(count2.getText(),errors);

        rec.annotatorName = annotatorName.getText();
		rec.commentText = commentText.getText();
		rec.flagComments = flagComments.isSelected();
		rec.flagVariations = flagVars.isSelected();

		rec.date1 = SearchRecord.parseDate(date1.getText(),errors, SearchRecord.LOWER_BOUND);
		rec.date2 = SearchRecord.parseDate(date2.getText(),errors, SearchRecord.UPPER_BOUND);

		rec.setWin(win.isSelected());
		rec.setDraw(draw.isSelected());
		rec.setLose(lose.isSelected());
		rec.setUnknown(unknown.isSelected());

		rec.setColorSensitive(colorSens.isSelected());
		rec.setCaseSensitive(caseSens.isSelected());
		rec.setSoundex(soundSens.isSelected());

		//  positional search
		Position pos = posEditor.board.getPosition();
		if (!pos.isEmpty())
		{
			rec.posFilter.setTargetPosition(pos.toString(), reversePosition.isSelected());

			rec.posFilter.setVariations(searchVariations.isSelected());
		}
		else
			rec.posFilter.clear();

		rec.finish(errors);	//	check plausability
	}

	public void showErrors(java.util.List errors)
	{
		StringBuffer text = new StringBuffer(Language.get("dialog.query.errors"));

		for (int i=0; i < errors.size(); i++)
		{
			text.append("\n");
			String key = errors.get(i).toString();
			text.append(Language.get(key));
		}

		JoDialog.showErrorDialog(text.toString());
	}

	//-------------------------------------------------------------------------------
	//	Basic Access
	//-------------------------------------------------------------------------------

	//-------------------------------------------------------------------------------
	//	interface ChangeListener
	//-------------------------------------------------------------------------------

	public void stateChanged(ChangeEvent evt)
	{
		if (colorSens.isSelected()) {
			whiteLabel.setForeground(Color.black);
			blackLabel.setForeground(Color.black);
		}
		else {
			whiteLabel.setForeground(Color.lightGray);
			blackLabel.setForeground(Color.lightGray);
		}

		caseSens.setEnabled(! soundSens.isSelected());

		activate(evt.getSource());
	}

	//-------------------------------------------------------------------------------
	//	interface DocumentListener
	//-------------------------------------------------------------------------------


	public void changedUpdate(DocumentEvent e) {
		activate(e.getDocument());
	}

	public void insertUpdate(DocumentEvent e) {
		activate(e.getDocument());
	}

	public void removeUpdate(DocumentEvent e) {
		activate(e.getDocument());
	}

	protected void activate(Object source)
	{
		setSearchFields(tempSearch,null);

        boolean t0 = tempSearch.hasInfoFilter();
        boolean t1 = tempSearch.hasCommentFilter();
        boolean t2 = tempSearch.hasPositionFilter();

		activateTab(0,t0);
		activateTab(1,t1);
		activateTab(2,t2);

        ListPanel lpanel = (ListPanel)JoPanel.get("window.gamelist");

        searchButton.setEnabled((lpanel!=null) && (t0||t1||t2));
        clearButton.setEnabled(t0||t1||t2|| !isEmptySearch());
	}

	protected void activateTab(int idx, boolean on)
	{
		if (on && dirtyIcon==null)
			dirtyIcon = new TextIcon("\uf303", FontUtil.fontAwesome(),12,Color.decode("#9c0000"));
		cardPanel.setIconAt(idx, on ? dirtyIcon:tabIcon[idx]);
	}

    public void searchCurrentPosition()
    {
        posEditor.defaultPosition();;
        cardPanel.setSelectedIndex(2);
    }

    //-------------------------------------------------------------------------------
    //	interface TableModelListener
    //-------------------------------------------------------------------------------

    /** sent by ListPanel   */
    public void tableChanged(TableModelEvent e)
    {
        IDBTableModel model = (IDBTableModel)e.getSource();
        boolean isEnabled = model.isWorking();

        String text = null;
        if (model.getRowCount() <= 0) {
            if (isEnabled)
                text = Language.get("dialog.query.search.in.progress");
            else
                text = Language.get("dialog.query.0.results");
        }
        else if (model.getRowCount() == 1)
            text = Language.get("dialog.query.1.result");
        else {
            text = Language.get("dialog.query.n.results");
            text = StringUtil.replace(text,"%count%",Integer.toString(model.getRowCount()));
        }

		if (rowCount!=null) {
			rowCount.setText(text);
            stopButton.setVisible(isEnabled);
		}
    }

    public void updateLanguage()
    {
        Language.update(cardPanel);
    }
}
