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

package org.itadaki.openoffice;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.itadaki.client.dictionary.DictionaryService;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XIndexAccess;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextRange;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.XSelectionChangeListener;
import com.sun.star.view.XSelectionSupplier;


/**
 * Manages the installation of selection watchers on new frames
 */
public class SelectionManager implements XSelectionChangeListener {

	/**
	 * If <code>true</code>, the dictionary will be searched for the current
	 * selection on any selection change
	 */
	private static boolean searchOnSelect = false;

	/**
	 * A counter used to temporarily lock dictionary search-on-select while,
	 * for instance, furigana processing is taking place
	 */
	private static AtomicInteger searchOnSelectLock = new AtomicInteger(0);

	/**
	 * OpenOffice selection suppliers
	 */
	private Set<XSelectionSupplier> knownSuppliers = new HashSet<XSelectionSupplier>();

	/**
	 * Communication queue to allow selection processing to happen outside the
	 * OpenOffice callback thread
	 */
	private BlockingQueue<XSelectionSupplier> selectionQueue = new ArrayBlockingQueue<XSelectionSupplier> (10);


	/**
	 * Watches for selection changes and forwards selected text to the
	 * dictionary 
	 */
	private class SelectionWatcher extends Thread {

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {

			while (true) {

				String lastSearchString = "";

				try {

					XSelectionSupplier supplier = null; 
					int workaroundCounter = 0;

					while (true) {

						boolean applyWorkaround = false;

						if (workaroundCounter > 0) {
							
							// Workaround for OpenOffice bug 68622
							// Keep polling the selection supplier for changes for a while
							XSelectionSupplier nextSupplier = SelectionManager.this.selectionQueue.poll (
									300 + ((10 - workaroundCounter) * 100),
									TimeUnit.MILLISECONDS
							);

							if (nextSupplier != null) {
								supplier = nextSupplier;
								applyWorkaround = true;
							}

						} else {

							// Wait for a new selection
							supplier = SelectionManager.this.selectionQueue.take();
							applyWorkaround = true;

						}


						// Perform search

						String searchString = null;

						try {

							Object selection = supplier.getSelection();

							searchString = getSelectionString (selection);

							if (applyWorkaround) {
								if (!"".equals (searchString)) {
									workaroundCounter = 10;								
								} else {
									workaroundCounter = 0;
								}
							}

							if (
									   searchOnSelect
									&& (searchOnSelectLock.intValue() == 0)
									&& !searchString.equals ("")
									&& !searchString.equals (lastSearchString)
							) {
								DictionaryService.getInstance().search (searchString, false);
							}

						} catch (NullPointerException e) {
							// The selection supplier we were watching became invalid. Nothing to do
						}


						lastSearchString = searchString;

						if (workaroundCounter > 0) {
							workaroundCounter--;
						}

					}

				} catch (InterruptedException e) {
					// Do nothing
				} catch (Throwable t) {
					ExceptionHelper.dealWith (t);
				}

			}

		}

	}


	/**
	 * Temporarily locks the dictionary search-on-select feature if it is
	 * enabled. Can be called recursively
	 */
	public static void lockSearchOnSelect() {

		searchOnSelectLock.getAndIncrement();

	}


	/**
	 * Releases a temporary lock on the dictionary search-on-select feature
	 */
	public static void unlockSearchOnSelect() {

		searchOnSelectLock.getAndDecrement();

	}


