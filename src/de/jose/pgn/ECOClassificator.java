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

package de.jose.pgn;

import de.jose.Language;
import de.jose.Util;
import de.jose.chess.HashKey;
import de.jose.chess.MatSignature;
import de.jose.chess.Position;
import de.jose.chess.Move;
import de.jose.util.map.IntIntMap;
import de.jose.util.map.LongIntMap;
import de.jose.util.map.IntHashMap;

import java.io.*;
import java.util.Iterator;

/**
 * classifies according to the ECO system
 *
 * hash keys are stored as long (8 bytes)
 * eco codes are stored as int (4 bytes)
 *
 * for example
 *    A12.3
 * becomes
 *    0x 65 41 42 03
 * i.e. its a kind of little endian string
 *
 * reversed color positions are indicated by an addiational
 *    0x 00 00 00 80
 *
 * to obtain the 3-letter eco code, use
 *     code >> 8;
 *
 * @author Peter Schäfer
 */


public class ECOClassificator
{
	/** magic number for eco.key file   */
	public static final int FILE_MAGIC = 0xfafafa01;

	/** automatically use fallback language (english)   */
	protected static boolean USE_FALLBACK_LANGUAGE  = true;

    /** indicates when a looked up position is not found    */
    public static final int NOT_FOUND = LongIntMap.NOT_FOUND;

	/**	the key map
	 * 	HashMap<HashKey,String>
	 * 	*/
	protected LongIntMap keys;
	/** terminal matsig
	 */
    protected MatSignature terminal;

	/**	translated names	*/
	protected Language language;

    /** for edit mode: current key counter  */
    protected IntIntMap counter;
    protected IntHashMap firstEntry;
    protected boolean dirtyAdd, dirtyModified;

    public ECOClassificator(boolean forUpdate)
    {
        keys = new LongIntMap();
        terminal = new MatSignature();
        terminal.setInitial();

        if (forUpdate) {
            counter = new IntIntMap();
            firstEntry = new IntHashMap();
        }
        else {
            counter = null;
            firstEntry = null;
        }
    }

    public boolean isDirtyAdd()         { return dirtyAdd; }
    public boolean isDirtyModified()    { return dirtyModified; }


	public void open(File keyFile)
		throws IOException
	{
		ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(keyFile));
            /** read magic number   */
            int magic = in.readInt();
            if (magic != FILE_MAGIC)
                throw new IOException("bad magic number");

