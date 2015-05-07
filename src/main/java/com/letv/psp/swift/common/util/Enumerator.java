package com.letv.psp.swift.common.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * HttpHandler class that wraps an <code>Enumeration</code> around a Java2
 * collection classes object <code>Iterator</code> so that existing APIs
 * returning Enumerations can easily run on top of the new collections.
 * Constructors are provided to easily create such wrappers.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2007/06/18 14:17:08 $
 */

public final class Enumerator<E> implements Enumeration<E> {


    // ----------------------------------------------------------- Constructors


    /**
     * Return an Enumeration over the values of the specified Collection.
     *
     * @param collection Collection whose values should be enumerated
     */
    public Enumerator(Collection<E> collection) {

        this(collection.iterator());

    }


    /**
     * Return an Enumeration over the values of the specified Collection.
     *
     * @param collection Collection whose values should be enumerated
     * @param clone true to clone iterator
     */
    public Enumerator(Collection<E> collection, boolean clone) {

        this(collection.iterator(), clone);

    }


    /**
     * Return an Enumeration over the values returned by the
     * specified {@link Iterable}.
     *
     * @param iterable {@link Iterable} to be wrapped
     */
    public Enumerator(Iterable<E> iterable) {
        this(iterable.iterator());
    }


    /**
     * Return an Enumeration over the values returned by the
     * specified {@link Iterable}.
     *
     * @param iterable {@link Iterable} to be wrapped
     * @param clone true to clone iterator
     */
    public Enumerator(Iterable<E> iterable, boolean clone) {
        this(iterable.iterator(), clone);
    }


    /**
     * Return an Enumeration over the values returned by the
     * specified Iterator.
     *
     * @param iterator Iterator to be wrapped
     */
    public Enumerator(Iterator<E> iterator) {

        this.iterator = iterator;

    }


    /**
     * Return an Enumeration over the values returned by the
     * specified Iterator.
     *
     * @param iterator Iterator to be wrapped
     * @param clone true to clone iterator
     */
    public Enumerator(Iterator<E> iterator, boolean clone) {

        super();
        if (!clone) {
            this.iterator = iterator;
        } else {
            List<E> list = new ArrayList<E>();
            while (iterator.hasNext()) {
                list.add(iterator.next());
            }
            this.iterator = list.iterator();
        }

    }

    
    /**
     * Return an Enumeration over the values of the specified Map.
     *
     * @param map Map whose values should be enumerated
     */
    public Enumerator(Map<?, E> map) {

        this(map.values().iterator());

    }


    /**
     * Return an Enumeration over the values of the specified Map.
     *
     * @param map Map whose values should be enumerated
     * @param clone true to clone iterator
     */
    public Enumerator(Map<?, E> map, boolean clone) {

        this(map.values().iterator(), clone);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The <code>Iterator</code> over which the <code>Enumeration</code>
     * represented by this class actually operates.
     */
    private Iterator<E> iterator = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Tests if this enumeration contains more elements.
     *
     * @return <tt>true</tt> if and only if this enumeration object
     *  contains at least one more element to provide, <tt>false</tt>
     *  otherwise
     */
    public boolean hasMoreElements() {

        return (iterator.hasNext());

    }


    /**
     * Returns the next element of this enumeration if this enumeration
     * has at least one more element to provide.
     *
     * @return the next element of this enumeration
     *
     * @exception NoSuchElementException if no more elements exist
     */
    public E nextElement() throws NoSuchElementException {

        return (iterator.next());

    }


}
