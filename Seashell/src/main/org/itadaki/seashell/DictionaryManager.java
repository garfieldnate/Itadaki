/*
 * Copyright (C) 2006-2007
 * Matt Francis <asbel@neosheffield.co.uk>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.itadaki.seashell;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.itadaki.seashell.edict.EdictDictionary;


/**
 * Manages a shared set of dictionaries, asynchronously providing feedback on availability
 * while indexing to interested classes
 */
public class DictionaryManager {

	/**
	 * Dictionary status listeners
	 */
	private Set<DictionaryStatusListener> listeners = new LinkedHashSet<DictionaryStatusListener>();

	/**
	 * Current dictionaries
	 */
	private Set<EdictDictionary> dictionaries = new LinkedHashSet<EdictDictionary>();


	/**
	 * Adds a dictionary status listener
	 *
	 * @param statusListener The listener to add
	 */
	public void addStatusListener (DictionaryStatusListener statusListener) {

		this.listeners.add (statusListener);

	}


	/**
	 * Returns the current dictionaries
	 *
	 * @return The current dictionaries
	 */
	public Set<EdictDictionary> getDictionaries() {

		return new LinkedHashSet<EdictDictionary> (this.dictionaries);

	}


	/**
	 * Sets the current dictionaries
	 *
	 * @param dictionaries The current dictionaries
	 */
	public void setDictionaries (final Set<EdictDictionary> dictionaries) {

		new Thread ("Dictionary Indexer") {

			@Override
			public void run() {
				synchronized (DictionaryManager.this) {
					notifyListenersUnavailable();
					ArrayList<EdictDictionary> dictionariesList = new ArrayList<EdictDictionary> (dictionaries);
					for (int i = 0; i < dictionariesList.size(); i++) {
						EdictDictionary dictionary = dictionariesList.get (i);
						if (!dictionary.hasIndex()) {
							try {
								notifyListenersIndexing (dictionary, dictionariesList.size() - i - 1);
								dictionary.createIndex();
							} catch (Exception e) {
								// TODO Something sensible
								e.printStackTrace();
							}
						}
					}
			
					DictionaryManager.this.dictionaries = dictionaries;
					notifyListenersReady();
				}
			}
			
		}.start();

	}


	/**
	 * Notify listeners that a dictionary is being indexed
	 * 
	 * @param dictionary 
	 * @param dictionariesRemaining 
	 */
	private void notifyListenersIndexing (EdictDictionary dictionary, int dictionariesRemaining) {
		for (DictionaryStatusListener listener : this.listeners) {
			listener.dictionariesIndexing (dictionary, dictionariesRemaining);
		}
	}


	/**
	 * Notify listeners that indexing has started
	 */
	private void notifyListenersUnavailable() {
		for (DictionaryStatusListener listener : this.listeners) {
			listener.dictionariesUnavailable();
		}
	}


	/**
	 * Notify listeners that indexing has successfully complete
	 */
	private void notifyListenersReady() {
		for (DictionaryStatusListener listener : this.listeners) {
			listener.dictionariesReady();
		}
	}


}
