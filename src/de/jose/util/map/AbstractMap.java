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

package de.jose.util.map;

import java.util.Collection;

/**
 * abstract base class for ObjHashSet, ObjHashMap, ObjIntMap, IntHashSet, IntHashMap and IntIntMap
 *
 * it uses an OPEN hash map, that means all entries are stored in the table itself,
 * there is no chained list of "buckets"
 *
 * note that open hashing is more sensitive to collisions than closed hashing (which is used by java.util)
 * so you should choose the initial size of the map carefully (about 2 times the expected size)
 *
 * -----------------------------------------------------------------------------------------------------------
 *
 * collisions are resolved by double hashing using the hash functions:
 * 	h1(k)	= 	k % m
 * for the first probe, and
 * 	h(k,i)	=	(h1(k) + i * h2(k)) % m
 * 	h2(k)	= 	k | 1
 * where m, the table size, is always a power of 2, and h2(k) is always odd
 * (this must be so to avoid infinite loops and/or sub-optimal performance)
 *
 * @author Peter Schäfer
 */

abstract public class AbstractMap
{

	//-------------------------------------------------------------------------
	// variables
	//-------------------------------------------------------------------------

	/**	keys are created by dervied classes */
	/**	values are created by derived classes	*/

	/**	current capacity == fKeys.length == fValues.length
	 * 	makes sure that one slot is always empty (so that we don't run into an infinte loop)
	 * */
	protected int fCapacity;
	/**	rehash threshold; must not be larger than fCapacity-1	*/
	protected int fThreshold;
	/**	thresholod factor	 (fThreshold = fThreshFactor * newCapacity) */
	protected float fThreshFactor;
	/**	current number of entries	*/
	protected int fSize;
	/**	aux variable: return value from lookup	*/
	protected int index;

	//-------------------------------------------------------------------------
	// constructors
	//-------------------------------------------------------------------------

	/**
	 * creates a new IntIntMap with the given capacity
	 * the capacity will automatically double when the loadFactor is reached
	 * (be aware however, that this is an expensive operation; if possible,
	 *  you should set the initial capacity to about 2 times of the expected size)
	 *
	 * @param capacity the initial capacity
	 * @param loadFactor the factor when the map is rehashed (0..1)
	 */
	protected AbstractMap(int capacity, float loadFactor) {
		ensureCapacity(capacity);
		fThreshFactor = loadFactor;
		fThreshold = calcThreshold(fThreshFactor, fCapacity);
		fSize = 0;
	}

	//-------------------------------------------------------------------------
	// basic access
	//-------------------------------------------------------------------------

	/**
	 * Returns the number of elements in this map (its cardinality).
	 *
	 * @return the number of elements in this map (its cardinality).
	 */
	public final int size() {
		return fSize;
	}

	/**
	 * Returns <tt>true</tt> if this map contains no elements.
	 *
	 * @return <tt>true</tt> if this map contains no elements.
	 */
	public final boolean isEmpty() {
		return fSize == 0;
	}

	/**
	 * 	Returns the capacity of the map. I.e. the number of allocated elements.
	 *
	 *	@return the current capacity
	 */
	public final int getCapacity() {
		return fCapacity;
	}

	/**
	 * Removes all of the elements from this map.
	 * This map will be empty after this call returns (unless it throws an
	 * exception).
	 */
	abstract public void clear();

    /**
     * create a string representation of the map
     *
     * @param buf a StringBuffer that will contain the result
     * @param separatorChars
     * @return the same StringBuffer
     */
    public final StringBuffer appendString(StringBuffer buf, String separatorChars)
    {
        char c0 = 0;
        char c1 = ',';
        char c2 = '=';
        char c3 = 0;

        if (separatorChars.length()>=4) {
            c0 = separatorChars.charAt(0);
            c1 = separatorChars.charAt(1);
            c2 = separatorChars.charAt(2);
            c3 = separatorChars.charAt(3);
        }
        else if (separatorChars.length()>=3) {
            c0 = separatorChars.charAt(0);
            c1 = separatorChars.charAt(1);
            c3 = separatorChars.charAt(2);
        }
        else if (separatorChars.length()>=2) {
            c1 = separatorChars.charAt(0);
            c2 = separatorChars.charAt(1);
        }
        else if (separatorChars.length()>=1) {
            c1 = separatorChars.charAt(0);
        }

        if (c0!=0)
            buf.append(c0);

        appendString(buf, c1,c2);

        if (c3!=0)
            buf.append(c3);

        return buf;
    }

    /**
     * create a string representation of the map
     *
     * @param separatorChars
     * @return a string representation of the map
     */
    public final String toString(String separatorChars)
    {
        StringBuffer buf = new StringBuffer();
        appendString(buf,separatorChars);
        return buf.toString();
    }

