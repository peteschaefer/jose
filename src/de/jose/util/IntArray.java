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

package de.jose.util;

import java.util.Arrays;
import java.util.StringTokenizer;

/**	a Dynamic Array of int's
 * <p>
 * modelled after java.util.ArrayList but stores int's instead of Objects.
 * Note that this class does not implement the java.util.List interface.
 * 
  */
public final class IntArray 
	implements Cloneable
{
	//-------------------------------------------------------------------------

	/**	the actual array	 */
	protected int[] fArray;
	/**	current fill size	 */
	private int fSize;
	/**	allocation block size	 */
	private static final int kBlockSize = 64;
	
	/**	Constructs an empty array. 
	 */
	public IntArray() {
		this(0);
	}
	
   /**	Constructs an array containing the elements of the specified array. 
    * 
    * @param array the initial contents of the array
    */ 
	public IntArray(int[] array) {
		this(array.length);
		addAll(array);
	}
	
	public IntArray(String[] array) {
		this(array.length);
		addAll(array);
	}
	
	
	/**	Constructs an empty array with the specified initial capacity. 
	 * 
	 * @param initialCapacity the initial capacity of the array
	 */
	public IntArray(int initialCapacity) {
		fArray = new int[initialCapacity];
		fSize = 0;
	}

	public IntArray(String s, String delimiter) 
	{
		this();
		StringTokenizer tok = new StringTokenizer(s,delimiter,false);
		while (tok.hasMoreTokens())
			add(Integer.parseInt(tok.nextToken().trim()));
	}
	
	/**	Inserts the specified element at the specified position in this array. 
	 * 
	 * @param index the index in the array. If index is larger than the current size,
	 *	the array grows automatically.
	 * @param element the element to be inserted
	 * @exception IndexOutOfBoundsException if index < 0
	 */
	public void add(int index, int element) {
		if (index<0)
			throw new IndexOutOfBoundsException();
		
		if (index>=fSize) {
			ensureCapacity(index+1);
			fSize = index+1;
		}
		else {
			ensureCapacity(fSize+1);
			System.arraycopy(fArray,index, fArray,index+1, fSize-index);
			fSize++;
		}
		fArray[index] = element;
	}
	
	/**	Appends the specified element to the end of this array. 
	 * 
	 * @param element the element to be added
	 */
	public void add(int element) {
		add(fSize,element);
	}
	
	public void add(String element) {
		add(Integer.parseInt(element.trim()));
	}
	
	/**	Appends all of the elements in the specified array to the end of this array. 
	 * 
	 * @param elems the elements to be added
	 */
	public void addAll(int[] elems) {
		if (elems!=null)
			addAll(fSize, elems, 0, elems.length);
	}
	
	public void addAll(String[] elems) {
		if (elems!=null)
			addAll(fSize, elems, 0, elems.length);
	}
	
	/**	Inserts a subset of the elements in the specified array into this array, 
	 * starting at the specified position. 
	 * 
	 * @param index the index in this array where the new elements will be inserted
	 * @param elems the new elements
	 * @param start the start index in elements
	 * @param length the number of elements to be added
	 * 
	 * @exception IndexOutOfBoundsException if index < 0, or start+length > elements.length
	 */
	public void addAll(int index, int[] elems, int start, int length) {
		if (elems==null)
			return;
		
		if (index<0)
			throw new IndexOutOfBoundsException();
		
		if (index>=fSize) {
			ensureCapacity(index+length);
			fSize = index+length;
		}
		else {
			ensureCapacity(fSize + length);
			System.arraycopy(fArray,index, fArray,index+length, fSize-index);
			fSize += length;
		}
		System.arraycopy(elems,0, fArray,start,length);
	}
	
	public void addAll(int index, String[] elems, int start, int length) {
		if (elems==null)
			return;
		
		if (index<0)
			throw new IndexOutOfBoundsException();
		
		if (index>=fSize) {
			ensureCapacity(index+length);
			fSize = index+length;
		}
		else {
			ensureCapacity(fSize + length);
			System.arraycopy(fArray,index, fArray,index+length, fSize-index);
			fSize += length;
		}
		arraycopy(elems,0, fArray,start,length);
	}
	
	/**	Appends all of the elements in the specified array to the end of this array. 
	 * 
	 * @param elems the elements to be added
	 */
	public void addAll(IntArray elems) {
		if (elems!=null)
			addAll(fSize, elems.fArray, 0, elems.size());
	}
	
	/**	Removes all of the elements from this array. 
	 * The capacity is not affected.
	 */
	public void clear() {
		fSize = 0;
	}
	
	/**	Returns true if this array contains the specified element.
	 * 
	 * @param element the element to search for
	 */
	public boolean contains(int element) {
		return indexOf(element) >= 0;
	}
	
	/**	Returns a copy of this IntArray instance. 
	 */
	public Object clone() {
		IntArray clone = new IntArray(fArray.length);
		clone.addAll(0, fArray,0,fSize);
		return clone;
	}
	
	/**	Increases the capacity of this IntArray instance, if necessary, 
	 * to ensure that it can hold at least the number of elements specified by the 
	 * minimum capacity argument. 
	 * 
	 * @param minCapacity the minimum capacity. If this value is smaller than the current
	 *	array size, it will be ignored.
	 */
	public void ensureCapacity(int minCapacity) {
		if (minCapacity <= fArray.length)
			return;
		int newBlocks = (minCapacity+kBlockSize-1) / kBlockSize;
		allocate(newBlocks*kBlockSize);
	}
	
	/**	Returns the element at the specified position in this array (startin at 0).
	 * 
	 * @param index the index in the array
	 * @exception IndexOutOfBoundsException if index < 0 or index >= size()
	 */
	public int get(int index) 
	{
		if (index < 0 || index >= fSize)
			throw new IndexOutOfBoundsException("index: " + index + " - size: " + fSize);
		return fArray[index];
	}
	
	/**	Searches for the first occurence of the given argument. 
	 * 
	 * @param elem the element to look for
	 */
	public int indexOf(int elem) {
		for (int i=0; i<fSize; i++)
			if (fArray[i]==elem)
				return i;
		return -1;
	}
	
	/**	 Tests if this array has no elements.
	 */
	public boolean isEmpty() {
		return fSize == 0;
	}

	/** Returns the index of the last occurrence of the specified object in this list. 
	 * 
	 * @param elem the element to look for
	 */
	public int lastIndexOf(int elem) {
		for (int i=fSize-1; i>=0; i--)
			if (fArray[i]==elem)
				return i;
		return -1;
	}
	
	/** Removes the element at the specified position in this list. 
	 * 
	 * @param index the position in the list
	 */
	public int remove(int index) {
		int result = get(index);
		removeRange(index,index+1);
		return result;
	}
	
	/** Removes from this List all of the elements whose index is between fromIndex, 
	 * inclusive and toIndex, exclusive. 
	 * 
	 * @param fromIndex the starting index (inclusive)
	 * @param toIndex the end index (exclusive)
	 * @exception IndexOutOfBoundsException if fromIndex < 0, or toIndex > size(),
	 *	or fromIndex > toIndex
	 */
	public void removeRange(int fromIndex, int toIndex) {
		if (fromIndex < 0 || toIndex > fSize || fromIndex > toIndex)
			throw new IndexOutOfBoundsException();
		
		int remSize = toIndex-fromIndex;
		System.arraycopy(fArray,toIndex, fArray,fromIndex, fSize-toIndex);
		fSize -= remSize;
	}
	
	/**	Replaces the element at the specified position in this list with the 
	 * specified element. 
	 * 
	 * @param index the position in the array
	 * @param element the new element
	 * @exception IndexOutOfBoundsException if index < 0
	 */
	public int set(int index, int element) {
		if (index < 0)
			throw new IndexOutOfBoundsException();
		if (index >= fSize) {
			ensureCapacity(index+1);
			fSize = index+1;
		}
		return fArray[index] = element;
	}
	
	/**	Returns the number of elements in this list. 
	 */
	public int size() {
		return fSize;
	}
	
	public void setSize(int sz)
	{
		if (sz > fArray.length)
			allocate(sz);
		if (sz > fSize)
			Arrays.fill(fArray,fSize,sz-fSize,0);
		fSize = sz;
	}
	
	/**	Returns an int[] array containing all of the elements in this list in the
	 * correct order.
	 */
	public int[] toArray() {
		int[] copy = new int[fSize];
		System.arraycopy(fArray,0, copy,0,fSize);
		return copy;
	}

	/**	@return the underlying int array
	 */
	public int[] getArray() {
		return fArray;
	}

	/**	@return an iterator (no check for concurrent modification
	 */
	public Iterator iterator() {
		return new Iterator();
	}

    /**	@return an iterator (no check for concurrent modification
     */
    public ObjIterator objIterator() {
        return new ObjIterator();
    }

	/**	Returns a String representation of this array. Elements are separated by commas
	 * and enclosed in parenthesis. An empty array returns "()".
	 * <p>
	 * Example: "(1,2,3,4)"
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("(");
		buf.append(arrayToString(fArray,0,fSize,","));
		buf.append(")");
		return buf.toString();
	}

	/**	Returns a String representation of this array. Elements are separated by commas
	 * and enclosed in parenthesis. An empty array returns "null".
	 * <p>
	 * Example: "(1,2,3,4)"
	 */
	public static String toString(IntArray array) {
		if (array==null || array.isEmpty())
			return null;
		else
			return array.toString();
	}

	public static String toString(int[] array, int from, int to, String separator)
	{
		StringBuffer buf = new StringBuffer();
		appendString(buf,array,from,to,separator);
		return buf.toString();
	}

	public static StringBuffer appendString(StringBuffer buf, int[] array, int from, int to, String separator)
	{
		if (from < to)
			buf.append(array[from++]);
		while (from < to) {
			buf.append(separator);
			buf.append(array[from++]);
		}
		return buf;
	}

	/**	Trims the capacity of this IntArray instance to be the array's current size.
	 */
	public void trimToSize() {
		allocate(fSize);
	}
	
	/**	Sorts the array in ascending order.
	 */
	public void sort() {
		java.util.Arrays.sort(fArray,0,fSize);
	}
	
	/** sets the capacity of the array
	 */
	protected void allocate(int newCapacity) {
		if (newCapacity == fArray.length) return;

		int[] copy = new int[newCapacity];
		System.arraycopy(fArray,0, copy,0, fSize);
		fArray = copy;
	}
	
	/**	static string utilities	 */
	
	public static final int[] stringToArray(String s, String delimiter) {
		return new IntArray(s,delimiter).toArray();		
	}
	
	public static final int[] stringToArray(String s) {
		return stringToArray(s," ,;()");
	}
	
	public static final String arrayToString(int[] a, String delimiter) {
		return arrayToString(a,0,a.length, delimiter);
	}
	
	public static final String arrayToString(int[] a) {
		return arrayToString(a,0,a.length, ",");
	}
	
	public static final String arrayToString(int[] a, int from, int to, String delimiter) {
		StringBuffer buf = new StringBuffer();
		if (from<to)
			buf.append(Integer.toString(a[from]));
		for (from++; from<to; from++) {
			buf.append(delimiter);
			buf.append(Integer.toString(a[from]));
		}
		return buf.toString();
	}
	
	/**
	 * Copies an array.
	 */
	public static final void arraycopy(String[] from, int from_idx, int[] to, int to_idx, int length) 
	{
		while (length-- > 0)		
			to[to_idx++] = Integer.parseInt(from[from_idx++].trim());
	}
	
	/**	some stack functions:	*/
	public void push(int elem)		{ add(elem); }
	public int peek()				{ return (fSize==0) ? 0:fArray[fSize-1]; }
	public int pop()				{ return (fSize==0) ? 0:fArray[--fSize]; }
	
	/**
	 * 
	 */
	public class Iterator
	{
		private int fCurrent;
		
		Iterator()					{ fCurrent = 0;	}
		public final boolean hasNext()	{ return fCurrent < size(); }
		public final int next()			{ return get(fCurrent++); }
		public final void remove()		{ removeRange(fCurrent,fCurrent+1); }
	}

    /**
     *
     */
    public class ObjIterator implements java.util.Iterator
    {
        private int fCurrent;

        ObjIterator()				{ fCurrent = 0;	}

        public final boolean hasNext()	{ return fCurrent < size(); }
        public final Object next()		{ return nextInt(); }
        public final int nextInt()		{ return get(fCurrent++); }
        public final void remove()		{ removeRange(fCurrent,fCurrent+1); }
    }

	
} // class IntArray
