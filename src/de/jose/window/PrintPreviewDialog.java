package de.jose.window;
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

import de.jose.CommandAction;
import de.jose.Command;
import de.jose.Application;
import de.jose.Util;
import de.jose.export.ExportContext;
import de.jose.pgn.Game;
import de.jose.profile.LayoutProfile;
import de.jose.profile.UserProfile;
import de.jose.view.input.UciSpinner;
import de.jose.view.input.JoButtonGroup;
import de.jose.view.JoPanel;
import de.jose.image.ImgUtil;
import de.jose.util.print.*;
import de.jose.util.xml.XMLUtil;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.Map;
import java.util.Collection;

/**
 *
 */
public class PrintPreviewDialog
        extends JoPanel
        implements ChangeListener, ItemListener
{
	protected JPanel buttonPane;

	protected PrintPreview printView;
	protected JScrollPane printViewScroller;

	protected PrintScaleComboBox scale;
	protected JToggleButton onePage, twoPage;
	protected JToggleButton landscape, portrait;
	protected JButton prevPage, nextPage;
	protected JSpinner currentPage;

	protected ExportContext context;

	public PrintPreviewDialog(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
	{
		super(profile,withContextMenu,withBorder);

		setLayout(new BorderLayout());
		context = new ExportContext();
	}

	public void init()
	{
		//  put the button page on top !
		buttonPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
		add(buttonPane,BorderLayout.NORTH);

		printView = new PrintPreview();
		printView.setBorder(new LineBorder(Color.black,1,true));

		printViewScroller = new JScrollPane(printView,
		        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		printViewScroller.getHorizontalScrollBar().setUnitIncrement(50);
		printViewScroller.getVerticalScrollBar().setUnitIncrement(50);

		printView.setParent(printViewScroller.getViewport());

		add(printViewScroller,BorderLayout.CENTER);

		scale = new PrintScaleComboBox(printView);
		scale.addItemListener(this);

		onePage = JoDialog.newToggleButton("print.preview.page.one", ImgUtil.getIcon("menu","print.preview.one"));
		twoPage = JoDialog.newToggleButton("print.preview.page.two", ImgUtil.getIcon("menu","print.preview.two"));

		JoButtonGroup bg = new JoButtonGroup("print.preview.page");
		bg.add(onePage);
		bg.add(twoPage);

		onePage.addChangeListener(this);
		twoPage.addChangeListener(this);

		landscape = JoDialog.newToggleButton("print.preview.ori.land", ImgUtil.getMenuIcon("print.preview.land"));
		portrait = JoDialog.newToggleButton("print.preview.ori.port", ImgUtil.getMenuIcon("print.preview.port"));

		bg = new JoButtonGroup("print.preview.ori");
		bg.add(landscape);
		bg.add(portrait);

		landscape.addChangeListener(this);
		portrait.addChangeListener(this);

		currentPage = new UciSpinner(1,99,1);
		currentPage.addChangeListener(this);

		buttonPane.add(JoDialog.newLabel("dialog.export.paper.format"));
		buttonPane.add(prevPage = JoDialog.newButton("print.preview.previous.page",ImgUtil.getMenuIcon("page.previous"),this));
		buttonPane.add(currentPage);
		buttonPane.add(nextPage = JoDialog.newButton("print.preview.next.page",ImgUtil.getMenuIcon("page.next"),this));
		buttonPane.add(Box.createHorizontalStrut(10));
		buttonPane.add(onePage);
		buttonPane.add(twoPage);
		buttonPane.add(Box.createHorizontalStrut(10));

		buttonPane.add(portrait);
		buttonPane.add(landscape);

		buttonPane.add(Box.createHorizontalStrut(10));
		buttonPane.add(JoDialog.newLabel("dialog.export.paper.size"));
		buttonPane.add(scale);
		buttonPane.add(Box.createHorizontalStrut(10));
		buttonPane.setBorder(new EmptyBorder(4,4,4,4));

		buttonPane.add(Box.createHorizontalStrut(10));
		buttonPane.add(JoDialog.newButton("menu.file.print.setup",this));
		buttonPane.add(JoDialog.newButton("menu.file.print",ImgUtil.getMenuIcon("menu.file.print"),this));
//		buttonPane.add(Box.createHorizontalStrut(10));
//		buttonPane.add(JoDialog.newButton(JoDialog.CANCEL,this));
//		buttonPane.add(Box.createHorizontalStrut(10));
//		buttonPane.add(JoDialog.newButton(JoDialog.HELP,this));

		read();
	}

	public void adjustContextMenu(Collection list, MouseEvent event)
	{
		super.adjustContextMenu(list, event);

		list.add("dialog.print.preview.refresh");
	}

	public void setContext(ExportContext acontext) throws Exception
	{
		this.context = acontext;
	}


	public void reset() throws Exception
	{
		printView.disposeDocument();    //  release FOP renderer, etc.

		if (context.preview==null)			//  create the preview document; this is highly document specific
			context.preview = context.createPrintableDocument();

		printView.setDocument(context.preview);
		printView.setPageFormat(context.profile.getPageFormat(true));        //printView.getFormat());

		setCurrentPage(1);
		adjustPages();
	}

	public void setPageFormat(PageFormat format)
	{
		printView.setPageFormat(format);
		adjustOrientation(format.getOrientation());
		adjustPages();
	}

	public void setOrientation(int orientation)
	{
		printView.setOrientation(orientation);
		adjustOrientation(orientation);
		adjustPages();
	}

	private void adjustOrientation(int orientation)
	{
		switch (orientation)
		{
		default:
		case PageFormat.PORTRAIT:
			portrait.setSelected(true);
			landscape.setSelected(false);
			break;
		case PageFormat.LANDSCAPE:
			portrait.setSelected(false);
			landscape.setSelected(true);
			break;
		}
	}

	public void adjustPages()
	{
		int max = printView.countPages();
		if (twoPage.isSelected()) max--;
		if (max < 1) max = 1;

		((SpinnerNumberModel)currentPage.getModel()).setMaximum(new Integer(max));
		if (getCurrentPage() > max) setCurrentPage(max);
		//  adjust buttons
		prevPage.setEnabled(getCurrentPage() > 1);
		nextPage.setEnabled(getCurrentPage() < max);
	}

	protected void adjustScale()
	{
		// scale modified
		double sc = scale.getScale();
		printView.setScale(sc);

		if (sc==PrintPreview.FIT_TEXT_WIDTH) {
			//  scroll to upper left corner
			Dimension dim = printView.getPreferredSize();
			Rectangle imgbounds = printView.getImageArea(dim,true);
			Point p0 = new Point();

			if (imgbounds.width > dim.width)
				p0.x = imgbounds.x;    //  align
			else
				p0.x = (dim.width-imgbounds.width)/2;       //  center

			if (imgbounds.height > dim.height)
				p0.y = imgbounds.y;     //  align
			else
				p0.y = (dim.height-imgbounds.height)/2;     //  center

			printViewScroller.getViewport().setViewPosition(p0);
		}
	}


	public void setScale(double scale) {
		printView.setScale(scale);
		this.scale.setScale(scale);
	}

	public void setTwoPage(boolean two)
	{
		printView.setTwoPage(two);
		onePage.setSelected(!two);
		twoPage.setSelected(two);
		adjustPages();
	}

	public int getCurrentPage()
	{
		return Util.toint(currentPage.getValue());
	}

	public void setCurrentPage(int page)
	{
		printView.setCurrentPage(page);
		currentPage.setValue(new Integer(page));
		adjustPages();
	}

	public void nextPage(int offset)
	{
		int oldPage = Util.toint(currentPage.getValue());
		int newPage = Util.inBounds(1, oldPage+offset, printView.countPages());
		if (oldPage != newPage)
			setCurrentPage(newPage);
	}

	public void read()
	{
		UserProfile prf = Application.theUserProfile;
		//  get page format
        PrinterJob job = PrinterJob.getPrinterJob();
		PageFormat format = prf.getPageFormat(false);
		format = PrintableDocument.validPageFormat(job,format);
		//  get scaling factors, etc.
		double scale = prf.getDouble("print.preview.scale",PrintPreview.FIT_PAGE);
		boolean twoPage = prf.getBoolean("print.preview.twopage",false);

		if (context.preview!=null)
			printView.setDocument(context.preview);
		else
			printView.setDocument(new AWTPrintableDocument(new Game(null,null),format));

		setPageFormat(format);
		setScale(scale);
		setTwoPage(twoPage);
		setCurrentPage(1);
	}

	public boolean save()
	{
		UserProfile prf = Application.theUserProfile;
		prf.set("print.preview.scale",scale.getValue());
		prf.set("print.preview.twopage",Util.toBoolean(twoPage.isSelected()));
		prf.setPageFormat(printView.getFormat());
		return true;
	}



	public void stateChanged(ChangeEvent e)
	{
		//  currentPage modified
		if (e.getSource()==currentPage) {
			//  next page
			setCurrentPage(((Number)currentPage.getValue()).intValue());
		}

		if (e.getSource()==onePage && onePage.isSelected()) setTwoPage(false);
		if (e.getSource()==twoPage && twoPage.isSelected()) setTwoPage(true);

		if (e.getSource()==portrait && portrait.isSelected()) {
			setOrientation(PageFormat.PORTRAIT);
			Application.theApplication.broadcast(new Command("print.orientation.modified",null,new Integer(PageFormat.PORTRAIT)));
		}
		if (e.getSource()==landscape && landscape.isSelected()) {
			setOrientation(PageFormat.LANDSCAPE);
			Application.theApplication.broadcast(new Command("print.orientation.modified",null,new Integer(PageFormat.LANDSCAPE)));
		}
	}

	public void itemStateChanged(ItemEvent e)
	{
		if (e.getSource()==scale && e.getStateChange()==ItemEvent.SELECTED)
			adjustScale();
	}

	public void setupActionMap(Map map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				//  always save settings
				save();
				hidePanel();
			}
		};
		map.put("dialog.button.cancel",action);
		map.put("dialog.button.close",action);
		map.put("dialog.button.ok",action);


		action = new CommandAction()  {
			public void Do(Command cmd) { nextPage(+1); }
		};
		map.put("print.preview.next.page",action);

		action = new CommandAction()  {
			public void Do(Command cmd) { nextPage(-1); }
		};
		map.put("print.preview.previous.page",action);

		action = new CommandAction()  {
			public void Do(Command cmd) throws Exception
			{
				//  forward to application
				cmd.data = context.source;
				Application.theCommandDispatcher.forward(cmd,Application.theApplication);
			}
		};
		map.put("menu.file.print.setup", action);



		action = new CommandAction()  {
			public boolean isEnabled(String code) {
				return true;
			}
			public void Do(Command cmd) throws Exception
			{
				//  close, then print
//				save();
//				hidePanel();

				//  forward
				cmd.code = "export.print";
				cmd.data = printView.getPrintableDocument();
				Application.theCommandDispatcher.forward(cmd,Application.theApplication);
			}
		};
		map.put("menu.file.print", action);
		map.put("export.print", action);


		action = new CommandAction() {
			public void Do(Command cmd) {
				cmd.code = "menu.help.context";
				cmd.data = printView;
				Application.theCommandDispatcher.forward(cmd,Application.theApplication);
			}
		};
		map.put("dialog.button.help",action);


		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				setPageFormat(context.profile.getPageFormat(true));
			}
		};
		map.put("print.settings.modified",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				//  force refresh
				//  flush cached xsl sheets !!
				XMLUtil.clearTransformers();
//				reset();
				context.preview.render();
				printView.repaint();
			}
		};
		map.put("dialog.print.preview.refresh",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				adjustPages();
				printView.repaint();
			}
		};
		map.put("doc.preview.refresh",action);


		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				save();
			}
		};
		map.put("update.user.profile", action);
	}
}
