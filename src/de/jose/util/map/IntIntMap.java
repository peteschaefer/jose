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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * a hash map that maps int keys to int values
 *
 * -----------------------------------------------------------------------------------------------------------
 * there are two integers that must not be used as hash keys or values:
 * 	Integer.MIN_VALUE+1 (= -2147483647)
 * and
 * 	Integer.MIN_VALUE+2 (= -2147483646)
 *
 * any other keys can be used, including 0, MIN_VALUE and MAX_VALUE
 * -----------------------------------------------------------------------------------------------------------
 *
 * it implements java.util.Map, but whenever possible you should prefer using 'int' methods rather than 'Object' methods.
 * For example, put(5,6) is better than put(new Integer(5), new Integer(6)).
 *
 * @author Peter Schäfer
 */

public class IntIntMap
        extends IntMap
        implements Map
{
	//-------------------------------------------------------------------------
	// constants
	//-------------------------------------------------------------------------

	/**
	 * return value from get() if the key is not found
	 */
	public static int NOT_FOUND	= Integer.MIN_VALUE+1;

	//-------------------------------------------------------------------------
	// variables
	//-------------------------------------------------------------------------

	/**	the value list	*
	 * each value is associated with an entry in fKeys
	 */
	protected int[]	fValues;

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
	 * @param loadFactor factor when the capacity increases automatically (0..1)
	 */
	public IntIntMap(int capacity, float loadFactor)
	{
		super(capacity,loadFactor);
		fValues = new int[fCapacity];
	}

	/**
	 * creates a new IntIntMap with default capacity (64) and load factor (0.8)
	 */
	public IntIntMap()
	{
		this(64,0.8f);
	}

	//-------------------------------------------------------------------------
	// basic access
	//-------------------------------------------------------------------------

	/**
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for
	 * this key, the old value is replaced by the specified value.  (A map
	 * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only
	 * if {@link #containsKey(java.lang.Object) m.containsKey(k)} would return
	 * <tt>true</tt>.))
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 * @return previous value associated with specified key, or <tt>NOT_FOUND</tt>
	 *	       if there was no mapping for key.
	 *
	 */
	public final int put(int key, int value)
	{
		if (key==0) key = ZERO_KEY;
		if (key==DELETED_KEY) return NOT_FOUND;

		if (!lookupForInsert(key)) {
			//	insert
			fKeys[index] = key;
			fValues[index] = value;

			if (++fSize >= fThreshold)	//	increase capacity
				rehash(2*fCapacity);

			return NOT_FOUND;
		}
		//	else: overwrite
		int result = fValues[index];
		fValues[index] = value;
		return result;
	}

	/**
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for
	 * this key, the old value is replaced by the specified value.  (A map
	 * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only
	 * if {@link #containsKey(java.lang.Object) m.containsKey(k)} would return
	 * <tt>true</tt>.))
	 *
	 * @param key key with which the specified value is to be associated. must be a Number.
	 * @param value value to be associated with the specified key. must be a Number.
	 * @return previous value associated with specified key, or <tt>null</tt>
	 *	       if there was no mapping for key.
	 *
	 * @throws java.lang.ClassCastException if the class of the specified key or value
	 * 	          is not java.lang.Number.
	 * @throws java.lang.NullPointerException this map does not permit <tt>null</tt>
	 *            keys or values.
	 */
	public final Object put(Object key, Object value)
	{
		int result = put(((Number)key).intValue(), ((Number)value).intValue());
		if (result==NOT_FOUND)
			return null;
		else
			return new Integer(result);
	}

	/**
	 * Returns the value to which this map maps the specified key.  Returns
	 * <tt>NOT_FOUND</tt> if the map contains no mapping for this key.
	 *
	 * <p>More formally, if this map contains a mapping from a key
	 * <tt>k</tt> to a value <tt>v</tt> such that <tt>(key==k)</tt>,
	 * then this method returns <tt>v</tt>; otherwise
	 * it returns <tt>NOT_FOUND</tt>.  (There can be at most one such mapping.)
	 *
	 * @param key key whose associated value is to be returned.
	 * @return the value to which this map maps the specified key, or
	 *	       <tt>NOT_FOUND</tt> if the map contains no mapping for this key.
	 *
	 * @see #containsKey(java.lang.Object)
	 */
	public final int get(int key)
	{
		if (key==0) key = ZERO_KEY;
		if (key==DELETED_KEY) return NOT_FOUND;

		if (lookup(key))
			return fValues[index];
		else
			return NOT_FOUND;
	}

	/**
	 * Returns the value to which this map maps the specified key.  Returns
	 * <tt>null</tt> if the map contains no mapping for this key.
	 *
	 * <p>More formally, if this map contains a mapping from a key
	 * <tt>k</tt> to a value <tt>v</tt> such that <tt>(key.equals(k))</tt>,
	 * then this method returns <tt>v</tt>; otherwise
	 * it returns <tt>null</tt>.  (There can be at most one such mapping.)
	 *
	 * @param key key whose associated value is to be returned. must be a Number.
	 * @return the value to which this map maps the specified key, or
	 *	       <tt>null</tt> if the map contains no mapping for this key.
	 *
	 * @throws java.lang.ClassCastException if the key is not of type java.lang.Number.
	 * @throws java.lang.NullPointerException key is <tt>null</tt>.
	 *
	 * @see #containsKey(java.lang.Object)
	 */
	public final Object get(Object key)
	{
		int value = get(((Number)key).intValue());
		if (value==NOT_FOUND)
			return null;
		else
			return new Integer(value);
	}

	/**
	  * Removes the mapping for this key from this map if it is present.
	  * More formally, if this map contains a mapping
	  * from key <tt>k</tt> to value <tt>v</tt> such that
	  * <code>(key==k)</code>, that mapping
	  * is removed.  (The map can contain at most one such mapping.)
	  *
	  * <p>Returns the value to which the map previously associated the key, or
	  * <tt>NOT_FOUND</tt> if the map contained no mapping for this key.
	  *  The map will not contain a mapping for
	  * the specified  key once the call returns.
	  *
	  * @param key key whose mapping is to be removed from the map.
	  * @return previous value associated with specified key, or <tt>NOT_FOUND</tt>
	  *	       if there was no mapping for key.
	  *
	  */
	public final int remove(int key)
	{
		if (key==0) key = ZERO_KEY;
		if (key==DELETED_KEY) return NOT_FOUND;

		if (lookup(key)) {	//	remove
			int result = fValues[index];
			removeInternal(index);
			return result;
		}
		else
			return NOT_FOUND;
	}

	/**
	  * Removes the mapping for this key from this map if it is present
	  * (optional operation).   More formally, if this map contains a mapping
	  * from key <tt>k</tt> to value <tt>v</tt> such that
	  * <code>(key.equals(k))</code>, that mapping
	  * is removed.  (The map can contain at most one such mapping.)
	  *
	  * <p>Returns the value to which the map previously associated the key, or
	  * <tt>null</tt> if the map contained no mapping for this key.
	  *  The map will not contain a mapping for
	  * the specified  key once the call returns.
	  *
	  * @param key key whose mapping is to be removed from the map. msut be a Number.
	  * @return previous value associated with specified key, or <tt>null</tt>
	  *	       if there was no mapping for key.
	  *
	  * @throws java.lang.ClassCastException if the key is not of type java.lang.Number.
	  * @throws java.lang.NullPointerException if the key is <tt>null</tt>.
	  */
	public final Object remove(Object key)
	{
		int result = remove(((Number)key).intValue());
		if (result==NOT_FOUND)
			return null;
		else
			return new Integer(result);
	}

	//-------------------------------------------------------------------------
	// methods
	//-------------------------------------------------------------------------

	/**
	 * Copies all of the mappings from the specified arrays to this map.
	 * The effect of this call is equivalent to that
	 * of calling {@link #put(int,int) put(k, v)} on this map once
	 * for each mapping from key <tt>k</tt> to value <tt>v</tt> in the
	 * specified arrays.  The behavior of this operation is unspecified if the
	 * specified map is modified while the operation is in progress.
	 *
	 * @param keys keys to be stored in this map
	 * @param values values to be stored in this map
	 *
	 * @throws java.lang.ArrayIndexOutOfBoundsException if values is shorter than keys
	 */
	public final int putAll(int[] keys, int[] values)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (put(keys[i],values[i]) != NOT_FOUND)
				result++;
		}
		return result;
	}

	/**
	 * Copies all of the mappings from the specified map to this map.
	 * The effect of this call is equivalent to that
	 * of calling {@link #put(java.lang.Object,java.lang.Object) put(k, v)} on this map once
	 * for each mapping from key <tt>k</tt> to value <tt>v</tt> in the
	 * specified map.  The behavior of this operation is unspecified if the
	 * specified map is modified while the operation is in progress.
	 *
	 * @param map Mappings to be stored in this map.
	 *
	 * @throws java.lang.ClassCastException if the class of a key or value in the
	 * 	          specified map is not java.lang.Number.
	 *
	 * @throws java.lang.NullPointerException the specified map is <tt>null</tt>, or if the
	 *         specified map contains <tt>null</tt> keys or values.
	 */
	public final void putAll(Map map)
	{
		if (map instanceof IntIntMap) {
			//	performance shortcut for IntIntMap
			IntIntMap imap = (IntIntMap)map;
			putAllInternal(imap.fKeys,imap.fValues);
			return;
		}
		if (map instanceof IntHashMap) {
			//	performance shortcut for IntHashMap
			IntHashMap imap = (IntHashMap)map;
			putAllInternal(imap.fKeys,imap.fValues);
			return;
		}
		//	else: Map of Objects
		java.util.Iterator i = map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry ety = (Map.Entry)i.next();
			put(ety.getKey(),ety.getValue());
		}
	}



	/**
	 * Removes from this map all of its elements that are contained in the
	 * specified array.
	 *
	 * @param  keys array that defines which elements will be removed from
	 *           this map.
	 * @return the number of mappings that were actually removed, or 0.
	 * @see    #remove(java.lang.Object)
	 */
	public final int removeAll(int[] keys)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (remove(keys[i]) != NOT_FOUND)
				result++;
		}
		return result;
	}

	/**
	 * Removes from this map all of its elements that are contained in the
	 * specified collection.
	 *
	 * <p>The collection is supposed to be an IntHashSet, and IntHashMap, an IntIntMap
	 * or a Collection of java.lang.Number objects.
	 *
	 * @param  coll collection that defines which elements will be removed from
	 *           this map.
	 * @return the number of mappings that were actually removed, or 0.
	 *
	 * @throws java.lang.ClassCastException if the types of one or more elements in the collection
	 * 				are not java.lang.Number.
	 * @see    #remove(java.lang.Object)
	 */
	public final int removeAll(Collection coll)
	{
		if (coll instanceof IntMap) {
			//	performance shortcut for IntMaps
			return removeAllInternal(((IntMap)coll).fKeys);
		}

		int result = 0;
		java.util.Iterator i = coll.iterator();
		while (i.hasNext())
			if (remove(((Number)i.next()).intValue()) != NOT_FOUND)
				result++;
		return result;
	}


	/**
	 * Removes all mappings to the given value.
	 *
	 * <p>Returns the number of mappings that were actually deleted from the map;
	 * or 0 if no mapping was deleted.
	 *
	 * @param value an mapping value
	 * @return the number of affected mappings, or 0 if the value was not present in the map
	 */
	public final int removeValue(int value)
	{
		int result = 0;
		for (int i=fKeys.length-1; i>=0; i--)
		{
			if (fKeys[i]==0 || fKeys[i]==DELETED_KEY)
				continue;
			if (fValues[i]==value) {
				removeInternal(i);
				result++;
			}
		}
		return result;
	}

	/**
	 * Removes all mappings to the given value.
	 *
	 * <p>Returns the number of mappings that were actually deleted from the map;
	 * or 0 if no mapping was deleted.
	 *
	 * @param value an mapping value
	 * @return the number of affected mappings, or 0 if the value was not present in the map
	 * @throws java.lang.ClassCastException if the type of the value is not java.lang.Number.
	 */
	public final boolean removeValue(Object value)
	{
		return removeValue(((Number)value).intValue()) > 0;
	}


	/**
	 * Returns <tt>true</tt> if this map maps one or more keys to the
	 * specified value.  More formally, returns <tt>true</tt> if and only if
	 * this map contains at least one mapping to a value <tt>v</tt> such that
	 * <tt>(value==v)</tt>.  This operation
	 * will probably require time linear in the map size for most
	 * implementations of the <tt>Map</tt> interface.
	 *
	 * @param value value whose presence in this map is to be tested.
	 * @return <tt>true</tt> if this map maps one or more keys to the
	 *         specified value.
	 */
	public final boolean containsValue(int value)
	{
		for (int i=fKeys.length-1; i>=0; i--)
		{
			if (fKeys[i]==0 || fKeys[i]==DELETED_KEY)
				continue;
			if (fValues[i]==value)
				return true;
		}
		return false;
	}

	/**
	 * Returns <tt>true</tt> if this map maps one or more keys to the
	 * specified value.  More formally, returns <tt>true</tt> if and only if
	 * this map contains at least one mapping to a value <tt>v</tt> such that
	 * <tt>(value.equals(v))</tt>.  This operation
	 * will probably require time linear in the map size for most
	 * implementations of the <tt>Map</tt> interface.
	 *
	 * @param value value whose presence in this map is to be tested. must be a Number.
	 * @return <tt>true</tt> if this map maps one or more keys to the
	 *         specified value.
	 * @throws java.lang.ClassCastException if the value is not of type java.lang.Number.
	 * @throws java.lang.NullPointerException if the value is <tt>null</tt>.
	 */
	public final boolean containsValue(Object value)
	{
		return containsValue(((Number)value).intValue());
	}


	/**
	 * Get an array of all values int this map.
	 *
	 * @return an array containing all values
	 */
	public final int[] valueArray()
	{
		return valueArray((int[])null);
	}

	/**
	 * Get an array of all values int this set.
	 * The values will be placed in the supplied array, if it is big enough.
	 * If the supplied result array is too small, a new array will be allocated and returned.
	 *
	 * @param result the array which will hold the values on return; may be null
	 * @return an array containing all values
	 */
	public final int[] valueArray(int[] result)
	{
		if (result==null || result.length < size())
			result = new int[size()];

		int j=0;
		for (int i=fKeys.length-1; i>=0 && j<fSize; i--)
			if (fKeys[i] != 0 && fKeys[i] != DELETED_KEY)
				result[j++] = fValues[i];
		return result;
	}


	/**
	 * Get an array of all values int this set.
	 * The values will be placed in the supplied array, if it is big enough.
	 * If the supplied result array is too small, a new array will be allocated and returned.
	 *
	 * @param result the array which will hold the values on return; may be null
	 * @return an array containing all values
	 */
	public final Object[] valueArray(Object[] result)
	{
		if (result==null)
			result = new Integer[size()];
		else if (result.length < size()) {
			Class clazz = result.getClass().getComponentType();
			result = (Object[])Array.newInstance(clazz,size());
		}

		int j=0;
		for (int i=fKeys.length-1; i>=0; i--)
			if (fKeys[i] != 0 && fKeys[i] != DELETED_KEY)
				result[j++] = new Integer(fValues[i]);
		return result;
	}

	//-------------------------------------------------------------------------
	// Set views
	//-------------------------------------------------------------------------

	/**
	 * Returns a set view of the keys contained in this map.  The set is
	 * backed by the map, so changes to the map are reflected in the set, and
	 * vice-versa.  If the map is modified while an iteration over the set is
	 * in progress, the results of the iteration are undefined.  The set
	 * supports element removal, which removes the corresponding mapping from
	 * the map, via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
	 * <tt>removeAll</tt> <tt>retainAll</tt>, and <tt>clear</tt> operations.
	 * It does not support the add or <tt>addAll</tt> operations.
	 *
	 * @return a set view of the keys contained in this map.
	 */
	public Set keySet()
	{
		return new IntIntMapKeySet();
	}


	/**
	 * Returns a set view of the mappings contained in this map.  Each element
	 * in the returned set is a (@link IntIntMap.Entry) which is a subclass of {@link java.util.Map.Entry}.
	 * The set is backed by the
	 * map, so changes to the map are reflected in the set, and vice-versa.
	 * If the map is modified while an iteration over the set is in progress,
	 * the results of the iteration are undefined.  The set supports element
	 * removal, which removes the corresponding mapping from the map, via the
	 * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not support
	 * the <tt>add</tt> or <tt>addAll</tt> operations.
	 *
	 * @return a set view of the mappings contained in this map.
	 */
	public Set entrySet()
	{
		return new IntIntMapEntrySet();
	}

	/**
	 * Returns a collection view of the values contained in this map.  The
	 * collection is backed by the map, so changes to the map are reflected in
	 * the collection, and vice-versa.  If the map is modified while an
	 * iteration over the collection is in progress, the results of the
	 * iteration are undefined.  The collection supports element removal,
	 * which removes the corresponding mapping from the map, via the
	 * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
	 * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations.
	 * It does not support the add or <tt>addAll</tt> operations.
	 *
	 * @return a collection view of the values contained in this map.
	 */
	public Collection values()
	{
		return new IntIntMapValueCollection();
	}

	/**
	 * Returns an iterator over the values in this map.  The elements are
	 * returned in no particular order (unless this set is an instance of some
	 * class that provides a guarantee).
	 *
	 * @return an iterator over the elements in this set.
	 */
	public final ValueIterator valueIterator()
	{
		return new ValueIterator();
	}

	/**
	 * Returns an iterator over the elements in this set.  The elements are
	 * returned in no particular order (unless this set is an instance of some
	 * class that provides a guarantee).
	 *
	 * <p>The returned elements are of type (@link IntIntMap.Entry) which is a subclass of (@link Map.Entry).
	 *
	 * @return an iterator over the elements in this set.
	 */
	public final EntryIterator entryIterator()
	{
		return new EntryIterator();
	}

	//-------------------------------------------------------------------------
	// protected parts
	//-------------------------------------------------------------------------

	/**
	 * resize the hash map
	 * @param newCapacity
	 */
	protected final void rehash(int newCapacity)
	{
		int[] oldKeys = fKeys;
		int[] oldValues = fValues;

		//	swap
		fCapacity  = newCapacity;
		fThreshold = calcThreshold(fThreshFactor,newCapacity);
		fKeys = new int[newCapacity];
		fValues = new int[newCapacity];
		//	fSize does not change

		for (int j=oldKeys.length-1; j>=0; j--)
		{
			int key = oldKeys[j];
			if (key==0 || key==DELETED_KEY) continue;	//	empty

			lookupForRehash(key);
			fKeys[index] = key;
			fValues[index] = oldValues[j];
		}
	}

	/**
	 * used for bulk copies from IntHashMap
	 * 0 and deleted keys ignored
	 */
	protected final int putAllInternal(int[] keys, Object[] values)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (keys[i]==0 || keys[i]==DELETED_KEY)
				continue;
			int ivalue = (values[i]==null) ? 0:((Number)values[i]).intValue();
			if (put(keys[i],ivalue) != NOT_FOUND)
				result++;
		}
		return result;
	}

	/**
	 * Copies all of the mappings from the specified arrays to this map.
	 * The effect of this call is equivalent to that
	 * of calling {@link #put(int,int) put(k, v)} on this map once
	 * for each mapping from key <tt>k</tt> to value <tt>v</tt> in the
	 * specified arrays.  The behavior of this operation is unspecified if the
	 * specified map is modified while the operation is in progress.
	 *
	 * 0 and deleted keys ignored
	 *
	 * @param keys keys to be stored in this map
	 * @param values values to be stored in this map
	 *
	 * @throws java.lang.ArrayIndexOutOfBoundsException if values is shorter than keys
	 */
	public final int putAllInternal(int[] keys, int[] values)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (keys[i]==0 || keys[i]==DELETED_KEY)
				continue;
			if (put(keys[i],values[i]) != NOT_FOUND)
				result++;
		}
		return result;
	}


	/**
	 * Removes from this map all of its elements that are contained in the
	 * specified array.
	 *
	 * 0 and deleted keys ignored.
	 *
	 * @param  keys array that defines which elements will be removed from
	 *           this map.
	 * @return the number of mappings that were actually removed, or 0.
	 * @see    #remove(java.lang.Object)
	 */
	public final int removeAllInternal(int[] keys)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (keys[i]==0 || keys[i]==DELETED_KEY)
				continue;
			if (remove(keys[i]) != NOT_FOUND)
				result++;
		}
		return result;
	}

	public boolean removeInternal(int idx) {
		if (super.removeInternal(idx)) {
			fValues[idx] = 0;
			return true;
		}
		else
			return false;
	}

    protected void appendString(StringBuffer buf, char entrySeparator, char keySeparator)
    {
        boolean any = false;
        for (int i=fKeys.length-1; i>=0; i--)
        {
            if (fKeys[i]==0 || fKeys[i]==DELETED_KEY)
                continue;
            if (any) buf.append(entrySeparator);
            buf.append(fKeys[i]);
            buf.append(keySeparator);
            buf.append(fValues[i]);
            any = true;
        }
    }

	//-------------------------------------------------------------------------
	// inner classes
	//-------------------------------------------------------------------------

	/**	an iterator that walks over the values of this map
	 * directly backed by the map
	 *
	 * returned by IntIntMap.valueIterator() and IntIntMap.values().iterator()
	 */
	public final class ValueIterator extends IntMap.IntIterator
	{
		/**	value as Integer object (created lazily)	*/
		protected Integer retained;

		/** @return the next key in the iteration		 */
		public int nextInt()
		{
			advance();
			return fValues[i];
		}

		/** @return the next key in the iteration		 */
		public Object next()
		{
			int idx = nextInt();
			if (retained==null || retained.intValue()!=idx)
				retained = new Integer(idx);
			return retained;
		}
	}

	/**	an iterator that walks over the entries in this map and
	 *  returns Map.Entry objects	*
	 *
	 * returned by IntIntMap.entryIterator() and entrySet().iterator()
	 */
	public final class EntryIterator extends IntMap.Iterator
	{
		/** @return the next entry in the iteration (and IntIntMap.Entry)		 */
		public Object next()
		{
			advance();
			return new Entry(i);
		}
	}

	/**	returned by EntryIterator
	 * */
	public final class Entry implements Map.Entry
 	{
		/**	hash map index	*/
		int i;
		/**	key as Integer (created lazily)	*/
		Integer keyRetained;
		/**	value as Integer (created lazily)	*/
		Integer valueRetained;

		protected Entry(int idx)	{ i = idx; }

		/**
		 * Returns the key corresponding to this entry.
		 *
		 * @return the key corresponding to this entry.
		 */
		public Object getKey()		{
			if (keyRetained==null) keyRetained = new Integer(getIntKey());
			return keyRetained;
		}

		/**
		 * Returns the key corresponding to this entry.
		 *
		 * @return the key corresponding to this entry.
		 */
		public int getIntKey()		{
			return (fKeys[i]==ZERO_KEY) ? 0 : fKeys[i];
		}

		/**
		 * Returns the value corresponding to this entry.  If the mapping
		 * has been removed from the backing map (by the iterator's
		 * <tt>remove</tt> operation), the results of this call are undefined.
		 *
		 * @return the value corresponding to this entry.
		 */
		public Object getValue()	{
			if (valueRetained==null) valueRetained = new Integer(getIntValue());
			return valueRetained;
		}

		/**
		 * Returns the value corresponding to this entry.  If the mapping
		 * has been removed from the backing map (by the iterator's
		 * <tt>remove</tt> operation), the results of this call are undefined.
		 *
		 * @return the value corresponding to this entry.
		 */
		public int getIntValue()	{ return fValues[i]; }

		/**
		 * Replaces the value corresponding to this entry with the specified
		 * value.  (Writes through to the map.)  The
		 * behavior of this call is undefined if the mapping has already been
		 * removed from the map (by the iterator's <tt>remove</tt> operation).
		 *
		 * @param value new value to be stored in this entry. must be a Number.
		 * @return old value corresponding to the entry.
		 *
		 * @throws java.lang.ClassCastException if the class of the specified value
		 * 	      is not java.lang.Number.
	     */
		public Object setValue(Object value)
		{
			Object result = getValue();
			if (value==null)
				fValues[i] = 0;
			else
				fValues[i] = ((Number)value).intValue();
			valueRetained = null;
			return result;
		}

		/**
		 * Replaces the value corresponding to this entry with the specified
		 * value.  (Writes through to the map.)  The
		 * behavior of this call is undefined if the mapping has already been
		 * removed from the map (by the iterator's <tt>remove</tt> operation).
		 *
		 * @param value new value to be stored in this entry.
		 * @return old value corresponding to the entry.
	     */
		public int setValue(int value)
		{
			int result = getIntValue();
			if (value!=result) {
				fValues[i] = value;
				valueRetained = null;
			}
			return result;
		}
	}


	/**	a Set view on the keys of this map
	 *
	 * returned by IntIntMap.keySet()
	 * */
	public class IntIntMapKeySet extends IntMap.KeySet implements Set
	{
		/**
		 * @see de.jose.util.map.IntIntMap#remove(java.lang.Object)
		 */
		public boolean remove(Object obj) {
			return IntIntMap.this.remove(((Number)obj).intValue()) != NOT_FOUND;
		}

		/**
		 * @throws java.lang.UnsupportedOperationException
		 */
		public boolean retainAll(Collection coll) {
//	TODO	return IntIntMap.retainAllKeys(coll);
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.jose.util.map.IntIntMap#removeAll(java.util.Collection)
		 */
		public boolean removeAll(Collection coll) {
			return IntIntMap.this.removeAll(coll) > 0;
		}

		/**
		 * @see de.jose.util.map.IntIntMap#keyIterator()
		 */
		public java.util.Iterator iterator()
		{
			return IntIntMap.this.keyIterator();
		}
	}

	/**
	 * 	a Collection view on the values in this map
	 *
	 * returned by IntIntMap.values()
	 */
	public class IntIntMapValueCollection extends IntMap.AbstractSet implements Collection
	{
		/**
		 * @see de.jose.util.map.IntHashMap#removeValue(java.lang.Object)
		 */
		public boolean remove(Object obj) {
			return IntIntMap.this.removeValue(obj);
		}

		/**
		 * @see de.jose.util.map.IntHashMap#valueIterator()
		 */
		public java.util.Iterator iterator() {
			return IntIntMap.this.valueIterator();
		}

		/**
		 * @see de.jose.util.map.IntHashMap#valueArray()
		 */
		public Object[] toArray() {
			return IntIntMap.this.valueArray((Object[]) null);
		}

		/**
		 * @see de.jose.util.map.IntHashMap#valueArray(java.lang.Object[])
		 */
		public Object[] toArray(Object[] array) {
			return IntIntMap.this.valueArray(array);
		}


		protected int[] toIntArray()
		{
			return IntIntMap.this.fValues;
		}
	}

	/**
	 * a Set view on the entries in this map
	 * elements are of type IntIntMap.Entry
	 *
	 * returned by IntIntMap.entrySet()
	 */
	public class IntIntMapEntrySet extends IntMap.AbstractSet implements Set
	{
		/**
		 * @see de.jose.util.map.IntIntMap#entryIterator()
		 */
		public java.util.Iterator iterator()
		{
			return IntIntMap.this.entryIterator();
		}
	}
}
