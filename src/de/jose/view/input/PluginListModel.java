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

package de.jose.view.input;

import de.jose.plugin.Plugin;
import org.w3c.dom.Element;

import javax.swing.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 */
public class PluginListModel
        extends AbstractListModel
{
	public static class Record
	{
		/** the (currently edited) configuration */
		public Element cfg;
		/** the associated plugin. null indicates a newly created config    */
		public Plugin plugin;
	}

	/** the set of currently edited configs
	 *  Vector<Record>  */
	protected Vector current = new Vector();
	/**
	 * the set of deleted entries
	 *  Vector<Record>
 	 */
	protected Vector deleted = new Vector();

	public Object getElementAt(int index)	{ return current.get(index); }

	public int getSize()                    { return current.size();		}


	public Iterator currentIterator()       { return current.iterator(); }
	public Iterator deletedIterator()       { return deleted.iterator(); }

	public void setPlugins(Vector plugins)
	{
		int oldSize = current.size();
		current.clear();
		deleted.clear();

		for (int i=0; i<plugins.size(); i++)
		{
			Plugin plug = (Plugin)plugins.get(i);
			Record rec = new Record();
			rec.plugin = plug;
			rec.cfg = (Element)rec.plugin.config.cloneNode(true);
			current.add(rec);
		}

		int newSize = current.size();

		if (oldSize > newSize)
			super.fireIntervalRemoved(this,newSize,oldSize);

		super.fireContentsChanged(this,0,Math.min(oldSize,newSize));

		if (oldSize < newSize)
			super.fireIntervalAdded(this,oldSize,newSize);
	}

	public Record addNewConfig(Element cfg)
	{
		Record rec = new Record();
		rec.cfg = cfg;
		rec.plugin = null;  //  indicates a newly created configuration

		current.add(rec);
		super.fireIntervalAdded(this, getSize()-1,getSize());
		return rec;
	}

	public int delete(Record rec)
	{
		int i = current.indexOf(rec);
		if (i >= 0) {
			current.remove(i);
			deleted.add(rec);
			super.fireIntervalRemoved(this,i,i+1);
		}
		return i;
	}

	public void commitDelete()
	{
		deleted.clear();
	}

	public void update(Record rec, Plugin plug)
	{
		int i= current.indexOf(rec);
		rec.cfg = plug.config;
		rec.plugin = plug;
		super.fireContentsChanged(this,i,i+1);
	}

	public String makeNewID(Element cfg)
	{
		/** get the set of used IDs */
		HashSet inuse = new HashSet();
		for (int i=0; i < current.size(); i++)
		{
			Record rec = (Record)current.get(i);
			if (rec.cfg!=cfg) inuse.add(Plugin.getId(rec.cfg).toLowerCase());
		}
		for (int i=0; i < deleted.size(); i++)
		{
			Record rec = (Record)deleted.get(i);
			if (rec.cfg!=cfg) inuse.add(Plugin.getId(rec.cfg).toLowerCase());
		}

		/** now create a unique ID  */
		String result = Plugin.getName(cfg).toLowerCase();
		if (!inuse.contains(result)) return result;

		for (int suffix=2; ; suffix++)
		{
			String s = result+suffix;
			if (!inuse.contains(s)) return s;
		}
	}

}
