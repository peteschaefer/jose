package de.jose.view.input;

import de.jose.Util;

import javax.swing.*;
import java.awt.*;

/**
 *  a JSpinner input box for use by the UCI dialog
 *
 *  @author Peter Sch�fer
 */
public class UciSpinner
        extends JSpinner
        implements ValueHolder
{
	public UciSpinner(int minValue, int maxValue, int defaultValue)
	{
		super(new SpinnerNumberModel(defaultValue, minValue, maxValue, +1));
		setMinimumSize(new Dimension(80,20));
		setPreferredSize(new Dimension(80,20));
		//	keep width of UCI spinners consistent (not so easy with GridBayLayout !!)
	}

	public void setValue(Object value)
	{
		super.setValue(Util.toNumber(value));
	}

}
