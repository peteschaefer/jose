/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch?fer
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.view;

import de.jose.*;
import de.jose.comm.Command;
import de.jose.comm.CommandAction;
import de.jose.pgn.ComboNag;
import de.jose.util.FontUtil;
import de.jose.pgn.PgnConstants;
import de.jose.pgn.PgnUtil;
import de.jose.profile.FontEncoding;
import de.jose.profile.LayoutProfile;
import de.jose.util.TextIcon;
import de.jose.view.style.JoFontConstants;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListDataListener;
import javax.swing.text.Style;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Map;

import static java.awt.GridBagConstraints.REMAINDER;

/**
 * todo
 *  reorganize list! a plain vertical list is not user-friendly
 *  we need something like the symbol picker in Word :)
 *
 *  - table for common nags (43 entries)
 *  - most used nags on top
 *  - mouse hover; text in status line
 *  - recently used nags in a separate list
 *  - there's a number of "composable" nag, how should their gui look like?
 *
 *      BorderLayout (south = status bar)
 *      GridLayout for icons
 *      CardLayout switch to combo boxes
 *
 *  [0]
 *  [1..6]  ! ? !! ?? !? ?!
 *  [7..9] forced, singular, worst move
 *  [10..13] drawish, quiet, active, unclear position
 *
 *  [140] with the idea
 *  [141] against
 *  [142] is better
 *  [143] is worse
 *  [144] is equivalent
 *  [145] RR (remark)
 *  [146] Novelty
 *  [147] Weak Point
 *  [148..150] endgame,line,diagonal
 *  [151..154] w/b bishop pair, opp colored bishops, same colored bishops
 *  [156..163] passed pawn, more pawns, with/withou/see/rank
 *
 *  [190..195] etc. double pawns, isolated pawns, connected pawns
 *
 *  [201] Diagram
 *  [250] deprecated Diagram (but maybe upside-down Diagram !?)
 *
 *   = 43 nags. present in table
 *
 *  --- [14..139] "composable" nags ---
 *  --- how should we present them? with an array of combo boxes? ---
 *  --- see class ComboNag ---
 *
 *  [14..21] white/black has a slight/moderate/decisive/crushing advantage
 *  [22..23] white/black is in Zugzwang
 *  [24..35] w/b has slight/moderate/decisive space/time advantage
 *
 *  [36..41] w/b has the/a lasting initiative/attack
 *  [42..47] w/b has insufficient/sufficient/more than adequate compentsion for material deficit
 *
 *  [48..65] w/b has slight/moderate/decisive center/kingside/queenside control advantage
 *
 *  [66..69] w/b has a vulnerable/well protected first rank
 *  [70..77] w/b has a poorly/well protected/placed king
 *
 *  [78..85] w/b has very/moderately weak/strong pawn structure
 *
 *  [86..101] w/b has poor/good knight/bishop/rook/queen placement
 *  [102..104] w/b has poor/good piece coordination
 *  [106..129] w/b has played the opening/middlegame/ending (very) poorly/well
 *  [130..135] w/b has slight/moderate/decisive counterplay
 *  [136..139] w/b has moderate/severe time control pressure
 *
 */
