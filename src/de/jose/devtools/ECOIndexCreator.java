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

import de.jose.chess.MatSignature;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.pgn.ECOClassificator;
import de.jose.pgn.Parser;
import de.jose.util.Properties;
import de.jose.util.file.XBufferedReader;
import de.jose.util.file.XStringBuffer;
import de.jose.util.map.IntHashSet;
import de.jose.util.map.LongIntMap;
import org.apache.tools.ant.DirectoryScanner;

import java.io.*;

/**
 * creates an index of the ECO openining classification
 * to be used by ECOClassificator
 *
 * ther are two files. eco.key contains the positional hash keys and pointers into the second file,
 * eco.properties, which contains the actual and textual data
 *
 * @author Peter Schäfer
 */

public class ECOIndexCreator
{

	class ECOParser extends Parser
	{
		ECOParser(Position pos, int options, boolean strict) {
			super(pos,options,strict);
		}

		protected void callbackLegalMove(Move mv) { /*	no-op	*/ }

		protected void callbackAnnotation(int nag) { /* no-op */ }

		protected void callbackResult(int result) { /* no-op */	}

		protected void callbackComment(char[] line, int start, int len) { /* no-op */ }

        protected void callbackInstruction(char[] line, int start, int len) { /* no-op */ }

		protected void callbackStartVariation() { throw new IllegalStateException(); }

		protected void callbackEndVariation() { throw new IllegalStateException(); 	}

		protected void callbackError(short errorCode, char[] line, int start, int len) {
			throw new IllegalStateException(lineno+":"+start+":"+len+" "+new String(line,0,max));
		}

		public void parse(char[] text, int start, int length) {
			super.parse(text, start, length, null, 0, null, 0, true);
		}
	}

	/**
	 * current eco code
	 */
	private String eco;
	/**
	 * current opening name
	 */
	private String name;
	/**
	 * current position
	 */
	private Position pos;
	/**
	 * text parser
	 */
	private ECOParser parser;
	/**
	 * total number of entries
	 */
	private int count,error_count;
    private int lineno;

    /**
     * translate file names
     */
    private Properties fileMap;

    /**
     * hash map
     */
    private ECOClassificator cls;

	/**
	 * writes eco.preferences text file
	 */
	private PrintWriter textPrnt;

	private XStringBuffer line, moves;
	private XBufferedReader in;
    private IntHashSet newCodes;


	public ECOIndexCreator(ECOClassificator cls) throws IOException
	{
		this.cls = cls;
        this.fileMap = new Properties();
        this.fileMap.load(new FileReader("filemap.properties"));
	}

	public void updateIndex(File sourceFile, File textFile)
		throws IOException
	{
        try {
            line = new XStringBuffer(250);
            moves = new XStringBuffer(520);
            in = new XBufferedReader(new FileReader(sourceFile));

            /**	read the source file; line by line	*/
            eco = null;
            name = null;
            pos = new Position();
            newCodes = new IntHashSet();

            parser = new ECOParser(pos,
                            Position.INCREMENT_HASH |
                            Position.INCREMENT_REVERSED_HASH |
                            Position.INCREMENT_SIGNATURE,
                            false);

            if (textFile!=null)
            {
                textPrnt = new PrintWriter(new FileWriter(textFile));
                textPrnt.print("reversed = ");
                textPrnt.println(fileMap.getProperty("reversed."+textFile.getName(),
                        "(reversed colors)"));
            }

            count = 0;
            lineno = 0;
            for (;;)
            {
                line.setLength(0);
//			in.skipWhiteSpace();
                if (! in.readLine(line)) break;
                lineno++;

//  			parseLineRebel();
//              parseLineScid();
                parseLineArena();
            }

            if (eco!=null)
                newEntry();
        } finally {
            in.close();
            if (textPrnt!=null) textPrnt.close();
        }
	}

/*
	Rebel file syntax
*/
	protected void parseLineRebel()  throws IOException
	{
		if (line.length()==0) return;		// ignore empty lines
		if (line.charAt(0)=='-') return;	//	ignore lines that start with a -

		if (Character.isUpperCase(line.charAt(0))) {
			if (eco!=null)
				newEntry();
			//	new eco code
			eco = line.substring(0,3);
			name = line.substring(4);
		}
		else {
			//	moves
			moves.append(' ');
			moves.append(line);
		}
	}

/*
	scid file syntax
*/
	protected void parseLineScid() throws IOException
	{
		if (line.length()==0) return;		// ignore empty lines
		if (line.charAt(0)=='#') {
            if (textPrnt!=null) textPrnt.println(line);
            return;	//	ignore lines that start with a #
        }

		if (Character.isUpperCase(line.charAt(0))) {
			if (eco!=null)
				newEntry();
			//	new eco code
			eco = line.substring(0,3);
			int i1 = line.indexOf('"');
			int i2 = line.lastIndexOf('"');
			name = line.substring(i1+1,i2);
			moves.append(line.substring(i2+1));
		}
		else {
			//	moves
			moves.append(' ');
			moves.append(line);
		}
	}

