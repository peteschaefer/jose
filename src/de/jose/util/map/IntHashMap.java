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
import java.util.Map;
import java.util.Set;

/**
 * a hash map that maps int keys to Object values
 *
 * -----------------------------------------------------------------------------------------------------------
 * any objects can be used as values, including null
 *
 * there are two integers that must not be used as hash keys:
 * 	Integer.MIN_VALUE+1 (= -2147483647)
 * and
 * 	Integer.MIN_VALUE+2 (= -2147483646)
 *
 * any other keys can be used, including 0, MIN_VALUE and MAX_VALUE
 * -----------------------------------------------------------------------------------------------------------
 *
 * it implements java.util.Map, but whenever possible you should prefer using 'int' methods rather than 'Object' methods.
 * For example, put(5,..) is better than put(new Integer(5),..).
 *
 * @author Peter Schäfer
 */

public class IntHashMap
        	extends IntMap
        	implements Map
{

	//-------------------------------------------------------------------------
	// variables
	//-------------------------------------------------------------------------

	/**	the value list	*
	 * each value is associated with an entry in fKeys
	 */
	protected Object[]	fValues;


	//-------------------------------------------------------------------------
	// constructors
	//-------------------------------------------------------------------------

	/**
	 * creates a new IntHashMap with the given capacity.
	 * the capacity will automatically double when the loadFactor is reached
	 * (be aware however, that this is an expensive operation; if possible,
	 *  you should set the initial capacity to about 2 times of the expected size)
	 *
	 * @param capacity the initial capacity
	 * @param loadFactor factor when the capacity increases automatically (0..1)
	 */
	public IntHashMap(int capacity, float loadFactor)
	{
		super(capacity,loadFactor);
		fValues = new Object[fCapacity];
	}

	/**
	 * creates a new IntIntMap with default capacity (64)
	 * and load factor (0.8)
	 */
	public IntHashMap()
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
	 * @return previous value associated with specified key, or <tt>null</tt>
	 *	       if there was no mapping for key.  A <tt>null</tt> return can
	 *	       also indicate that the map previously associated <tt>null</tt>
	 *	       with the specified key, if the implementation supports
	 *	       <tt>null</tt> values.
	 */
	public final Object put(int key, Object value)
	{
		if (key==0) key = ZERO_KEY;
		if (key==DELETED_KEY) return null;

		if (!lookupForInsert(key)) {		//	insert
			fKeys[index] = key;
			fValues[index] = value;

			if (++fSize >= fThreshold)		//	increase capacity
				rehash(2*fCapacity);

			return null;
		}

		//	else: overwrite
		Object result = fValues[index];
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
	 * this method implements the java.util.Map interface.
	 * Whenever possible you should use put(int,Object) instead.
	 *
	 * @param key key with which the specified value is to be associated. must be a Number.
	 * @param value value to be associated with the specified key.
	 * @return previous value associated with specified key, or <tt>null</tt>
	 *	       if there was no mapping for key.  A <tt>null</tt> return can
	 *	       also indicate that the map previously associated <tt>null</tt>
	 *	       with the specified key, if the implementation supports
	 *	       <tt>null</tt> values.
	 *
	 * @throws java.lang.ClassCastException if the class of the specified key is not java.lang.Number.
	 */
	public final Object put(Object key, Object value)
	{
		return put(((Number)key).intValue(), value);
	}

	/**
	 * Returns the value to which this map maps the specified key.  Returns
	 * <tt>null</tt> if the map contains no mapping for this key.  A return
	 * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
	 * map contains no mapping for the key; it's also possible that the map
	 * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
	 * operation may be used to distinguish these two cases.
	 *
	 * <p>More formally, if this map contains a mapping from a key
	 * <tt>k</tt> to a value <tt>v</tt> such that <tt>(key==null ? k==null :
	 * key.equals(k))</tt>, then this method returns <tt>v</tt>; otherwise
	 * it returns <tt>null</tt>.  (There can be at most one such mapping.)
	 *
	 * @param key key whose associated value is to be returned.
	 * @return the value to which this map maps the specified key, or
	 *	       <tt>null</tt> if the map contains no mapping for this key.
	 *
	 * @see #containsKey(java.lang.Object)
	 */
	public final Object get(int key)
	{
		if (key==0) key = ZERO_KEY;
		if (lookup(key))
			return fValues[index];
		else
			return null;
	}

	/**
	 * Returns the value to which this map maps the specified key.  Returns
	 * <tt>null</tt> if the map contains no mapping for this key.  A return
	 * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
	 * map contains no mapping for the key; it's also possible that the map
	 * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
	 * operation may be used to distinguish these two cases.
	 *
	 * <p>More formally, if this map contains a mapping from a key
	 * <tt>k</tt> to a value <tt>v</tt> such that <tt>(key==null ? k==null :
	 * key.equals(k))</tt>, then this method returns <tt>v</tt>; otherwise
	 * it returns <tt>null</tt>.  (There can be at most one such mapping.)
	 *
	 * this method implements the java.util.Map interface.
	 * Whenever possible you should use get(int) instead.
	 *
	 * @param key key whose associated value is to be returned. must be a Number.
	 * @return the value to which this map maps the specified key, or
	 *	       <tt>null</tt> if the map contains no mapping for this key.
	 *
	 * @throws java.lang.ClassCastException if the key is not of type java.lang.Number.
	 *
	 * @see #containsKey(java.lang.Object)
	 */
	public final Object get(Object key) {
		return get(((Number)key).intValue());
	}

    /**
     * Removes the mapping for this key from this map if it is present.
     * More formally, if this map contains a mapping
     * from key <tt>k</tt> to value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
     * is removed.  (The map can contain at most one such mapping.)
     *
     * <p>Returns the value to which the map previously associated the key, or
     * <tt>null</tt> if the map contained no mapping for this key.  (A
     * <tt>null</tt> return can also indicate that the map previously
     * associated <tt>null</tt> with the specified key if the implementation
     * supports <tt>null</tt> values.)  The map will not contain a mapping for
     * the specified  key once the call returns.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.
     */
    public final Object remove(int key)
	{
		if (key==0) key = ZERO_KEY;
	    if (key==DELETED_KEY) return null;

		if (lookup(key)) {
			//	remove
			Object result = fValues[index];
			removeInternal(index);
			return result;
		}
		else {
			return null;
		}
	}

    /**
     * Removes the mapping for this key from this map if it is present.
     * More formally, if this map contains a mapping
     * from key <tt>k</tt> to value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
     * is removed.  (The map can contain at most one such mapping.)
     *
     * <p>Returns the value to which the map previously associated the key, or
     * <tt>null</tt> if the map contained no mapping for this key.  (A
     * <tt>null</tt> return can also indicate that the map previously
     * associated <tt>null</tt> with the specified key if the implementation
     * supports <tt>null</tt> values.)  The map will not contain a mapping for
     * the specified  key once the call returns.
     *
     * this method implements the java.util.Map interface.
     * Whenever possible you should use remove(int) instead.
     *
     * @param key key whose mapping is to be removed from the map. must be a Number.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.
     *
     * @throws java.lang.ClassCastException if the key is not of type java.lang.Number.
     */
    public final Object remove(Object key)
	{
		return remove(((Number)key).intValue());
	}

	//-------------------------------------------------------------------------
	// methods
	//-------------------------------------------------------------------------

	/**
	 * Removes all mappings to the given value.
	 * Values are compared with the equals() method.
	 *
	 * <p>Returns the number of mappings that were actually deleted from the map;
	 * or 0 if no mapping was deleted.
	 *
	 * @param value an mapping value, or null
	 * @return the number of affected mappings, or 0 if the value was not present in the map
	 */
	public final int removeValue(Object value)
	{
		int result = 0;
		if (value!=null) {
			for (int i=fKeys.length-1; i>=0; i--)
			{
				if (fKeys[i]==0 || fKeys[i]==DELETED_KEY)
					continue;
				if (value.equals(fValues[i])) {
					removeInternal(i);
					result++;
				}
			}
		}
		else {
			for (int i=fKeys.length-1; i>=0; i--)
			{
				if (fKeys[i]==0 || fKeys[i]==DELETED_KEY)
					continue;
				if (fValues[i]==null) {
					removeInternal(i);
					result++;
				}
			}
		}
		return result;
	}

	/**
	 * Removes all mappings to the given values.
	 * Values are compared with the equals() method.
	 *
	 * <p>Returns the number of mappings that were actually deleted from the map;
	 * or 0 if no mapping was deleted.
	 *
	 * @param coll a Collection of objects
	 * @return the number of affected mappings, or 0 if the value was not present in the map
	 */
	public final int removeAllValues(Collection coll)
	{
		int result = 0;
		for (int i=fKeys.length-1; i>=0; i--)
		{
			if (fKeys[i]==0 || fKeys[i]==DELETED_KEY)
				continue;
			Object value = fValues[i];
			if (coll.contains(value)) {
				removeInternal(i);
				result++;
			}
		}
		return result;
	}

	/**
	 * Removes all mappings that are NOT contained in the given collection.
	 * Values are compared with the equals() method.
	 *
	 * @param coll a Collection of objects
	 * @return the number of affected mappings, or 0 if the value was not present in the map
	 */
	public final int retainAllValues(Collection coll)
	{
		int result = 0;
		for (int i=fKeys.length-1; i>=0; i--)
		{
			if (fKeys[i]==0 || fKeys[i]==DELETED_KEY)
				continue;
			Object value = fValues[i];
			if (! coll.contains(value)) {
				removeInternal(i);
				result++;
			}
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
	 * <p>The map is supposed to be an IntHashMap, and IntIntMap, or a Map
	 * that contains java.lang.Number objects as keys.
	 *
	 * @param map Mappings to be stored in this map.
	 *
	 * @throws java.lang.ClassCastException if the class of a key is not java.lang.Number.
	 */
	public final void putAll(Map map)
	{
		if (map instanceof IntHashMap) {
			//	performance shortcut for IntHashMaps
			IntHashMap imap = (IntHashMap)map;
			putAllInternal(imap.fKeys,imap.fValues);
			return;
		}
		if (map instanceof IntIntMap) {
			//	performance shortcut for IntIntMaps
			IntIntMap imap = (IntIntMap)map;
			putAllInternal(imap.fKeys,imap.fValues);
			return;
		}
		//	else Object map, got to iterate
		java.util.Iterator i = map.entrySet().iterator();
		while (i.hasNext())
		{
			Map.Entry ety = (Map.Entry)i.next();
			put(ety.getKey(),ety.getValue());
		}
	}


	/**
	 * Removes from this set all of its elements that are contained in the
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
	public final int removeAll(Collection coll) {
		if (coll instanceof IntMap) {
			//	performance shortcut for IntMaps
			return removeAllInternal(((IntMap)coll).fKeys);
		}

		int result = 0;
		java.util.Iterator i = coll.iterator();
		while (i.hasNext())
			if (remove(((Number)i.next()).intValue()) != null)
				result++;
		return result;
	}

	/**
	 * shortcut for adding all values from an IntIntMap
	 */
 	private final int putAll(int[] keys, int[] values)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (put(keys[i], new Integer(values[i])) != null)
				result++;
		}
		return result;
	}


	/**
	  * Removes from this map all of its elements that are contained in the
	  * specified list of keys.
	  *
	  * @param  keys a list of keys
	  * @return the number of mappings that were actually removed, or 0.
	  *
	  * @see    #remove(java.lang.Object)
	  */
	public final int removeAll(int[] keys)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (remove(keys[i]) != null)
				result++;
		}
		return result;
	}

	/**
	  * Copies all of the mappings from the specified map to this map.
	  *  The effect of this call is equivalent to that
	  * of calling {@link #put(java.lang.Object,java.lang.Object) put(k, v)} on this map once
	  * for each mapping from key <tt>k</tt> to value <tt>v</tt> in the
	  * specified map.
	  *
	  *  0 and deleted keys ignored
	  *
	  * @param keys a list of keys
	  * @param values a list of associated values (which must be of the same length as keys!)
	  *
	  * @throws java.lang.ArrayIndexOutOfBoundsException if values is shorted than keys.
	  */
 	public final int putAll(int[] keys, Object[] values)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (put(keys[i],values[i]) != null) result++;
		}
		return result;
	}

	/**
	   * Removes all of the elements from this map.
	   * This map will be empty after this call returns.
	   */
	public final void clear()
	{
		super.clear();
		Arrays.fill(fValues,null);
	}


	/**
	 * Returns an array containing all of the values in this map.
	 * Obeys the general contract of the <tt>Collection.toArray</tt> method.
	 *
	 * @return an array containing all of the elements in this set.
	 */
	public final Object[] valueArray()
	{
		return valueArray(null);
	}

	/**
	 * Returns an array containing all of the values in this map; the
	 * runtime type of the returned array is that of the specified array.
	 * Obeys the general contract of the
	 * <tt>Collection.toArray(Object[])</tt> method.
	 *
	 * @param result the array into which the values of this map are to
	 *		be stored, if it is big enough; otherwise, a new array of the
	 * 		same runtime type is allocated for this purpose.
	 * @return an array containing the values of this map.
	 * @throws    java.lang.ArrayStoreException the runtime type of a is not a supertype
	 *            of the runtime type of every element in this set.
	 */
	public final Object[] valueArray(Object[] result)
	{
		if (result==null) {
			result = new Object[size()];
		} else if (result.length < size()) {
			Class clazz = result.getClass().getComponentType();
			result = (Object[])Array.newInstance(clazz,size());
		}

		int j=0;
		for (int i=fKeys.length-1; i>=0 && j<fSize; i--)
			if (fKeys[i] != 0 && fKeys[i]!=DELETED_KEY)
				result[j++] = fValues[i];
		return result;
	}


	/**
	 * Returns <tt>true</tt> if this map maps one or more keys to the
	 * specified value.  More formally, returns <tt>true</tt> if and only if
	 * this map contains at least one mapping to a value <tt>v</tt> such that
	 * <tt>(value==null ? v==null : value.equals(v))</tt>.  This operation
	 * will probably require time linear in the map size for most
	 * implementations of the <tt>Map</tt> interface.
	 *
	 * @param value value whose presence in this map is to be tested.
	 * @return <tt>true</tt> if this map maps one or more keys to the
	 *         specified value.
	 */
	public final boolean containsValue(Object value)
	{
		if (value!=null) {
			for (int i=fKeys.length-1; i>=0; i--)
			{
				if (fKeys[i]==0 || fKeys[i]==DELETED_KEY)
					continue;
				if (value.equals(fValues[i]))
					return true;
			}
		}
		else {
			for (int i=fKeys.length-1; i>=0; i--)
			{
				if (fKeys[i]==0 || fKeys[i]==DELETED_KEY)
					continue;
				if (fValues[i]==null)
					return true;
			}
		}
		return false;
	}

	//-------------------------------------------------------------------------
	// Views
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
		return new IntHashMapKeySet();
	}

	/**
	 * Returns a set view of the mappings contained in this map.  Each element
	 * in the returned set is a {@link java.util.Map.Entry}.  The set is backed by the
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
		return new IntHashMapEntrySet();
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
		return new IntHashMapValueCollection();
	}

	/**
	 * Returns an iterator over the values in this map.  The elements are
	 * returned in no particular order.
	 *
	 * @return an iterator over the values in this map.
	 */
	public final ValueIterator valueIterator()
	{
		return new ValueIterator();
	}

	/**
	 * Returns an iterator over the values in this map.  The elements are
	 * returned in no particular order.
	 *
	 * Each element in the returned iterator is a (@link IntHashMap.Entry),
	 * which is a subclass of {@link java.util.Map.Entry}.
	 *
	 * @return an iterator over the values in this map.
	 */
	public final EntryIterator entryIterator()
	{
		return new EntryIterator();
	}


	//-------------------------------------------------------------------------
	// protecetd parts
	//-------------------------------------------------------------------------

	/**
	 * resize the hash map
	 * @param newCapacity the new capacity
	 */
	protected final void rehash(int newCapacity)
	{
		int[] oldKeys = fKeys;
		Object[] oldValues = fValues;

		//	swap
		fCapacity  = newCapacity;
		fThreshold = calcThreshold(fThreshFactor,newCapacity);
		fKeys = new int[newCapacity];
		fValues = new Object[newCapacity];
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
	 * shortcut for adding all values from an IntIntMap
	 *
	 * 0 and deleted keys ignored
	 */
 	private final int putAllInternal(int[] keys, int[] values)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (keys[i]==0 || keys[i]==DELETED_KEY)
				continue;
			if (put(keys[i], new Integer(values[i])) != null)
				result++;
		}
		return result;
	}


	/**
	  * Removes from this map all of its elements that are contained in the
	  * specified list of keys.
	  *
	  * 0 and deleted keys ignored
	  *
	  * @param  keys a list of keys
	  * @return the number of mappings that were actually removed, or 0.
	  *
	  * @see    #remove(java.lang.Object)
	  */
	public final int removeAllInternal(int[] keys)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (keys[i]==0 || keys[i]==DELETED_KEY)
				continue;
			if (remove(keys[i]) != null)
				result++;
		}
		return result;
	}

	/**
	  * Copies all of the mappings from the specified map to this map.
	  *  The effect of this call is equivalent to that
	  * of calling {@link #put(java.lang.Object,java.lang.Object) put(k, v)} on this map once
	  * for each mapping from key <tt>k</tt> to value <tt>v</tt> in the
	  * specified map.
	  *
	  *  0 and deleted keys ignored
	  *
	  * @param keys a list of keys
	  * @param values a list of associated values (which must be of the same length as keys!)
	  *
	  * @throws java.lang.ArrayIndexOutOfBoundsException if values is shorted than keys.
	  */
 	public final int putAllInternal(int[] keys, Object[] values)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (keys[i]==0 || keys[i]==DELETED_KEY)
				continue;
			if (put(keys[i],values[i]) != null) result++;
		}
		return result;
	}

	/**
	 * actual removal
	 * @param idx index in the hash array
	 * @return
	 */
	protected boolean removeInternal(int idx)
	{
		if (super.removeInternal(idx)) {
			fValues[idx] = null;
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

	/**
	 * the following set and iterator classes are used to implmemt the java.util.Map interface
	 * however, some methods are not supported, for example keySet().retainAll()
	 */

	/**	an Iterator that walks over the values in the map
	 * 	it is directly backed by the map.
	 *
	 * 	returned by valueIterator() and values().iterator()
	 * */
	public class ValueIterator extends IntMap.Iterator
	{
		/** @return the next value in the iteration		 */
		public Object next()
		{
			advance();
			return fValues[i];
		}
	}


	/**	an Iterator that walks over the entries in this map.
	 * 	returned objects are of type IntHashMap.Entry which is a subclass of Map.Entry.
	 * 	the iterator is directly backed by the map.
	 *
	 * returned by entryInterator() and entrySet().iterator();
	 * */
	public class EntryIterator extends IntMap.Iterator
	{
		/** @return the next entry in the iteration	 (an IntHashMap.Entry object)	 */
		public Object next()
		{
			advance();
			return new Entry(i);
		}
	}

	/**	an Iterator that walks over the entries in this map.
	 * 	it is directly backed by the map.
	 *
	 * 	returned by entryIterator() and entrySet().iterator()
	 * */
	public class Entry implements Map.Entry
 	{
		/**	map index	*/
		int i;
		/**	the key as Integer object; created lazily	*/
		Integer keyRetained;

		protected Entry(int idx)		{ i = idx; }

		/**
		 * Returns the key corresponding to this entry.
		 *
		 * @return the key corresponding to this entry.
		 */
		public int getIntKey()		{
			return (fKeys[i]==ZERO_KEY) ? 0 : fKeys[i];
		}

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
		 * Returns the value corresponding to this entry.  If the mapping
		 * has been removed from the backing map (by the iterator's
		 * <tt>remove</tt> operation), the results of this call are undefined.
		 *
		 * @return the value corresponding to this entry.
		 */
		public Object getValue()	{ return fValues[i]; }

		/**
		 * Replaces the value corresponding to this entry with the specified
		 * value.  (Writes through to the map.)  The
		 * behavior of this call is undefined if the mapping has already been
		 * removed from the map (by the iterator's <tt>remove</tt> operation).
		 *
		 * @param value new value to be stored in this entry.
		 * @return old value corresponding to the entry.
		 *
	     */
		public Object setValue(Object value)
		{
			Object result  = getValue();
			fValues[i] = value;
			return result;
		}
	}


	/**	a Set view on the keys of this map
	 * 	it is directly backed by the map
	 *
	 * returned by IntHashMap.keySet()
	 */
	public class IntHashMapKeySet extends IntMap.KeySet implements Set
	{
		/**
		 * @see de.jose.util.map.IntHashMap#remove(java.lang.Object)
		 */
		public boolean remove(Object obj) {
			return IntHashMap.this.remove(((Number)obj).intValue()) != null;
		}

		/**
		 * @throws java.lang.UnsupportedOperationException
		 */
		public boolean retainAll(Collection coll) {
//	TODO	return IntHashMap.retainAllKeys(coll);
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.jose.util.map.IntHashMap#removeAll(java.util.Collection)
		 */
		public boolean removeAll(Collection coll) {
			return IntHashMap.this.removeAll(coll) > 0;
		}

		/**
		 * @see de.jose.util.map.IntHashMap#keyIterator()
		 */
		public java.util.Iterator iterator()
		{
			return IntHashMap.this.keyIterator();
		}
	}

	/**
	 * a Collection view on the values in this map
	 * 	it is directly backed by the map
	 *
	 * returned by IntHashMap.values()
	 */
	public class IntHashMapValueCollection extends IntMap.AbstractSet implements Collection
	{
		/**
		 * @see de.jose.util.map.IntHashMap#removeValue(java.lang.Object)
		 */
		public boolean remove(Object obj) {
			return IntHashMap.this.removeValue(obj) > 0;
		}

		/**
		 * @see de.jose.util.map.IntHashMap#removeAllValues(java.util.Collection)
		 */
		public boolean removeAll(Collection coll) {
			return IntHashMap.this.removeAllValues(coll) > 0;
		}

		/**
		 * @see de.jose.util.map.IntHashMap#retainAllValues(java.util.Collection)
		 */
		public boolean retainAll(Collection coll) {
			return IntHashMap.this.retainAllValues(coll) > 0;
		}

		/**
		 * @see de.jose.util.map.IntHashMap#valueIterator()
		 */
		public java.util.Iterator iterator() {
			return IntHashMap.this.valueIterator();
		}

		/**
		 * @see de.jose.util.map.IntHashMap#valueArray()
		 */
		public Object[] toArray() {
			return IntHashMap.this.valueArray((Object[]) null);
		}

		/**
		 * @see de.jose.util.map.IntHashMap#valueArray(java.lang.Object[])
		 */
		public Object[] toArray(Object[] array) {
			return IntHashMap.this.valueArray(array);
		}
	}

	/**
	 * a Set view on the entries in this map
	 * 	it is directly backed by the map
	 *
	 * returned by IntHashMap.entrySet()
	 */
	public class IntHashMapEntrySet extends IntMap.AbstractSet implements Set
	{
		/**
		 * @see de.jose.util.map.IntHashMap#entryIterator()
		 */
		public java.util.Iterator iterator()
		{
			return IntHashMap.this.entryIterator();
		}
	}

}