	/**
	 * Gets the text of an OpenOffice selection, limited to the maximum allowed
	 * dictionary search length
	 *
	 * @param selection The OpenOffice selection object
	 * @return The selection text as a String
	 */
	private static String getSelectionString (Object selection) {

		StringBuilder builder = new StringBuilder();

		try {

			XServiceInfo xServInfo = (XServiceInfo) UnoRuntime.queryInterface (XServiceInfo.class, selection);

			if (xServInfo.supportsService ("com.sun.star.text.TextRanges")) {

				XIndexAccess indexAccess = (XIndexAccess) UnoRuntime.queryInterface (XIndexAccess.class, selection);
				XTextRange textRange = null;

				for (int i = 0; i < indexAccess.getCount(); i++) {

					textRange = (XTextRange) UnoRuntime.queryInterface (XTextRange.class, indexAccess.getByIndex (i));

					try {

						XEnumerationAccess paragraphEnumerationAccess = (XEnumerationAccess) UnoRuntime.queryInterface (
								XEnumerationAccess.class,
								textRange
						);
						XEnumeration paragraphEnumeration = paragraphEnumerationAccess.createEnumeration();

						while (paragraphEnumeration.hasMoreElements()) {

							XTextContent textContent = (XTextContent) UnoRuntime.queryInterface (
									XTextContent.class,
									paragraphEnumeration.nextElement()
							);
							XServiceInfo serviceInfo = (XServiceInfo) UnoRuntime.queryInterface (XServiceInfo.class, textContent);

							if (serviceInfo.supportsService ("com.sun.star.text.Paragraph")) {

								XEnumerationAccess portionAccess = (XEnumerationAccess) UnoRuntime.queryInterface (
										XEnumerationAccess.class,
										textContent
								);

								XEnumeration portionEnumeration = portionAccess.createEnumeration();

								while (portionEnumeration.hasMoreElements()) {

									Object portion = portionEnumeration.nextElement();

									XPropertySet portionProperties = (XPropertySet) UnoRuntime.queryInterface (
											XPropertySet.class,
											portion
									);

									String textPortionType = (String)portionProperties.getPropertyValue ("TextPortionType");

									if (textPortionType.equals ("Text")) {
										XTextRange portionTextRange = (XTextRange) UnoRuntime.queryInterface (XTextRange.class, portion);
										String content = portionTextRange.getString();
										builder.append (content);
										if (builder.length() > Commands.MAX_DICTIONARY_SEARCH_LENGTH) {
											return builder.substring (0, Commands.MAX_DICTIONARY_SEARCH_LENGTH);
										}
									}

								}

							}

						}

					} catch (NoSuchElementException e) {
						// TODO http://www.openoffice.org/issues/show_bug.cgi?id=74054
						// Iterating over text sections inside table cells has some issues.
					}

				}

			}

		} catch (Throwable t) {
			// Invalid range exceptions can occur due to the lack of synchronisation
			// with dispose events et al. Probably better simply to ignore the
			// exception, given the risk of deadlocks in trying to synchronise the
			// whole affair properly
		}

		return builder.toString();

	}


	/**
	 * Sets the current search-on-select state
	 *
	 * @param searchOnSelect If <code>true</code>, the dictionary will be
	 *                       searched for the current selection on any
	 *                       selection change
	 */
	public static void setSearchOnSelect (boolean searchOnSelect) {

		SelectionManager.searchOnSelect = searchOnSelect;

	}


	/**
	 * If the given frame has not previously been set up, installs our selection
	 * watcher
	 *
	 * @param frame The frame to set up
	 */
	public void installFrame (XFrame frame) {

		synchronized (this.knownSuppliers) {

			XSelectionSupplier selectionSupplier = (XSelectionSupplier) UnoRuntime.queryInterface (XSelectionSupplier.class, frame.getController());

			if (!this.knownSuppliers.contains (selectionSupplier)) {
				selectionSupplier.addSelectionChangeListener (this);
				this.knownSuppliers.add (selectionSupplier);
			}

		}

	}


	/* XSelectionChangeListener interface */

	/* (non-Javadoc)
	 * @see com.sun.star.view.XSelectionChangeListener#selectionChanged(com.sun.star.lang.EventObject)
	 */
	public void selectionChanged (EventObject event) {

		// This notifier is called from a "funny" context - doing almost
		// anything of interest with OpenOffice will result in a deadlock

		XSelectionSupplier selectionSupplier = (XSelectionSupplier) UnoRuntime.queryInterface (XSelectionSupplier.class, event.Source);

		if (
				   (selectionSupplier != null)
				&& (searchOnSelectLock.intValue() == 0)
		   )
		{
			this.selectionQueue.clear();
			this.selectionQueue.offer (selectionSupplier);
		}

	}


	/* (non-Javadoc)
	 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
	 */
	public void disposing (EventObject event) {

		XSelectionSupplier selectionSupplier = (XSelectionSupplier) UnoRuntime.queryInterface (XSelectionSupplier.class, event);
		if (selectionSupplier != null) {
			this.knownSuppliers.remove (selectionSupplier);
			selectionSupplier.removeSelectionChangeListener (this);
		}

	}


	/**
	 * Default constructor
	 */
	public SelectionManager() {

		// Initialise selection watcher
		new SelectionWatcher().start();

	}

}