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
import de.jose.window.JoMenuBar;
import de.jose.image.ImgUtil;
import de.jose.pgn.*;
import de.jose.profile.LayoutProfile;
import de.jose.profile.UserProfile;
import de.jose.task.GameSource;
import de.jose.task.Task;
import de.jose.task.NalimovOnlineQuery;
import de.jose.task.db.GameUtil;
import de.jose.util.AWTUtil;
import de.jose.util.ListUtil;
import de.jose.view.input.MoveFormatList;
import de.jose.view.input.LanguageList;
import de.jose.view.style.JoStyleContext;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import static de.jose.pgn.INodeConstants.ANNOTATION_NODE;

public class DocumentPanel
		extends JoPanel
		implements ChangeListener, MouseListener, ClipboardOwner
{
	/** sent by DocumentEditor when a move is input on the keyboard */
	public static final int EVENT_USER_MOVE = 1001;

	protected Game theGame;
	/**	provisional; this will be replaced by de.jose.Document	 */

	protected JoTabbedPane theTabPane;
	protected JButton closeButton;
	/**	-1 indicates that we do not use a tab pane	*/
	protected int currentTabIndex;

	protected DocumentEditor theTextPane;
	protected JScrollPane theScroller;

//	protected static ImageIcon tabIcon 		= ImgUtil.getIcon(null,"tab.close");
//	protected static ImageIcon tabIconOff 	= ImgUtil.getIcon(null,"tab.close.off");
	protected static ImageIcon dirtyIcon    = ImgUtil.getIcon(null,"tab.dirty");

	class DocumentPanelLayout extends OverlayLayout
	{
		DocumentPanelLayout() {
			super(DocumentPanel.this);
		}

		public void layoutContainer(Container target)
        {
			super.layoutContainer(target);            
			if (currentTabIndex >= 0 && theTabPane != null) {
				//  put the close button in the upper right corner
				Rectangle bounds = target.getBounds();
				Rectangle tabBounds = theTabPane.getBoundsAt(theTabPane.getSelectedIndex());

                if (Version.mac)
                    closeButton.setBounds(12,4, 24,24);
                else switch (theTabPane.getTabLayoutPolicy())
                {
				case JTabbedPane.SCROLL_TAB_LAYOUT:
						closeButton.setBounds(bounds.width-48, tabBounds.y+4, 15,15);
						break;
				case JTabbedPane.WRAP_TAB_LAYOUT:
						closeButton.setBounds(bounds.width-18, tabBounds.y+5, 15,15);
						break;
				}
			}
		}
	}

	protected Object[] STYLE_MENU = {
		"menu.edit.style",  //  title
		"menu.edit.bold",
		"menu.edit.italic",
		"menu.edit.underline",
		"menu.edit.plain",
		ContextMenu.SEPARATOR,
		"menu.edit.left",
		"menu.edit.center",
		"menu.edit.right",
		ContextMenu.SEPARATOR,
		"menu.edit.larger",
        "menu.edit.smaller",
        "menu.edit.color",
//        ContextMenu.SEPARATOR,
//        "menu.edit.option",new Integer(6),
	};

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public DocumentPanel(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
	{
		super(profile,withContextMenu,withBorder);
		setLayout(new DocumentPanelLayout());
        focusPriority = 2;
		titlePriority = 9;
	}

	public Component getFocusComponent() {
        return theTextPane;
    }

    public void init() throws Exception
	{
		theGame = Application.theApplication.theGame;
		theTextPane = new DocumentEditor(DocumentEditor.emptyGame,this);

	    closeButton = new JButton(ImgUtil.getIcon(null,"tab.close"));
	    closeButton.setActionCommand("menu.game.close");
	    closeButton.addActionListener(this);
	    closeButton.setName("menu.game.close");
	    closeButton.setToolTipText(Language.getTip("menu.game.close"));
	    closeButton.setVisible(false);
        if (Version.mac) {
            closeButton.setMargin(new Insets(2,2,2,2));
            closeButton.putClientProperty("JButton.buttonType","toolbar");
        }

		add(closeButton);

		theTabPane = new JoTabbedPane();	//	not yet visible !
		currentTabIndex = -1;

		theTabPane.addMouseListener(this);

		updateFromProfile(Application.theUserProfile);

		theScroller = new JScrollPane(theTextPane,
									JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(theScroller, BorderLayout.CENTER);

		adjustTabs(Application.theHistory.currentIndex());
		theTextPane.adjustHighlight();
	}

    protected void modifyFontSize(float factor, int min)
    {
        JoStyleContext context = Application.theUserProfile.getStyleContext();

        if ((theTextPane.noneSelected()||theTextPane.allSelected())
                && theTextPane.modifyFontSizeRoot(context,factor,min))
            /*  change root */ ;
        else if (theTextPane.modifyFontSizeLocal(factor,min))
            /*  change local */ ;
        else if (theTextPane.modifyFontSizeGlobal(context, factor,min))
            /*  change global */ ;
        else {
            /*  can't help it */
            AWTUtil.beep(this);
            return;
        }

        Application.theApplication.broadcast(new Command("styles.modified",null,
                                            Application.theApplication,Boolean.TRUE));
    }

    protected void toggleAttribute(Object attribute)
    {
        JoStyleContext context = Application.theUserProfile.getStyleContext();

        if (theTextPane.allSelected() && theTextPane.toggleAttributeRoot(context,attribute))
            /* change root */ ;
        else if (theTextPane.toggleAttributeLocal(attribute))
            /** change local */ ;
        else if (theTextPane.toggleAttributeGlobal(context,attribute))
            /** change global */ ;
        else {
            /*  can't help it */
            AWTUtil.beep(DocumentPanel.this);
            return;
        }

        Application.theApplication.broadcast(new Command("styles.modified",null,
                                        Application.theApplication,Boolean.TRUE));
    }

    protected void plainAttribute()
    {
        JoStyleContext context = Application.theUserProfile.getStyleContext();

        if (theTextPane.plainAttributeLocal())
            /** change local */ ;
        else if (theTextPane.plainAttributeGlobal(context))
            /** change global */ ;
        else {
            /*  can't help it */
            AWTUtil.beep(DocumentPanel.this);
            return;
        }

        Application.theApplication.broadcast(new Command("styles.modified",null,
                                        Application.theApplication,Boolean.TRUE));
    }

    protected void alignParagraph(int align)
    {
        JoStyleContext context = Application.theUserProfile.getStyleContext();

        if (theTextPane.alignParagraphLocal(align))
            /** change local */ ;
        else if (theTextPane.alignParagraphGlobal(context,align))
            /** change global */ ;
        else {
            /*  can't help it */
            AWTUtil.beep(DocumentPanel.this);
            return;
        }

        Application.theApplication.broadcast(new Command("styles.modified",null,
                                        Application.theApplication,Boolean.TRUE));
    }

    protected void setColor(Color color)
    {
        JoStyleContext context = Application.theUserProfile.getStyleContext();

        if (theTextPane.allSelected() && theTextPane.setColorRoot(context,color))
            /*  change root */ ;
        else if (theTextPane.setColorLocal(color))
            /** change local */ ;
        else if (theTextPane.setColorGlobal(Application.theUserProfile.getStyleContext(),color))
            /** global change */ ;
        else {
            /** can't help it */
            AWTUtil.beep(DocumentPanel.this);
        }

        Application.theApplication.broadcast(new Command("styles.modified",null,
                                        Application.theApplication,Boolean.TRUE));
    }

    protected void setFigurines(boolean font, String language)
    {
        Application.theUserProfile.setFigurineFont(font);
        if (language!=null)
            Application.theUserProfile.setFigurineLanguage(language);

        Application.theApplication.broadcast(new Command("styles.modified",null,
                                        Application.theApplication,Boolean.TRUE));
    }

	public void setupActionMap(Map map)
	{
		super.setupActionMap(map);
/*
		CommandAction action;
		action = new CommandAction() {
			public void Do(Command cmd) {
				theGame.clear();
			}
		};
		map.put("menu.file.new", action);
		map.put("menu.file.new.from.here", action);
*/
		CommandAction action = new CommandAction() {
			public void Do(Command cmd) {
                theTextPane.adjustHighlight();
			}
		};
		map.put("move.notify", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				theGame = (Game)cmd.data;
				int tabIndex = ((Number)cmd.moreData).intValue();
				adjustTabs(tabIndex);
				theTextPane.setDocument(theGame);
				theTextPane.adjustHighlight();
				theTextPane.repaint();
			}
		};
		map.put("switch.game", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (theTextPane!=null)
					theTextPane.setDocument(DocumentEditor.emptyGame);
				/**	clear game so that re-formatting the document is not visible to the user	*/
			}
		};
		map.put("prepare.game", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				UserProfile prf = (UserProfile)cmd.data;
				/**	store history (only db id's)	*/
				int[] gids = Application.theHistory.getDBIds();
				if (gids==null || gids.length==0)
					prf.setHistory(null);
				else
					prf.setHistory(gids);
			}
		};
		map.put("update.user.profile", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (cmd.code.equals("tab.place.top"))
					theTabPane.setTabPlacement(JTabbedPane.TOP);
				if (cmd.code.equals("tab.place.left"))
					theTabPane.setTabPlacement(JTabbedPane.LEFT);
				if (cmd.code.equals("tab.place.bottom"))
					theTabPane.setTabPlacement(JTabbedPane.BOTTOM);
				if (cmd.code.equals("tab.place.right"))
					theTabPane.setTabPlacement(JTabbedPane.RIGHT);
				if (cmd.code.equals("tab.layout.wrap"))
					theTabPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
				if (cmd.code.equals("tab.layout.scroll"))
					theTabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			}
		};
		map.put("tab.place.top",action);
		map.put("tab.place.left",action);
		map.put("tab.place.bottom",action);
		map.put("tab.place.right",action);
		map.put("tab.layout.wrap",action);
		map.put("tab.layout.scroll",action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				toggleAttribute(StyleConstants.Bold);
			}
		};
		map.put("menu.edit.bold",action);

		action = new CommandAction() {
			public void Do(Command cmd) {
                toggleAttribute(StyleConstants.Italic);
			}
		};
		map.put("menu.edit.italic",action);

		action = new CommandAction() {
			public void Do(Command cmd) {
                toggleAttribute(StyleConstants.Underline);
			}
		};
		map.put("menu.edit.underline",action);

		action = new CommandAction() {
			public void Do(Command cmd) {
                plainAttribute();
			}
		};
		map.put("menu.edit.plain",action);

		action = new CommandAction() {
			public void Do(Command cmd) {
                modifyFontSize(1.15f,+1);
			}
		};
		map.put("menu.edit.larger",action);

		action = new CommandAction() {
			public void Do(Command cmd)  {
                modifyFontSize(0.85f,-1);
			}
		};
		map.put("menu.edit.smaller",action);

		action = new CommandAction() {
			public void Do(Command cmd)  {
				//  popup color palette
                //  get color at selection
                Color color = theTextPane.getSelectionColor();
                color = JColorChooser.showDialog(DocumentPanel.this, Language.get("font.color"), color);
                if (color!=null) {
                    if (color.equals(Color.white))
                        setColor(null); //=inherited color
                    else
                        setColor(color);
                }
			}
		};
		map.put("menu.edit.color",action);

		action = new CommandAction() {
			public void Do(Command cmd)  {
				alignParagraph(StyleConstants.ALIGN_LEFT);
			}
		};
		map.put("menu.edit.left",action);

		action = new CommandAction() {
			public void Do(Command cmd)  {
				alignParagraph(StyleConstants.ALIGN_CENTER);
			}
		};
		map.put("menu.edit.center",action);

		action = new CommandAction() {
			public void Do(Command cmd)  {
				alignParagraph(StyleConstants.ALIGN_RIGHT);
			}
		};
		map.put("menu.edit.right",action);

        action = new CommandAction() {
            public void Do(Command cmd) {
                setFigurines(true,null);
            }
        };
        map.put("figurine.usefont.true",action);

        action = new CommandAction() {
            public void Do(Command cmd) {
                String langCode = (String)cmd.data;
                setFigurines(false,langCode);
            }
        };
        map.put("figurine.usefont.false",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws BadLocationException {
				Node node = (Node)cmd.data;
				//	delete one comment
				switch (node.type()) {
				case COMMENT_NODE:
				case ANNOTATION_NODE:
				case DIAGRAM_NODE:
						node.remove(theGame);	//	remove from text
						node.remove();	//	remove from hierarchy
						theGame.updateMoveCount(node);
						break;
				default:
						throw new IllegalArgumentException("can't delete "+node);
				}
			}
		};
		map.put("doc.menu.delete.comment",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				LineNode line = (LineNode)cmd.data;
				//	promote line
				//theGame.printDocStructure("tree-before.txt");
				theGame.promoteLine(line);
				//theGame.printDocStructure("tree-after.txt");
				reformat();	// todo necessary ?

				cmd = new Command("move.notify",null,null,Boolean.TRUE);
				Application.theApplication.broadcast(cmd);
			}
		};
		map.put("doc.menu.line.promote",action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				LineNode line = (LineNode)cmd.data;
				//	remove all comments
				line.removeComments();
				reformat();
			}
		};
		map.put("doc.menu.line.uncomment",action);

		action = new CommandAction() {
			public boolean isSelected(String cmd)
			{
				int current = Application.theUserProfile.getInt("doc.move.format");
				int menu = MoveFormatList.toConst(cmd);
				return current==menu;
			}

			public void Do(Command cmd)
			{
				cmd.data = new Integer(MoveFormatList.toConst(cmd.code));
				Application.theUserProfile.set("doc.move.format",cmd.data);
				reformat();
			}
		};
		map.put("move.format.short",action);
		map.put("move.format.long",action);
		map.put("move.format.algebraic",action);
		map.put("move.format.correspondence",action);
		map.put("move.format.english",action);
		map.put("move.format.telegraphic",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws BadLocationException {
				Node nd = (Node)cmd.data;
				int nag = ((Number)cmd.moreData).intValue();

                if (nag==PgnConstants.NAG_DIAGRAM || nag==PgnConstants.NAG_DIAGRAM_DEPRECATED)
                    theTextPane.insertDiagram(nd);
                else {
                    theTextPane.select(nd.getStartOffset(),nd.getEndOffset());
                    String text = PgnUtil.annotationString(nag);
                    replaceSelection(text);
                }
/*
                theTextPane.caretListen = false;
				if (nd.is(ANNOTATION_NODE)) {
					//	replace existing
					AnnotationNode ann = (AnnotationNode)nd;
					ann.setCode(nag);
					ann.remove(theGame);
					ann.insert(theGame,ann.getStartOffset());
				}
				else if (nd.next()!=null && nd.next().is(ANNOTATION_NODE)) {
					//	replace existing
					AnnotationNode ann = (AnnotationNode)nd.next();
					ann.setCode(nag);
					ann.remove(theGame);
					ann.insert(theGame,ann.getStartOffset());
				}
				else {
					//	Move Node - append annotation
					AnnotationNode ann = new AnnotationNode(nag);
					ann.insertAfter(nd);
					ann.insert(theGame,ann.getStartOffset());
				}
				theTextPane.caretListen = true;
				theTextPane.adjustHighlight();
*/			}
		};
		for (int i=1; i<=256; i++)
			map.put("pgn.nag."+i, action);

		action = new CommandAction() {
			public void Do(Command cmd) throws BadLocationException {
				Node nd = (Node)cmd.data;

				if (nd.is(ANNOTATION_NODE))
					removeAnnotation((AnnotationNode)nd);
				else {
					nd = nd.next(ANNOTATION_NODE);
					if (nd!=null) removeAnnotation((AnnotationNode)nd);
				}
			}
		};
		map.put("doc.menu.remove.annotation",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws BadLocationException {
				Node nd = (Node)cmd.data;

				theTextPane.select(nd.getStartOffset(),nd.getStartOffset());
				Application.theCommandDispatcher.handle(
								new Command("window.toolbar.symbols",DocumentPanel.this),
								Application.theApplication);
			}
		};
		map.put("doc.menu.more.annotations",action);


		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				Game gm = (Game)cmd.data;
				GameUtil.copyPGNtoSystemClipboard(gm);

				cmd = new Command("move.notify",null,null,Boolean.TRUE);
				Application.theApplication.broadcast(cmd);
			}
		};
		map.put("menu.edit.copy.pgn",action);


        action = new CommandAction() {
            public void Do(Command cmd) throws Exception
            {
                theTextPane.cut();
            }
        };
        map.put("menu.edit.cut",action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception
            {
                theTextPane.copy();
            }
        };
        map.put("menu.edit.copy",action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception
            {
                theTextPane.paste();
            }
        };
        map.put("menu.edit.paste",action);

		action = new CommandAction() {
			public CommandListener forward(CommandListener current)
			{
				return Application.theApplication;
			}
		};
		map.put("menu.edit.paste.pgn",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				//  notification from style editor
				Object source = cmd.data;
				boolean allModified = Util.toboolean(cmd.moreData);
				if (allModified)
					reformat();     //  all styles modified; need to rebuild doc structure
				else
					repaint();      //  only size modified; repaint() is sufficient
			}
		};
		map.put("styles.modified",action);

        action = new CommandAction() {
            public boolean isEnabled(String cmd) {
                return theGame!=null;
            }

            public void Do(Command cmd) throws Exception
            {
                Task task = new NalimovOnlineQuery(theGame,
                                theGame.getCurrentMove(),
                                theGame.getPosition());
                task.start();
            }
        };
        map.put("menu.game.nalimov.online",action);
	}

	protected void removeAnnotation(AnnotationNode ann) throws BadLocationException
	{
		theTextPane.caretListen = false;

		ann.remove(theGame);
		ann.remove();

		theTextPane.caretListen = true;
		theTextPane.adjustHighlight();
	}

	public void updateProfile(UserProfile prf)
	{
		prf.set("doc.panel.tab.placement", theTabPane.getTabPlacement());
		prf.set("doc.panel.tab.layout", theTabPane.getTabLayoutPolicy());
	}

	public void updateFromProfile(UserProfile prf)
		throws Exception
	{
		int tabPlacement = prf.getInt("doc.panel.tab.placement");
		int tabLayout = prf.getInt("doc.panel.tab.layout");
		if (tabPlacement > 0)
			theTabPane.setTabPlacement(tabPlacement);
		if (tabLayout > 0)
			theTabPane.setTabLayoutPolicy(tabLayout);
		theTextPane.setTextAntialising(prf.getBoolean("doc.panel.antialias"));
	}

	public void reformat()
	{
		theTextPane.reformat();
		theTextPane.repaint();
	}

	public void replaceSelection(String text)
	{
		theTextPane.replaceSelection(text);
	}

	public void adjustTabs()
	{
		adjustTabs(currentTabIndex);
	}

	public void adjustTabs(int newIndex)
	{
//		if (!inited) return;

		//	don't listen to ChangeEvents while working on the tabbed pane
		theTabPane.removeChangeListener(this);

		History hist = Application.theHistory;

		boolean needsTabPane = (hist.size() > 1);
		boolean useTabPane = currentTabIndex >= 0;

		if (needsTabPane && !useTabPane) {
			remove(theScroller);
			add(theTabPane);
			closeButton.setVisible(true);
			currentTabIndex = 0;
		}

		if (!needsTabPane && useTabPane) {
			remove(theTabPane);
			closeButton.setVisible(false);
			theTabPane.removeAll();
			theScroller.setBounds(theTabPane.getBounds());
			add(theScroller);
			currentTabIndex = -1;
		}

		if (currentTabIndex >= 0) {
			Vector titles = hist.getTabTitles();

			if (theTabPane.getTabCount() != hist.size()) {
				theTabPane.removeAll();

				if (newIndex >= 0)
					currentTabIndex = newIndex;
				if (currentTabIndex >= hist.size())
					currentTabIndex = hist.size()-1;

				for (int i=0; i<hist.size(); i++) {
					if (i==currentTabIndex)
						theTabPane.addTab((String)titles.get(i),
						        hist.isDirty(i) ? dirtyIcon:null,
						        theScroller,hist.getToolTip(i));
					else
						theTabPane.addTab((String)titles.get(i),
						        hist.isDirty(i) ? dirtyIcon:null,
						        new JLabel(""),hist.getToolTip(i));
				}
				theTabPane.doLayout();
				//	accounts for a bug (?) in TabbePaneUI
				theTabPane.setSelectedIndex(currentTabIndex);
				/**	note that JTabPane does not allow to add the same component twice !
				 * 	that's why we fill up the tab pane with dummy components
				 */
			}
			else
				adjustTabTitles();

			if (newIndex >= 0 && newIndex != currentTabIndex) {
				theTabPane.setComponentAt(currentTabIndex,new JLabel(""));
				theTabPane.setComponentAt(newIndex,theScroller);
				theTabPane.setSelectedIndex(currentTabIndex = newIndex);
			}
			//	else: selection does not change
		}


		theGame = hist.get(Math.max(currentTabIndex,0));
		if (!Util.equals(theGame,theTextPane.getDocument()))
			theTextPane.setDocument(theGame);

		if (currentTabIndex >= 0)
			theTabPane.addChangeListener(this);
	}


	public void adjustTabTitles()
	{
		if (currentTabIndex < 0) return;	//	no tabs displayed

		Vector titles = Application.theHistory.getTabTitles();
		for (int i=0; i < titles.size(); i++)
			adjustTabTitle(i,titles);
	}

	public void adjustTabTitle(int i, Vector titles)
	{
		History hist = Application.theHistory;
		if (titles==null) titles = hist.getTabTitles();
		theTabPane.setTitleAt(i, (String)titles.get(i));
		theTabPane.setToolTipTextAt(i, hist.getToolTip(i));
		theTabPane.setIconAt(i, hist.isDirty(i) ? dirtyIcon:null);
	}

	public void adjustTabTitle(Game g)
	{
		if (currentTabIndex < 0) return;	//	no tabs displayed

		History hist = Application.theHistory;
		Vector titles = hist.getTabTitles();

		adjustTabTitle(hist.indexOf(g),titles);
	}

	public void adjustTabToolTip(int i)
	{
		History hist = Application.theHistory;
		theTabPane.setToolTipTextAt(i, hist.getToolTip(i));
	}

	public void adjustTabToolTip(Game g)
	{
		if (currentTabIndex < 0) return;	//	no tabs displayed

		History hist = Application.theHistory;
		adjustTabToolTip(hist.indexOf(g));
	}

	public int getTabClicked(MouseEvent event)
	{
		if (currentTabIndex >= 0) {
			Point pt = AWTUtil.convertPoint(event,this);
			/**
			 * please note that pt is already relative to the tabbed pane
			 * however, findIndex() will apply the current scrolling offset
			 * so we have to account for it...
			 * assumes that the Tabbed Pane is always located in the upper left corner
			 */
			return theTabPane.findIndex(pt);
		}
		else
			return -1;    //  tabs not visible
	}

	public void adjustContextMenu(Collection list, MouseEvent event)
	{
		super.adjustContextMenu(list,event);

		//	check for click in tab area
		int tabClicked = getTabClicked(event);

		list.add(ContextMenu.SEPARATOR);

		if (tabClicked >= 0) {
			Integer param = new Integer(tabClicked);
			GameSource gms = GameSource.singleGame(Application.theHistory.get(tabClicked).getId());

			list.add("menu.game.close");
			list.add(param);

			list.add("menu.game.close.all.but");
			list.add(param);
			list.add("menu.game.close.all");

			list.add("menu.game.details");
			list.add(gms);

			list.add(ContextMenu.SEPARATOR);
/*
			Collection submenu = new ArrayList();
			submenu.add("tab.place");
			submenu.add((theTabPane.getTabPlacement()==JTabbedPane.TOP) ? Boolean.TRUE:Boolean.FALSE);
			submenu.add("tab.place.top");
			submenu.add((theTabPane.getTabPlacement()==JTabbedPane.LEFT) ? Boolean.TRUE:Boolean.FALSE);
			submenu.add("tab.place.left");
			submenu.add((theTabPane.getTabPlacement()==JTabbedPane.BOTTOM) ? Boolean.TRUE:Boolean.FALSE);
			submenu.add("tab.place.bottom");
			submenu.add((theTabPane.getTabPlacement()==JTabbedPane.RIGHT) ? Boolean.TRUE:Boolean.FALSE);
			submenu.add("tab.place.right");
			list.add(submenu);
*/
            if (!Version.mac)
            {
                Collection submenu = new ArrayList();
                submenu.add("tab.layout");
                submenu.add(Util.toBoolean(theTabPane.getTabLayoutPolicy()==JTabbedPane.WRAP_TAB_LAYOUT));
                submenu.add("tab.layout.wrap");
                submenu.add(Util.toBoolean(theTabPane.getTabLayoutPolicy()==JTabbedPane.SCROLL_TAB_LAYOUT));
                submenu.add("tab.layout.scroll");
                list.add(submenu);
            }
            //  setTabLayoutPolicy() has zero effect on Aqua l&f
		}
		else {
			GameSource source1 = GameSource.singleGame(theGame.getId());
			GameSource source2 = GameSource.gameObject(theGame);

			//	applies to curent game
			list.add("menu.game.close");
			list.add("menu.game.details");
			if (theGame.isDirty()) {
				list.add("menu.file.save");
				if (!theGame.isNew())
					list.add("menu.file.revert");
			}
			list.add(ContextMenu.SEPARATOR);

			//  copy as pgn
			list.add("menu.edit.copy.pgn");
			list.add(theGame);
			//  paste pgn
			list.add("menu.edit.paste.pgn");
			//  ecofy
			list.add("menu.edit.ecofy");
			list.add(source2);
			list.add(ContextMenu.SEPARATOR);
		}

		//  check for click in text area
        Node nd = theTextPane.getNode(event.getPoint());
        if (nd != null && theGame.isMainTree(nd))
		{
			//	node specific commands
			switch (nd.type())
			{
			case MOVE_NODE:
				list.add(createAnnotationMenu(nd));
				break;
			case COMMENT_NODE:
			case ANNOTATION_NODE:
			case DIAGRAM_NODE:
				list.add("doc.menu.delete.comment");
				list.add(nd);
				break;
			}

			addAnnotationMenuItem(list,nd, PgnConstants.NAG_DIAGRAM);
			list.add(null);

			//	line specific commands
			LineNode line = nd.parent();
			if (line.level() > 1) {
				//	sub-variation
				list.add("doc.menu.line.promote");
				list.add(line);
			}
			if (nd.previous()==line.first())	//	prefix
				list.add("doc.menu.line.delete");
			else
				list.add("doc.menu.line.cut");
			list.add(nd);

			list.add("doc.menu.line.uncomment");
			list.add(line);
			list.add(ContextMenu.SEPARATOR);
		}

		list.add(STYLE_MENU);
		//	move format
		Collection submenu = MoveFormatList.createMenu("move.format",
									Application.theUserProfile.getInt("doc.move.format"));
        list.add(submenu);
        //  figurines
        boolean usefont = Application.theUserProfile.useFigurineFont();
        String lang = Application.theUserProfile.getFigurineLanguage();

        list.add(createFigurineMenu("dialog.option.font.figurine",usefont,lang,null));

        list.add(null);
        list.add("menu.edit.option");
        list.add(new Integer(6));
	}


    public static JMenu createFigurineMenu(String menuId, boolean usefont, String language, ActionListener[] listeners)
    {
        JMenu menu = new JMenu();
        JoMenuBar.initItem(menu,menuId);

        createFigurineMenu(menu,usefont,language,listeners);
        return menu;
    }

    public static void createFigurineMenu(JMenu menu, boolean usefont, String language, ActionListener[] listeners)
    {
        menu.removeAll();
        JMenuItem font = JoMenuBar.createMenuItem("figurine.usefont.true",usefont);
        menu.add(font);
        menu.addSeparator();

        Vector langCodes = Language.getList("fig.langs");
	    Vector langItems = new Vector();
        for (int i=0; i < langCodes.size(); i++)
        {
            String lang = (String)langCodes.get(i);
            JMenuItem item = JoMenuBar.createMenuItem("figurine.usefont.false", !usefont && lang.equalsIgnoreCase(language));
            item.putClientProperty("action.data",lang);
            item.setText(Language.get("lang."+lang));
            langItems.add(item);
        }
	    //  sort alphabetically
	    ListUtil.sort(langItems,JoMenuBar.SORT_BY_TITLE);
	    for (int i=0; i < langItems.size(); i++)
	        menu.add((JMenuItem)langItems.get(i));

        if (listeners!=null)
            for (int i=0; i<listeners.length; i++)
                JoMenuBar.addMenuItemListener(menu,listeners[i]);
    }



	protected Collection createAnnotationMenu(Node nd)
	{
		Collection menu = new ArrayList();
		menu.add("doc.menu.annotate");		//	title

		if (nd.is(ANNOTATION_NODE) || nd.next()!=null && nd.next().is(ANNOTATION_NODE)) {
			menu.add("doc.menu.remove.annotation");
			menu.add(nd);
		}

		addAnnotationMenuItem(menu,nd, PgnConstants.NAG_GOOD);
		addAnnotationMenuItem(menu,nd, PgnConstants.NAG_VERY_GOOD);
		addAnnotationMenuItem(menu,nd, PgnConstants.NAG_BAD);
		addAnnotationMenuItem(menu,nd, PgnConstants.NAG_VERY_BAD);
		addAnnotationMenuItem(menu,nd, PgnConstants.NAG_INTERESTING);
		addAnnotationMenuItem(menu,nd, PgnConstants.NAG_DUBIOUS);

		menu.add(null);

		addAnnotationMenuItem(menu,nd, PgnConstants.NAG_SLIGHT_ADVANTAGE_WHITE);
		addAnnotationMenuItem(menu,nd, PgnConstants.NAG_SLIGHT_ADVANTAGE_BLACK);
		addAnnotationMenuItem(menu,nd, PgnConstants.NAG_MODERATE_ADVANTAGE_WHITE);
		addAnnotationMenuItem(menu,nd, PgnConstants.NAG_MODERATE_ADVANTAGE_BLACK);
		addAnnotationMenuItem(menu,nd, PgnConstants.NAG_DECISIVE_ADVANTAGE_WHITE);
		addAnnotationMenuItem(menu,nd, PgnConstants.NAG_DECISIVE_ADVANTAGE_BLACK);

		menu.add(null);
		menu.add("doc.menu.more.annotations");
		menu.add(nd);

		return menu;
	}

	protected void addAnnotationMenuItem(Collection menu, Node nd, int nag)
	{
		menu.add("pgn.nag."+nag);
		menu.add(nd);
		menu.add(new Integer(nag));
	}

	//	implements ChangeListener
	public void stateChanged(ChangeEvent e)
	{
		//	listen to tab change events
		int newIndex = theTabPane.getSelectedIndex();
		int current = Application.theHistory.currentIndex();

		if (newIndex != current)
			Application.theApplication.switchGame(newIndex);
			//	will eventually call back ...
	}



    //  implements MouseListener

    public void mouseClicked(MouseEvent e)
    {
/*
        Node nd = getNode(e.getPoint());
        if (nd != null) {
            if (nd instanceof MoveNode) {
                //  goto move
            }
            if (nd instanceof CommentNode) {
                //  move caret
            }
        }
*/
	    //  middle mouse -> close tab
	    int mods = e.getModifiers();
	    if (Util.allOf(mods,MouseEvent.BUTTON2_MASK))
	    {
		    int tab = getTabClicked(e);
		    if (tab >= 0)
		    {
			    //  close tab
			    final Command cmd = new Command("menu.game.close",e,new Integer(tab));
			    SwingUtilities.invokeLater(new Runnable() {
				    public void run()
				    {
						Application.theCommandDispatcher.handle(cmd,Application.theApplication);
				    }
			    });
		    }
	    }
    }

    public void mousePressed(MouseEvent e)
    {
    }

    public void mouseReleased(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }
}