    /**
     * create a string representation of the map
     *
     * @return a string representation of the map
     */
    public final String toString()
    {
        return toString("(,=)");
    }

	//-------------------------------------------------------------------------
	// protected parts
	//-------------------------------------------------------------------------


	/**
	 * resize the hash map
	 * must be overwritten by derived class to adjust the value arrays, too
	 *
	 * @param newCapacity
	 */
	abstract protected void rehash(int newCapacity);

	/**
	 * Makes sure that this map can hold at least 'capacity' elements.
	 * The current capacity is increased, if necessary.
	 *
	 * <p> The current implementation will adjust the capacity so that it is
	 * always a power of 2.
	 *
	 * @return true if the capacity had to be increased
	 */
	abstract protected boolean ensureCapacity(int newCapacity);

	/**
	 * calculate the load factor threshold when rehashing occurs
	 */
	protected int calcThreshold(float factor, int capacity) {
		int result = (int) (capacity * factor);
		if (result < 2) result = 2;
		if (result > capacity - 1) result = capacity - 1;
		/*	make sure that one slot is always empty
			so that lookup() won't run into an infinite loop !
		*/
		return result;
	}

	/**
	 * remove an entry
	 * @param idx slot index
	 * @return true if the entry was actually removed
	 */
	abstract protected boolean removeInternal(int idx);

    abstract protected void appendString(StringBuffer buf, char entrySeparator, char keySeparator);

	//-------------------------------------------------------------------------
	// inner classes
	//-------------------------------------------------------------------------

	/**
	 * an Iterator that walks over the key set of an IntMap
	 *	 gets overwritten by several subclasses
	 */
	abstract public class Iterator implements java.util.Iterator
 	{
		/**	current slot index	*/
		protected int i;
		/**	next slot index	*/
		protected int nexti;

		/**	creates a new Iterator	*/
		public Iterator() {
			i = -1;
			nexti = -1;
		}

		/**
		 * Returns <tt>true</tt> if the iteration has more elements. (In other
		 * words, returns <tt>true</tt> if <tt>next</tt> would return an element
		 * rather than throwing an exception.)
		 *
		 * @return <tt>true</tt> if the iterator has more elements.
		 */
		public boolean hasNext() {
            if (nexti < 0) nexti = fetch(0);
			return nexti < fCapacity;
		}

		/**
		 *
		 * Removes from the underlying map the last element returned by the
		 * iterator.  This method can be called only once per
		 * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
		 * the underlying collection is modified while the iteration is in
		 * progress in any way other than by calling this method.
		 *
		 * @exception java.lang.IllegalStateException if the <tt>next</tt> method has not
		 *		  yet been called
		 */
		public void remove() {
			if (i < 0) throw new IllegalStateException("must call next() first");
			removeInternal(i);
		}


		/**	looks for the next valid slot	*/
		abstract protected int fetch(int j);

		/**	advances the iterator to the next valid slot	*/
		protected void advance() {
            if (nexti < 0)
                i = fetch(0);
            else
			    i = nexti;
			nexti = fetch(i + 1);
		}
	}

	/**
	 * an abstract Set view; base class for several derived classes
	 * set manipulation is not supported, unless overwritten
	 */
	public class AbstractSet {
		/**
		 * @see de.jose.util.map.IntMap#size();
		 */
		public int size() {
			return AbstractMap.this.size();
		}

		/**
		 * @see de.jose.util.map.IntMap#isEmpty();
		 */
		public boolean isEmpty() {
			return AbstractMap.this.isEmpty();
		}

		/**
		 * @see de.jose.util.map.IntMap#clear();
		 */
		public void clear() {
			AbstractMap.this.clear();
		}

		/**
		 * @throws java.lang.UnsupportedOperationException
		 */
		public boolean add(Object obj) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @throws java.lang.UnsupportedOperationException
		 */
		public boolean addAll(Collection coll) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @throws java.lang.UnsupportedOperationException
		 */
		public boolean remove(Object obj) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @throws java.lang.UnsupportedOperationException
		 */
		public boolean contains(Object obj) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @throws java.lang.UnsupportedOperationException
		 */
		public java.util.Iterator iterator() {
			throw new UnsupportedOperationException();
		}

		/**
		 * @throws java.lang.UnsupportedOperationException
		 */
		public Object[] toArray() {
			throw new UnsupportedOperationException();
		}

		/**
		 * @throws java.lang.UnsupportedOperationException
		 */
		public Object[] toArray(Object[] result) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @throws java.lang.UnsupportedOperationException
		 */
		public boolean containsAll(Collection coll) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @throws java.lang.UnsupportedOperationException
		 */
		public boolean retainAll(Collection coll) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @throws java.lang.UnsupportedOperationException
		 */
		public boolean removeAll(Collection coll) {
			throw new UnsupportedOperationException();
		}

	}

}
