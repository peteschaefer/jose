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

package de.jose.window;

import de.jose.*;
import de.jose.util.StringUtil;
import de.jose.chess.Position;
import de.jose.pgn.Game;
import de.jose.pgn.PgnConstants;
import de.jose.pgn.PgnUtil;
import de.jose.pgn.TagNode;
import de.jose.view.DocumentPanel;
import de.jose.view.input.JDateField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GameDetailsDialog
		extends JoTabDialog
		implements PgnConstants
{
	protected int GId;
	protected Game game;
	protected HashMap values;
	protected JPanel moreTags;

	protected static GridBagConstraints KEY_ONE =
			new GridBagConstraints(0,GridBagConstraints.RELATIVE, 1,1, 0,0,
								   GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL,
								   INSETS_NORMAL, 0,0);

    protected static final Dimension SIZE = Version.mac ?
                    new Dimension(420,280) : new Dimension(420,240);

	public GameDetailsDialog(String name)
	{
		super(name,true);
        frame.setSize(SIZE.width,SIZE.height);

		addTab(newGridPane());
		addTab(newGridPane());
		addTab(newGridPane());

		addButtons(OK_CANCEL);
		addSpacer(20);
		addButtons(APPLY_REVERT);
		addSpacer(10);
		addButton(HELP);

		values = new HashMap();

		JoMenuBar.assignMnemonics(getTabbedPane());
	}

	public void initTab0(Component comp0)
	{
		JPanel tab0 = (JPanel)comp0;
		JTextField field;

		tab0.add(newLabel("dialog.details.event"),LABEL_ONE);
		add(tab0, newTextField(TAG_EVENT), ELEMENT_ROW);

		tab0.add(newLabel("dialog.details.site"),LABEL_ONE);
		add(tab0, newTextField(TAG_SITE), ELEMENT_ROW);

		tab0.add(newLabel("dialog.details.date"),LABEL_ONE);
		add(tab0, field=newPgnDateField(TAG_DATE), ELEMENT_TWO_SMALL);

		tab0.add(newLabel("dialog.details.round"),LABEL_THREE);
		add(tab0, field=newTextField(TAG_ROUND), ELEMENT_FOUR_SMALL);
		setColumns(field,8);

		tab0.add(newLabel("dialog.details.eventdate"), LABEL_ONE);
		add(tab0, field=newPgnDateField(TAG_EVENT_DATE), ELEMENT_TWO);

		tab0.add(newLabel("dialog.details.board"), LABEL_THREE);
		add(tab0, field=newTextField(TAG_BOARD), ELEMENT_FOUR_SMALL);
		setColumns(field,8);

		tab0.add(new JLabel(""),ELEMENT_REMAINDER);
	}

	public void initTab1(Component comp1)
	{
		JPanel tab1 = (JPanel)comp1;
		JTextField field;

		tab1.add(newLabel(""), LABEL_ONE);
		tab1.add(newLabel("dialog.details.white"), ELEMENT_TWO_SMALL);
		tab1.add(newLabel("dialog.details.black"), ELEMENT_ROW_SMALL);

		tab1.add(newLabel("dialog.details.name"), LABEL_ONE);
		add(tab1, newTextField(TAG_WHITE), ELEMENT_TWO);
		add(tab1, newTextField(TAG_BLACK), ELEMENT_ROW);

		tab1.add(newLabel("dialog.details.elo"), LABEL_ONE);
		add(tab1, field=newIntegerField(TAG_WHITE_ELO), ELEMENT_TWO_SMALL);
		setColumns(field,4);
		add(tab1, field=newIntegerField(TAG_BLACK_ELO), ELEMENT_ROW_SMALL);
		setColumns(field,4);

		tab1.add(newLabel("dialog.details.title"), LABEL_ONE);
		add(tab1, field=newTextField(TAG_WHITE_TITLE), ELEMENT_TWO_SMALL);
		setColumns(field,12);
		add(tab1, field=newTextField(TAG_BLACK_TITLE), ELEMENT_ROW_SMALL);
		setColumns(field,12);

		tab1.add(newLabel("dialog.details.result"), LABEL_ONE);
		Box box = Box.createHorizontalBox();
		add(box,newToggleButton(TAG_RESULT+".1-0"),null);
		add(box,newToggleButton(TAG_RESULT+".0-1"),null);
		add(box,newToggleButton(TAG_RESULT+".1/2"),null);
		add(box,newToggleButton(TAG_RESULT+".*"),null);
		newButtonGroup(TAG_RESULT);
		tab1.add(box, ELEMENT_ROW);

		tab1.add(new JLabel(""),ELEMENT_REMAINDER);
	}

	public void initTab2(Component comp2)
	{
		JPanel tab2 = (JPanel)comp2;
		JTextField field;

		moreTags = newGridPane();
//		moreTags.setBorder(new EmptyBorder(0,0,0,0));

		moreTags.add(newLabel("dialog.details.eco"),LABEL_ONE);
		add(moreTags, field=newTextField(TAG_ECO), ELEMENT_TWO);
		field.setColumns(4);

		JButton add_button;
		add(moreTags, add_button=newButton("dialog.details.add","add"), ELEMENT_ROW_SMALL);
		add_button.setBorder(new EmptyBorder(0,0,0,0));

		moreTags.add(newLabel("dialog.details.opening"),LABEL_ONE);
		add(moreTags, field=newTextField(TAG_OPENING), ELEMENT_ROW);

		moreTags.add(newLabel("dialog.details.annotator"),LABEL_ONE);
		add(moreTags, newTextField(TAG_ANNOTATOR), ELEMENT_ROW);

		JScrollPane scroller = new JScrollPane(moreTags);
		scroller.setBorder(new EmptyBorder(0,0,0,0));
		tab2.add(scroller,ELEMENT_REMAINDER);

//		tab2.add(new JLabel(""),ELEMENT_REMAINDER);
	}

	public void show()
	{
		Component owner = Application.theApplication.docPanel();
		if (owner!=null && owner.isShowing())
			stagger(owner,10,10, SIZE.width,SIZE.height);
		else
			center(SIZE.width,SIZE.height);

		super.show();
	}

	public void setGame(Game game)
	{
		GId = 0;
		this.game = game;
		values.clear();
		game.getTagValues(values);
	}

	public void setGameId(int GameId) throws Exception
	{
		GId = GameId;
		game = new Game(null, new Position());
		values.clear();
		game.readInfo(GId);
		game.getTagValues(values);
	}

	public void readTab0()  { read(0,values); }
	public void readTab1()  { read(1,values); }

	private JTextField getMoreKey(int j)
	{
		String key = "more.key."+j;
		JTextField field = (JTextField)elements.get(key);
		if (field==null) {
			add(moreTags, field=newTextField(key), KEY_ONE);
			field.setHorizontalAlignment(JTextField.RIGHT);
		}
		else
			field.setVisible(true);
		return field;
	}

	private JTextField getMoreValue(int j)
	{
		String key = "more.val."+j;
		JTextField field = (JTextField)elements.get(key);
		if (field==null)
			add(moreTags, field=newTextField(key), ELEMENT_ROW);
		else
			field.setVisible(true);
		return field;
	}

	private boolean hideMoreKey(int j)
	{
		String key = "more.key."+j;
		Component field = (Component)elements.get(key);
		if (field!=null)
			field.setVisible(false);
		return field!=null;
	}

	private boolean hideMoreValue(int j)
	{
		String key = "more.val."+j;
		Component field = (Component)elements.get(key);
		if (field!=null)
			field.setVisible(false);
		return field!=null;
	}

	private int addMoreLine()
	{
		for (int j=1; j <= 24; j++)
		{
			JTextField field = (JTextField)elements.get("more.key."+j);
			if (field==null || !field.isVisible()) {
				getMoreKey(j);
				getMoreValue(j);
				return j;
			}
		}
		return -1;
	}

	public void readTab2()
	{
		read(2,values);

		Iterator i = game.getMoreTags().iterator();
		int j=1;
		while (i.hasNext()) {
			TagNode tag = (TagNode)i.next();
			String key = tag.getKey();
			Object value = tag.getValue();

			if (Game.DEFAULT_TAGS.contains(key)) continue;

			if (Game.TAG_VARIANT.equalsIgnoreCase(key)
					&& StringUtil.isEmpty((String)value)) continue;
			//  ignore empty "VARIANT" tag

			values.remove(tag.getKey());
			values.put("more.tag."+j, tag);

			getMoreKey(j).setText(key);
			getMoreValue(j).setText((String)value);

			j++;
		}

		while (hideMoreKey(j) && hideMoreValue(j)) {
            values.remove("more.tag."+j);
			j++;
        }
	}

	public boolean save()
	{
		java.util.List errors = save(values);

		if (errors!=null) {
			//  parse error
			Throwable ex = (Throwable)errors.get(0);
			if (ex instanceof NumberFormatException) {
				String text = Language.get("query.error.number.format")+"\n'"+ex.getMessage()+"'";
				JoDialog.showErrorDialog(text);
				return false;
			}
			else if (ex instanceof JDateField.DateFormatException) {
				String text = Language.get("query.error.date.format")+"\n'"+ex.getMessage()+"'";
				JoDialog.showErrorDialog(text);
				return false;
			}
			else {
				Application.error(ex);
				return false;
			}
		}


		for (int j=1; j<=24; j++) {
			String key = (String)values.get("more.key."+j);
			String value = (String)values.get("more.val."+j);
			TagNode tag = (TagNode)values.get("more.tag."+j);

			values.remove("more.key."+j);
			values.remove("more.val."+j);
			values.remove("more.tag."+j);

			if (StringUtil.isEmpty(key) || StringUtil.isEmpty(value))
				continue;

			if (tag!=null) {
				tag.setKey(key);
				tag.setValue(value);
			}
			values.put(key,value);
		}

		values.remove("dialog.details.add");    //  can't use it

		//  apply to current game / or database

        String resString = (String)values.remove(TAG_RESULT);
		game.setTagValues(values);
        byte result = PgnUtil.parseResult(resString);
		boolean resultDirty = game.setResult(result);

		if (GId > 0) {
			if (!saveToDatabase(game,resultDirty)) return false;
		}
		else {
			//	update document panel
			DocumentPanel docPanel = Application.theApplication.docPanel();
			if (docPanel!=null)
				docPanel.reformat();
			else
				game.reformat();	//	does this make sense ?

			//  ... and save to database
			if (game.getId() > 0 && !saveToDatabase(game,resultDirty)) return false;
		}

		return true;
	}

	public void setupActionMap(Map map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				addMoreLine();
				moreTags.revalidate();
			}
		};

		map.put("dialog.details.add",action);
	}

	private boolean saveToDatabase(Game game, boolean resultDirty)
	{
		try {
			//	save directly to database
			game.saveInfo(resultDirty);
			Application.theApplication.broadcast(new Command("game.modified",null,new Integer(game.getId())));
			return true;
		} catch (Exception ex) {
			Application.error(ex);
			return false;
		}
	}

}