    /**
     *      {A00   Creepy Crawly (Basman) Opening }1.a3 e5 2.h3 d5
     * @throws IOException
     */
    protected void parseLineArena() throws IOException
    {
        if (line.length()==0) return;		// ignore empty lines

        if (line.charAt(0)=='*')
        {
            if (textPrnt!=null) {
                textPrnt.print("#");
                textPrnt.println(line.substring(1));
            }
            return;	//	ignore lines that start with a *
        }

        int k1 = line.indexOf('{');
        int k2 = line.lastIndexOf('}');

        if (k1<0 || k2<0) throw  new IOException("braces expected: "+line);

        if (eco!=null) newEntry();

        eco = line.substring(k1+1,k1+4);
        name = line.substring(k1+7,k2);
        moves.append(line.substring(k2+1));
    }

	private void newEntry()
		throws IOException
	{
		parser.parse(moves.getValue(),0,moves.length());
        /** lookup */
        int code = cls.lookup(pos.getHashKey());
        if (code!=ECOClassificator.NOT_FOUND)
        {
            //  same position already classified (within same input file ?)
            if (eco.equals(cls.getEcoCode(code,3)) && !newCodes.contains(code))
            {
                //  good (existing key)
                eco = cls.getEcoCode(code,6);
                printLine(eco,name);
            }
            else
            {
                //  bad (overrides with different eco, or within same file)
/*
                int newCode = cls.add(eco, pos.getHashKey(), pos.getMatSig());
                eco = cls.getEcoCode(newCode,6);
*/
                System.out.println("  Warning: "+eco+" "+name+" overrides "+cls.getEcoCode(code,6));
                error_count++;
            }
        }
        else {
            //  add new classification key
/*
            code = cls.add(eco, pos.getHashKey(), pos.getMatSig());
            eco = cls.getEcoCode(code,6);
*/
            System.out.println("  Warning: "+eco+" "+name+" missing");
            error_count++;
        }

        newCodes.add(code);

		/** check terminal positions    */
//		terminal.mergeWith(pos.getMatSig());

		moves.setLength(0);
		eco = name = null;
	}


    private void printLine(String eco, String name)
    {
        //  write language file
        if (textPrnt!=null) {
            textPrnt.print(eco);
            textPrnt.print("=");
            textPrnt.println(name);
        }
        count++;
	}


	public static void main(String[] args)
		throws IOException
	{
		if (args.length < 2) {
			System.err.println("<key file> <source files> ");
			return;
		}

		File keyFile 		= new File(args[0]);

        ECOClassificator cls = new ECOClassificator(true);
        if (keyFile.exists()) cls.open(keyFile);

        ECOIndexCreator creator = new ECOIndexCreator(cls);

        DirectoryScanner dscan = new DirectoryScanner();
        String[] includes = new String[args.length-1];
        System.arraycopy(args,1, includes,0, includes.length);
        dscan.setIncludes(includes);
        dscan.setBasedir(new File("."));
        dscan.setCaseSensitive(false);
        dscan.scan();

        File outputDir = new File("properties");
        outputDir.mkdirs();

        String[] files = dscan.getIncludedFiles();
        for (int i=0; i < files.length; i++)
        {
            File sourceFile = new File(files[i]);
            String targetName = creator.fileMap.getProperty(sourceFile.getName(), sourceFile.getName());
            File textFile = new File(outputDir, targetName);

            System.out.println("["+sourceFile.getName()+" -> "+textFile.getName());
            creator.updateIndex(sourceFile,textFile);
        }

        System.out.println(creator.count+" lines.");
        System.out.println(creator.error_count+" warnings.");
/*
        if (keyFile.exists())
            keyFile.renameTo(new File(keyFile.getParentFile(), keyFile.getName()+".bak"));
*/

        if (cls.isDirtyModified())
            System.out.println("Codes have been modified; rebuild languages files.");
        else if (cls.isDirtyAdd())
            System.out.println("New Codes have been added. Please update other language files.");

/*
        cls.write(keyFile);
*/
	}
}
