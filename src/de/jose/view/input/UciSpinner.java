package de.jose.view.input;

import de.jose.Util;

import javax.swing.*;

/**
 *  a JSpinner input box for use by the UCI dialog
 *
 *  @author Peter Schäfer
 */
public class UciSpinner
        extends JSpinner
        implements ValueHolder
{
	public UciSpinner(int minValue, int maxValue, int defaultValue)
	{
		super(new SpinnerNumberModel(defaultValue, minValue, maxValue, +1));
	}

	public void setValue(Object value)
	{
		super.setValue(Util.toNumber(value));
	}

}
