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

package org.itadaki.client.dictionary;

import org.itadaki.client.dictionary.settings.Settings;
import org.itadaki.client.dictionary.ui.DictionaryWindow;

import java.util.Map;

import javax.swing.*;


/**
 * Itadaki dictionary tool
 */
public class DictionaryService implements SystemListener {

	/**
	 * Shared DictionaryService instance
	 */
	private static DictionaryService instance;

	/**
	 * The dictionary search window
	 */
	private DictionaryWindow dictionaryWindow;

	/**
	 * Interface to the required system environment
	 */
	private SystemProvider systemProvider;


	/**
	 * Creates the shared DictionaryService instance
	 *
	 * @param systemProvider Interface to the required system environment
	 * @return The DictionaryService instance
	 */
	public static DictionaryService createInstance (SystemProvider systemProvider) {

		synchronized (DictionaryService.class) {

			if (instance == null) {
				instance = new DictionaryService (systemProvider);
			} else {
				throw new IllegalStateException ("DictionaryService already instantiated!");
			}

			return instance;

		}

	}


	/**
	 * Retrieves the DictionaryService instance
	 *
	 * @return The DictionaryService instance
	 */
	public static DictionaryService getInstance() {

		synchronized (DictionaryService.class) {

			if (instance == null) {
				throw new IllegalStateException ("DictionaryService not yet instantiated!");
			}
			return instance;

		}

	}


	/**
	 * Returns the SystemProvider interface
	 *
	 * @return The SystemProvider interface
	 */
	public SystemProvider getSystemProvider() {

		return this.systemProvider;

	}


	/**
	 * Shows the dictionary window and brings it into focus with a blank search
	 */
	public void showWindow() {

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {

				// The following are ordered so that the dictionary input field gets
				// focus. Check that this doesn't break if refactoring
				DictionaryService.this.dictionaryWindow.setVisible(true);
				DictionaryService.this.dictionaryWindow.requestFocus();
				DictionaryService.this.dictionaryWindow.resetSearch();

			}

		});

	}


	/**
	 * Perform and output a search
	 *
	 * @param searchKey The search key
	 * @param focusWindow If <code>true</code>, the dictionary window will be
	 *                    shown if it is hidden and brought into focus
	 */
	public void search (final String searchKey, final boolean focusWindow) {

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {

				if (focusWindow) {
					DictionaryService.this.dictionaryWindow.setVisible (true);
					DictionaryService.this.dictionaryWindow.requestFocus();
				}

				if (DictionaryService.this.dictionaryWindow.isVisible()) {
					DictionaryService.this.dictionaryWindow.search (searchKey);
				}

			}

		});

	}


	/* SystemListener interface */

	public void systemDictionariesUpdated (Map<String,String> systemDictionaries) {

		Settings.getInstance().setSystemDictionaries (systemDictionaries);

	}


	/**
	 * Private constructor. Call {@link #getInstance()} instead to get the
	 * shared instance
	 *
	 * @param systemProvider Interface to the required system environment
	 */
	private DictionaryService (final SystemProvider systemProvider) {

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {

				Settings settings = Settings.getInstance();

				DictionaryService.this.systemProvider = systemProvider;
				DictionaryService.this.systemProvider.setSystemListener (DictionaryService.this);
				DictionaryService.this.systemProvider.setSearchOnSelect (settings.getSearchOnSelect());

				settings.setSystemDictionaries (systemProvider.getSystemDictionaries());

				DictionaryService.this.dictionaryWindow = new DictionaryWindow();
				DictionaryService.this.dictionaryWindow.setAlwaysOnTop (settings.getAlwaysOnTop());

				settings.notifyListeners();

			}

		});

	}


}
