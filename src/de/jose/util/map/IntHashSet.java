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
import java.util.Set;

/**
 * a set of ints, backed by a hash map
 *
 * -----------------------------------------------------------------------------------------------------------
 * there are two integers that must not be used as hash keys:
 * 	Integer.MIN_VALUE+1 (= -2147483647)
 * and
 * 	Integer.MIN_VALUE+2 (= -2147483646)
 *
 * any other keys can be used, including 0, MIN_VALUE and MAX_VALUE
 * -----------------------------------------------------------------------------------------------------------
 *
 * it implements java.util.Set, but whenever possible you should prefer using 'int' methods rather than 'Object' methods.
 * For example, add(5) is better than add(new Integer(5)).
 *
 * @author Peter Schäfer
 */
public class IntHashSet
        extends IntMap
        implements Set
{
	//-------------------------------------------------------------------------
	// constructors
	//-------------------------------------------------------------------------

	/**
	 * creates a new IntHashSet with the given capacity.
	 * the capacity will automatically double when the loadFactor is reached
	 * (be aware however, that this is an expensive operation; if possible,
	 *  you should set the initial capacity to about 2 times of the expected size)
	 *
	 * @param capacity the initial capacity
	 * @param loadFactor the factor when the map is rehashed (0..1)
	 */
	public IntHashSet(int capacity, float loadFactor)
	{
		super(capacity,loadFactor);
	}

	/**
	 * creates a new IntHashSet with default capacity (64) and load factor (0.8)
	 */
	public IntHashSet()
	{
		this(64, 0.8f);
	}

    public IntHashSet(Collection copy)
    {
        this();
        addAll(copy);
    }

	//-------------------------------------------------------------------------
	// basic access
	//-------------------------------------------------------------------------

	/**
	 * Adds the specified element to this set if it is not already present.
	 * More formally, adds the specified element,
	 * <code>o</code>, to this set if this set contains no element
	 * <code>e</code> such that <code>(key==e)</code>.  If this set already contains the specified
	 * element, the call leaves this set unchanged and returns <tt>false</tt>.
	 * In combination with the restriction on constructors, this ensures that
	 * sets never contain duplicate elements.<p>
	 *
	 * @param key element to be added to this set.
	 * @return <tt>true</tt> if this set did not already contain the specified
	 *         element.
	 */
	public final boolean add(int key)
	{
		if (key==0) key = ZERO_KEY;
		if (key==DELETED_KEY) return false;

		if (!lookupForInsert(key)) {	//	insert
			fKeys[index] = key;

			if (++fSize >= fThreshold)	//	increase capacity
				rehash(2*fCapacity);

			return true;
		}

		//	else: alreeady present
		return false;
	}

	/**
	 * Adds the specified element to this set if it is not already present
	 * (optional operation).  More formally, adds the specified element,
	 * <code>o</code>, to this set if this set contains no element
	 * <code>e</code> such that <code>(key==e)</code>.  If this set already contains the specified
	 * element, the call leaves this set unchanged and returns <tt>false</tt>.
	 * In combination with the restriction on constructors, this ensures that
	 * sets never contain duplicate elements.<p>
	 *
	 * @param key element to be added to this set. must be a Number.
	 * @return <tt>true</tt> if this set did not already contain the specified
	 *         element.
	 *
	 * @throws java.lang.ClassCastException if the class of the specified element
	 * 	       is not java.lang.Number.
	 * @throws java.lang.NullPointerException if the specified element is null.
	 */
	public final boolean add(Object key)
	{
		return add(((Number)key).intValue());
	}

	/**
	 * Removes the specified element from this set if it is present.
	 * More formally, removes an element <code>e</code> such that
	 * <code>(key==e)</code>, if the set contains
	 * such an element.  Returns <tt>true</tt> if the set contained the
	 * specified element (or equivalently, if the set changed as a result of
	 * the call).  (The set will not contain the specified element once the
	 * call returns.)
	 *
	 * @param key element to be removed from this set, if present.
	 * @return true if the set contained the specified element.
	 */
	public final boolean remove(int key)
	{
		if (key==0) key = ZERO_KEY;
		if (lookup(key)) {	//	remove
			removeInternal(index);
			return true;
		}
		else
			return false;
	}

	/**
	 * Removes the specified element from this set if it is present (optional
	 * operation).  More formally, removes an element <code>e</code> such that
	 * <code>(key.equals(e))</code>, if the set contains
	 * such an element.  Returns <tt>true</tt> if the set contained the
	 * specified element (or equivalently, if the set changed as a result of
	 * the call).  (The set will not contain the specified element once the
	 * call returns.)
	 *
	 * @param key element to be removed from this set, if present. must be a Number.
	 * @return true if the set contained the specified element.
	 * @throws java.lang.ClassCastException if the type of the specified element
	 * 	       is not java.lang.Number.
	 * @throws java.lang.NullPointerException if the specified element is null.
	 */
	public final boolean remove(Object key)
	{
		return remove(((Number)key).intValue());
	}

	//-------------------------------------------------------------------------
	// methods
	//-------------------------------------------------------------------------

	/**
	 * Adds all of the elements in the specified array to this set if
	 * they're not already present.  The <tt>addAll</tt> operation effectively
	 * modifies this set so that its value is the <i>union</i> of the two
	 * sets.
	 *
	 * @param keys array whose elements are to be added to this set.
	 * @return the number of elements actually added to this set.
	 *
	 * @see #add(java.lang.Object)
	 */
	public final int addAll(int[] keys)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
			if (add(keys[i])) result++;
		return result;
	}

	/**
	 * Adds all of the elements in the specified collection to this set if
	 * they're not already present.  If the specified
	 * collection is also a set, the <tt>addAll</tt> operation effectively
	 * modifies this set so that its value is the <i>union</i> of the two
	 * sets.  The behavior of this operation is unspecified if the specified
	 * collection is modified while the operation is in progress.
	 *
	 * @param keys collection of java.lang.Number objects whose elements are to be added to this set.
	 * @return <tt>true</tt> if this set changed as a result of the call.
	 *
	 * @throws java.lang.ClassCastException if the class of some element of the
	 * 		  specified collection is not java.lang.Number.
	 * @throws java.lang.NullPointerException if the specified collection contains one
	 *           or more null elements.
	 * @see #add(java.lang.Object)
	 */
	public boolean addAll(Collection keys)
	{
		if (keys instanceof IntMap) {
			//	performance shortcut for IntHashSets
			IntMap imap = (IntMap)keys;
			return addAllInternal(imap.fKeys) > 0;
		}
		if (keys instanceof IntMap.AbstractSet) {
			//	performance shortcut for IntMap Set views	*/
			IntMap.AbstractSet iset = (IntMap.AbstractSet)keys;
			return addAllInternal(iset.toIntArray()) > 0;
		}

		//	else: Set of Numbers
		boolean result = false;
		java.util.Iterator i = keys.iterator();
		while (i.hasNext())
			if (add(i.next()))
				result = true;
		return result;
	}

	/**
	 * Removes from this set all of its elements that are contained in the
	 * specified array.  This operation effectively modifies this
	 * set so that its value is the <i>asymmetric set difference</i> of
	 * the two sets.
	 *
	 * @param  keys array that defines which elements will be removed from
	 *           this set.
	 * @return the number of elements actually removed from this set
	 *
	 * @throws java.lang.UnsupportedOperationException if the <tt>removeAll</tt>
	 * 		  method is not supported by this Collection.
	 * @throws java.lang.ClassCastException if the types of one or more elements in this
	 *            set are incompatible with the specified collection
	 *            (optional).
	 * @throws java.lang.NullPointerException if this set contains a null element and
	 *            the specified collection does not support null elements
	 *            (optional).
	 * @throws java.lang.NullPointerException if the specified collection is
	 *           <tt>null</tt>.
	 * @see    #remove(java.lang.Object)
	 */
	public final int removeAll(int[] keys)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (remove(keys[i])) result++;
		}
		return result;
	}

	/**
	 * Removes from this set all of its elements that are contained in the
	 * specified collection.  If the specified
	 * collection is also a set, this operation effectively modifies this
	 * set so that its value is the <i>asymmetric set difference</i> of
	 * the two sets.
	 *
	 * @param  keys collection that defines which elements will be removed from
	 *           this set. must contain java.lang Number objects.
	 * @return <tt>true</tt> if this set changed as a result of the call.
	 *
	 * @throws java.lang.ClassCastException if the types of one or more elements in this
	 *            set are not java.lang.Number).
	 * @throws java.lang.NullPointerException if this set contains a null element.
	 * @throws java.lang.NullPointerException if the specified collection is
	 *           <tt>null</tt>.
	 * @see    #remove(java.lang.Object)
	 */
	public final boolean removeAll(Collection keys)
	{
		if (keys instanceof IntHashSet) {
			//	performance shortcut for IntHashSet
			IntHashSet iset = (IntHashSet)keys;
			return removeAllInternal(iset.fKeys) > 0;
		}
		if (keys instanceof IntMap.AbstractSet) {
			//	performance shortcur for IntMap Set views
			IntMap.AbstractSet iset = (IntMap.AbstractSet)keys;
			return removeAllInternal(iset.toIntArray()) > 0;
		}
		//	else: Set of Numbers
		boolean result = false;
		java.util.Iterator i = keys.iterator();
		while (i.hasNext())
			if (remove(i.next()))
				result = true;
		return result;
	}

	/**
	 * Retains only the elements in this set that are contained in the
	 * specified collection (optional operation).  In other words, removes
	 * from this set all of its elements that are not contained in the
	 * specified collection.  If the specified collection is also a set, this
	 * operation effectively modifies this set so that its value is the
	 * <i>intersection</i> of the two sets.
	 *
	 * <p> this method is only implemented for IntHashSets.
	 * It does not accept other types of Collections.
	 *
	 * @param keys collection that defines which elements this set will retain.
	 * 			must be of type IntHashSet
	 * @return <tt>true</tt> if this collection changed as a result of the
	 *         call.
	 * @throws java.lang.UnsupportedOperationException if the passed argument is not an IntHashSet
	 * @see #remove(java.lang.Object)
	 */
	public final boolean retainAll(Collection keys)
	{
		if (keys instanceof IntHashSet)
			return retainAll((IntHashSet)keys);
		else
			throw new UnsupportedOperationException("retainAll() accepts only an IntHashSet as parameter");
	}

	/**
	 * Retains only the elements in this set that are contained in the
	 * specified collection (optional operation).  In other words, removes
	 * from this set all of its elements that are not contained in the
	 * specified collection.  If the specified collection is also a set, this
	 * operation effectively modifies this set so that its value is the
	 * <i>intersection</i> of the two sets.
	 *
	 * <p> this method is only implemented for IntHashSets.
	 * It does not accept other types of Collections.
	 *
	 * @param set collection that defines which elements this set will retain.
	 * @return <tt>true</tt> if this collection changed as a result of the
	 *         call.
	 * @see #remove(java.lang.Object)
	 */
    public final boolean retainAll(IntHashSet set)
	{
		boolean result = false;
	    for (int i=fKeys.length-1; i>=0; i--)
	    {
		    if (fKeys[i]==0 || fKeys[i]==DELETED_KEY)
		    	continue;
		    if (!set.contains(fKeys[i])) {
		    	removeInternal(i);
			    result = true;
		    }
	    }
	    return result;
    }



	/**
	 * Returns <tt>true</tt> if this set contains all of the elements of the
	 * specified array.  This
	 * method returns <tt>true</tt> if it is a <i>subset</i> of this set.
	 *
	 * @param  keys array to be checked for containment in this set.
	 * @return <tt>true</tt> if this set contains all of the elements of the
	 * 	       specified collection.
	 * @see    #contains(java.lang.Object)
	 */
	public final boolean containsAll(int[] keys)
	{
		return containsAllKeys(keys);
	}

	/**
	 * Returns <tt>true</tt> if this set contains all of the elements of the
	 * specified collection.  If the specified collection is also a set, this
	 * method returns <tt>true</tt> if it is a <i>subset</i> of this set.
	 *
	 * @param  keys collection to be checked for containment in this set.
	 * @return <tt>true</tt> if this set contains all of the elements of the
	 * 	       specified collection.
	 * @throws java.lang.ClassCastException if the types of one or more elements
	 *         in the specified collection are not java.lang.Number.
	 * @throws java.lang.NullPointerException if the specified collection contains one
	 *         or more null elements.
	 * @throws java.lang.NullPointerException if the specified collection is
	 *         <tt>null</tt>.
	 * @see    #contains(java.lang.Object)
	 */
	public final boolean containsAll(Collection keys)
	{
		if (keys instanceof IntHashSet) {
			//	performance shortcut for IntHashSet
			IntHashSet iset = (IntHashSet)keys;
			return containsAllInternal(iset.fKeys);
		}
		if (keys instanceof IntMap.AbstractSet) {
			//	performance shortcut for IntMap Set views
			IntMap.AbstractSet iset = (IntMap.AbstractSet)keys;
			return containsAllInternal(iset.toIntArray());
		}
		//	else: Collection of Numbers
		java.util.Iterator i = keys.iterator();
		while (i.hasNext())
			if (!contains(i.next()))
				return false;
		return true;
	}

	//-------------------------------------------------------------------------
	// methods
	//-------------------------------------------------------------------------

	/**
	 * Get an array of all values int this set
	 *
	 * @return an array containing all values
	 */
	public final int[] toIntArray()
	{
		return super.keys();
	}

	/**
	 * Get an array of all values int this set.
	 * The values will be placed in the supplied array, if it is big enough.
	 * If the supplied result array is too small, a new array will be allocated and returned.
	 *
	 * @param result the array which will hold the values on return; may be null
	 * @return an array containing all values
	 */
	public final int[] toIntArray(int[] result)
	{
		return super.keys(result);
	}

	/**
	 * Returns an array containing all of the elements in this set.
	 * Obeys the general contract of the <tt>Collection.toArray</tt> method.
	 *
	 * @return an array containing all of the elements in this set (which are all of type Integer).
	 */
	public final Object[] toArray()
	{
		return toArray(null);
	}

	/**
	 * Returns an array containing all of the elements in this set; the
	 * runtime type of the returned array is that of the specified array.
	 * Obeys the general contract of the
	 * <tt>Collection.toArray(Object[])</tt> method.
	 *
	 * @param result the array into which the elements of this set are to
	 *		be stored, if it is big enough; otherwise, a new array of the
	 * 		same runtime type is allocated for this purpose.
	 * @return an array containing the elements of this set.
	 * @throws    java.lang.ArrayStoreException the runtime type of 'result' is not java.lang.Integer or a superclass of it.
	 * @throws java.lang.NullPointerException if the specified array is <tt>null</tt>.
	 */
	public final Object[] toArray(Object[] result)
	{
		return super.keys(result);
	}

	/**
	 * Returns an iterator over the elements in this set.  The elements are
	 * returned in no particular order (unless this set is an instance of some
	 * class that provides a guarantee).
	 *
	 * <p>This method implements the java.util.Set interface.
	 * In practice, you should prefer using (@link IntMap#keyIterator()) instead, because
	 * IntMap.IntIterator has an additional nextInt() method.
	 *
	 * @return an iterator over the elements in this set.
	 */
	public final java.util.Iterator iterator()
	{
		return keyIterator();
	}

	//-------------------------------------------------------------------------
	// private parts
	//-------------------------------------------------------------------------

	/**
	 * resize the hash map
	 * @param newCapacity
	 */
	protected final void rehash(int newCapacity)
	{
		int[] oldKeys = fKeys;

		//	swap
		fCapacity  = newCapacity;
		fThreshold = calcThreshold(fThreshFactor,newCapacity);
		fKeys = new int[newCapacity];
		//	fSize does not change

		for (int j=oldKeys.length-1; j>=0; j--)
		{
			int key = oldKeys[j];
			if (key==0 || key==DELETED_KEY) continue;	//	empty

			lookupForRehash(key);
			fKeys[index] = key;
		}
	}

	/**
	 * Removes from this set all of its elements that are contained in the
	 * specified array.  This operation effectively modifies this
	 * set so that its value is the <i>asymmetric set difference</i> of
	 * the two sets.
	 *
	 * 0 and deleted keys ignored.
	 *
	 * @param  keys array that defines which elements will be removed from
	 *           this set.
	 * @return the number of elements actually removed from this set
	 *
	 * @throws java.lang.UnsupportedOperationException if the <tt>removeAll</tt>
	 * 		  method is not supported by this Collection.
	 * @throws java.lang.ClassCastException if the types of one or more elements in this
	 *            set are incompatible with the specified collection
	 *            (optional).
	 * @throws java.lang.NullPointerException if this set contains a null element and
	 *            the specified collection does not support null elements
	 *            (optional).
	 * @throws java.lang.NullPointerException if the specified collection is
	 *           <tt>null</tt>.
	 * @see    #remove(java.lang.Object)
	 */
	public final int removeAllInternal(int[] keys)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (keys[i]==0 || keys[i]==DELETED_KEY)
				continue;
			if (remove(keys[i])) result++;
		}
		return result;
	}


	/**
	 * Adds all of the elements in the specified array to this set if
	 * they're not already present.  The <tt>addAll</tt> operation effectively
	 * modifies this set so that its value is the <i>union</i> of the two
	 * sets.
	 *
	 * 0 and deleted keys ignored
	 *
	 * @param keys array whose elements are to be added to this set.
	 * @return the number of elements actually added to this set.
	 *
	 * @see #add(java.lang.Object)
	 */
	public final int addAllInternal(int[] keys)
	{
		int result = 0;
		for (int i=keys.length-1; i>=0; i--)
		{
			if (keys[i]==0 || keys[i]==DELETED_KEY)
				continue;
			if (add(keys[i])) result++;
		}
		return result;
	}
	/**
	 * Returns <tt>true</tt> if this set contains all of the elements of the
	 * specified array.  This
	 * method returns <tt>true</tt> if it is a <i>subset</i> of this set.
	 *
	 * 0 and deleted keys ignored
	 *
	 * @param  keys array to be checked for containment in this set.
	 * @return <tt>true</tt> if this set contains all of the elements of the
	 * 	       specified collection.
	 * @see    #contains(java.lang.Object)
	 */
	public final boolean containsAllInternal(int[] keys)
	{
		for (int i = keys.length - 1; i >= 0; i--)
		{
			if (keys[i]==0 || keys[i]==DELETED_KEY)
				continue;
			if (!containsKey(keys[i]))
				return false;
		}
		return true;
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
            any = true;
        }
    }

}
