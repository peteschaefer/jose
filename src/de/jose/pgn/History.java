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

package de.jose.pgn;

import de.jose.*;
import de.jose.util.IntArray;
import de.jose.view.DocumentPanel;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

public class History
		implements DocumentListener
{
    /** list of Games (corresponds to tab indexes)  */
    protected Vector games;
	/**	list of tab titles	*/
	protected Vector tabTitles;
    /** current index   */
    protected int current;

    /** create a new, empty History */
    public History()
    {
        games = new Vector();
		tabTitles = new Vector();
        current = 0;
    }

    /**
     * get a game from history
     */
    public final Game get(int offset)	    { return (Game)games.get(offset); }

	public final int size() 				{ return games.size(); }

	public final int currentIndex()			{ return current; }

	public final boolean hasPrevious()		{ return current > 0; }

	public final boolean hasNext()			{ return (current+1) < size(); }

	public final Game[] getArray(boolean includeEmpty)          
	{
		if (games.isEmpty()) return null;

		ArrayList collect = new ArrayList();
		for (int i=0; i<games.size(); i++) {
			Game gm = (Game)games.get(i);
			if (gm!=null && (!gm.isEmpty() || includeEmpty))
				collect.add(gm);
		}
		if (collect.isEmpty())
			return null;
		else
			return (Game[])collect.toArray(new Game[collect.size()]);
	}

	public final int indexOf(Game g)
	{
		for (int i=size()-1; i>=0; i--)
			if (g == get(i))
				return i;
		return -1;
	}

	public final int indexOf(int GId)
	{
		for (int i=size()-1; i>=0; i--)
			if (get(i).getId()==GId)
				return i;
		return -1;
	}

	public final Game getById(int GId)
	{
		for (int i=size()-1; i>=0; i--)
			if (get(i).getId()==GId)
				return get(i);
		return null;
	}

    /**
     * @return the current game
     * */
    public final Game getCurrent()         	{ return get(current); }

	public final void setCurrent(int index)	{ current = index; }

	public final Vector getTabTitles()
	{
		tabTitles.setSize(size());

		for (int i=0; i<size(); i++) {
			String title = get(i).getTabTitle(32);
			if (title.length()==0) title = Language.get("tab.untitled");
			tabTitles.setElementAt(title,i);
		}

		//	find duplicates
		for (int i=size()-1; i>=0; i--) {
			String title = (String)tabTitles.get(i);
			int dupl1 = Util.count(tabTitles,0,i, title);
			int dupl2 = Util.count(tabTitles,i+1,size(), title);

			if ((dupl1+dupl2) > 0)
				title += " ["+(dupl1+1)+"]";
			Game g = get(i);
//			if (g.isDirty()) title += " *";
			tabTitles.setElementAt(title,i);
		}

		return tabTitles;
	}

	public String getToolTip(int index)
	{
		String toolTip = get(index).getTabToolTip();
		if (toolTip.length()==0) toolTip = Language.get("tab.untitled");
		return toolTip;
	}

	public boolean isDirty(int idx)
	{
		Game g = get(idx);
		return g.isDirty();
	}

	public int[] getDBIds()
	{
		IntArray result = new IntArray(size());
		for (int i=0; i<size(); i++) {
			Game g = get(i);
			if (g.getId() > 0) result.add(g.getId());
		}
		if (result.size()==0)
			return null;
		else
			return result.toArray();
	}

	public void add(Game g)
	{
		games.add(g);
		g.addDocumentListener(this);
		current = size()-1;
	}

	public boolean contains(Game g)
	{
		return games.contains(g);
	}

	public void remove(int index)
	{
		Game g = get(index);
		g.removeDocumentListener(this);

		games.remove(index);
		if (current > 0 && current >= size())
			current--;
	}

	public void remove(Game g)
	{
		remove(games.indexOf(g));
	}

	public void removeAll()
	{
		for (int i=size()-1; i>=0; i--)
			get(i).removeDocumentListener(this);

		games.clear();
		current = 0;
	}

	public void removeAllBut(int index)
	{
		Game g = get(index);
		removeAll();
		add(g);
	}

	public void saveAllBut(int index) throws Exception
	{
		for (int i=size()-1; i>=0; i--)
			if (i != index) {
				get(i).save();
				Application.theApplication.broadcast(new Command("game.modified",null,new Integer(get(i).getId())));
			}
	}

	public boolean isDirtyBut(int index)
	{
		for (int i=size()-1; i>=0; i--) {
			if (i==index) continue;
			if (get(i).isDirty()) return true;
		}
		return false;
	}

	//	implements DocumentListener
	public void changedUpdate(DocumentEvent e)
	{
		//	we are only interested in DirtyEvents and TagChangeEvents
		DocumentPanel panel = Application.theApplication.docPanel();
		if (panel==null) return;
		Game g = (Game)e.getDocument();

		if (e instanceof Document.DirtyEvent) {
			//	forward this event to document panel: adjust tab labels
			panel.adjustTabTitle(g);
			//	make a (dummy) broadcast to adjust tool bars
			Application.theApplication.broadcast("doc.dirty");
		}
		if (e instanceof Game.TagChangeEvent) {
			String key = ((Game.TagChangeEvent)e).getKey();
			if (key.equals(PgnConstants.TAG_WHITE) || key.equals(PgnConstants.TAG_BLACK)) {
				//	forward this event to document panel: adjust tab labels
				panel.adjustTabTitle(g);
			}
			else {
				panel.adjustTabToolTip(g);
			}
		}
	}

	//	implements DocumentListener
	public void insertUpdate(DocumentEvent e) {
	}

	//	implements DocumentListener
	public void removeUpdate(DocumentEvent e) {
	}

    /**
     * @return true if there are any "dirty" (modified) games in history
     */
    public final boolean isDirty() {
        for (int i = size()-1; i >= 0; i--)
            if (get(i).isDirty()) return true;
        return false;
    }

    public void saveAll()  throws Exception
    {
        for (int i=0; i < size(); i++) {
            Game g = get(i);
            if (g.isNew()) {
                g.saveAs(Collection.AUTOSAVE_ID, 0);
				Application.theApplication.broadcast(new Command("collection.modified",null,new Integer(Collection.AUTOSAVE_ID)));
			}
            else if (g.isDirty()) {
                g.save();
				Application.theApplication.broadcast(new Command("game.modified",null,new Integer(g.getId())));
			}
        }
    }

    public void clearDirty()
    {
        for (int i=0; i < size(); i++) {
            get(i).clearDirty();
        }
    }
}
