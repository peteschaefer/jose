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

package de.jose.view.colorchooser;

import de.jose.Language;
import de.jose.image.ImgUtil;
import de.jose.image.Surface;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

public class GradientChooserPanel
		extends AbstractColorChooserPanel
        implements ActionListener
{
    //-------------------------------------------------------------------------------
    //	variables
    //-------------------------------------------------------------------------------

    /**	first color is got from chooser panel */
	/**	second color	*/
	private Color	secondColor;
    private boolean reversed;
	/**	first point	*/
	private Point2D.Float	firstPoint;
	/**	second point	*/
	private Point2D.Float	secondPoint;
	/**	is it cyclic ?	*/
	private boolean cyclic;

	/**	owning color chooser	*/
	private JoSurfaceChooser chooser;
    /** view panel */
    private GradientView gradView;
    /** first color button */
    private GradientColorButton butColor1;
    private GradientColorButton butColor2;
    /** cyclic check box */
    private JCheckBox chkCyclic;

    //-------------------------------------------------------------------------------
    //	ctor
    //-------------------------------------------------------------------------------

    public GradientChooserPanel(JoSurfaceChooser ch)
    {
        super();

        chooser = ch;

        //  setup layout
        JPanel borderPanel = new JPanel( new BorderLayout());

		butColor1 = new GradientColorButton("colorchooser.gradient.color1");
		butColor1.addActionListener(this);
        butColor1.setPreferredSize(new Dimension(120,20));

		butColor2 = new GradientColorButton("colorchooser.gradient.color2");
		butColor2.addActionListener(this);
        butColor2.setPreferredSize(new Dimension(120,20));

		chkCyclic = new JCheckBox(Language.get("colorchooser.gradient.cyclic"), cyclic);
		chkCyclic.setActionCommand("colorchooser.gradient.cyclic");
		chkCyclic.addActionListener(this);

        gradView = new GradientView();
        gradView.setPreferredSize(new Dimension(240,240));

		JPanel buttonPanel = new JPanel(new GridLayout(3,1));
		buttonPanel.add(butColor1);
		buttonPanel.add(butColor2);
		buttonPanel.add(chkCyclic);

		borderPanel.add(buttonPanel,BorderLayout.WEST);
        borderPanel.add(gradView,BorderLayout.CENTER);
        add(borderPanel);

        firstPoint = new Point2D.Float(0f,0f);
        secondPoint = new Point2D.Float(1f,1f);
        secondColor = Color.black;
        update();
//		add(gradView,BorderLayout.CENTER);
    }

    //-------------------------------------------------------------------------------
    //	accessors
    //-------------------------------------------------------------------------------

    /** @return the first gradient color */
    public Color getFirstColor()
    {
        return reversed ? secondColor : chooser.getColor();
    }

    /** @return the second gradient color */
    public Color getSecondColor()
    {
        return reversed ? chooser.getColor() : secondColor;
    }

    /** @return a Gradient Paint */
    public GradientPaint getGradientPaint(float zx, float zy, float scalex, float scaley)
    {

		//	scale gradient from (1,1) to (scalex,scaley)
        Point2D p1 = new Point2D.Float(zx+firstPoint.x*scalex, zy+firstPoint.y*scaley);
        Point2D p2 = new Point2D.Float(zx+secondPoint.x*scalex, zy+secondPoint.y*scaley);

        return new GradientPaint(p1, getFirstColor(),
                                 p2, getSecondColor(),
                                 cyclic);
    }

    /** fill in te gradient paramaters of a Surface */
    public void setGradient(Surface srf)
    {
        if (srf==null) {
            firstPoint = new Point2D.Float(0,0);
            secondPoint = new Point2D.Float(1,1);
            if (ImgUtil.isDark(chooser.getColor()))
                secondColor = chooser.getColor().brighter();
            else
                secondColor = chooser.getColor().darker();
            cyclic = false;
            reversed = false;
        }
        else {
            firstPoint.x = srf.x1;
            firstPoint.y = srf.y1;
            secondPoint.x = srf.x2;
            secondPoint.y = srf.y2;
            secondColor = srf.gradientColor;
            if (secondColor==null) throw new NullPointerException("second color must not be null");
            cyclic = srf.cyclic;
            reversed = srf.reversed;
        }
    }

    /** fill in te gradient paramaters of a Surface */
    public void getGradient(Surface srf)
    {
        srf.x1 = firstPoint.x;
        srf.y1 = firstPoint.y;
        srf.x2 = secondPoint.x;
        srf.y2 = secondPoint.y;
        srf.gradientColor = secondColor;
        srf.cyclic = cyclic;
        srf.reversed = reversed;
    }

    public void update()
    {
        butColor1.setColor(getFirstColor());
        butColor2.setColor(getSecondColor());
        chkCyclic.setSelected(cyclic);
        gradView.repaint();
    }

    protected void chooseFirstColor()
    {
        if (reversed) {
            //  swap colors
            Color col = secondColor;
            secondColor = chooser.getColor();
            chooser.setColor(col);
            reversed = false;
        }
        //  switch to first tab
        chooser.switchTab(0);
    }

    protected void chooseSecondColor()
    {
        if (!reversed) {
            //  swap colors
            Color col = secondColor;
            secondColor = chooser.getColor();
            chooser.setColor(col);
            reversed = true;
        }
        //  switch to first tab
        chooser.switchTab(0);
    }

    //-------------------------------------------------------------------------------
	//	implements ActionListener
	//-------------------------------------------------------------------------------

    public void actionPerformed(ActionEvent evt)
	{
		String command = evt.getActionCommand();

		if (command.equals("colorchooser.gradient.color1"))
            chooseFirstColor();

		if (command.equals("colorchooser.gradient.color2"))
            chooseSecondColor();

		if (command.equals("colorchooser.gradient.cyclic"))
		{
            cyclic = chkCyclic.isSelected();
            chooser.gradientChanged();
            gradView.repaint();
		}
	}


    //-------------------------------------------------------------------------------
	//	extends AbstractChooserPanel
	//-------------------------------------------------------------------------------

	public void buildChooser()
	{
		/*	what shall we do here ? */
	}

	/**	called when the model changes
	 */
	public void updateChooser()
	{
		/*	what shall we do here ? */
	}

	public String getDisplayName()
	{
		return Language.get("colorchooser.gradient");
	}

	public Icon getSmallDisplayIcon()
	{
		return null;
	}

	public Icon getLargeDisplayIcon()
	{
		return null;
	}

    public int getDisplayedMnemonicIndex()
    {
        return Language.getMnemonicCharIndex("colorchooser.gradient");
    }

    public int getMnemonic()
    {
        return (int)Language.getMnemonic("colorchooser.gradient");
    }

    //-------------------------------------------------------------------------------
    //	inner class: selection view
    //-------------------------------------------------------------------------------

    private class GradientView
                extends JComponent
                implements MouseListener, MouseMotionListener
    {
        public GradientView()
		{
			super();
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		private static final int outerInset =  8;
		private static final int innerInset = 10;

		//	mouse dragging
		private int dragState = 0;

		public void paint(Graphics g)
		{
			Rectangle outerBox = new Rectangle(outerInset,outerInset,
									getWidth()-2*outerInset, getHeight()-2*outerInset);
			Rectangle innerBox =  new Rectangle(innerInset,innerInset,
									getWidth()-2*innerInset, getHeight()-2*innerInset);

			Graphics2D g2 = (Graphics2D)g;
			//	fill the interior with the paint (scale gradient to real size)
            GradientPaint gp = getGradientPaint(innerBox.x,innerBox.y, innerBox.width,innerBox.height);
            g2.setPaint(gp);
            g2.fillRect(innerBox.y,innerBox.y, innerBox.width,innerBox.height);

			//	paint a nice border
			g2.setColor(Color.black);
			g2.drawRect(outerBox.x,outerBox.y, outerBox.width-1,outerBox.height-1);
			g2.drawRect(outerBox.x+1,outerBox.y+1, outerBox.width-2,outerBox.height-2);

			//	draw first dot
			Point pi = innerPoint(firstPoint);
			g2.setColor(getFirstColor());
			g2.fillOval(pi.x-6,pi.y-6, 12,12);
			g2.setColor(Color.black);
			g2.drawOval(pi.x-6,pi.y-6, 12,12);

			//	draw second dot
			pi = innerPoint(secondPoint);
			g2.setColor(getSecondColor());
			g2.fillOval(pi.x-6,pi.y-6, 12,12);
			g2.setColor(Color.black);
			g2.drawOval(pi.x-6,pi.y-6, 12,12);
		}

		private Point innerPoint(Point2D pd)
		{
			return new Point((int)(innerInset + pd.getX()*(getWidth()-2*innerInset)),
							(int)(innerInset + pd.getY()*(getHeight()-2*innerInset)));
		}

		private Point2D.Float gradientPoint(Point pi)
		{
			float x = ((float)pi.x-innerInset) / (getWidth()-2*innerInset);
			float y = ((float)pi.y-innerInset) / (getHeight()-2*innerInset);
			if (x < 0.0) x = 0.0f;
			if (y < 0.0) y = 0.0f;
			if (x > 1.0) x = 1.0f;
			if (y > 1.0) y = 1.0f;
			return new Point2D.Float(x,y);
		}

		public void mouseClicked(MouseEvent evt)
		{
			switch (dragState) {
			case 1:	chooseFirstColor(); break;
			case 2: chooseSecondColor(); break;
			}
			dragState = 0;
		}

		public void mousePressed(MouseEvent evt)
		{
			Point p1 = innerPoint(firstPoint);
			Point p2 = innerPoint(secondPoint);
			if (p1.distance(evt.getPoint()) <= 6)
				dragState = 1;
			else if (p2.distance(evt.getPoint()) <= 6)
				dragState = 2;
			else
				dragState = 0;
		}

		public void mouseReleased(MouseEvent evt)	{
//			mouseDragged(evt);
            chooser.gradientChanged();
		}

		public void mouseDragged(MouseEvent evt)
		{
			Point2D.Float pd = gradientPoint(evt.getPoint());
			switch (dragState) {
			case 1:	firstPoint = pd; break;
			case 2: secondPoint = pd; break;
			}
			repaint();
		}

		public void mouseMoved(MouseEvent evt)		{	}
		public void mouseEntered(MouseEvent evt)	{	}
		public void mouseExited(MouseEvent evt)		{	}
    }

    //-------------------------------------------------------------------------------
	//	inner class: color display button
	//-------------------------------------------------------------------------------

    private static class GradientColorButton extends JoColorButton
    {
        GradientColorButton()                       { super(); }

        GradientColorButton(String command)         { super(command); }

        public void actionPerformed(ActionEvent evt)
        {
            //  TODO
        }
    }

}
