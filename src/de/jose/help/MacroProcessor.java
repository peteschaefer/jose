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

package de.jose.help;

import de.jose.util.file.FileUtil;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public class MacroProcessor
{
	class MacroDefinition {
		String name;			//	macro name
		Method expandMethod = null;	//	expansion method
		String expansion = "";	//	expanded value
		String[] args = null;	//	argument names

		public String getKey() {
			if (args==null || args.length==0)
				return name;
			else
				return name+"("+args.length+")";
		}

		public int closingIdx(String line) {
			String closeKey = "<!--/@"+name+"-->";
			int i = line.indexOf(closeKey);
			if (i>=0)
				return i+closeKey.length();
			else
				return -1;
		}
	}

	/**	map of macro definitions	*/
	protected HashMap defines;
	/**	root directory	*/
	protected File rootDir;
	protected File currentFile;

	public MacroProcessor()
	{
		defines = new HashMap();
	}

	public void defineMacro(String name, String expansion, String[] argNames)
	{
		MacroDefinition def = new MacroDefinition();
		def.name = name;
		def.expansion = expansion;
		def.args = argNames;
		defineMacro(def);
	}

	public void defineMacro(String name, String methodName, int argc)
	{
		Class[] paramTypes;
		if (argc<=0)
			paramTypes = null;
		else {
			paramTypes = new Class[argc];
			Arrays.fill(paramTypes,java.lang.String.class);
		}

		try {
			Method method = getClass().getMethod(methodName, paramTypes);
			method.setAccessible(true);

			MacroDefinition def = new MacroDefinition();
			def.name = name;
			def.expandMethod = method;
			if (argc<=0)
				def.args = null;
			else {
				def.args = new String[argc];
				for (int i=0; i<argc; i++)
					def.args[i] = "arg"+i;
			}

			defines.put(def.getKey(),def);

		} catch (NoSuchMethodException e) {
			throw new RuntimeException("method "+methodName+" not found");
		} catch (SecurityException e) {
			throw new RuntimeException("method "+methodName+" not accessible");
		}
	}

	public void defineMacro(MacroDefinition def)
	{
		defines.put(def.getKey(),def);
	}

	public void setRootDirectory(File dir) {
		rootDir = dir;
	}

	public void setRootDirectory(String dir) {
		rootDir = new File(dir);
	}

	public void readDefinition(String file)
		throws IOException
	{
		readDefinition(new FileReader(file));
	}

	public void readDefinition(File file)
		throws IOException
	{
		readDefinition(new FileReader(file));
	}

	public void readDefinition(Reader rin)
		throws IOException
	{
		BufferedReader in = new BufferedReader(rin);
		MacroDefinition def = null;

		for (;;) {
			String line = in.readLine();
			if (line==null) break;
			if (line.startsWith("<!--@")) {
				//	starts a macro definition
				def = new MacroDefinition();
				parseLongMacroDef(line, def);
			}
			else if (def!=null && def.closingIdx(line)>=0) {
				defineMacro(def);
				def = null;
			}
			else if (def!=null) {
				if (def.expansion.length() > 0)
					def.expansion += "\n";
				def.expansion += line;
			}
		}
		in.close();
	}

	public void process(File in)
		throws IOException
	{
		File temp = File.createTempFile("temp",".mac",in.getParentFile());
		process(in,temp);

		in.delete();
		temp.renameTo(in);
	}

	public void process(File in, File out)
		throws IOException
	{
		currentFile=in;
		process(new FileReader(in), new FileWriter(out));
		currentFile=null;
	}

	public void process(Reader rin, Writer out)
		throws IOException
	{
		BufferedReader in = new BufferedReader(rin);
		MacroDefinition call = null;
		MacroDefinition def = null;
		String line = null;

		for (;;) {
			if (line==null) line = in.readLine();
			if (line==null) break;

			if (call!=null) {
				int cidx = call.closingIdx(line);
				if (cidx >= 0) {
					//	close call
					out.write(expand(def,call.args,true));
					call = def = null;
					line = line.substring(cidx);
				}
				else
					line = null;		/* ignore line inside calls	*/
				continue;
			}

			int didx = line.indexOf("<!--@");
			if (didx >= 0)
			{
				//	starts a new macro call
				out.write(expand(line.substring(0,didx),true));
				line = line.substring(didx);

				call = new MacroDefinition();
				parseLongMacroDef(line,call);
				def = (MacroDefinition)defines.get(call.getKey());
				if (def==null) {
					System.err.println("definition for "+call.getKey()+" not found");
					call = null;
					out.write(line.charAt(0));
					line = line.substring(1);
				}
				continue;
			}
			//	else: not inside call and line contains no opening call
			out.write(expand(line,true));
			out.write("\n");
			line=null;
		}

		in.close();
		out.close();
	}

	protected String expand(String line, boolean persistent)
	{
		return expand(line,null,null,persistent);
	}

	protected String expand(String line, String[] argNames, String[] argValues, boolean persistent)
	{
		StringBuffer buf = new StringBuffer(line);
		boolean modified;
		do {
			modified = false;
			if (argNames!=null && expandArgs(buf,argNames,argValues))
				modified = true;
			if (expandMacros(buf,persistent))
				modified = true;
			persistent = false;	//	persistent only on very top level
		} while (modified);
		return buf.toString();
	}

	protected boolean expandArgs(StringBuffer buf, String[] argNames, String[] argValues)
	{
		boolean modified = false;
		for (int i=0; i<argNames.length; i++) {
			String key = "@"+argNames[i]+"@";
			String value;
			if (i>=argValues.length || argValues[i]==null)
				value = "";
			else
				value = argValues[i];

			for (;;) {
				int idx = buf.indexOf(key);
				if (idx<0) break;
				buf.replace(idx,idx+key.length(), value);
				modified = true;
			}
		}
		return modified;
	}

	protected boolean expandMacros(StringBuffer buf, boolean persistent)
	{
		int i = 0, j;
		boolean modified = false;
		for (;;) {
			i = buf.indexOf("@@",i);
			if (i < 0) break;

			j = buf.indexOf("@",i+2);
			if (j < 0) break;

			if (expandMacro(buf, i,j+1,persistent))
				modified = true;
			else
				i=j+1;
		}
		return modified;
	}

	protected boolean expandMacro(StringBuffer buf, int start, int end, boolean persistent)
	{
		MacroDefinition call = new MacroDefinition();
		parseMacroDef(buf.substring(start+2,end-1), call);
		MacroDefinition def = (MacroDefinition)defines.get(call.getKey());
		if (def==null) {
			System.err.println("definition for "+call.getKey()+" not found");
			return false;
		}
		String expansion = expand(def,call.args,persistent);
		buf.replace(start,end,expansion);
		return true;
	}

	protected String expand(MacroDefinition def, String[] argValues, boolean persistent)
	{
		StringBuffer buf = new StringBuffer();
		if (persistent) {
			buf.append("<!--@");
			buf.append(def.name);
			if (argValues!=null)
				for (int argc=0; argc<argValues.length; argc++) {
					buf.append(":");
					buf.append(argValues[argc]);
				}
			buf.append("-->");
			//  &nbsp; is needed to trick out the Java HTML layout engine
			//  otherwise it would eat up all whitespace
		}

		if (def.expandMethod!=null)
			try {
				Object value = def.expandMethod.invoke(this,argValues);
				if (value!=null)
					buf.append(value.toString());
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("error during method call ");
			}
		else
			buf.append(expand(def.expansion,def.args,argValues,false));

		if (persistent) {
			buf.append("<!--/@");
			//  &nbsp; is needed to trick out the Java HTML layout engine
			//  otherwise it would eat up all whitespace
			buf.append(def.name);
			buf.append("-->");
		}
		return buf.toString();
	}

	protected void parseLongMacroDef(String line, MacroDefinition def)
	{
		int start = line.indexOf("<!--@");
		int end = line.indexOf("-->");

		parseMacroDef(line.substring(start+5,end), def);
	}

	protected void parseMacroDef(String line, MacroDefinition def)
	{
		List tokens = tokenize(line,":");
		if (tokens.size() < 1)
			throw new RuntimeException("macro name expected");
		def.name = (String)tokens.get(0);
		if (tokens.size() == 1)
			def.args = null;
		else {
			tokens.remove(0);
			def.args = new String[tokens.size()];
			tokens.toArray(def.args);
		}
	}

	protected static List tokenize(String text, String delim)
	{
		Vector collect = new Vector();
		StringTokenizer tokenizer = new StringTokenizer(text,delim);
		while (tokenizer.hasMoreTokens())
			collect.add(tokenizer.nextToken());
		return collect;
	}

	public String expandPath(String path)
	{
		File target = new File(rootDir,path);
		return FileUtil.getRelativePath(currentFile.getParentFile(),target,"/");
	}


	public static void main(String[] args)
		throws IOException
	{
		MacroProcessor mac = new MacroProcessor();
		File dir = new File("D:/jose/work/doc/man");
		mac.setRootDirectory(dir);
		mac.defineMacro("path", "expandPath", 1);
		mac.readDefinition(new File(dir,"macros.html"));
		mac.process(new File(dir,"01-menu/01-file.html"));
	}
}
