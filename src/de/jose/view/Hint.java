package de.jose.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class Hint extends Timer
{
	int from;
	int to;
	Color color;
	Object implData;

	Hint(int delay, int from, int to, Color color, ActionListener listener)
	{
		super(delay,listener);
		this.from = from;
		this.to = to;
		this.color = color;
	}
}
