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

package de.jose.window;

import de.jose.Language;
import de.jose.Util;
import de.jose.Version;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class JoTabDialog
		extends JoDialog
		implements ChangeListener
{
	/** bitset of tabs that have been inited    */
	protected int inited;
	protected int currentTab;
	protected Vector tabElements;
	protected boolean listenTabChanges;

	public JoTabDialog(String name, boolean modal)
	{
		super(name,modal);
		inited = 0;
		currentTab = -1;
		tabElements = new Vector();
		listenTabChanges = true;
	}

	protected boolean isInited(int tab) {
		return Util.anyOf(inited, 1<<tab);
	}

	protected void setInited(int tab) {
		inited = Util.set(inited,1<<tab,true);
	}

	public Component getTab(int idx)
	{
		return getTabbedPane().getComponentAt(idx);
	}

	public Component getCurrentTab()
	{
		return getTab(currentTab);
	}

	public void show()
	{
		init(getTabbedPane().getSelectedIndex());
		super.show();
	}

	protected synchronized boolean init(int idx)
	{
		if (isInited(idx)) return false;

		currentTab = idx;
		switch (idx) {
		case 0: initTab0(getTabbedPane().getComponentAt(0)); break;
		case 1: initTab1(getTabbedPane().getComponentAt(1)); break;
		case 2: initTab2(getTabbedPane().getComponentAt(2)); break;
		case 3: initTab3(getTabbedPane().getComponentAt(3)); break;
		case 4: initTab4(getTabbedPane().getComponentAt(4)); break;
		case 5: initTab5(getTabbedPane().getComponentAt(5)); break;
		case 6: initTab6(getTabbedPane().getComponentAt(6)); break;
		case 7: initTab7(getTabbedPane().getComponentAt(7)); break;
		case 8: initTab8(getTabbedPane().getComponentAt(8)); break;
		case 9: initTab9(getTabbedPane().getComponentAt(9)); break;
		default: initTab(idx,getTabbedPane().getComponentAt(idx)); break;
		}

		setInited(idx);
		read(idx);
		return true;
	}

	protected void initTab0(Component tab0)     { /* overwrite ! */ }
	protected void initTab1(Component tab1)     { /* overwrite ! */ }
	protected void initTab2(Component tab2)     { /* overwrite ! */ }
	protected void initTab3(Component tab3)     { /* overwrite ! */ }
	protected void initTab4(Component tab4)     { /* overwrite ! */ }
	protected void initTab5(Component tab5)     { /* overwrite ! */ }
	protected void initTab6(Component tab6)     { /* overwrite ! */ }
	protected void initTab7(Component tab7)     { /* overwrite ! */ }
	protected void initTab8(Component tab8)     { /* overwrite ! */ }
	protected void initTab9(Component tab9)     { /* overwrite ! */ }
	protected void initTab(int idx, Component tab)     { /* overwrite ! */ }

	protected synchronized void activate(int idx)
	{
		currentTab = idx;
		switch (idx) {
		case 0: activateTab0(getTabbedPane().getComponentAt(0)); break;
		case 1: activateTab1(getTabbedPane().getComponentAt(1)); break;
		case 2: activateTab2(getTabbedPane().getComponentAt(2)); break;
		case 3: activateTab3(getTabbedPane().getComponentAt(3)); break;
		case 4: activateTab4(getTabbedPane().getComponentAt(4)); break;
		case 5: activateTab5(getTabbedPane().getComponentAt(5)); break;
		case 6: activateTab6(getTabbedPane().getComponentAt(6)); break;
		case 7: activateTab7(getTabbedPane().getComponentAt(7)); break;
		case 8: activateTab8(getTabbedPane().getComponentAt(8)); break;
		case 9: activateTab9(getTabbedPane().getComponentAt(9)); break;
		default: activateTab(idx,getTabbedPane().getComponentAt(idx)); break;
		}
	}

	protected void activateTab0(Component tab0)     { /* overwrite ! */ }
	protected void activateTab1(Component tab1)     { /* overwrite ! */ }
	protected void activateTab2(Component tab2)     { /* overwrite ! */ }
	protected void activateTab3(Component tab3)     { /* overwrite ! */ }
	protected void activateTab4(Component tab4)     { /* overwrite ! */ }
	protected void activateTab5(Component tab5)     { /* overwrite ! */ }
	protected void activateTab6(Component tab6)     { /* overwrite ! */ }
	protected void activateTab7(Component tab7)     { /* overwrite ! */ }
	protected void activateTab8(Component tab8)     { /* overwrite ! */ }
	protected void activateTab9(Component tab9)     { /* overwrite ! */ }
	protected void activateTab(int idx, Component tab)     { /* overwrite ! */ }


	protected synchronized void read(int idx)
	{
		currentTab = idx;
		switch (idx) {
		case 0: readTab0(); break;
		case 1: readTab1(); break;
		case 2: readTab2(); break;
		case 3: readTab3(); break;
		case 4: readTab4(); break;
		case 5: readTab5(); break;
		case 6: readTab6(); break;
		case 7: readTab7(); break;
		case 8: readTab8(); break;
		case 9: readTab9(); break;
		default: readTab(idx); break;
		}
	}

	protected void readTab0()     { /* overwrite ! */ }
	protected void readTab1()     { /* overwrite ! */ }
	protected void readTab2()     { /* overwrite ! */ }
	protected void readTab3()     { /* overwrite ! */ }
	protected void readTab4()     { /* overwrite ! */ }
	protected void readTab5()     { /* overwrite ! */ }
	protected void readTab6()     { /* overwrite ! */ }
	protected void readTab7()     { /* overwrite ! */ }
	protected void readTab8()     { /* overwrite ! */ }
	protected void readTab9()     { /* overwrite ! */ }
	protected void readTab(int idx)  { /* overwrite ! */ }


	public void read() throws Exception
	{
		for (int i=0; i<32; i++)
			if (isInited(i))
				read(i);
	}

	public void read(int idx, HashMap values)
	{
		Map map = (HashMap)tabElements.get(idx);
		Iterator i = map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry ety = (Map.Entry)i.next();
			String name = (String)ety.getKey();
			Object comp = ety.getValue();

			Object value = values.get(name);
			setValue(comp,value);
		}
	}

	public java.util.List save(int idx, HashMap values)
	{
		Vector errors = null;
		Map map = (HashMap)tabElements.get(idx);
		Iterator i = map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry ety = (Map.Entry)i.next();
			String name = (String)ety.getKey();
			Object comp = ety.getValue();

			try {
				Object value = getValue(comp);
				values.put(name,value);
			} catch (Throwable ex) {
				//  parse errors
				if (errors==null) errors = new Vector();
				errors.add(ex);
			}
		}
		return errors;
	}

	public Object reg(String name, Object comp)
	{
		super.reg(name,comp);

		if (name != null) {
			HashMap map = (HashMap)tabElements.get(currentTab);
			map.put(name, comp);
		}
		return comp;
	}

	//-------------------------------------------------------------------------------
	//	Basic Access
	//-------------------------------------------------------------------------------

	public final JTabbedPane getTabbedPane()
	{
		return (JTabbedPane)getElementPane();
	}
	
	public void addTab(Component comp, Icon icon)
	{
		int indx = getTabbedPane().getComponentCount()+1;
		if (Version.mac) indx--; // don't ask why
		String name = getName()+".tab."+indx;
		comp.setName(name);
		tabElements.add(new HashMap());
		listenTabChanges = false;
		getTabbedPane().addTab(Language.get(name), icon, comp, Language.getTip(name));
		listenTabChanges = true;
	}
	
	public final void addTab(Component comp)
	{
		addTab(comp,null);
	}
	
	public final void setTab(int idx)
	{
		init(idx);
		activate(idx);
		getTabbedPane().setSelectedIndex(idx);
	}

	public void show(int idx)
	{
		show();
		toFront();
		setTab(idx);
	}

	/**
	 * @return the appropriate component when the HELP button is pressed
	 */
	protected Component getHelpFocus()
	{
		return getTabbedPane().getSelectedComponent();
	}

	//-------------------------------------------------------------------------------
	//	Interface ChangeListener
	//-------------------------------------------------------------------------------
	
	public void stateChanged(ChangeEvent e)
	{
		if (listenTabChanges && (e.getSource()==getTabbedPane()))
		{
			int idx = getTabbedPane().getSelectedIndex();
			init(idx);
			activate(idx);
		}
	}

	//-------------------------------------------------------------------------------
	//	Protected Access
	//-------------------------------------------------------------------------------
	
	/**	called by the ctor
	 */
	protected JComponent createElementPane()
	{
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addChangeListener(this);
		return tabPane;
	}
}