public class SymbolBar 
        extends JoPanel
		implements PgnConstants, ItemListener
{
    private static final Insets margin = new Insets(0,2,0,2);

    //protected JPanel cardPanel;
    //protected CardLayout cardLayout;

    //protected JPanel boxPane;
    //protected GridLayout boxGrid;
    protected GridBagLayout gridLayout;
    protected int gridRows,gridCols;

    protected ArrayList<JButton> easyButtons;
    protected ArrayList<JButton> normalButtons;

    protected ComboNag comboNag;
    protected JButton comboButton;
    protected JPanel comboPane;
    protected JComboBox<String> comboColor,comboAdjective,comboSubst,comboSelector;
    protected JLabel comboVerb;

    private static final int BUTTON_SIZE = 20;

    //protected BoxLayout comboLayout;
//    protected JScrollPane boxScroller,comboScroller;

    protected Font symbolFont, textFont, labelFont;
    protected FontEncoding fontEncoding = null;
	//protected JoBigLabel[] labels;

/*
 *  [1..6]  ! ? !! ?? !? ?!
 *  [7..9] forced, singular, worst move
 *  [10..13] drawish, quiet, active, unclear position
 *
 *  [140] with the idea
 *  [141] against
 *  [142] is better
 *  [143] is worse
 *  [144] is equivalent
 *  [145] RR (remark)
 *  [146] Novelty
 *  [147] Weak Point
 *  [148..150] endgame,line,diagonal
 *  [151..154] w/b bishop pair, opp colored bishops, same colored bishops
 *  [156..163] passed pawn, more pawns, with/withou/see/rank
 *
 *  [190..195] etc. double pawns, isolated pawns, connected pawns
 *
 *  [201] Diagram
 *  [250] deprecated Diagram (but maybe upside-down Diagram !?)
 */
    protected static final int[] EASY_CODES = new int[] {
            1,2,3,4,5,6, // ! ?
            14,15,16,17,18,19 // +/-
    };
    protected static final int[] NORMAL_CODES = new int[] {
            7,8,9,
            10,11,12,13,
            140,141,142,143,144,145,146,147,
            148,149,150,
            151,152,153,154,
            156,157,158,159,160,161,162,163,
            190,191,192,193,194,195,
            201 /*,250*/
    };

    public SymbolBar(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
    {
        super(profile,withContextMenu,withBorder);
        setLayout(gridLayout=new GridBagLayout());
        gridRows=gridCols=0;
    }

    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        onResize(width,height);
        super.setBounds(x, y, width, height);
    }

    private void onResize(int width, int height) {
        int rows = height / BUTTON_SIZE;
        int cols = width / BUTTON_SIZE;

        if (rows != gridRows || cols != gridCols) {
            gridRows = rows;
            gridCols = cols;
            relayout();
        }
    }

    private void relayout()
    {
        int cells = gridRows * gridCols;
        int n=0;
        if (cells >= (normalButtons.size()+easyButtons.size())) {
            n = relayoutButtons(easyButtons,n);
            n = relayoutButtons(normalButtons,n);
        }
        else {
            relayoutButtons(easyButtons,-1);
            n = relayoutButtons(normalButtons,n);
        }

        int bottomcols = n % gridCols;
        if (bottomcols>0 && bottomcols+8 <= gridCols) {
            //  put combo boxes into the bottom row
            relayoutComboPane(n);
        }
        else {
            int nextRow = (n / gridCols)+1;
            if (nextRow < gridRows) {
                relayoutComboPane(nextRow*gridCols);
            }
            else if (gridRows > 1 || gridCols >= 8) {
                gridRows++;
                relayoutComboPane(nextRow*gridCols);
            }
            else {
                relayoutComboPane(-1);
            }
        }

        gridLayout.invalidateLayout(this);
    }

    private void relayoutComboPane(int i)
    {
        comboButton.setVisible(i>=0);
        comboPane.setVisible(i>=0);

        if (i >= 0) {
            relayoutButton(comboButton, i++);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = i % gridCols;
            c.gridy = i / gridCols;
            c.gridwidth = REMAINDER;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            gridLayout.setConstraints(comboPane, c);
        }
    }

    private int relayoutButtons(ArrayList<JButton> buttons, int i)
    {
        for(JButton button : buttons)
            relayoutButton(button,i++);
        return i;
    }

    private void relayoutButton(JButton button, int i) {
        button.setVisible(i >= 0);
        if (i >= 0) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = i %gridCols;
            c.gridy = i /gridCols;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.CENTER;
            c.fill = GridBagConstraints.NONE;
            gridLayout.setConstraints(button,c);
        }
    }

    public void init()
    {
        comboPane = new JPanel();
        LayoutManager comboLayout = new BoxLayout(comboPane,BoxLayout.X_AXIS);

        Style symbolStyle = Application.theUserProfile.getStyleContext().getStyle("body.symbol");
        textFont = new Font("Dialog", Font.PLAIN, 14);
        labelFont = new Font("Dialog", Font.PLAIN, 10);
        fontEncoding = null;

        if (symbolStyle != null) {
            String fontFamily = JoFontConstants.getFontFamily(symbolStyle);
            fontEncoding = FontEncoding.getEncoding(fontFamily);
            symbolFont = FontUtil.newFont(fontFamily, Font.PLAIN, 14);
        }

        makeSymbolButtons();
        makeComboPane();

        relayout();
    }


    private void makeComboPane()
    {
        comboPane.add(makeActionButton("\uf060","switch.symbols"));
        comboPane.add(comboButton = makeSymbolButton(1,symbolFont,"?","?"));
        comboButton.setMinimumSize(new Dimension(32,32));
        comboButton.setPreferredSize(new Dimension(32,32));

        comboPane.add(Box.createHorizontalStrut(10));
        comboPane.add(comboColor = new JComboBox<>());
        comboPane.add(comboVerb = new JLabel());
        comboPane.add(comboAdjective = new JComboBox<>());
        comboPane.add(comboSubst = new JComboBox<>());
        comboPane.add(comboSelector = new JComboBox<>(ComboNag.ALL_SELECTORS));

        comboColor.addItemListener(this);
        comboAdjective.addItemListener(this);
        comboSubst.addItemListener(this);
        comboSelector.addItemListener(this);

        setComboNag(ComboNag.ALL[0]);
    }

    private void setModel(JComboBox combo, String[] model)
    {
        combo.setVisible(model!=null && model.length>0);
        combo.setModel(new DefaultComboBoxModel(model));
    }

    private void setComboNag(ComboNag comboNag)
    {
        this.comboNag = comboNag;
        setModel(comboColor, comboNag.color);
        this.comboVerb.setText(comboNag.verb);
        setModel(comboAdjective, comboNag.adjective);
        setModel(comboSubst, comboNag.subst);

        this.comboColor.setSelectedIndex(comboNag.selcol);
        this.comboAdjective.setSelectedIndex(comboNag.seladj);
        this.comboSubst.setSelectedIndex(comboNag.selsubst);
        this.comboSelector.setSelectedItem(comboNag.selector);

        setSymbol(comboButton,comboNag.code());
    }

    public void itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange()==ItemEvent.SELECTED
                && e.getSource()==comboSelector) {
            String sel = (String) comboSelector.getSelectedItem();
            if (!sel.equals(comboNag.selector)) {
                setComboNag(ComboNag.findBySelector(sel));
            }
        }
        if (e.getStateChange()==ItemEvent.SELECTED
                && (e.getSource()==comboColor
                || e.getSource()==comboAdjective
                || e.getSource()==comboSubst))
        {
            comboNag.select(
                    comboColor.getSelectedIndex(),
                    comboAdjective.getSelectedIndex(),
                    comboSubst.getSelectedIndex());
            setSymbol(comboButton,comboNag.code());
        }
    }

    private JButton makeSymbolButton(int nag)
    {
        String tip = Language.getTip("pgn.nag."+nag);
        tip += " ($"+nag+")";
        //if (tip==null) continue;    //  not a valid annotation

        String text = null;
        if (fontEncoding != null)
            text = fontEncoding.getSymbol(nag);

        if (text != null)
            return makeSymbolButton(nag, symbolFont,text, tip);
        else {
            text = Language.get("pgn.nag."+nag);
            if (text.length() > 4)
                text = "$"+nag;
            return makeSymbolButton(nag, textFont, text, tip);
        }
    }

    private void makeSymbolButtons()
    {
        easyButtons = new ArrayList<>();
        normalButtons = new ArrayList<>();

        for (int i=0; i < EASY_CODES.length; ++i) {
            JButton button = makeSymbolButton(EASY_CODES[i]);
            easyButtons.add(button);
            this.add(button);   // constraints are attached later
        }
        for (int i=0; i < NORMAL_CODES.length; ++i) {
            JButton button = makeSymbolButton(NORMAL_CODES[i]);
            normalButtons.add(button);
            this.add(button);  // constraints are attached later
        }
    }

    private void updateLanguage(JButton button)
    {
        int nag = (Integer)button.getClientProperty("nag");
        String tip = Language.getTip("pgn.nag." + nag);
        button.setToolTipText(tip);
    }

    public void updateLanguage()
	{
		for (JButton button : easyButtons) updateLanguage(button);
        for (JButton button : normalButtons) updateLanguage(button);
	}

	public void updateFormat()
	{
		Style symbolStyle = Application.theUserProfile.getStyleContext().getStyle("body.symbol");
		textFont = new Font("Dialog", Font.PLAIN, 14);
		labelFont = new Font("Dialog", Font.PLAIN, 10);

		if (symbolStyle != null) {
			String fontFamily = JoFontConstants.getFontFamily(symbolStyle);
			fontEncoding = FontEncoding.getEncoding(fontFamily);
			symbolFont = FontUtil.newFont(fontFamily, Font.PLAIN, 14);
		}

		for(JButton button : easyButtons) updateFormat(button);
        for(JButton button : normalButtons) updateFormat(button);
	}

    private void updateFormat(JButton button)
    {
        int nag = (Integer)button.getClientProperty("nag");
        String text = null;
        if (fontEncoding != null)
            text = fontEncoding.getSymbol(nag);

        if (text != null) {
            button.setFont(symbolFont);
            button.setText(text);
        }
        else {
            text = Language.get("pgn.nag."+nag);
            if (text.length() > 4)
                text = "$"+nag;

            button.setFont(textFont);
            button.setText(text);
        }
    }

    public void setupActionMap(Map map)
    {
        super.setupActionMap(map);

		CommandAction action;
		action = new CommandAction() {
			public void Do(Command cmd) {
				updateLanguage();
			}
		};
		map.put("update.language", action);

        action = new CommandAction() {
            public void Do(Command cmd) {
                DocumentPanel docPanel = Application.theApplication.docPanel();
                if (docPanel != null) {
					int nag = Integer.parseInt(cmd.code);
					String text = PgnUtil.annotationString(nag);
                    docPanel.replaceSelection(text);
                }
            }
        };

        for (int nag=1; nag <= NAG_MAX; nag++)
        {
            String name = String.valueOf(nag);
            map.put(name,action);
        }


	    action = new CommandAction() {
		    public void Do(Command cmd) throws Exception
		    {
			    //  notification from style editor
			    Object source = cmd.data;
			    boolean allModified = Util.toboolean(cmd.moreData);
			    if (allModified)
				    updateFormat();
			    else
				    /* only size modified; don't mind */ ;
		    }
	    };
	    map.put("styles.modified",action);
    }

    public void setSymbol(JButton button, int nag)
    {
        //if (nag==NAG_DIAGRAM_DEPRECATED) continue;
        String tip = Language.getTip("pgn.nag."+nag);
        tip += " ($"+nag+")";
        //if (tip==null) continue;    //  not a valid annotation

        String text = null;
        if (fontEncoding != null)
            text = fontEncoding.getSymbol(nag);

        if (text != null) {
            button.setFont(symbolFont);
        }
        else {
            text = Language.get("pgn.nag."+nag);
            if (text.length() > 4)
                text = "$"+nag;
            button.setFont(textFont);
        }
        button.setText(text);
        button.setToolTipText(tip);
    }

    public JButton makeSymbolButton(int nag, Font font, String text, String tip)
    {
        String name = String.valueOf(nag);
        JButton button = new JButton();
        button.setName(name);
        button.setActionCommand(name);
        button.setFont(font);
        button.setText(text);
        button.setToolTipText(tip);
        button.addActionListener(this);
        button.setBorderPainted(true);
        button.setBackground(Color.white);
        button.setBorder(new LineBorder(Color.lightGray,2,true));
        button.setMargin(margin);
        button.putClientProperty("nag",nag);

        Dimension size = new Dimension(BUTTON_SIZE, BUTTON_SIZE);
        button.setMinimumSize(size);
        button.setPreferredSize(size);
        button.setMaximumSize(size);

        //JoBigLabel label = new JoBigLabel(tip,1,40);
        //label.setFont(labelFont);

		//buttons[nag] = button;
		//labels[nag] = label;

//        elementPane.add(button, (column==0) ? JoDialog.ELEMENT_ONE : JoDialog.ELEMENT_THREE);
//        elementPane.add(label, (column==0) ? JoDialog.ELEMENT_TWO : JoDialog.ELEMENT_FOUR);
		//boxPane.add(button);
		//elementPane.add(label, JoDialog.ELEMENT_TWO);
        return button;
    }

    public JButton makeActionButton(String icon, String command)
    {
        JButton button = new JButton();
        Font awefont = FontUtil.fontAwesome().deriveFont(Font.PLAIN, 12);
        //button.setFont(awefont);
        //button.setText("\uf061");
        button.setIcon(new TextIcon(icon,awefont,Color.black));
        button.addActionListener(this);
        button.setActionCommand(command);
        return button;
    }

}
