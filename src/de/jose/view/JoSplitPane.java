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

import com.formdev.flatlaf.ui.FlatSplitPaneUI;
import de.jose.AbstractApplication;
import de.jose.comm.CommandAction;
import de.jose.comm.CommandListener;
import de.jose.Util;
import de.jose.window.JoFrame;

import javax.swing.*;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.metal.MetalSplitPaneUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Map;

public class JoSplitPane
		extends JSplitPane
		implements JoComponent, CommandListener
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	/*	adjust divider location for first component	*/
	public static final int DIVIDE_FIRST	= -1;
	/*	adjust divider location for second component	*/
	public static final int DIVIDE_SECOND	= -2;
	/*	adjust divider location for relative weigths	*/
	public static final int DIVIDE_WEIGHT	= -3;
	/*  don't adjust divider location, just keep it */
	public static final int DIVIDE_USER     = -4;
	public static final int DIVIDER_SIZE = 8;

	protected int dividerMethod;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public JoSplitPane(int newOrientation, int dividerMethod,
                  JoComponent newLeftComponent,
                  JoComponent newRightComponent,
				  Dimension size)
	{
		super(newOrientation, false,
                (Component)newLeftComponent,
                (Component)newRightComponent);
        /*
         * workaround for GTK look & feel. This value is missing and would crash JoSplitPaneUI
         * (note that the actual divider size will be overwritten, anyway)
         */
//        System.out.println(UIManager.getDefaults().get("SplitPaneUI"));
//        UIManager.put("SplitPane.dividerSize",16);
//		setUI(new JoSplitPaneUI());

		if (size==null)
			size = getSize();
		else
			setSize(size);
//		setDividerSize(3);
		setOneTouchExpandable(false);
		
		this.dividerMethod = dividerMethod;
		if (size.width > 0 && size.height > 0)
            adjustDivider(size);

        updateContinuousLayout();
		AbstractApplication.theCommandDispatcher.addCommandListener(this);
	}

    protected void adjustDivider(Dimension size)
    {
	    if (size==null || size.width <= 0 || size.height <= 0)
	        return;

	    int newDividerLocation = dividerMethod;
        double resizeWeight;

	    double min1,min2;
	    double max1,max2;

	    //  min and max divider locations
	    if (orientation==HORIZONTAL_SPLIT)
	    {
		    min1 = firstComponent().getMinimumSize().width;
		    max1 = firstJoComponent().getMaximumSize(orientation).width;

		    max2 = size.width - secondComponent().getMinimumSize().width;
		    min2 = size.width - secondJoComponent().getMaximumSize(orientation).width;
	    }
	    else
	    {
			min1 = firstComponent().getMinimumSize().height;
		    max1 = firstJoComponent().getMaximumSize(orientation).height;

		    max2 = size.height - secondComponent().getMinimumSize().height;
		    min2 = size.height - secondJoComponent().getMaximumSize(orientation).height;
	    }

        switch (dividerMethod) {
        case DIVIDE_FIRST:
            if (orientation==HORIZONTAL_SPLIT) {
                newDividerLocation = Math.min(leftComponent.getWidth(), (int)max1);
                if ((newDividerLocation + getDividerSize()) > max2)
                    newDividerLocation = DIVIDE_WEIGHT;
            }
            else {
                newDividerLocation = Math.min(leftComponent.getHeight(), (int)max1);
                if ((newDividerLocation + getDividerSize()) > max2)
                    newDividerLocation = DIVIDE_WEIGHT;
            }
            break;

        case DIVIDE_SECOND:
            if (orientation==HORIZONTAL_SPLIT)
            {
                int newWidth = Math.min(rightComponent.getWidth(),
                            secondJoComponent().getMaximumSize(orientation).width);
                newDividerLocation = size.width-newWidth-getDividerSize();
                if (newDividerLocation < min1)
                    newDividerLocation = DIVIDE_WEIGHT;
            }
            else
            {
                int newHeight = Math.min(rightComponent.getHeight(),
                            secondJoComponent().getMaximumSize(orientation).height);
                newDividerLocation = size.height-newHeight-getDividerSize();
                if (newDividerLocation < min1)
                    newDividerLocation = DIVIDE_WEIGHT;
            }
            break;

	    case DIVIDE_USER:
	        newDividerLocation = getDividerLocation();
	        break;

        case DIVIDE_WEIGHT:
            /*	setting the divider location to a negative value
             *	will trigger a recalculation based on the resize weight */
            break;

		default:
			//  dividerMethod is already the correct value
			break;
        }

	    // once the divider location is established, let the user resize it
	    dividerMethod = DIVIDE_USER;

        if ((newDividerLocation < 0) || (size.width==0) || (size.height==0))
        {
            //	divide by weight
            if (orientation==HORIZONTAL_SPLIT) {
                double sum = firstJoComponent().getWeightX() + secondJoComponent().getWeightX();
                resizeWeight = (sum==0.0) ? 0.5 : (firstJoComponent().getWeightX()/sum);
            }
            else {
                double sum = firstJoComponent().getWeightY() + secondJoComponent().getWeightY();
                resizeWeight = (sum==0.0) ? 0.5 : (firstJoComponent().getWeightY()/sum);
            }
            if (newDividerLocation <= 0)
            {
                if (orientation==HORIZONTAL_SPLIT)
                    newDividerLocation = (int)Math.round(resizeWeight * size.width);
                else
                    newDividerLocation = (int)Math.round(resizeWeight * size.height);
            }
        }
        else {
            //	keep resize ratio
            if (orientation==HORIZONTAL_SPLIT)
                resizeWeight = (double)newDividerLocation / size.width;
            else
                resizeWeight = (double)newDividerLocation / size.height;
        }

//        if (resizeWeight <= 0.0) resizeWeight = 0.01;	//	resizeWeight==0.0 doesn't work well
//        if (resizeWeight >= 1.0) resizeWeight = 0.99;

	    //  check min/max constraints
		double minD = Math.max(min1,min2);
		double maxD = Math.min(max1,max2);

		if (minD>maxD)
			newDividerLocation = (int)((minD+maxD)/2);
		else if (newDividerLocation < minD)
			newDividerLocation = (int)minD;
		else if (newDividerLocation > maxD)
			newDividerLocation = (int)maxD;

		if (newDividerLocation > 0)
		{
//			System.err.println("setDividerLocation("+newDividerLocation+
//					","+firstComponent().getName()+
//					","+secondComponent().getName()+")");
			if (newDividerLocation!=getDividerLocation())
				setDividerLocation(newDividerLocation);
		}
        if (resizeWeight>0.0 && resizeWeight<1.0)
        {
//	        System.err.println("setResizeWeight("+resizeWeight+
//	                ","+firstComponent().getName()+
//	                ","+secondComponent().getName()+")");
	        if (resizeWeight!=getResizeWeight())
	            setResizeWeight(resizeWeight);
	    }
    }

    @Override
    public void reshape(int x, int y, int width, int height)
    {
        //  adjust divider locations, based on new size
	    if (width > 0 && height > 0 && firstComponent()!=null && secondComponent()!=null)
	        adjustDivider(new Dimension(width,height));
        super.reshape(x,y,width,height);
    }

	@Override
	public void setBounds(int x, int y, int width, int height) {
		//  adjust divider locations, based on new size
		if (width > 0 && height > 0 && firstComponent()!=null && secondComponent()!=null)
			adjustDivider(new Dimension(width,height));
		super.setBounds(x, y, width, height);
	}

	public String getName()
	{
		StringBuilder buf = new StringBuilder();
		buf.append("(");
		if (getLeftComponent()!=null) buf.append(getLeftComponent().getName());
		buf.append("/");
		if (getRightComponent()!=null) buf.append(getRightComponent().getName());
		buf.append(")");
		return buf.toString();
	}


	public Component firstComponent()   { return getLeftComponent(); }
	public Component secondComponent()	{ return getRightComponent(); }

    public JoComponent firstJoComponent()   { return (JoComponent)getLeftComponent(); }
    public JoComponent secondJoComponent()	{ return (JoComponent)getRightComponent(); }

	public Component otherComponent(Component aComponent)
	{
		if (aComponent==firstComponent())
			return secondComponent();
		else
			return firstComponent();
	}

	public void replaceFirst(Component b)
	{
		if (firstComponent()!=null) {
			Rectangle bounds = firstComponent().getBounds();
			Dimension max = ((JoComponent)b).getMaximumSize(getOrientation());
			if (getOrientation()==HORIZONTAL_SPLIT) {
                if (max.width < bounds.width) bounds.width = max.width;
				setDividerLocation(bounds.width);
            }
			else if (getOrientation()==VERTICAL_SPLIT) {
                if (max.height < bounds.height) bounds.height = max.height;
				setDividerLocation(bounds.height);
            }
			b.setBounds(bounds);
		}
        else if (secondComponent()!=null) {
            if (getOrientation()==HORIZONTAL_SPLIT)
                setDividerLocation(getWidth()-secondComponent().getWidth());
            else
                setDividerLocation(getHeight()-secondComponent().getHeight());
        }
		setLeftComponent(b);
		updateContinuousHierarchy();
//		setDividerLocation(getDividerLocation());
		/*	looks like a noop but will effectively avoid a new layout */
//	really needed ?
	}

	public void replaceSecond(Component b)
	{
		if (secondComponent()!=null) {
			Rectangle bounds = secondComponent().getBounds();
			Dimension max = ((JoComponent)b).getMaximumSize(getOrientation());
			if (getOrientation()==HORIZONTAL_SPLIT) {
                if (max.width < bounds.width) bounds.width = max.width;
				setDividerLocation(getWidth()-bounds.width);
            }
			else if (getOrientation()==VERTICAL_SPLIT) {
                if (max.height < bounds.height) bounds.height = max.height;
				setDividerLocation(getHeight()-bounds.height);
            }
			b.setBounds(bounds);
		}
        else if (firstComponent()!=null) {
            if (getOrientation()==HORIZONTAL_SPLIT)
                setDividerLocation(firstComponent().getWidth());
            else
                setDividerLocation(firstComponent().getHeight());
        }
		setRightComponent(b);
		updateContinuousHierarchy();
//		setDividerLocation(getDividerLocation());
		/*	looks like a noop but will effectively avoid a new layout		 */
//	really needed ?
	}

	protected void updateContinuousHierarchy()
	{
		for (Container c = this; c != null; c = c.getParent())
			if (c instanceof JoSplitPane)
				((JoSplitPane)c).updateContinuousLayout();
	}

	protected void updateContinuousLayout()
	{
		setContinuousLayout(firstJoComponent().isContinuousLayout() && secondJoComponent().isContinuousLayout());

		/*
		 * hide dividers for fixed-size components (toolbars, in particular)
		 */
		if (getOrientation()==HORIZONTAL_SPLIT)
			setDividerEnabled(firstJoComponent().getWeightX() > 0.0 && secondJoComponent().getWeightX() > 0.0);
		else
			setDividerEnabled(firstJoComponent().getWeightY() > 0.0 && secondJoComponent().getWeightY() > 0.0);
	}

	private void setDividerEnabled(boolean movable) {
		Object value = UIManager.get("SplitPane.dividerSize");
		int dividerSize = (int)Util.nvl(value,Integer.valueOf(DIVIDER_SIZE));
		setDividerSize(movable ? dividerSize:2);
	}

	public void startContinuousResize()
	{
		if (firstComponent()!=null)
			firstJoComponent().startContinuousResize();
		if (secondComponent()!=null)
			secondJoComponent().startContinuousResize();
	}

	public void finishContinuousResize()
	{
		if (firstComponent()!=null)
			firstJoComponent().finishContinuousResize();
		if (secondComponent()!=null)
			secondJoComponent().finishContinuousResize();
	}

	/**	overrides JSplitPane.isValidateRoot	 */
	public boolean isValidateRoot()
	{
		return false;
	}

	public JoFrame getParentFrame()
	{
		return (JoFrame)getTopLevelAncestor();
	}

	public Dimension getMaximumSize(int orientation)
	{
		double mwidth = 0;
		double mheight = 0;   //  accounts for arithmetic overflow
		if (firstJoComponent()!=null) {
			Dimension max1 = firstJoComponent().getMaximumSize(orientation);
			mwidth += max1.width;
			mheight += max1.height;
		}
		if (secondJoComponent()!=null) {
			Dimension max2 = secondJoComponent().getMaximumSize(orientation);
			mwidth += max2.width;
			mheight += max2.height;
		}
		if (orientation==HORIZONTAL_SPLIT)
			mwidth += getDividerSize();
		else
			mheight += getDividerSize();
		return new Dimension(
		        (int)Util.inBounds(Integer.MIN_VALUE,mwidth,Integer.MAX_VALUE),
		        (int)Util.inBounds(Integer.MIN_VALUE,mheight,Integer.MAX_VALUE));
	}

	public Dimension getMaximumSize()
	{
		double mwidth = 0;
		double mheight = 0;
		if (firstComponent()!=null) {
			Dimension max1 = firstComponent().getMaximumSize();
			mwidth += max1.width;
			mheight += max1.height;
		}
		if (secondJoComponent()!=null) {
			Dimension max2 = secondComponent().getMaximumSize();
			mwidth += max2.width;
			mheight += max2.height;
		}
		if (orientation==HORIZONTAL_SPLIT)
			mwidth += getDividerSize();
		else
			mheight += getDividerSize();
		return new Dimension(
		        (int)Util.inBounds(Integer.MIN_VALUE,mwidth,Integer.MAX_VALUE),
		        (int)Util.inBounds(Integer.MIN_VALUE,mheight,Integer.MAX_VALUE));
	}

	public Dimension getMinimumSize()
	{
		double mwidth = 0;
		double mheight = 0;
		if (firstComponent()!=null) {
			Dimension min1 = firstComponent().getMinimumSize();
			mwidth += min1.width;
			mheight += min1.height;
		}
		if (secondComponent()!=null) {
			Dimension min2 = secondComponent().getMinimumSize();
			mwidth += min2.width;
			mheight += min2.height;
		}
		if (orientation==HORIZONTAL_SPLIT)
			mwidth += getDividerSize();
		else
			mheight += getDividerSize();
		return new Dimension(
		        (int)Util.inBounds(Integer.MIN_VALUE,mwidth,Integer.MAX_VALUE),
		        (int)Util.inBounds(Integer.MIN_VALUE,mheight,Integer.MAX_VALUE));
	}

	public void setVisible(boolean vis)
	{
		for (int i=0; i<getComponentCount(); i++)
			getComponent(i).setVisible(vis);
		super.setVisible(vis);
	}

	//-------------------------------------------------------------------------------
	//	interface CommandListener
	//-------------------------------------------------------------------------------

	public CommandListener getCommandParent()
	{
		return getParentFrame();
	}

	public int numCommandChildren()
	{
		return 2;
	}

	public CommandListener getCommandChild(int i)
	{
		switch (i) {
		case 0:	return (CommandListener)firstComponent();
		case 1:	return (CommandListener)secondComponent();
		default:	throw new IllegalArgumentException("i");
		}
	}

	public void setupActionMap(Map<String, CommandAction> map)
	{
		/*	we do not handle event on ourselves	*/
	//	return;
	}

	//-------------------------------------------------------------------------------
	//	interface JoComponent
	//-------------------------------------------------------------------------------

	public boolean showContextMenu()		{ return false; }

	@Override
    public boolean showControls() {
        return false;
    }

    public void adjustContextMenu(Collection<Object> ignore, MouseEvent event)
	{	}

	public float getWeightX() {
        if (firstComponent()==null && secondComponent()==null)
            return 1.0f; //  strange ?
        else if (secondComponent()==null)
            return ((JoComponent)firstComponent()).getWeightX();
        else if (firstComponent()==null)
            return ((JoComponent)secondComponent()).getWeightX();
		else if (getOrientation()==HORIZONTAL_SPLIT)
			return ((JoComponent)firstComponent()).getWeightX() +
				   ((JoComponent)secondComponent()).getWeightX();
		else
			return Math.max(((JoComponent)firstComponent()).getWeightX(),
							((JoComponent)secondComponent()).getWeightX());
	}

	public float getWeightY() {
        if (firstComponent()==null && secondComponent()==null)
            return 1.0f; //  strange ?
        else if (secondComponent()==null)
            return ((JoComponent)firstComponent()).getWeightY();
        else if (firstComponent()==null)
            return ((JoComponent)secondComponent()).getWeightY();
		else if (getOrientation()==HORIZONTAL_SPLIT)
			return Math.max(((JoComponent)firstComponent()).getWeightY(),
							((JoComponent)secondComponent()).getWeightY());
		else
			return ((JoComponent)firstComponent()).getWeightY() +
				   ((JoComponent)secondComponent()).getWeightY();
	}

	public String getDockingSpots()	{
		if (getOrientation()==HORIZONTAL_SPLIT)
			return DOCK_VERTICAL;
		else
			return DOCK_HORIZONTAL;
	}

	public Point getDockingSpot(char location)
	{
		Point p = new Point(getWidth(), getHeight());
		int d = getDividerLocation() + getDividerSize()/2;

		switch (location) {
		case DOCK_NORTH:	p.x = d; p.y=0; break;
		case DOCK_SOUTH:	p.x = d; break;
		case DOCK_EAST:		p.y = d; break;
		case DOCK_WEST:		p.x = 0; p.y = d; break;
		case DOCK_CENTER:	p.x/=2; p.y/=2; break;
		}

		return p;
	}
}
