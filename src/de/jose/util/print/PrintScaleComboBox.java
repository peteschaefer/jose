package de.jose.util.print;

import de.jose.view.input.ValueHolder;
import de.jose.util.StringUtil;
import de.jose.Util;
import de.jose.Language;

import javax.swing.*;
import javax.swing.plaf.metal.MetalComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.text.DecimalFormat;
import java.awt.*;

/**
 *
 */
public class PrintScaleComboBox
        extends JComboBox
        implements ValueHolder
{
	protected static final Object[] DEFAULT_VALUES =
	{
		new Item(0.2), new Item(0.33), new Item(0.5), new Item(0.71), new Item(1.0),
		new Item(1.5), new Item(2.0), new Item(3.0), new Item(5.0),
		new Item(PrintPreview.FIT_PAGE),
		new Item(PrintPreview.FIT_PAGE_WIDTH),
		new Item(PrintPreview.FIT_TEXT_WIDTH),
	};

	protected static DecimalFormat PERCENT_FORMAT = new DecimalFormat("###0.#%");

	static class Item extends Number
	{
		double value;

		Item(double value) { this.value = value; }

		public String toString() {
			if (value > 0.0)
				return PERCENT_FORMAT.format(value);
			else if (value==PrintPreview.FIT_PAGE_WIDTH)
				return Language.get("print.preview.fit.width");
			else if (value==PrintPreview.FIT_TEXT_WIDTH)
				return Language.get("print.preview.fit.textwidth");
			else /*if (value==PrintView.FIT_PAGE)*/
				return Language.get("print.preview.fit.page");
		}

		public double doubleValue()                 { return value; }
		public float floatValue()                   { return (float)value; }
		public int intValue()                       { return (int)value; }
		public long longValue()                     { return (long)value; }

		public boolean equals(Object obj)
		{
			double thatValue = toItem(obj).value;
			return this.value == thatValue;
/*
			if (this.value <= 0.0)
				return thatValue <= 0.0;
*/
		}
	}

	class RightAlignedComboBoxEditor
		extends MetalComboBoxEditor
	{
		RightAlignedComboBoxEditor() {
			super();
			editorBorderInsets.right = 4;
			editor.setHorizontalAlignment(JTextField.RIGHT);
		}

		public void setItem(Object anObject)
		{
			Item item = toItem(anObject);
			if (item.value <= 0.0) {
				double effectiveScale =  view.getEffectiveScale(item.value);
				item = new Item(effectiveScale);
			}
			super.setItem(item);
		}
	}

	protected PrintPreview view;

	public PrintScaleComboBox(PrintPreview view)
	{
		super(DEFAULT_VALUES);
		setEditable(true);
//		setMaximumSize(new Dimension(160,24));
		ListCellRenderer renderer = getRenderer();
		if (renderer instanceof JLabel)
			((JLabel)renderer).setHorizontalAlignment(JLabel.RIGHT);
		setEditor(new RightAlignedComboBoxEditor());
		this.view = view;
	}

	public void setSelectedItem(Object anObject)
	{
		Item item = toItem(anObject);
		super.setSelectedItem(item);
		getEditor().setItem(item);
	}

	public double getScale()                        { return toItem(getSelectedItem()).value; }
	public void setScale(double scale)              { setSelectedItem(new Item(scale)); }

	public Object getValue()                        { return new Double(getScale()); }
	public void setValue(Object value)              { setSelectedItem(value); }

	private static Item toItem(Object obj)
	{
		if (obj==null)
			return null;
		if (obj instanceof Item)
			return (Item)obj;
		if (obj instanceof Number)
			 return new Item(((Number)obj).doubleValue());
		//  else:
		double value;
		String str = obj.toString();
		//  first, try % pattern
		try {
			value = PERCENT_FORMAT.parse(str).doubleValue();
		} catch (Throwable ex) {
			try {
				//  then try number
				value = Util.todouble(str)/100.0;
			} catch (Throwable ex2) {
				value = -1.0;
			}
		}
		return new Item(value);
	}
}
