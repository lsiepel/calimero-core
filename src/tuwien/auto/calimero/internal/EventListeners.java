/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2006, 2015 B. Malinowsky

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    Linking this library statically or dynamically with other modules is
    making a combined work based on this library. Thus, the terms and
    conditions of the GNU General Public License cover the whole
    combination.

    As a special exception, the copyright holders of this library give you
    permission to link this library with independent modules to produce an
    executable, regardless of the license terms of these independent
    modules, and to copy and distribute the resulting executable under terms
    of your choice, provided that you also meet, for each linked independent
    module, the terms and conditions of the license of that module. An
    independent module is a module which is not derived from or based on
    this library. If you modify this library, you may extend this exception
    to your version of the library, but you are not obligated to do so. If
    you do not wish to do so, delete this exception statement from your
    version.
*/

package tuwien.auto.calimero.internal;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;

/**
 * Container for keeping event listeners.
 * <p>
 * The assumption for implementation of this class is that iterating over event listeners
 * is the predominant operation, adding and removing listeners not.
 *
 * @author B. Malinowsky
 */
public class EventListeners<T extends EventListener>
{
	private final List<T> listeners = new ArrayList<>();
	private List<T> listenersCopy;
	private final Logger logger;


	/**
	 * Creates a new event listeners container object.
	 *
	 * @param logger optional logger for log output
	 */
	public EventListeners(final Logger logger)
	{
		this.logger = logger;
		createCopy();
	}

	/**
	 * Creates a new event listeners container object.
	 */
	public EventListeners()
	{
		this.logger = null;
		createCopy();
	}

	/**
	 * Adds the specified event listener <code>l</code> to this container.
	 * <p>
	 * If <code>l</code> is
	 * <code>null</code> or was already added as listener, no action is performed.
	 *
	 * @param l the listener to add
	 */
	public void add(final T l)
	{
		if (l == null)
			return;
		synchronized (listeners) {
			if (!listeners.contains(l)) {
				listeners.add(l);
				createCopy();
			}
			else if (logger != null)
				logger.warn("event listener already registered");
		}
	}

	/**
	 * Removes the specified event listener <code>l</code> from this container.
	 * <p>
	 * If <code>l</code> was not added in the first place, no action is performed.
	 *
	 * @param l the listener to remove
	 */
	public void remove(final T l)
	{
		synchronized (listeners) {
			if (listeners.remove(l))
				createCopy();
		}
	}

	/**
	 * Removes all event listeners from this container.
	 */
	public void removeAll()
	{
		synchronized (listeners) {
			listeners.clear();
			createCopy();
		}
	}

	/**
	 * Returns a list of all event listeners. Trying to modify the returned list will have no impact
	 * on the event listeners kept by this container. It might also fail if the returned list is
	 * unmodifiable.
	 *
	 * @return list of all event listeners in this container, with list size equal to the number of
	 *         currently maintained listeners
	 */
	public List<T> listeners()
	{
		return listenersCopy;
	}

	public void fire(final Consumer<? super T> c)
	{
		final List<T> list = listeners();
		for (final T l : list) {
			try {
				c.accept(l);
			}
			catch (final RuntimeException rte) {
				remove(l);
				logger.error("removed event listener", rte);
			}
		}
	}

	List<T> listenersList()
	{
		return listenersCopy;
	}

	private void createCopy()
	{
		listenersCopy = new ArrayList<>(listeners);
	}
}
