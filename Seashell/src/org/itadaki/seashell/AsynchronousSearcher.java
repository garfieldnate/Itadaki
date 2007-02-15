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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.itadaki.seashell.edict.EdictDictionary;
import org.itadaki.seashell.edict.EdictSearchResults;
import org.itadaki.seashell.edict.EdictSearcher;


/**
 * Asynchronous dictionary searcher
 */
public class AsynchronousSearcher extends Thread {

	/**
	 * Default number of search results per block to aim for
	 */
	private static final int DEFAULT_SEARCH_CHUNK = 1000;

	/**
	 * Queue of searches
	 * Inherently thread safe
	 */
	private BlockingQueue<String> searchQueue = new LinkedBlockingQueue<String>();

	/**
	 * Listeners for the search results
	 * Thread safe by virtue of Collections.synchronizedMap()
	 */
	private Map<SearchListener, Integer> listeners = Collections.synchronizedMap (new WeakHashMap<SearchListener, Integer>());

	/**
	 * Dictionary to search
	 * Thread safe by virtue of AtomicReference
	 */
	private AtomicReference<Set<EdictDictionary>> dictionaries = new AtomicReference<Set<EdictDictionary>>();


	/**
	 * Start a new search. Any currently running search will be aborted
	 * 
	 * @param query The search query
	 */
	public void search (String query) {

		this.searchQueue.offer (query);

	}


	/**
	 * Abort the running search, if any
	 */
	public void abortSearch() {

		this.interrupt();

	}


	/**
	 * Set dictionaries to search
	 *
	 * @param dictionaries The dictionaries to search
	 */
	public void setDictionaries (Set<EdictDictionary> dictionaries) {

		Set<EdictDictionary> dictionariesCopy = new LinkedHashSet<EdictDictionary> (dictionaries);
		this.dictionaries.set (dictionariesCopy);

	}


	/**
	 * Add a search listener
	 *
	 * @param listener The listener to add
	 */
	public void addListener (SearchListener listener) {

		this.listeners.put (listener, 1);

	}


	/**
	 * Notify all listeners of the start of a search
	 * 
	 * @param searchQuery The search Query 
	 */
	private void notifyListenersStarted (String searchQuery) {

		for (SearchListener listener : this.listeners.keySet()) {
			listener.searchStarted (searchQuery);
		}

	}


	/**
	 * Notify all listeners of a block of search results
	 * 
	 * @param mode The search mode of the results
	 * @param dictionary The dictionary from which the results arise
	 * @param resultIndices The results
	 */
	private void notifyListenersResults (SearchMode mode, EdictDictionary dictionary, List<Integer> resultIndices) {

		for (SearchListener listener : this.listeners.keySet()) {
			listener.searchResults (mode, dictionary, resultIndices);
		}

	}


	/**
	 * Notify all listeners of the successful end of a search
	 */
	private void notifyListenersFinished() {

		for (SearchListener listener : this.listeners.keySet()) {
			listener.searchFinished();
		}

	}


	/**
	 * Notify all listeners that a search was aborted
	 */
	private void notifyListenersAborted() {

		for (SearchListener listener : this.listeners.keySet()) {
			listener.searchAborted();
		}

	}


	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		List<Integer> resultIndices;

		while (true) {

			try {

				String searchQuery;

				do {
					searchQuery = this.searchQueue.take();
				} while (!this.searchQueue.isEmpty());

				notifyListenersStarted (searchQuery);

				if (searchQuery.length() > 0) {
					HashMap<EdictDictionary,EdictSearchResults> resultsMap = new HashMap<EdictDictionary,EdictSearchResults>();

					Set<EdictDictionary> dictionaries = this.dictionaries.get();

					for (EdictDictionary dictionary : dictionaries) {
						
						try {
							EdictSearcher searcher = dictionary.searcher();
							EdictSearchResults results = searcher.search (searchQuery);
							resultsMap.put (dictionary, results);
						} catch (DictionaryException e) {
							// TODO Feed back failure to listeners
							e.printStackTrace();
						}

					}
					SearchMode[] modes = { SearchMode.EXACT, SearchMode.START, SearchMode.END, SearchMode.MIDDLE };

					for (SearchMode mode : modes) {

						for (EdictDictionary dictionary : dictionaries) {

							EdictSearchResults results = resultsMap.get (dictionary);

							// Results can be null if we pass in a string not encodable in the target dictionary
							if (results != null) {

								Iterator<Integer> resultsIterator = results.iterator (mode);
		
								while (resultsIterator.hasNext()) {
									int resultCount = 0;
		
									// If an abort or a new search has been requested, bail out now
									if (interrupted() || !this.searchQueue.isEmpty()) {
										throw new InterruptedException();
									}
		
									resultIndices = new ArrayList<Integer>();
									while (resultsIterator.hasNext() && resultCount < DEFAULT_SEARCH_CHUNK) {
										resultCount++;
										int position = resultsIterator.next();
										resultIndices.add (position);
									}
									notifyListenersResults (mode, dictionary, resultIndices);
								}

							}

						}
					}

				}

				notifyListenersFinished();

			} catch (InterruptedException e) {

				// We receieved an abort or a new search
				Thread.interrupted();
				notifyListenersAborted();

			}

		}

	}


	/**
	 * Default constructor
	 */
	public AsynchronousSearcher () {

		super ("Dictionary Searcher");

		setDaemon (true);

	}

}
