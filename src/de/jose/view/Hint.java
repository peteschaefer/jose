package de.jose.view;

import de.jose.chess.Constants;
import de.jose.chess.EngUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class Hint extends Timer
{
	int from;
	int to;
	Color color;
	String label=null;
	Object implData;

	Hint(int delay, int from, int to, Color color, ActionListener listener)
	{
		super(delay,listener);
		this.from = from;
		this.to = to;
		this.color = color;
	}

	public boolean isKnightMove()
	{
		int fd = EngUtil.fileDiff(to,from);
		int rd = EngUtil.rowDiff(to,from);
		if (fd==0 || rd==0 || Math.abs(fd)==Math.abs(rd))
			return false;
		else
			return true;
	}

	public int intermediateSquare()
	{
		//	for knight moves, we paint a kinked arrow
		int fd = EngUtil.fileDiff(to,from);
		int rd = EngUtil.rowDiff(to,from);
		if (fd==0 || rd==0 || Math.abs(fd)==Math.abs(rd))
			return 0;	//	orthogonal, or diagonal move
		//	else: a knight move!
		//	either fd==+/-2, or rd==+/-2
		//	either fd==+/-1 or rd == +/-1
		return EngUtil.squareAdd(from, fd/2, rd/2);
	}
}
