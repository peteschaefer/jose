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
import de.jose.view.style.JoFontConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.Style;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import static java.awt.GridBagConstraints.REMAINDER;

/**
 *
 */
public class SymbolBar 
        extends JoPanel
		implements PgnConstants, ActionListener
{
    private static final Insets margin = new Insets(0,2,0,2);

    //protected JPanel cardPanel;
    //protected CardLayout cardLayout;

    //protected JPanel boxPane;
    //protected GridLayout boxGrid;
    protected JPanel content;
    protected GridBagLayout gridLayout;
    protected int gridRows,gridCols;

    protected ArrayList<JButton> easyButtons;
    protected ArrayList<JButton> normalButtons;
    protected ArrayList<JButton> exoticButtons;

    protected ComboNag comboNag;
    protected JButton comboButton;
    protected JPanel comboPane;
    protected RowLayout comboLayout;
    protected JComboBox<String> comboColor,comboAdjective,comboSubst,comboSelector;
    protected JLabel comboVerb;

    private static final int BUTTON_SIZE = 28;

    //protected BoxLayout comboLayout;
//    protected JScrollPane boxScroller,comboScroller;

    protected Font symbolFont, textFont, smallFont, labelFont;
    protected FontEncoding fontEncoding = null;
	//protected JoBigLabel[] labels;

    //  easy codes have plaintext expressions. They can be typed (no need for buttons).
    protected static final int[] EASY_CODES = new int[] {
            1,2,3,4,5,6, 10,// ! ?
            14,15,16,17,18,19 // +/-
    };
    // common NAG codes
    protected static final int[] NORMAL_CODES = new int[] {
            44, 45, 7,/*8,9,*/
            /*11,12,*/ 13,
            140,141,142,143,144,145,146,147,
            148,149,150,
            151,/*152,*/153,154,
            156,/*157,*/ 191,192,193,
            158,159, /*160,*/ 161,/*162,*//*163,*/
            /*190,*/ /*194,195,*/
            /*201*//*,250*/
            //  also available as Combos
            24, 32, 50, 62, 56,
            22, 36, 40,
            132, 136
    };
    //  exotic NAG codes (not standardized but implemented by FigurineSymbols
    protected static final int[] EXOTIC_CODES = new int[] {
            202, 203, 204, 205, 206, 207, 208, 209,
            210, 211, 212, 213, 214, 215, 216, 217, 218, 219,
            /*220,*/ 221, 223, 222,  225, 224, 226, 227, 228, 229,
            230, 231, 232, 233, 234, 235, 236, 237, 238, 239,
            /*240,*/ 241, 242, 243, 244, 245, 246, 247
    };

    public SymbolBar(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
    {
        super(profile,withContextMenu,withBorder);
        content = new JPanel();
        content.setLayout(gridLayout=new GridBagLayout());

        JScrollPane scroller = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setWheelScrollingEnabled(true);

        setLayout(new BorderLayout());
        add(scroller,BorderLayout.CENTER);
        gridRows=gridCols=1;
    }

    public void init()
    {
        Style symbolStyle = Application.theUserProfile.getStyleContext().getStyle("body.symbol");
        textFont = new Font("Dialog", Font.PLAIN, 16);
        smallFont = new Font("Dialog", Font.PLAIN, 9);
        labelFont = new Font("Dialog", Font.PLAIN, 10);
        fontEncoding = null;

        if (symbolStyle != null) {
            String fontFamily = JoFontConstants.getFontFamily(symbolStyle);
            fontEncoding = FontEncoding.getEncoding(fontFamily);
            symbolFont = FontUtil.newFont(fontFamily, Font.PLAIN, 16);
        }

        makeSymbolButtons();
        makeComboPane();

        relayout();
    }

    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        onResize(width,height);
        super.setBounds(x, y, width, height);
    }

    private void onResize(int width, int height)
    {
        if (!isInited()) return;

        int rows = Math.max(1,height / BUTTON_SIZE);
        int cols = Math.max(1,width / BUTTON_SIZE);

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
        if (cells >= (normalButtons.size()+easyButtons.size()+exoticButtons.size())
            && supportsRange(fontEncoding,EXOTIC_CODES))
        {   //  show complete set of buttons
            n = relayoutButtons(easyButtons,n);
            n = relayoutButtons(normalButtons,n);
            n = relayoutButtons(exoticButtons,n);
        }
        else if (cells >= (normalButtons.size()+easyButtons.size()))
        {   //  hide exotic buttons
            n = relayoutButtons(easyButtons,n);
            n = relayoutButtons(normalButtons,n);
            relayoutButtons(exoticButtons,-Integer.MAX_VALUE);
        }
        else {
            //  don't show "easy" buttons
            relayoutButtons(easyButtons,-Integer.MAX_VALUE);
            n = relayoutButtons(normalButtons,n);
            relayoutButtons(exoticButtons,-Integer.MAX_VALUE);
        }

        int bottomcols = n % gridCols;
        if (bottomcols>0 && (bottomcols+16) <= gridCols) {
            //  put combo boxes into the bottom row
            relayoutComboPane(n,BoxLayout.X_AXIS);
        }
        else {
            int nextRow = (n / gridCols)+1;
            if (nextRow < gridRows) {
                relayoutComboPane(nextRow*gridCols, (gridCols>=16) ? BoxLayout.X_AXIS: BoxLayout.Y_AXIS);
            }
            else if (gridRows > 1 && gridCols >= 5) {
                gridRows++;
                relayoutComboPane(nextRow*gridCols, (gridCols>=16) ? BoxLayout.X_AXIS: BoxLayout.Y_AXIS);
            }
            else {
                relayoutComboPane(-Integer.MAX_VALUE,BoxLayout.X_AXIS);
            }
        }

        gridLayout.invalidateLayout(this);
    }

    private void relayoutComboPane(int i, int preferredAxis)
    {
        //comboButton.setVisible(i>=0);
        comboPane.setVisible(i>=0);
        comboButton.setVisible(i>=0);

        //comboPane.setLayout(new BoxLayout(comboPane,preferredAxis));
        /*if (preferredAxis==BoxLayout.X_AXIS) {

            comboSelector.setBorder(new EmptyBorder(4,0,2,0));
            comboSubst.setBorder(new EmptyBorder(4,0,2,0));
            comboColor.setBorder(new EmptyBorder(4,0,2,0));
            comboAdjective.setBorder(new EmptyBorder(4,0,2,0));
        }
        else {
            comboVerb.setBorder(null);
            comboSelector.setBorder(null);
            comboSubst.setBorder(null);
            comboColor.setBorder(null);
            comboAdjective.setBorder(null);
        }*/

        //comboPane.setLayout(comboLayout);

        if (i >= 0) {
            relayoutButton(comboButton,i++);
            int rest = gridCols-(i%gridCols);
            comboLayout.setRowSize(rest*BUTTON_SIZE);
            comboLayout.invalidateLayout(comboPane);
            //relayoutButton(comboButton, i++);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = i % gridCols;
            c.gridy = i / gridCols;
            c.gridwidth = REMAINDER;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(5,0,0,0);
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
            c.gridx = i % gridCols;
            c.gridy = i / gridCols;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.NONE;
            gridLayout.setConstraints(button,c);
        }
    }


    private void makeComboPane()
    {
        comboPane = new JPanel();
        comboLayout = new RowLayout();
        comboPane.setLayout(comboLayout);

        //comboPane.add(makeActionButton("\uf060","switch.symbols"));
        //comboPane.add();
        comboButton = makeSymbolButton(1,symbolFont,"?","?");

//        comboPane.add(Box.createHorizontalStrut(10));
//        comboPane.add(comboButton);

        //comboPane.add(comboVerb = new JLabel("="));
        comboPane.add(comboColor = new JComboBox<>());
        comboPane.add(comboVerb = new JLabel());
        comboPane.add(comboAdjective = new JComboBox<>());
        comboPane.add(comboSubst = new JComboBox<>());
        comboPane.add(comboSelector = new JComboBox<>(ComboNag.ALL_SELECTORS));

        comboColor.setMaximumSize(new java.awt.Dimension(980,20));
        comboVerb.setBorder(new EmptyBorder(1,3,0,3));
        comboAdjective.setMaximumSize(new java.awt.Dimension(980,20));
        comboSubst.setMaximumSize(new java.awt.Dimension(980,20));
        comboSelector.setMaximumSize(new java.awt.Dimension(980,20));

        comboColor.addActionListener(this);
        comboAdjective.addActionListener(this);
        comboSubst.addActionListener(this);
        comboSelector.addActionListener(this);

        //comboPane.setBorder(new LineBorder(Color.lightGray,2,true));

        content.add(comboButton);
        content.add(comboPane);    //  grid constraints are added later
        setComboNag(ComboNag.ALL[0]);
    }

    private void setModel(JComboBox combo, String[] model)
    {
        combo.setVisible(model!=null && model.length>0);
        combo.setModel(new DefaultComboBoxModel(model));
    }

    private void setSelectedIndex(JComboBox combo, int index)
    {
        if (index < combo.getModel().getSize())
            combo.setSelectedIndex(index);
        else
            combo.setSelectedItem(null);
    }

    private void setComboNag(ComboNag comboNag)
    {
        this.comboNag = comboNag;
        setModel(comboColor, comboNag.color);
        this.comboVerb.setText(comboNag.verb);
        setModel(comboAdjective, comboNag.adjective);
        setModel(comboSubst, comboNag.subst);

        setSelectedIndex(this.comboColor,comboNag.selcol);
        setSelectedIndex(this.comboAdjective,comboNag.seladj);
        setSelectedIndex(this.comboSubst,comboNag.selsubst);
        this.comboSelector.setSelectedItem(comboNag.selector);

        setSymbol(comboButton,comboNag.code());
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource()==comboSelector) {
            String sel = (String) comboSelector.getSelectedItem();
            if (!sel.equals(comboNag.selector)) {
                setComboNag(ComboNag.findBySelector(sel));
            }
        }
        if (e.getSource()==comboColor
                || e.getSource()==comboAdjective
                || e.getSource()==comboSubst)
        {
            comboNag.select(
                    comboColor.getSelectedIndex(),
                    comboAdjective.getSelectedIndex(),
                    comboSubst.getSelectedIndex());
            setSymbol(comboButton,comboNag.code());
        }
        super.actionPerformed(e);
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
                return makeSymbolButton(nag, smallFont, "$"+nag, tip);
            else
                return makeSymbolButton(nag, textFont, text, tip);
        }
    }

    private void makeSymbolButtons()
    {
        easyButtons = new ArrayList<>();
        normalButtons = new ArrayList<>();
        exoticButtons = new ArrayList<>();

        makeSymbolButtons(EASY_CODES, easyButtons);
        makeSymbolButtons(NORMAL_CODES, normalButtons);
        makeSymbolButtons(EXOTIC_CODES, exoticButtons);
    }

    private void makeSymbolButtons(int[] codes, ArrayList<JButton> result) {
        for (int i = 0; i < codes.length; ++i) {
            JButton button = makeSymbolButton(codes[i]);
            result.add(button);
            content.add(button);   // constraints are attached later
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
        for (JButton button : exoticButtons) updateLanguage(button);
        updateLanguage(comboButton);
	}

	public void updateFormat()
	{
		Style symbolStyle = Application.theUserProfile.getStyleContext().getStyle("body.symbol");
		if (symbolStyle != null) {
			String fontFamily = JoFontConstants.getFontFamily(symbolStyle);
			fontEncoding = FontEncoding.getEncoding(fontFamily);
			symbolFont = FontUtil.newFont(fontFamily, Font.PLAIN, 16);
		}

		for(JButton button : easyButtons) updateFormat(button);
        for(JButton button : normalButtons) updateFormat(button);
        for(JButton button : exoticButtons) updateFormat(button);
        updateFormat(comboButton);
        relayout();
	}

    private void updateFormat(JButton button)
    {
        int nag = (Integer)button.getClientProperty("nag");
        setSymbol(button,nag);
    }

    private boolean supportsRange(FontEncoding fontEncoding, int[] codes)
    {
        for(int nag : codes)
            if (fontEncoding.getSymbol(nag) != null)
                return true;
        return false;
    }

    public void setupActionMap(Map<String, CommandAction> map)
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
                    text = (String)Util.nvl(text,"$"+nag);
                    docPanel.replaceSelection(text,nag);
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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintComponent(g);
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
            if (text.length() > 4) {
                text = "$" + nag;
                button.setFont(smallFont);
            }
            else {
                button.setFont(textFont);
            }
        }
        button.setText(text);
        button.setToolTipText(tip);
        button.setActionCommand(String.valueOf(nag));
    }

    public JButton makeSymbolButton(int nag, Font font, String text, String tip)
    {
        String name = String.valueOf(nag);
        JButton button = new JButton();
        button.setName(name);
        button.setActionCommand(name);
        button.setFont(font);
        button.setText(text);
        //button.setIcon(new TextIcon(text,font,Color.black));
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

    public JButton makeActionButton(char icon, String command)
    {
        JButton button = new JButton();
        button.setIcon(FontUtil.awesomeIcon(icon,12,Color.black));
        button.addActionListener(this);
        button.setActionCommand(command);
        return button;
    }

}
