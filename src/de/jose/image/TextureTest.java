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

package de.jose.image;

import de.jose.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class TextureTest
	extends JComponent
	implements ActionListener
{
	protected JDialog dlg;
	protected File dir;
	protected String[] fileList;
	protected int current;
	protected Image img;
	protected JLabel label;
	
	public static void main(String[] args)
	{
		try {
			TextureTest tt = new TextureTest();
			tt.openDialog(args[0]);
		} catch (Exception ex) {
			Application.error(ex);
		}
	}
	
	public void openDialog(String path)
		throws IOException
	{
		dir = new File(path);
		fileList = TextureCache.getInstalledTextures();
		current = -1;
		
		dlg = new JDialog();
		dlg.setModal(true);
		
		label = new JLabel();
		label.setSize(380,20);
	
		JButton backButton = new JButton("< Back");
		backButton.setActionCommand("back");
		backButton.addActionListener(this);
		
		JButton nextButton = new JButton("Next >");
		nextButton.setActionCommand("next");
		nextButton.addActionListener(this);
		
		JButton deleteButton = new JButton("Delete");
		deleteButton.setActionCommand("delete");
		deleteButton.addActionListener(this);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		
		dlg.getContentPane().setLayout(new BorderLayout());
		
		setSize(380,380);
		dlg.getContentPane().add(this, BorderLayout.CENTER);
		dlg.getContentPane().add(label, BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(backButton);
		buttonPanel.add(nextButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(cancelButton);
		dlg.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		showNextTexture(+1);
		
		dlg.setBounds(40,40, 400,400);
		dlg.show();
	}
	
	public void paintComponent(Graphics g)
	{
		g.setColor(Color.black);
		g.drawRect(0,0,getWidth()-1,getHeight()-1);
		
		TextureCache.paintTexture(g, 0, 0, getWidth(), getHeight(), img, -80, -80);
	}
	
	public void showNextTexture(int increment)
	{
		current += increment;
		if (current < 0 || current >= fileList.length)
			System.exit(+1);
		
		img = TextureCache.getTexture(fileList[current], TextureCache.LEVEL_MAX);
		label.setText(fileList[current]+" ("+img.getWidth(null)+" * "+img.getHeight(null)+")");
		repaint();
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		if ("back".equals(evt.getActionCommand())) {
			showNextTexture(-1);
		}
		if ("next".equals(evt.getActionCommand())) {
			showNextTexture(+1);
		}
		if ("delete".equals(evt.getActionCommand())) {
			File f = new File(dir,fileList[current]);
			f.delete();
			showNextTexture(+1);
		}
		if ("cancel".equals(evt.getActionCommand()))
			System.exit(+1);
	}
	
	protected void getAllFiles(File directory, Vector list)
	{
		File[] files = directory.listFiles();
		for (int i=0; i<files.length; i++) {
			if (files[i].isHidden())
				continue;
			if (files[i].isDirectory())
				getAllFiles(files[i],list);
			
			String name = files[i].getName().toLowerCase();
			if (name.endsWith(".gif") || name.endsWith(".jpg") || name.endsWith(".jpeg"))
				list.add(files[i]);
		}
	}
}
