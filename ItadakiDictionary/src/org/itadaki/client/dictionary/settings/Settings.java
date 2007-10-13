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

package org.itadaki.client.dictionary.settings;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import org.itadaki.client.dictionary.DictionaryService;
import org.itadaki.client.dictionary.utility.CloneUtil;


/**
 * Application settings manager
 */
public class Settings {

	/**
	 * Shared static instance
	 */
	private static Settings staticInstance = null;

	/**
	 * Array of possible colours for dictionary highlights
	 */
	private static Color[] possibleColours = new Color[] { Color.WHITE, Color.LIGHT_GRAY, Color.PINK, Color.YELLOW, Color.CYAN, Color.ORANGE };

	/**
	 * Dictionary file settings
	 */
	private ArrayList<DictionarySettings> dictionarySettings = new ArrayList<DictionarySettings>();

	/**
	 * Dictionary window always-on-top settings
	 */
	private boolean alwaysOnTop = false;

	/**
	 * Dictionary window search-on-select settings
	 */
	private boolean searchOnSelect = false;

	/**
	 * Listeners for settings changes
	 */
	private Map<SettingsListener, Integer> listeners = Collections.synchronizedMap (new WeakHashMap<SettingsListener, Integer>());

	/**
	 * The Properties filename to store settings in
	 */
	private String propertiesFilename;

	/**
	 * The Properties instance
	 */
	private Properties properties;


	/**
	 * The type of a dictionary
	 */
	public static enum DictionaryType implements Serializable {

		/**
		 * A system-provided dictionary
		 */
		SYSTEM,

		/**
		 * A locally-defined dictionary
		 */
		LOCAL

	}


	/**
	 * Dictionary settings
	 */
	public static class DictionarySettings implements Serializable {

		/**
		 * Serial version UID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The type of the dictionary
		 */
		private DictionaryType type = DictionaryType.LOCAL;

		/**
		 * Dictionary's filename
		 */
		private String fileName = "";

		/**
		 * Dictionary's display name
		 */
		private String displayName = "";

		/**
		 * Background highlight to use in search results
		 */
		private Color highlightBackgroundColour = null;


		/**
		 * Retrieves the dictionary's type
		 *
		 * @return The dictionary's type
		 */
		public DictionaryType getType() {

			return this.type;

		}


		/**
		 * Retrieves the dictionary's filename
		 *
		 * @return The dictionary's filename
		 */
		public String getFileName() {

			return this.fileName;

		}

		
		/**
		 * Sets the dictionary's filename
		 *
		 * @param fileName The dictionary's filename
		 */
		public void setFileName (String fileName) {

			this.fileName = fileName;

		}

		
		/**
		 * Retrieves the dictionary's display name
		 *
		 * @return The dictionary's display name
		 */
		public String getDisplayName() {

			return this.displayName;

		}

		
		/**
		 * Sets the dictionary's display name
		 *
		 * @param displayName The dictionary's display name
		 */
		public void setDisplayName (String displayName) {

			this.displayName = displayName;

		}

		
		/**
		 * Retrieves the dictionary's background highlight colour
		 *
		 * @return The dictionary's background highlight colour
		 */
		public Color getHighlightBackgroundColour() {

			return this.highlightBackgroundColour;

		}

		
		/**
		 * Sets dictionary's background highlight colour
		 *
		 * @param highlightBackgroundColour The dictionary's background highlight colour
		 */
		public void setHighlightBackgroundColour (Color highlightBackgroundColour) {

			this.highlightBackgroundColour = highlightBackgroundColour;

		}


		/**
		 * @param type The dictionary's type
		 * @param fileName The dictionary's filename
		 * @param displayName The dictionary's display name
		 * @param highlightBackgroundColour The dictionary's background highlight colour
		 */
		public DictionarySettings (DictionaryType type, String fileName, String displayName, Color highlightBackgroundColour) {

			this.type = type;
			this.fileName = fileName;
			this.displayName = displayName;
			this.highlightBackgroundColour = highlightBackgroundColour;

		}


		/**
		 * Default constructor
		 */
		public DictionarySettings () {
			
		}

	}