            /** read hash keys  */
            while (in.available() > 0) {
                long hash = in.readLong();
                if (hash==0L) break;

                int code = in.readInt();
                keys.put(hash,code);

                if (counter!=null) {
                    //  update counters
                    int ckey = code & 0xFFFFFF00;
                    int ccount = code & 0x0000007F;
                    int cvalue = counter.get(ckey);
                    if (cvalue==LongIntMap.NOT_FOUND)
                        counter.put(ckey,ccount);
                    else
                        counter.put(ckey,Math.max(cvalue,ccount));
                    if (ccount==0)
                        firstEntry.put(ckey,new Long(hash));
                }
            }
            /** read terminal matsig  */
            long wsig = in.readLong();
            long bsig = in.readLong();
            terminal = new MatSignature(wsig,bsig);

        } finally {
            in.close();
        }
    }

    public void write(File keyFile) throws IOException
    {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(keyFile));

            /** magic number    */
            out.writeInt(FILE_MAGIC);

            /** hash keys   */
            Iterator i = keys.entrySet().iterator();
            while (i.hasNext())
            {
                LongIntMap.Entry ety = (LongIntMap.Entry)i.next();

                long hash = ety.getLongKey();
                out.writeLong(hash);

                int code = ety.getIntValue();
                out.writeInt(code);
            }
            /** 0L indicates end of hash keys   */
            out.writeLong(0L);

            /** terminal matsig */
            out.writeLong(terminal.wsig);
            out.writeLong(terminal.bsig);

        } finally {
            out.close();
        }
    }


	public ECOClassificator(ECOClassificator that)
	{
        if (that.counter!=null)
        {
            //  writable copy - not necesary
            throw new UnsupportedOperationException();
        }
        else
        {
            //  read-only copy
            this.keys = that.keys;   //  keys can be shared - not a problem at all
            this.language = that.language;
            this.terminal = that.terminal.cloneSig();
            this.counter = null;
        }
	}

	public void setLanguage(File directory, String lang)
		throws IOException
	{
		if (language!=null && language.langCode.equals(lang)) return;   //  no change

		if (Language.exists(directory,Language.ECO_PROP_FILE,lang))
			language = new Language(directory,Language.ECO_PROP_FILE,lang,USE_FALLBACK_LANGUAGE);    //  with or w/out fallback ? // false);
		else if (language==null)
			language = new Language(directory,Language.ECO_PROP_FILE,null,false);
	}

	public String getEcoCode(int code, int maxLen)
	{
		StringBuffer buf = new StringBuffer();
        buf.append((char)((code>>24) & 0x000000FF));
        buf.append((char)((code>>16) & 0x000000FF));
        buf.append((char)((code>>8) & 0x000000FF));

        if (maxLen > 3) {
            int offset = code & 0x0000007f;
            if (offset > 0) {
                buf.append(".");
                buf.append(Integer.toString(offset));
            }
        }
        if (maxLen > 6 && Util.anyOf(0x00000080,code))
            buf.append("x");    //  indicates reversed colors

        if (buf.length() > maxLen) buf.setLength(maxLen);
        return buf.toString();
	}

	public String getOpeningName(int code)
	{
        String pkey = getEcoCode(code,6);

        String text = language.get1(pkey,pkey);
        if (Util.anyOf(0x00000080,code))
            return text+" "+language.get1("reversed","?");
        else
            return text;
	}


	public int lookup(HashKey key)
	{
        if (key==null) return NOT_FOUND;

        int code = keys.get(key.value());

        if (code==NOT_FOUND) return NOT_FOUND;

        if (key.isReversed()) code |= 0x00000080;
		return code;
	}

	public int lookup(HashKey key, HashKey reversedKey)
	{
		int result = lookup(key);
		if (result==NOT_FOUND)
			result = lookup(reversedKey);
		return result;
	}

	public int lookup(Position pos)
	{
		return lookup(pos.getHashKey(), pos.getReversedHashKey());
	}

	public int lookup(Position pos, Move move)
	{
		boolean islegal = false;
		int wasOptions = pos.getOptions();
		try {
			pos.setOption(Position.INCREMENT_HASH,true);
			pos.setOption(Position.INCREMENT_REVERSED_HASH,true);

			islegal = pos.tryMove(move);
			if (islegal) return lookup(pos);
		} finally {
			if (islegal) pos.undoMove();
			pos.setOptions(wasOptions);
		}
		//  otherwise
		return NOT_FOUND;
	}

    public int add(String eco, HashKey key, MatSignature sig)
    {
        long hash = key.value();
        int code = newCode(eco,hash);
        keys.put(hash,code);
        terminal.mergeWith(sig);
        return code;
    }

    public int newCode(String eco, long hash)
    {
        int code = toInt(eco,3);
        int count = counter.get(code);
        if (count==LongIntMap.NOT_FOUND) {
            count = 0;      //  first entry, no offset
            firstEntry.put(code,new Long(hash));
        }
        else if (count==0)
        {
            //  upgrade Axx to Axx.1
            Long hash1 = (Long)firstEntry.get(code);
            firstEntry.remove(code);

            keys.put(hash1.longValue(), code | (1<<24));
            dirtyModified = true;
            count = 2;
        }
        else {
            dirtyAdd = true;
            count++;        //  increment
        }

        counter.put(code,count);
        code |= count << 24;
        return code;
    }

    private static int toInt(String eco, int maxLen)
    {
        int code =  (((int)eco.charAt(0))<<24) |
                    (((int)eco.charAt(1))<<16) |
                    (((int)eco.charAt(3))<<8);

        if (eco.length() > 4 && maxLen > 4)
        {
            int offset = Integer.parseInt(eco.substring(4));
            code |= (offset & 0x0000007f);
        }
        if (eco.endsWith("x") && maxLen > 6)
            code |= 0x00000080;

        return code;
    }

	public boolean isReachable(MatSignature sig)
	{
		return terminal.isReachableFrom(sig);
		/** is terminal signature still reachable ? */
	}
}
