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

package de.jose.devtools;

import com.sun.j3d.utils.timer.J3DTimer;
import de.jose.Version;
import de.jose.util.StringUtil;

import java.util.HashMap;

/**
 *
 * @author Peter Schäfer
 */

public class Profiler
{
	private static int gMarkSeq = 1;

	private static HashMap sumMap = new HashMap();
	static {
		Version.hasJava3d(true,false);
	};
	
	private static class Mark implements Cloneable
    {
		String label;
		long nanos;

		long max;
		long sum;

		Mark(String lab) {
			label = lab;
			reset();
		}

		long current()       { return Profiler.currentTimeNanos()-nanos; }

		boolean hold()          {
			long cur = current();
			boolean isMax = (cur>max);
			if (isMax)
				max = cur;
			sum += cur;
			return isMax;
		}

		long reset()         {
			return nanos = Profiler.currentTimeNanos();
		}

	}

	static void print(String label, String comment, long value) {
		System.out.print("[");
		System.out.print(label);
		System.out.print(" ");
		if (comment!=null) {
			System.out.print(comment);
			System.out.print(" ");
		}
        String n = Long.toString(value);
		n = StringUtil.fillLeft(n,9,"0");
		String nanos    = n.substring(n.length()-3);
		String millis   = n.substring(n.length()-6, n.length()-3);
		String secs     = n.substring(0, n.length()-6);

		System.out.print(secs);
		System.out.print(":");
		System.out.print(millis);
		System.out.print(":");
		System.out.print(nanos);

		System.out.println("]");
	}

	public static final long currentTimeNanos()       { return J3DTimer.getValue(); }

	public static Object set(String label)
	{
		Mark mark = (Mark)sumMap.get(label);
		if (mark==null) {
			mark = new Mark(label);
			sumMap.put(label,mark);
		}
		mark.reset();
		return mark;
	}
	public static Object set()                        { return new Mark("profile-"+(gMarkSeq++)); }

	public static long current(Object mark)           { return ((Mark)mark).current(); }
	public static long reset(Object mark)             { return ((Mark)mark).reset(); }

	public static void print(Object mark)             { print(mark,null); }
	public static void printMax(Object mark)          { printMax(mark,null); }

	public static void print(Object obj, String comment)
	{
		Mark mark = (Mark)obj;
		mark.hold();
		print(mark.label,comment, mark.current()); 
	}

	public static void printMax(Object obj, String comment)
	{
		Mark mark = (Mark)obj;
		if (mark.hold())
			print(mark.label,comment, mark.max);
	}
}
