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
import de.jose.util.FontUtil;
import de.jose.pgn.PgnConstants;
import de.jose.pgn.PgnUtil;
import de.jose.profile.FontEncoding;
import de.jose.profile.LayoutProfile;
import de.jose.view.input.JoBigLabel;
import de.jose.view.style.JoFontConstants;
import de.jose.window.JoDialog;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.Style;
import java.awt.*;
import java.util.Map;

public class SymbolBar 
        extends JoPanel
		implements PgnConstants
{
    private static final Insets margin = new Insets(0,2,0,2);

    protected JPanel elementPane;
    protected Font symbolFont, textFont, labelFont;
	protected JButton[] buttons;
	protected JoBigLabel[] labels;

    public SymbolBar(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
    {
        super(profile,withContextMenu,withBorder);
        setLayout(new BorderLayout());
        setBackground(Color.white);

        elementPane = new JPanel(new GridBagLayout());

        JScrollPane scroller = new JScrollPane(elementPane);
        scroller.getVerticalScrollBar().setUnitIncrement(20);
        add(scroller, BorderLayout.CENTER);
    }

    public void init()
    {
        Style symbolStyle = Application.theUserProfile.getStyleContext().getStyle("body.symbol");
        textFont = new Font("Dialog", Font.PLAIN, 14);
        labelFont = new Font("Dialog", Font.PLAIN, 10);
        FontEncoding fontEncoding = null;

        if (symbolStyle != null) {
            String fontFamily = JoFontConstants.getFontFamily(symbolStyle);
            fontEncoding = FontEncoding.getEncoding(fontFamily);
            symbolFont = FontUtil.newFont(fontFamily, Font.PLAIN, 14);
        }

		buttons = new JButton[256];
		labels = new JoBigLabel[256];

        int col = 0;
        for (int nag=1; nag <= NAG_MAX; nag++)
        {
	        if (nag==NAG_DIAGRAM_DEPRECATED) continue;
            String tip = Language.getTip("pgn.nag."+nag);
            if (tip==null) continue;    //  not a valid annotation

            String text = null;
            if (fontEncoding != null)
                text = fontEncoding.getSymbol(nag);

            if (text != null)
                addButton(nag, symbolFont,text, tip, col++ % 2);
            else {
                text = Language.get("pgn.nag."+nag);
                if (text.length() > 4)
                    text = "$"+nag;
                addButton(nag, textFont, text, tip, col++ % 2);
            }
        }
    }

	public void updateLanguage()
	{
		for (int nag=1; nag <= NAG_MAX; nag++)
			if (labels[nag]!=null) {
				String tip = Language.getTip("pgn.nag."+nag);
				buttons[nag].setToolTipText(tip);
				labels[nag].setText(tip);
			}
	}

	public void reformat()
	{
		Style symbolStyle = Application.theUserProfile.getStyleContext().getStyle("body.symbol");
		textFont = new Font("Dialog", Font.PLAIN, 14);
		labelFont = new Font("Dialog", Font.PLAIN, 10);
		FontEncoding fontEncoding = null;

		if (symbolStyle != null) {
			String fontFamily = JoFontConstants.getFontFamily(symbolStyle);
			fontEncoding = FontEncoding.getEncoding(fontFamily);
			symbolFont = FontUtil.newFont(fontFamily, Font.PLAIN, 14);
		}

		for (int nag=1; nag <= NAG_MAX; nag++)
		{
			if (buttons[nag]==null) continue;

			String text = null;
			if (fontEncoding != null)
				text = fontEncoding.getSymbol(nag);

			if (text != null) {
				buttons[nag].setFont(symbolFont);
				buttons[nag].setText(text);
			}
			else {
				text = Language.get("pgn.nag."+nag);
				if (text.length() > 4)
					text = "$"+nag;

				buttons[nag].setFont(textFont);
				buttons[nag].setText(text);
			}
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
				    reformat();
			    else
				    /* only size modified; don't mind */ ;
		    }
	    };
	    map.put("styles.modified",action);
    }

    public void addButton(int nag, Font font, String text, String tip, int column)
    {
        String name = String.valueOf(nag);
        JButton button = new JButton();
        button.setName(name);
        button.setActionCommand(name);
        button.setFont(font);
        button.setText(text);
        button.setToolTipText(tip);
        button.addActionListener(this);
        button.setBorderPainted(false);
        button.setBackground(Color.white);
        button.setBorder(new LineBorder(Color.black,1,true));
        button.setMargin(margin);

        JoBigLabel label = new JoBigLabel(tip,1,40);
        label.setFont(labelFont);

		buttons[nag] = button;
		labels[nag] = label;

//        elementPane.add(button, (column==0) ? JoDialog.ELEMENT_ONE : JoDialog.ELEMENT_THREE);
//        elementPane.add(label, (column==0) ? JoDialog.ELEMENT_TWO : JoDialog.ELEMENT_FOUR);
		elementPane.add(button, JoDialog.ELEMENT_ONE);
		elementPane.add(label, JoDialog.ELEMENT_TWO);
    }

}
