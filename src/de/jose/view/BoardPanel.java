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

package de.jose.view;

import de.jose.*;
import de.jose.chess.EngUtil;
import de.jose.pgn.DiagramNode;
import de.jose.image.ImgUtil;
import de.jose.chess.Constants;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.plugin.AnalysisRecord;
import de.jose.plugin.EnginePlugin;
import de.jose.plugin.Plugin;
import de.jose.plugin.Score;
import de.jose.profile.LayoutProfile;
import de.jose.profile.UserProfile;
import de.jose.profile.FontEncoding;
import de.jose.util.AWTUtil;
import de.jose.util.ClipboardUtil;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.datatransfer.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.io.IOException;

/**
 * Panel that displays the chess board
 */

public class BoardPanel
		extends JoPanel
		implements IBoardAdapter, Constants, CommandListener, MessageListener, ClipboardOwner
{
	
	private BoardView2D view2d;
	private BoardView3D view3d;
	private CardLayout layout;

	/**	this object is responsible for painting and user interaction
	 */
	protected BoardView theView;
	
	/**	reference to Application.theApplication.theGame.position	 */
	protected Position position;
	
	/**	can the user select pieces with the mouse ?	 */
	protected boolean mouseSelect;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------
	
	public BoardPanel(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
	{
		super(profile,withContextMenu,withBorder);
		
		position = AbstractApplication.theAbstractApplication.theGame.getPosition();
		mouseSelect = true;
		titlePriority = 10;

		setLayout(layout = new CardLayout());
		setDoubleBuffered(Version.useDoubleBuffer());
		//	BoardViews will use their own buffering techniques
        setOpaque(true);
		setFocusable(false);    //  don't request keyboard focus
	}

	public void init()
		throws Exception
	{
		if (AbstractApplication.theAbstractApplication != null && AbstractApplication.theAbstractApplication.getEnginePlugin() != null)
			connectTo(Application.theApplication.getEnginePlugin());
		
		boolean use3d = AbstractApplication.theUserProfile.getBoolean("board.3d")
						&& Version.hasJava3d(false,false);
		
		if (use3d)
			set3d();
		else
			set2d();
	}

	public BoardView getView ()
	{
		return theView;
	}

	public void updateProfile(UserProfile prf)
		throws Exception
	{
		if (view2d != null) view2d.updateProfile(prf);
		if (view3d != null) {
			if (/*view3d.needsReset(prf)*/false) {
				System.err.println("3D reset");
				AWTUtil.setWaitCursor(this);
				view3d.closeScene();
				view3d.init();
				AWTUtil.setDefaultCursor(this);
				//	doesn't work, why ? (TODO)
			}
			else
				view3d.updateProfile(prf);
		}
		if (theView != null) theView.refresh(true);
	}

	protected void connectTo(Plugin plugin)
	{
		plugin.addMessageListener(this);
	}
	
	public void set2d()
	{
		if (is2d()) return;
		
		if (theView != null)
		{
			theView.setVisible(false);
			theView.activate(false);
		}
		
		if (view2d==null) {
			view2d = new BoardView2D(this,true);
			view2d.init();
			add(view2d, "2d");
		}
		
		if (theView != null) {
			view2d.flip(theView.flipped);
			view2d.showCoords(theView.showCoords);
			view2d.showEvalbar(theView.showEvalbar);
			view2d.showSuggestions(theView.showSuggestions);
		}
		
		theView=view2d;
		layout.show(this,"2d");
		
		theView.setVisible(true);
		theView.activate(true);
	}

	public void set3d() throws Exception
	{
		set3d(null);
	}

    protected JLabel showWaitLabel()
    {
        if (view2d!=null) {
            JLabel waitLabel = new JLabel(Language.get("wait.3d"));
   //         waitLabel.setForeground(Color.red);
   //         waitLabel.setBackground(Color.lightGray);
            waitLabel.setOpaque(true);
            waitLabel.setHorizontalAlignment(JLabel.CENTER);
            waitLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
            waitLabel.setFont(waitLabel.getFont().deriveFont(24f));
            waitLabel.setSize(view2d.getWidth()-60,80);
            AWTUtil.centerOn(waitLabel,this);

            view2d.add(waitLabel);
            //  paint immediateley
            waitLabel.paint(waitLabel.getGraphics());
            return waitLabel;
        }
        else
            return null;
    }

	public void set3d(GraphicsConfiguration gc)
	        throws Exception
	{
		if (is3d()) return;

		if (theView != null)
		{
			theView.setVisible(false);
			theView.activate(false);
		}

        JLabel waitLabel = null;
        try {

            if (view3d==null) {
                waitLabel = showWaitLabel();

				Version.hasJava3d(true,Application.theUserProfile.getBoolean("board.3d.ogl"));
                AWTUtil.setWaitCursor(this);
                view3d = new BoardView3D(this);
                view3d.init(gc);
                add(view3d, "3d");
                AWTUtil.setDefaultCursor(this);
            }

            if (theView != null) {
                view3d.flip(theView.flipped);
                view3d.showCoords(theView.showCoords);
				view3d.showEvalbar(theView.showEvalbar);
				view3d.showSuggestions(theView.showSuggestions);
            }

            theView = view3d;
            layout.show(this,"3d");

            theView.setVisible(true);
            theView.activate(true);

        } finally {
            if (waitLabel!=null)
                view2d.remove(waitLabel);
        }
	}

	public boolean is2d()		{	return theView != null && theView == view2d;	}
	public boolean is3d()		{	return theView != null && theView == view3d;	}

	public boolean has2d()		{	return view2d != null; }
	public boolean has3d()		{	return view3d != null; }

	public BoardView2D get2dView()	{ return view2d; }
	public BoardView3D get3dView()	{ return view3d; }


	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------

	
	public boolean isContinuousLayout()
	{
		return true;
	}


	public void startContinuousResize()
	{
		/** while continous resizing:
		 *  don't compute piece images, it's just too expensive
		 *  scale down the current image (looks good enough)
		 */
		if (is2d()) get2dView().startContinuousResize();
	}

	public void finishContinuousResize()
	{
		/**
		 * return to normal painting
		 */
		if (is2d()) get2dView().finishContinuousResize();
	}

	public void move(Move mv, float time)
	{
		if (theView!=null) theView.move(mv,time);
	}


	//-------------------------------------------------------------------------------
	//	Interface JoComponent
	//-------------------------------------------------------------------------------
	
	public void adjustContextMenu(Collection list, MouseEvent event)
	{
		super.adjustContextMenu(list,event);
		list.add(ContextMenu.SEPARATOR);
		
		if (Version.hasJava3d(false,false))
		{
			list.add(Util.toBoolean(is2d()));
			list.add("menu.game.2d");
			list.add(Util.toBoolean(is3d()));
			list.add("menu.game.3d");
			list.add(ContextMenu.SEPARATOR);
		}

		list.add(AbstractApplication.theUserProfile.get("board.flip"));
		list.add("menu.game.flip");
		
		list.add(AbstractApplication.theUserProfile.get("board.coords"));
		list.add("menu.game.coords");

		list.add(AbstractApplication.theUserProfile.get("board.evalbar"));
		list.add("menu.game.evalbar");

		list.add(AbstractApplication.theUserProfile.get("board.suggestions"));
		list.add("menu.game.suggestions");

		if (is3d()) {
            list.add(AbstractApplication.theUserProfile.get("board.3d.clock"));
            list.add("board.3d.clock");
			list.add("board.3d.screenshot");
			list.add("board.3d.defaultview");
		}

		//  Clipboard
		list.add(ContextMenu.SEPARATOR);

		ArrayList submenu = new ArrayList();
		submenu.add("menu.edit.copy");     //  copy FEN/image
		if (!is3d())
			submenu.add("menu.edit.copy.imgt");
		submenu.add("menu.edit.copy.img");
		submenu.add("menu.edit.copy.text");
		submenu.add("menu.edit.copy.fen");

		list.add(submenu);

		list.add("menu.edit.paste");    //  paste FEN

		list.add(ContextMenu.SEPARATOR);

        list.add("menu.edit.search.current");

		list.add("menu.edit.option");
	}
	
	public float getWeightX()	{ return 3.0f; }
	public float getWeightY()	{ return 8.0f; }
	

	//-------------------------------------------------------------------------------
	//	Interface CommandListener
	//-------------------------------------------------------------------------------
	
	public void setupActionMap(Map map)
	{
		super.setupActionMap(map);

		CommandAction action;

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (is3d())
					view3d.setDefaultOrbit(view3d.flipped);
			}
		};
		map.put("board.3d.defaultview", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (is3d()) {
					boolean on = !AbstractApplication.theUserProfile.getBoolean("board.3d.clock");
					AbstractApplication.theUserProfile.set("board.3d.clock",on);
					view3d.showClock(on);
				}
			}
		};
		map.put("board.3d.clock", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				boolean flip;
				if (cmd.data != null)
					flip = ((Boolean)cmd.data).booleanValue();
				else
					flip = !theView.flipped;	//	toggle
                AbstractApplication.theUserProfile.set("board.flip",flip);
				if (theView!=null)
				    theView.flip(flip);
			}
		};
		map.put("broadcast.board.flip", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (theView!=null) {
					boolean show;
				 	if (cmd.data != null)
						show = ((Boolean)cmd.data).booleanValue();
					else
					 	show = !theView.showCoords; //	toggle
                    AbstractApplication.theUserProfile.set("board.coords",show);
					theView.showCoords(show);
				}
			}
		};
		map.put("broadcast.board.coords", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (theView!=null) {
					boolean show;
					if (cmd.data != null)
						show = ((Boolean)cmd.data).booleanValue();
					else
						show = !theView.showEvalbar; //	toggle
					AbstractApplication.theUserProfile.set("board.evalbar",show);
					theView.showEvalbar(show);
				}
			}
		};
		map.put("broadcast.board.evalbar", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (theView!=null) {
					boolean show;
					if (cmd.data != null)
						show = ((Boolean)cmd.data).booleanValue();
					else
						show = !theView.showSuggestions; //	toggle
					AbstractApplication.theUserProfile.set("board.suggestions",show);
					theView.showSuggestions(show);
				}
			}
		};
		map.put("broadcast.board.suggestions", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (theView!=null)
				    theView.refresh(true);
			}
		};
		map.put("menu.file.new", action);
		map.put("menu.edit.undo", action);
		map.put("broadcast.edit.game", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				//  copy FEN string
				ClipboardUtil.setPlainText(position.toString(), BoardPanel.this);
			}
		};
		map.put("menu.edit.copy.fen", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				//  copy styled text (text/html and/or text/rtf ??)
				String fontFamily = Application.theUserProfile.getString("font.diagram");
				FontEncoding enc = FontEncoding.getEncoding(fontFamily);
				String text = DiagramNode.toString(position.toString(),enc);

				ClipboardUtil.setStyledText(text, fontFamily, 16, BoardPanel.this);
			}
		};
		map.put("menu.edit.copy.text", action);


		action = new CommandAction() {
			public void Do(Command cmd) {
				//  copy image with opaque/or transparent background
				captureImage(cmd.code.endsWith(".imgt"));
			}
		};
		map.put("menu.edit.copy.img", action);
		map.put("menu.edit.copy.imgt", action);
		map.put("menu.edit.copy", action);


		action = new CommandAction() {
			public void Do(Command cmd) {
				//  paste FEN from clipboard
				String fen = ClipboardUtil.getPlainText(this);
				if (fen != null)
					try {
						//  note that this is not necessarily a vaild FEN string
//						cmd = new Command("menu.game.setup",cmd.event,fen,null); //  displays SetupDialog
						cmd = new Command("new.game.setup",null,fen);   //  set the position immediately
						Application.theCommandDispatcher.forward(cmd, Application.theApplication);
					} catch (Throwable e) {
						/** parse error in FEN string ? don't mind  */
						AWTUtil.beep(BoardPanel.this);  //  "beep"
					}
				else
					AWTUtil.beep(BoardPanel.this);  //  "beep"
			}
		};
		map.put("menu.edit.paste", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (theView!=null)
				    theView.refresh(false);
			}
		};
  		map.put("move.notify", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				UserProfile prf = (UserProfile)cmd.data;
				if (view2d != null) view2d.storeProfile(prf);
				if (view3d != null) view3d.storeProfile(prf);
				prf.set("board.3d", is3d());
			}
		};
		map.put("update.user.profile", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (view3d != null) view3d.close();
				//	not strictly necessary but may be useful
			}
		};
		map.put("menu.file.quit", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				connectTo((Plugin)cmd.data);
			}
		};
		map.put("new.plugin", action);


		action = new CommandAction() {
			public void Do(Command cmd) {
				if (theView!=null) theView.refresh(true);
			}
		};
		map.put("switch.game", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (getView()!=null)
					getView().showAnimationHints = Util.toboolean(cmd.moreData);
			}
		};
		map.put("change.animation.settings",action);
	}
	
	public void handleMessage(Object who, int what, Object data)
	{
		//	message from Plugin
		//  or from view, after an image has been captured
		switch (what) {
		case BoardView.MESSAGE_CAPTURE_IMAGE:
			try {
				ClipboardUtil.setImage((Image)data,this);
			} catch (Exception e) {
				Application.error(e);
			}
			break;

		case EnginePlugin.THINKING:	mouseSelect = false; break;
		default:				    mouseSelect = true; break;
		}
		//	update eval bar
		switch(what) {
			case EnginePlugin.ANALYZING:
			case EnginePlugin.THINKING:
			case EnginePlugin.PONDERING:
				AnalysisRecord a = (AnalysisRecord)data;
				EnginePlugin plugin = (EnginePlugin)who;
				if (a==null || a.maxpv==0)
					theView.setScore(null,null);	//	nothing to be done
				else
					theView.setScore(a.eval[0],plugin);
				theView.repaint();
				break;

			case EnginePlugin.PAUSED:
				theView.setScore(null,null);
				theView.repaint();
				break;
		}

		//	update suggestions (only in ANALYZTE mode, not during thinking and pondering)
		switch(what) {
			case EnginePlugin.ANALYZING:
				//theView.hideAllHints(false);
				if (theView.showSuggestions) {
					AnalysisRecord a = (AnalysisRecord)data;
					EnginePlugin plugin = (EnginePlugin)who;
					if (a==null) break;
					if (!a.wasPvModified()) break;
					showAnalysisHints(a, plugin);
				}
				break;
			case EnginePlugin.PAUSED:
				//	hide all suggestions
				theView.hideAllHints(true);
				break;
		}
	}

	protected void captureImage(boolean transparent)
	{
		//  may return immediately
		//  upon complete, the command will be issues to ourself
		theView.captureImage(this,transparent);
	}

	protected void showAnalysisHints(AnalysisRecord a, EnginePlugin plugin)
	{
		ArrayList<Hint> hints = new ArrayList<Hint>();
		if (a.maxpv==0) return;

		int pov = (a.white_next) ? 1 : -1;
		int cp = a.eval[0].cp*pov;
		int cpmin = cp;
		int cpmax = cp;

		int MAX_HINTS = 6;	//	don't show too many
		int SCORE_DROP = 15;	//	don't shaw bad moves

		//	find interesting moves from PV list
		for (int idx=0; idx < a.maxpv && hints.size() < MAX_HINTS; idx++)
		{
			if (a.moves[idx]==null || a.moves[idx].isEmpty())
				continue;
			cp = a.eval[idx].cp*pov;
			if (cp < (cpmin-SCORE_DROP))
				break;	//	move is not interesting

			if (cp > cpmax) cpmax = cp;
			if (cp < cpmin) cpmin = cp;

			Move mv = a.moves[idx].get(0);
			Hint hint = new Hint(0,mv.from,mv.to,null,null);
			hint.implData = cp;
			hints.add(hint);
		}
		//	update colors
		for(Hint hint : hints)
		{
			cp = (Integer)hint.implData;
			assert(cp>=cpmin && cp<=cpmax);
			float cp1;
			if (cpmin==cpmax)
				cp1 = 1.0f;
			else
				cp1 = (float)(cp-cpmin) / (cpmax-cpmin);
			hint.color = suggestionColor(cp1);
		}
		//	sort by Z order
		Collections.sort(hints,new CompareHintsByZorder());
		//	add to view
		theView.showAllHints(hints);
	}

	public static class CompareHintsByZorder implements Comparator<Hint>
	{
		public int compare(Hint a, Hint b) {
			if (a.from != b.from)
				return (a.from - b.from);
			int la = EngUtil.euclidDistSq(a.from,a.to);
			int lb = EngUtil.euclidDistSq(b.from,b.to);
			return la-lb;
		}
	}


	//-------------------------------------------------------------------------------
	//	implements IBoardAdapter
	//-------------------------------------------------------------------------------
	
	public Position getPosition() 				{ return position; }

	/**	get a piece from the internal board 	 */
	public final int pieceAt(int square)		{ return position.pieceAt(square); }
	
	/**	get a piece from the internal board 	 */
	public final int pieceAt(int file, int row)	{ return position.pieceAt(file,row); }

	public final int movesNext()				{ return position.movesNext(); }
	
	public final boolean canMove(int square)	{ return mouseSelect && position.canMove(square); }
	
	/**	@return true if the given move is legal
	 */
	public final boolean isLegal(Move mv)
	{
		if (position.isMate()) {
			mv.setMate();
			return false;
		}
		else if (position.isStalemate()) {
			mv.setStalemate();
			return false;
		}
		/**
		 * Draw_3 and Draw_50 are accepted as legal moves (though the game is actually finished)
		 */

		int oldOptions = position.getOptions();
		position.setOptions(Position.DETECT_ALL);

		boolean result = position.tryMove(mv);
		if (result) position.undoMove();

		position.setOptions(oldOptions);
		return result;
	}
	
	/**	make a user move
	 */
	public final void userMove(Move mv)
	{
		AbstractApplication.theCommandDispatcher.handle(new Command("move.user", null, mv),this);
	}
	

	public void showHint(Object data)
	{
		//  requested hint: explicitly show hint
		if (isShowing())
		{
			BoardView bv = getView();
			if (bv!=null) {
				if (data instanceof Move)
					bv.showHint((Move)data, 2000, BoardView.ENGINE_HINT_COLOR);
				else
					bv.showHint(EnginePanel.getHintTip(data), 2000, BoardView.ENGINE_HINT_COLOR);
}
		}
//		 bHint.setText(data.toString());
	}

	public static Color suggestionColor(float val)
	{
		Color base;
		if (val >= 0.95)
			base = Color.blue;
		else if (val >= 0.65)
			base = Color.green;
		else if (val >= 0.30)
			base = Color.yellow;
		else
			base = Color.red;
		int rgb = base.getRGB();
		rgb += 48<<24;	//	alpha
		return new Color(rgb,true);
	}

}
