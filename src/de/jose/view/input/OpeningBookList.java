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

package de.jose.view.input;

import de.jose.book.BookFile;
import de.jose.Application;
import de.jose.comm.Command;
import de.jose.Version;
import de.jose.window.JoDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;


/**
 * OpeningBookList
 *
 * @author Peter Sch�fer
 */
public class OpeningBookList
		extends JList
		implements MouseListener
{
	/** Vector<BookFile> */
	private DefaultListModel model;
	private OpeningBookCellRenderer rend;

	public OpeningBookList(Vector lib)
	{
		super(new DefaultListModel());
		model = (DefaultListModel) this.getModel();
		setCellRenderer(rend = new OpeningBookCellRenderer());
		addMouseListener(this);

		updateListData(lib);
	}

	public void updateListData(Vector lib)
	{
		model.removeAllElements();
		for (int i=0; i < lib.size(); i++)
			model.addElement(((BookFile)lib.elementAt(i)).shallowClone());
	}

	/**
	 * @return Vector<BookFile>
	 */
	public Vector getEntries()
	{
		Vector result = new Vector();
		for (int i=0; i < model.size(); i++)
			result.add(model.elementAt(i));
		return result;
	}

	public void removeSelected()
	{
		Object[] selected = getSelectedValues();
		if (selected.length==0) return;

		for (int i=selected.length-1; i >= 0; i--)
		{
			BookFile bf = (BookFile) selected[i];
			model.remove(model.indexOf(selected[i]));
	}
		this.repaint();
	}

	public void add(File file)
	{
		/**
		 * is this book alraedy contained in the list ?
		 * is there an XML config element ?
		 */
		BookFile entry;
		for (int i=0; i < model.getSize(); i++)
		{
			entry = (BookFile)model.elementAt(i);
			if (file.equals(entry.file)) {
				entry.open();
				return;
			}
		}
		//  else:
		entry = new BookFile(file, Application.theApplication.theConfig);
		entry.open();
		model.addElement(entry);
		this.repaint();
	}

	public void moveSelected(int offset)
	{
		int[] selected = getSelectedIndices();
		if (selected.length==0) return;

		Arrays.sort(selected);
		clearSelection();

		if (offset < 0) {
			//  move up
			for (int i=0; i < selected.length; i++) {
				selected[i] = moveEntry(selected[i], selected[i]+offset);
				addSelectionInterval (selected[i],selected[i]);
			}
		}
		else {
			//  move down
			for (int i=selected.length-1; i>=0; i--) {
				selected[i] = moveEntry(selected[i], selected[i]+offset);
				addSelectionInterval (selected[i],selected[i]);
			}
		}

		this.repaint();
	}

	private int moveEntry(int index1, int index2)
	{
		if (index2<0) index2 = 0;
		if (index2>=getModel().getSize()) index2 = getModel().getSize()-1;

		if (index1==index2) return index1;

		BookFile entry = (BookFile) model.remove(index1);
		model.insertElementAt(entry, index2);

		return index2;
	}

	// ------------- implements MouseListener ---------------------------------------------------

	public void mouseClicked(MouseEvent e)
	{
		forwardMouseEvent(e);
	}

	public void mousePressed(MouseEvent e)
		{
		forwardMouseEvent(e);
		}

	public void mouseReleased(MouseEvent e)
	{
		forwardMouseEvent(e);
	}

	public void mouseEntered(MouseEvent e)
	{
		forwardMouseEvent(e);
	}

	public void mouseExited(MouseEvent e)
	{
		forwardMouseEvent(e);
	}

	private void forwardMouseEvent(MouseEvent e)
	{
		//  simulate clicks in check box and labels
		int cellIndex = locationToIndex(e.getPoint());
		if (cellIndex >= 0 && cellIndex < model.size())
		{
            Point p0 = indexToLocation(cellIndex);
			rend.updateValue((BookFile)model.elementAt(cellIndex));
			rend.forwardMouseEvent(e, p0);
		}
	}

	private class OpeningBookCellRenderer
			extends JPanel
			implements ListCellRenderer, ActionListener, ItemListener {
		private BookFile book;
		private JCheckBox checkbox;
		private JoStyledLabel label;

		public OpeningBookCellRenderer()
		{
			super(new GridBagLayout());

			checkbox = new JCheckBox();
			checkbox.setOpaque(false);

			label = new JoStyledLabel("");
			label.setOpaque(false);
			this.setOpaque(true);

			add(checkbox, JoDialog.LABEL_ONE_LEFT);
			add(label, JoDialog.ELEMENT_TWO);
		}

		public Component getListCellRendererComponent(JList list, Object value, int index,
		                                              boolean isSelected, boolean cellHasFocus)
		{
			if (isSelected)
				setBackground(getSelectionBackground());
			else
				setBackground(Color.white);

			setValue((BookFile)value);

			return this;
		}

		private void setValue(BookFile value)
		{
			book = value;
			if (book!=null)
			{
				label.setText(book.getInfoText());
				checkbox.setEnabled(book.isEnabled());
				checkbox.setSelected(book.isOpen());
			}
				else
			{
				label.setText("");
				checkbox.setEnabled(false);
				checkbox.setSelected(false);
			}
		}

		private void updateValue(BookFile value)
		{
			if (value != book) setValue(value);
		}

		protected void forwardMouseEvent(MouseEvent e, Point p0)
		{
			Point mp = new Point(e.getPoint().x-p0.x, e.getPoint().y-p0.y);

			if (checkbox.getBounds().contains(mp))
			{
				setLocation(p0);
				if (!Version.mac)
				{
					e = new MouseEvent(checkbox,
							e.getID(), e.getWhen(), e.getModifiers(),
							mp.x - checkbox.getX(),
							mp.y - checkbox.getY(),
							e.getClickCount(), e.isPopupTrigger(), e.getButton());

					try {
						checkbox.addItemListener(this);
						checkbox.dispatchEvent(e);
					} catch (Throwable thrw) {
						thrw.printStackTrace();
					} finally {
						checkbox.removeItemListener(this);
					}
				}
				else if (e.getID()==MouseEvent.MOUSE_RELEASED)
				{
					//  the above code doesn't work on Mac JDK, don't know why.
					//  Fall back to a simpler strategy:
					try {
						checkbox.addItemListener(this);
						checkbox.setSelected(! checkbox.isSelected());
					} catch (Throwable thrw) {
						thrw.printStackTrace();
					} finally {
						checkbox.removeItemListener(this);
					}
				}
			}
			else if (label.getBounds().contains(mp))
			{
				setLocation(p0);
				e = new MouseEvent(label,
						e.getID(), e.getWhen(), e.getModifiers(),
						mp.x-label.getX(),
						mp.y-label.getY(),
						e.getClickCount(), e.isPopupTrigger(), e.getButton());

				try {
					label.addActionListener(this);
					label.dispatchEvent(e);
				} catch (Throwable thrw) {
					thrw.printStackTrace();
				} finally {
					label.removeActionListener(this);
				}
			}

		}

		public void actionPerformed(ActionEvent e)
		{
			//  in response to a label click
			String url = e.getActionCommand();
			//  download this
			Command cmd = new Command("book.file.download", e, url);
			Application.theCommandDispatcher.forward(cmd, Application.theApplication);
		}

		public void itemStateChanged(ItemEvent e)
		{
			//  in response to a checkbox click
			boolean selected = checkbox.isSelected();
			book.open(selected);
			OpeningBookList.this.repaint();
		}
	}
}
