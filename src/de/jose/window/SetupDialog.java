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
import de.jose.chess.Constants;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.chess.Board;
import de.jose.view.BoardEditView;
import de.jose.view.SetupBoardAdapter;
import de.jose.view.input.JoStyledLabel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Map;

/**
 * @author Peter Schäfer
 */
public class SetupDialog
		extends JoDialog
        implements ChangeListener
{
	protected SetupBoardAdapter setup;
	protected BoardEditView view;

    public void setupActionMap(Map map)
    {
        super.setupActionMap(map);    //To change body of overriden methods use Options | File Templates.

        CommandAction action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
                view.clearPosition();
            }
        };
        map.put("dialog.setup.clear",action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
                view.initialPosition();
	            checkBoxListen = false;
	            setValue("dialog.setup.frc.index",518);
	            checkBoxListen = true;
            }
        };
        map.put("dialog.setup.initial",action);

	    action = new CommandAction() {
	        public void Do(Command cmd) throws Exception
	        {
		        //  enable all castlings
		        checkBoxListen = false;
		        enableAllCastlings();
		        setFRCIndex(Board.SHUFFLE_CHESS, -1);
		        checkBoxListen = true;
	        }
	    };
	    map.put("dialog.setup.shuffle",action);

	    action = new CommandAction() {
	        public void Do(Command cmd) throws Exception
	        {
		        //  enable all castlings
		        checkBoxListen = false;
		        enableAllCastlings();
		        setFRCIndex(Board.FISCHER_RANDOM, -1);
		        checkBoxListen = true;
	        }
	    };
	    map.put("dialog.setup.frc",action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
                view.defaultPosition();
            }
        };
        map.put("dialog.setup.copy",action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception
            {
	            String next = (String)getValueByName("dialog.setup.next");
	            int nextColor = next.equals("white") ? Constants.WHITE:Constants.BLACK;
	            int moveNo = ((Integer)getValueByName("dialog.setup.move.no")).intValue();
	            setup.pos.setFirstMove(moveNo,nextColor);
	            adjustCastling();

                view.copyToClipboard();
            }
        };
        map.put("menu.edit.copy.fen",action);
        map.put("menu.edit.copy",action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
                view.pasteFromClipboard();
            }
        };
        map.put("menu.edit.paste",action);
    }

	public void enableAllCastlings()
	{
		setValue("dialog.setup.castling.frc",true);
		setValue("dialog.setup.castling.wk",true);
		setValue("dialog.setup.castling.wq",true);
		setValue("dialog.setup.castling.bk",true);
		setValue("dialog.setup.castling.bq",true);
	}

	public SetupDialog(String name)
		{
			super(name,false);
			readOnFailedSave = false;	//	keep data when save fails

			Dimension screensize = frame.getGraphicsConfiguration().getBounds().getSize();
			center(Math.min(screensize.width,640), Math.min(screensize.height,480));

			Position pos = new Position();
			pos.setOption(Position.INCREMENT_HASH, false);
			pos.setOption(Position.INCREMENT_REVERSED_HASH, false);
			pos.setOption(Position.EXPOSED_CHECK, false);

			setup = new SetupBoardAdapter(pos) {
					public void userMove(Move mv)
					{
						super.userMove(mv);
						adjustControls();
					}
			};

			view = new BoardEditView(setup);
			view.init();
			view.setBorder(new BevelBorder(BevelBorder.LOWERED));

			GridBagConstraints constr = new GridBagConstraints(0,0,
					1,GridBagConstraints.REMAINDER,
					100.0,1.0,
					GridBagConstraints.WEST,GridBagConstraints.BOTH,
					INSETS_NORMAL, 0,0);
			getElementPane().add(view, constr);

	    JPanel controls = newGridBox(null);

			add(controls, 0,0,2, reg(newRadioButton("dialog.setup.next.white")),ELEMENT_TWO_SMALL);
			add(controls, 0,1,2, reg(newRadioButton("dialog.setup.next.black")),ELEMENT_TWO_SMALL);
			newButtonGroup("dialog.setup.next");

			JSpinner spinner = newSpinner("dialog.setup.move.no");
			((SpinnerNumberModel)spinner.getModel()).setMinimum(new Integer(1));
			((SpinnerNumberModel)spinner.getModel()).setMaximum(new Integer(999));

			spinner.setMinimumSize(new Dimension(48,18));
			add(controls, 0,2,1, newLabel("dialog.setup.move.no"), LABEL_ONE);
			add(controls, 1,2,1, reg(spinner), ELEMENT_TWO);
			getElementPane().add(controls,ELEMENT_TWO_SMALL);

//		getElementPane().add(Box.createVerticalStrut(10),ELEMENT_TWO);

	    //  Castling Checkboxes
	    Box castl = Box.createVerticalBox();
			castl.setBorder(new TitledBorder(Language.get("dialog.setup.castling")));
			castl.add(reg(newCheckBox("dialog.setup.castling.wk",null,this)));
	    castl.add(reg(newCheckBox("dialog.setup.castling.wq",null,this)));
	    castl.add(reg(newCheckBox("dialog.setup.castling.bk",null,this)));
	    castl.add(reg(newCheckBox("dialog.setup.castling.bq",null,this)));

			JCheckBox frc_castl = (JCheckBox) castl.add(reg(newCheckBox("dialog.setup.castling.frc",null,this)));
			frc_castl.addChangeListener(this);

	    getElementPane().add(castl,ELEMENT_TWO_SMALL);

			//  frc spinner
			spinner = newSpinner("dialog.setup.frc.index");
			((SpinnerNumberModel)spinner.getModel()).setMinimum(new Integer(1));
			((SpinnerNumberModel)spinner.getModel()).setMaximum(new Integer(2880));
			spinner.getModel().setValue(new Integer(518));
			spinner.addChangeListener(this);

			Box frc = Box.createHorizontalBox();
			frc.setBorder(new TitledBorder(Language.get("dialog.setup.shuffle.title")));
			frc.add(reg(spinner));
//		frc.add(newSmallButton("dialog.setup.frc",       null));
			frc.add(newIconButton("dialog.setup.frc","menu.file.new.frc"));
			frc.add(newIconButton("dialog.setup.shuffle","menu.file.new.shuffle"));

			getElementPane().add(frc,ELEMENT_TWO_SMALL);

	    //  Edit buttons
	    StringBuffer buf = new StringBuffer();
	    buf.append("<div style='font-size:10pt;'>");
	    newLinkButton(buf,"dialog.setup.clear",     "menu.edit.clear"); buf.append("<br>");
	    newLinkButton(buf,"dialog.setup.initial",   "menu.web.home"); buf.append("<br>");
	    newLinkButton(buf,"dialog.setup.copy",      "menu.edit.copy"); buf.append("<br>");
	    newLinkButton(buf,"menu.edit.copy.fen",     "menu.edit.copy"); buf.append("<br>");
	    newLinkButton(buf,"menu.edit.paste",        "menu.edit.paste");
	    buf.append("</div>");

	    JoStyledLabel buttons = new JoStyledLabel(buf.toString());
	    buttons.addActionListener(this);
	    getElementPane().add(buttons,ELEMENT_TWO);

			addButtons(OK_CANCEL);
			addSpacer(20);
			addButton(REVERT);
			addSpacer(10);
			addButton(HELP);

			JoMenuBar.assignMnemonics(buttonPane);
		}

	/**	update when shown	*/
	public void show()
	{
		super.show();
		read();
	}

	public void setFEN(String fen)
	{
		view.setup(fen);
	}

	public void setFRCIndex(int frcVariant, int frcIndex)
	{
        frcIndex = view.setup(frcVariant,frcIndex);
	    setValue("dialog.setup.frc.index", frcIndex);
	}

	public void read()
	{
		//	update color info
		view.updateProfile(Application.theUserProfile);
		//	copy position from main board panel
		Position mainPos = AbstractApplication.theAbstractApplication.theGame.getPosition();

		setup.pos.setup(mainPos);
		view.refresh(true);

		checkBoxListen = false;
		setValue    ("dialog.setup.next",        setup.pos.whiteMovesNext() ? "white":"black");
		setValue    ("dialog.setup.move.no",     setup.pos.gameMove());
		setValue("dialog.setup.castling.frc", Application.theUserProfile.getBoolean("dialog.setup.castling.frc",true));
		checkBoxListen = true;

		adjustControls();
	}

	public boolean save()
	{
		String next = (String)getValueByName("dialog.setup.next");
		int nextColor = next.equals("white") ? Constants.WHITE:Constants.BLACK;
		int moveNo = ((Integer)getValueByName("dialog.setup.move.no")).intValue();

		setup.pos.setFirstMove(moveNo,nextColor);
		adjustCastling();

		String[] error = setup.pos.checkLegality();
		if (error != null) {
			JoDialog.showErrorDialog(error);
			return false;
		}

		String[] warning = setup.pos.checkPlausibility();
		if ((warning != null) &&
			(JoDialog.showOKCancelDialog(warning)==JOptionPane.CANCEL_OPTION))
			return false;

		String fen = setup.pos.toString();
		Command cmd = new Command("new.game.setup",null,fen);
		Application.theCommandDispatcher.forward(cmd,this);
		return true;
	}

	private void adjustCastling()
	{
		boolean classicRules = !getBooleanValue("dialog.setup.castling.frc");
		setup.pos.userSetCastling(
		    isEnabled("dialog.setup.castling.wk") && getBooleanValue("dialog.setup.castling.wk"),
		    isEnabled("dialog.setup.castling.wq") && getBooleanValue("dialog.setup.castling.wq"),
		    isEnabled("dialog.setup.castling.bk") && getBooleanValue("dialog.setup.castling.bk"),
		    isEnabled("dialog.setup.castling.bq") && getBooleanValue("dialog.setup.castling.bq"),
		    classicRules);
	}

	/**
	 * implements IBoardAdapter
	 */

	protected boolean checkBoxListen = true;

	protected void adjustControls()
	{
		//  respond to position changes
		checkBoxListen = false;
		boolean classicRules = !getBooleanValue("dialog.setup.castling.frc");
		Application.theUserProfile.set("dialog.setup.castling.frc",classicRules);

//		setValue    ("dialog.setup.next",        setup.pos.whiteMovesNext() ? "white":"black");
//		setValue    ("dialog.setup.move.no",     setup.pos.gameMove());

		if (setEnabled("dialog.setup.castling.wk", setup.pos.couldCastle(Position.WHITE_KINGS_CASTLING,classicRules)))
			setValue("dialog.setup.castling.wk", setup.pos.canCastle(Position.WHITE_KINGS_CASTLING));

		if (setEnabled("dialog.setup.castling.wq", setup.pos.couldCastle(Position.WHITE_QUEENS_CASTLING,classicRules)))
			setValue("dialog.setup.castling.wq", setup.pos.canCastle(Position.WHITE_QUEENS_CASTLING));

		if (setEnabled("dialog.setup.castling.bk", setup.pos.couldCastle(Position.BLACK_KINGS_CASTLING,classicRules)))
			setValue("dialog.setup.castling.bk", setup.pos.canCastle(Position.BLACK_KINGS_CASTLING));

		if (setEnabled("dialog.setup.castling.bq", setup.pos.couldCastle(Position.BLACK_QUEENS_CASTLING,classicRules)))
			setValue("dialog.setup.castling.bq", setup.pos.canCastle(Position.BLACK_QUEENS_CASTLING));

		checkBoxListen = true;
	}

	public void stateChanged(ChangeEvent e) {
		//  respond to checkbox changes
		if (checkBoxListen)
		{
			adjustCastling();
			adjustControls();
		}
		//  FRC

		if (((Component)e.getSource()).getName().equals("dialog.setup.frc.index"))
		{
			int frcIndex = getIntValue("dialog.setup.frc.index");
			if (frcIndex>960)
				setFRCIndex(Board.SHUFFLE_CHESS, frcIndex);
			else
				setFRCIndex(Board.FISCHER_RANDOM, frcIndex);
		}
	}
}
