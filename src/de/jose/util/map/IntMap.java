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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * abstract base class for IntHashSet, IntHashMap and IntIntMap
 *
 * it uses an OPEN hash map, that means all entries are stored in the table itself,
 * there is no chained list of "buckets"
 *
 * note that open hashing is more sensitive to collisions than closed hashing (which is used by java.util)
 * so you should choose the initial size of the map carefully (about 2 times the expected size)
 *
 * -----------------------------------------------------------------------------------------------------------
 *
 * there are two integers that must not be used as hash keys:
 * 	Integer.MIN_VALUE+1 (= -2147483647)
 * and
 * 	Integer.MIN_VALUE+2 (= -2147483646)
 *
 * any other keys can be used, including 0, MIN_VALUE and MAX_VALUE
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

abstract public class IntMap
        	extends AbstractMap
{
	//-------------------------------------------------------------------------
	// constants
	//-------------------------------------------------------------------------

	/**	empty slots are indicated by 0
	 * 	however, we would like to use 0 as a key,
	 * 	so 0 gets mapped to Integer.MIN_VALUE+1
	 */
	protected static int ZERO_KEY = Integer.MIN_VALUE + 1;
	/**	marker for deleted entries
	 * 	(deleted entries need a special marker so that collisions chains are not broken;
	 *   however, they can be overwritten for inserts)
	 */
	protected static int DELETED_KEY = Integer.MIN_VALUE + 2;

	//-------------------------------------------------------------------------
	// variables
	//-------------------------------------------------------------------------

	/**	the key list	*/
	protected int[] fKeys;
	/**	values are created by derived classes	*/

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
	protected IntMap(int capacity, float loadFactor) {
		super(capacity,loadFactor);
	}

	//-------------------------------------------------------------------------
	// basic access
	//-------------------------------------------------------------------------

	/**
	 * Returns <tt>true</tt> if this set contains the specified element.  More
	 * formally, returns <tt>true</tt> if and only if this set contains an
	 * element <code>e</code> such that <code>(key==e))</code>.
	 *
	 * @param key element whose presence in this set is to be tested.
	 * @return <tt>true</tt> if this set contains the specified element.
	 */
	public final boolean containsKey(int key) {
		if (key == 0) key = ZERO_KEY;
		return lookup(key);
	}

	/**
	 * Returns <tt>true</tt> if this set contains the specified element.  More
	 * formally, returns <tt>true</tt> if and only if this set contains an
	 * element <code>e</code> such that <code>(key.equals(e))</code>.
	 *
	 * @param key element whose presence in this set is to be tested. must be a Number.
	 * @return <tt>true</tt> if this set contains the specified element.
	 * @throws java.lang.ClassCastException if the type of the specified element
	 * 	       is not java.lang.Number.
	 * @throws java.lang.NullPointerException if the specified element is null.
	 */
	public final boolean containsKey(Object key) {
		return containsKey(((Number) key).intValue());
	}

	/**
	 * Returns <tt>true</tt> if this set contains all of the elements of the
	 * specified array.  This
	 * method returns <tt>true</tt> if it is a <i>subset</i> of this set.
	 *
	 * @param  keys array to be checked for containment in this set.
	 * 		must contains java.lang.Number objects.
	 * @return <tt>true</tt> if this set contains all of the elements of the
	 * 	       specified array.
	 * @see    #contains(java.lang.Object)
	 */
	public final boolean containsAllKeys(int[] keys) {
		for (int i = keys.length - 1; i >= 0; i--)
			if (!containsKey(keys[i]))
				return false;
		return true;
	}

	/**
	 * Returns <tt>true</tt> if this set contains all of the elements of the
	 * specified collection.  If the specified collection is also a set, this
	 * method returns <tt>true</tt> if it is a <i>subset</i> of this set.
	 *
	 * @param  coll collection to be checked for containment in this set.
	 * 		must contains java.lang.Number objects.
	 * @return <tt>true</tt> if this set contains all of the elements of the
	 * 	       specified collection.
	 * @throws java.lang.ClassCastException if the types of one or more elements
	 *         in the specified collection are not of type java.lang.Number.
	 * @throws java.lang.NullPointerException if the specified collection contains one
	 *         or more null elements.
	 * @throws java.lang.NullPointerException if the specified collection is
	 *         <tt>null</tt>.
	 * @see    #contains(java.lang.Object)
	 */
	public final boolean containsAllKeys(Collection coll)
	{
		if (coll instanceof IntMap) {
			//	performance shortcut for other IntMaps
			IntMap imap = (IntMap)coll;
			return containsAllKeys(imap.fKeys);
		}
		if (coll instanceof AbstractSet) {
			//	performance shortcut for IntMap Set views
			AbstractSet iset = (AbstractSet)coll;
			return containsAllKeys(iset.toIntArray());
		}
		//	else: Collection of Numbers
		java.util.Iterator i = coll.iterator();
		while (i.hasNext())
			if (!contains(((Number)i.next()).intValue()))
				return false;
		return true;
	}

	public final boolean containsAllKeys(IntMap imap) {
		return containsAllKeys(imap.fKeys);
	}

	/**
	 * Returns <tt>true</tt> if this set contains the specified element.  More
	 * formally, returns <tt>true</tt> if and only if this set contains an
	 * element <code>e</code> such that <code>(key==e))</code>.
	 *
	 * @param key element whose presence in this set is to be tested.
	 * @return <tt>true</tt> if this set contains the specified element.
	 */
	public final boolean contains(int key) {
		return containsKey(key);
	}

	/**
	 * Returns <tt>true</tt> if this set contains the specified element.  More
	 * formally, returns <tt>true</tt> if and only if this set contains an
	 * element <code>e</code> such that <code>(key.equals(e))</code>.
	 *
	 * @param key element whose presence in this set is to be tested. must be a Number.
	 * @return <tt>true</tt> if this set contains the specified element.
	 * @throws java.lang.ClassCastException if the type of the specified element
	 * 	       is not java.lang.Number.
	 * @throws java.lang.NullPointerException if the specified element is null.
	 */
	public final boolean contains(Object key) {
		return containsKey(((Number)key).intValue());
	}

	/**
	 * Removes all of the elements from this map.
	 * This map will be empty after this call returns (unless it throws an
	 * exception).
	 */
	public void clear() {
		Arrays.fill(fKeys, 0);
		fSize = 0;
	}

	//-------------------------------------------------------------------------
	// methods
	//-------------------------------------------------------------------------

	/**
	 * Makes sure that this map can hold at least 'capacity' elements.
	 * The current capacity is increased, if necessary.
	 *
	 * <p> The current implementation will adjust the capacity so that it is
	 * always a power of 2.
	 *
	 * @return true if the capacity had to be increased
	 */
	public final boolean ensureCapacity(int capacity) {
		if (capacity > fCapacity) {
			//	find the next power of 2
			int pow2 = 1;
			while (pow2 < capacity) pow2 <<= 1;

			if (fKeys == null) {
				//	first call
				fCapacity = pow2;
				fKeys = new int[pow2];
			} else
				rehash(pow2);
			return true;
		} else
			return false;
	}


	/**
	 * Get an array of all keys int this map.

	 * @return an array containing all keys
	 */
	public final int[] keys() {
		return keys((int[]) null);
	}

	/**
	 * Get an array of all keys int this map.
	 * The keys will be placed in the supplied array, if it is big enough.
	 * If the supplied result array is too small, a new array will be allocated and returned.
	 *
	 * @param result the array which will hold the keys on return; may be null
	 * @return an array containing all keys
	 */
	public final int[] keys(int[] result)
	{
		if (result == null || result.length < size())
			result = new int[size()];

		int j = 0;
		for (int i = fKeys.length-1; i >= 0; i--)
			if (fKeys[i] != 0 && fKeys[i] != DELETED_KEY) {
				result[j] = fKeys[i];
				if (result[j] == ZERO_KEY) result[j] = 0;
				j++;
			}
		return result;
	}

	/**
	 * Get an array of all keys int this map.
	 * The keys will be placed in the supplied array, if it is big enough.
	 * If the supplied result array is too small, a new array will be allocated and returned.
	 *
	 * @param result the array which will hold the keys on return; may be null
	 * @return an array containing all keys
	 * @throws    java.lang.ArrayStoreException the runtime type of 'result' is not java.lang.Number or a superclass of it.
	 */
	public final Object[] keys(Object[] result)
	{
		if (result == null)
			result = new Integer[size()];
		else if (result.length < size()) {
			Class clazz = result.getClass().getComponentType();
			result = (Object[]) Array.newInstance(clazz, size());
		}

		int j = 0;
		for (int i = fKeys.length-1; i >= 0; i--)
			if (fKeys[i] != 0 && fKeys[i] != DELETED_KEY) {
				if (fKeys[i] == ZERO_KEY)
					result[j] = new Integer(0);
				else
					result[j] = new Integer(fKeys[i]);
				j++;
			}
		return result;
	}

	/**
	 * Returns an iterator over the keys in this map.  The keys are
	 * returned in no particular order.
	 *
	 * <p>The returned Iterator is of type IntMap.IntIterator.
	 * It has an additional nextInt() method that returns an 'int' instead of an 'Object'
	 *
	 * @return an iterator over the elements in this set.
	 */
	public final IntIterator keyIterator() {
		return new IntIterator();
	}

	//-------------------------------------------------------------------------
	// protected parts
	//-------------------------------------------------------------------------


//	public int lookups = 0;
//	public int collisions = 0;
//	public int rehash = 0;

	/**
	 * find a key in the hash table
	 * @param key
	 * @return if the key is present, its index;
	 * 		otherwise -1-index
	 */
	protected final boolean lookup(int key) {
//		lookups++;
		index = (key & 0x7fffffff) % fCapacity;	//	first probe

		int fkey = fKeys[index];
		if (fkey == 0)	//	empty
			return false;
		if (fkey == key)	//	found
			return true;

		//	else: collision
		int y = (key & 0x3fffffff) | 0x00000001;
		/**	y must be ODD to ensure max. probe length (assuming that fCapacity is a power of 2)
		 * 	this is vital since we have no check for infinte loops ;-)
		 */
		for (;;) {
//			collisions++;
			index = (index + y) % fCapacity;
			fkey = fKeys[index];
			if (fkey == 0)	//	empty
				return false;
			if (fkey == key)	//	found
				return true;
		}
	}


	/**
	 * same as lookup, only that DELETED entries will be overwritten
	 * @param key
	 * @return if the key is present, its index;
	 * 		otherwise -1-index
	 */
	protected final boolean lookupForInsert(int key) {
//		lookups++;
		index = (key & 0x7fffffff) % fCapacity;	//	first probe

		int fkey = fKeys[index];
		if (fkey == 0 || fkey == DELETED_KEY)	//	empty
			return false;
		if (fkey == key)	//	found
			return true;

		//	else: collision
		int y = (key & 0x3fffffff) | 0x00000001;
		for (;;) {
//			collisions++;
			index = (index + y) % fCapacity;
			fkey = fKeys[index];
			if (fkey == 0 || fkey == DELETED_KEY)	//	empty
				return false;
			if (fkey == key)	//	found
				return true;
		}
	}

	/**
	 * same as lookup, but we can be sure that the entry is not yet in the table
	 */
	protected final void lookupForRehash(int key) {
//		rehash++;	/* debugging	*/
		index = (key & 0x7fffffff) % fCapacity;	//	first probe

		if (fKeys[index] != 0) {
			int y = (key & 0x3fffffff) | 0x00000001;
			do {
				index = (index + y) % fCapacity;
			} while (fKeys[index] != 0);
		}
	}

	/**
	 * resize the hash map
	 * must be overwritten by derived class to adjust the value arrays, too
	 *
	 * @param newCapacity
	 */
	abstract protected void rehash(int newCapacity);


	/**
	 * remove an entry
	 * @param idx slot index
	 * @return true if the entry was actually removed
	 */
	protected boolean removeInternal(int idx) {
		int fkey = fKeys[idx];
		if (fkey == 0 || fkey == DELETED_KEY)
			return false;
		fKeys[idx] = DELETED_KEY;
		fSize--;
		return true;
	}

	//-------------------------------------------------------------------------
	// inner classes
	//-------------------------------------------------------------------------

	/**
	 * an Iterator that walks over the key set of an IntMap
	 *	 gets overwritten by several subclasses
	 */
	abstract public class Iterator extends AbstractMap.Iterator implements java.util.Iterator
 	{

		/**	looks for the next valid slot	*/
		protected int fetch(int j) {
			while (j < fCapacity && (fKeys[j] == 0 || fKeys[j] == DELETED_KEY))
				j++;
			return j;
		}
	}

	/**
	 * an Iterator with an additional nextInt() method
	 * that returns the next 'int' value
	 *
	 * calling nextInt() should be preferred to next()
	 */
	public class IntIterator extends Iterator implements java.util.Iterator
 	{
		/**
		 * Returns the next element in the iteration.
		 *
		 * @return the next element in the iteration.
		 * @exception java.util.NoSuchElementException iteration has no more elements.
		 */
		public int nextInt() {
			advance();
			if (i >= fCapacity)
				throw new NoSuchElementException("no more elements");
			return (fKeys[i] == ZERO_KEY) ? 0 : fKeys[i];
		}

		/**
		 * Returns the next element in the iteration.
		 *
		 * @return the next element in the iteration.
		 * @exception java.util.NoSuchElementException iteration has no more elements.
		 */
		public Object next() {
			return new Integer(nextInt());
		}
	}


	/**
	 * an abstract Set view; base class for several derived classes
	 * set manipulation is not supported, unless overwritten
	 */
	public class AbstractSet extends AbstractMap.AbstractSet
 	{
		/**
		 * @return the associated int arrays (which is either fKeys or fValues)
		 */
		protected int[] toIntArray()
		{
			return IntMap.this.fKeys;
		}
	}

	/**
	 * abstract Set view used by several derived classes
	 * derived classes may want to overwrite the iterator() and toArray() methods
	 */
	public class KeySet extends AbstractSet {
		/**
		 * @see de.jose.util.map.IntMap#contains(java.lang.Object)
		 */
		public boolean contains(Object obj) {
			return IntMap.this.contains(obj);
		}

		/**
		 * @see de.jose.util.map.IntMap#keyIterator()
		 */
		public java.util.Iterator iterator() {
			return IntMap.this.keyIterator();
		}

		/**
		 * @see de.jose.util.map.IntMap#keys()
		 */
		public Object[] toArray() {
			return IntMap.this.keys((Object[]) null);
		}

		/**
		 * @see de.jose.util.map.IntMap#keys(java.lang.Object[])
		 */
		public Object[] toArray(Object[] array) {
			return IntMap.this.keys(array);
		}

		/**
		 * @see de.jose.util.map.IntMap#containsAllKeys(java.util.Collection)
		 */
		public boolean containsAll(Collection coll) {
			return IntMap.this.containsAllKeys(coll);
		}
	}

}