	/**
	 * Stores the current settings to disk
	 */
	private void writePropertiesFile() {

		try {

			File propertiesFile = new File (this.propertiesFilename);
			File tempFile = new File (this.propertiesFilename + ".tmp." + new Random().nextInt(10000));

			FileOutputStream outputStream = new FileOutputStream (tempFile);
			this.properties.store (outputStream, "Dictionary settings");
			outputStream.close();

			propertiesFile.delete();
			tempFile.renameTo (propertiesFile);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	/**
	 * Returns the set of possible dictionary highlight colours
	 *
	 * @return The set of possible dictionary highlight colours
	 */
	public static Color[] getPossibleHighlightColours() {

		return possibleColours;

	}


	/**
	 * Returns the next available dictionary highlight colour in a sequence
	 *
	 * @param colour The colour to follow. If the given colour isn't a member
	 *               of the sequence, the first colour in the sequence is
	 *               returned.
	 * @return The next colour in the sequence
	 */
	public static Color getNextHighlightColour (Color colour) {

		int nextIndex = 0;
		for (int i = 0; i < possibleColours.length; i++) {
			if (possibleColours[i].equals (colour)) {
				nextIndex = (i + 1) % possibleColours.length;
			}
		}

		return possibleColours[nextIndex];

	}


	/**
	 * Notify all listeners of a settings change
	 */
	public void notifyListeners() {

		synchronized (this) {
			for (SettingsListener listener : this.listeners.keySet()) {
				listener.settingsChanged();
			}
		}

	}


	/**
	 * Add a search listener
	 *
	 * @param listener The listener to add
	 */
	public void addListener (SettingsListener listener) {

		synchronized (this) {
			this.listeners.put (listener, 1);
		}

	}


	/**
	 * Returns a private copy of the current DictionarySettings collection
	 *
	 * @return The current DictionarySettings collection
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<DictionarySettings> getDictionarySettings() {

		synchronized (this) {

			ArrayList<DictionarySettings> clonedDictionarySettings = (ArrayList) CloneUtil.clone (this.dictionarySettings);

			return clonedDictionarySettings;

		}

	}


	/**
	 * Sets the current DictionarySettings collection. Any changes are applied to Preferences storage
	 *
	 * @param dictionarySettings The DictionarySettings collection to set
	 */
	@SuppressWarnings("unchecked")
	public void putDictionarySettings (ArrayList<DictionarySettings> dictionarySettings) {

		synchronized (this) {

			int previousDictionaryCount = this.dictionarySettings.size();
	
			this.dictionarySettings = (ArrayList) CloneUtil.clone (dictionarySettings);
	
			int i;
			for (i = 0; i < this.dictionarySettings.size(); i++) {
	
				DictionarySettings settings = this.dictionarySettings.get (i);
				this.properties.setProperty ("dictionary." + i + ".type", settings.getType().toString());
				this.properties.setProperty ("dictionary." + i + ".filename", settings.getFileName());
				this.properties.setProperty ("dictionary." + i + ".displayname", settings.getDisplayName());
				String colourRGB = (settings.getHighlightBackgroundColour() == null) ? "" : "" + settings.getHighlightBackgroundColour().getRGB();
				this.properties.setProperty ("dictionary." + i + ".colour", colourRGB);
	
			}
	
			if (i <= previousDictionaryCount) {
				for (; i < previousDictionaryCount; i++) {
					this.properties.remove ("dictionary." + i + ".type");
					this.properties.remove ("dictionary." + i + ".filename");
					this.properties.remove ("dictionary." + i + ".displayname");
					this.properties.remove ("dictionary." + i + ".colour");
				}
			}
	
			writePropertiesFile();

			notifyListeners();

		}

	}


	/**
	 * Retrieves the current dictionary window always-on-top setting
	 *
	 * @return The current dictionary window always-on-top setting
	 */
	public boolean getAlwaysOnTop() {

		return this.alwaysOnTop;

	}


	/**
	 * Sets the current dictionary window always-on-top setting

	 * @param alwaysOnTop The new dictionary window always-on-top setting
	 */
	public void setAlwaysOnTop (boolean alwaysOnTop) {

		this.alwaysOnTop = alwaysOnTop;

		this.properties.setProperty ("alwaysOnTop", this.alwaysOnTop ? "1" : "0");

		writePropertiesFile();

		// Nobody is currently interested in listening to the always-on-top
		// setting.
		// If we did signal changes here, dictionary settings changes should be
		// differentiated from other changes to avoid unwanted search reloads

	}


	/**
	 * Retrieves the current dictionary window search-on-select setting
	 *
	 * @return The current dictionary window search-on-select setting
	 */
	public boolean getSearchOnSelect() {

		return this.searchOnSelect;

	}


	/**
	 * Sets the current dictionary window search-on-select setting

	 * @param searchOnSelect The new dictionary window search-on-select setting
	 */
	public void setSearchOnSelect (boolean searchOnSelect) {

		this.searchOnSelect = searchOnSelect;

		this.properties.setProperty ("searchOnSelect", this.searchOnSelect ? "1" : "0");

		writePropertiesFile();

		DictionaryService.getInstance().getSystemProvider().setSearchOnSelect (searchOnSelect);

		// Probably the responsiblity for propagating this change should be
		// elsewhere
		// If we did signal changes here, dictionary settings changes should be
		// differentiated from other changes to avoid unwanted search reloads

	}


	/**
	 * Sets the available system dictionaries. Any current system dictionaries
	 * not in the supplied list are removed.
	 *
	 * @param systemDictionaries The new list of system dictionaries
	 */
	public void setSystemDictionaries (Map<String,String> systemDictionaries) {

		synchronized (this) {

			// Create a set of filenames of the existing system dictionaries
			// Remove cached system dictionaries that are no longer present
			Set<String> existingSystemDictionarySet = new HashSet<String>();
			Color lastColour = null;
			for (Iterator<DictionarySettings> iterator = this.dictionarySettings.iterator(); iterator.hasNext(); ) {
				DictionarySettings settings = iterator.next();
				try {
					String existingFilename = settings.getFileName();
					File existingFile = new File (existingFilename);
					if (existingFile.isFile()) {
						existingSystemDictionarySet.add (existingFilename);
						if ((settings.getType() == DictionaryType.SYSTEM) && !systemDictionaries.containsKey (existingFilename)) {
							iterator.remove();
						}
						lastColour = settings.getHighlightBackgroundColour();
					} else {
						iterator.remove();
					}
				} catch (Exception e) {
					iterator.remove();
				}
			}

			// Add missing system dictionaries
			for (String newFilename : systemDictionaries.keySet()) {

				String newDisplayName = systemDictionaries.get (newFilename);
				if (!existingSystemDictionarySet.contains (newFilename)) {
					Color nextColour = getNextHighlightColour (lastColour);
					this.dictionarySettings.add (new DictionarySettings (DictionaryType.SYSTEM, newFilename, newDisplayName, nextColour));
					lastColour = nextColour;
				}
	
			}

			putDictionarySettings(this.dictionarySettings);

		}

	}


	/**
	 * Fetch the single static instance of this class
	 *
	 * @return The global Settings instance
	 */
	public static Settings getInstance() {

		synchronized (Settings.class) {

			if (staticInstance == null) {
				staticInstance = new Settings();
			}

			return staticInstance;

		}

	}


	/**
	 * Private default constructor. Prevents direct instantiation.
	 */
	private Settings() {

		this.properties = new Properties();

		// Set user data directory in a system dependent way (ick)
		String osName = System.getProperty ("os.name");
		String userHome = System.getProperty ("user.home");
		String propertiesDirectory;
		if (osName.startsWith ("Windows")) {
			String applicationDataName = System.getenv ("APPDATA");
			if (applicationDataName == null) {
				applicationDataName = "Application Data";
			}
			propertiesDirectory = userHome + File.separator + applicationDataName + File.separator + "Itadaki";
		} else {
			propertiesDirectory = userHome + File.separator + ".itadaki";
		}
		File propertiesDirectoryFile = new File (propertiesDirectory);
		if (!propertiesDirectoryFile.exists()) {
			propertiesDirectoryFile.mkdirs();
		}
		this.propertiesFilename = propertiesDirectory  + File.separator + "dictionary.properties";  


		// Load settings
		try {
			FileInputStream inputStream = new FileInputStream (this.propertiesFilename);
			this.properties.load (inputStream);
			inputStream.close();
		} catch (FileNotFoundException e) {
			// Do nothing
		} catch (Exception e) {
			e.printStackTrace();
		}


		// Parse always-on-top setting
		String alwaysOnTop = this.properties.getProperty ("alwaysOnTop", null);
		this.alwaysOnTop = ("1".equals (alwaysOnTop));


		// Parse always-on-top setting
		String searchOnSelect = this.properties.getProperty ("searchOnSelect", null);
		this.searchOnSelect = ("1".equals (searchOnSelect));


		// Parse dictionary file settings
		int i = 0;

		boolean done = false;
		while (!done) {

			String dictionaryFileName = this.properties.getProperty ("dictionary." + i + ".filename", null);
			if (dictionaryFileName == null) {

				done = true;

			} else {

				String dictionaryTypeName = this.properties.getProperty ("dictionary." + i + ".type", null);
				DictionaryType dictionaryType;
				if ("SYSTEM".equals (dictionaryTypeName)) {
					dictionaryType = DictionaryType.SYSTEM;
				} else {
					dictionaryType = DictionaryType.LOCAL;
				}

				String dictionaryDisplayName = this.properties.getProperty ("dictionary." + i + ".displayname", "");
				String colourString = this.properties.getProperty ("dictionary." + i + ".colour", "");
				Color colour = null;
				if (colourString.length() > 0) {
					colour = new Color (new Integer (colourString));
				}
				this.dictionarySettings.add (new DictionarySettings (
						dictionaryType, dictionaryFileName, dictionaryDisplayName, colour
				));

			}

			i++;

		}

	}

}
