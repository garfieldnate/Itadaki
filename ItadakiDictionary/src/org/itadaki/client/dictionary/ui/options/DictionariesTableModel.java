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

package org.itadaki.client.dictionary.ui.options;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.itadaki.client.dictionary.settings.Settings;
import org.itadaki.client.dictionary.settings.Settings.DictionarySettings;
import org.itadaki.client.dictionary.settings.Settings.DictionaryType;

/**
 * Table model for display of configured dictionaries
 */
public class DictionariesTableModel extends AbstractTableModel {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Dictionary settings collection
	 */
	private ArrayList<DictionarySettings> dictionarySettings;


	/**
	 * Return the edited dictionary settings
	 *
	 * @return The edited dictionary settings
	 */
	public ArrayList<DictionarySettings> getDictionarySettings() {

		return this.dictionarySettings;

	}


	/**
	 * Return the dictionary settings at a given index
	 *
	 * @param settingsIndex The index to fetch 
	 * @return The dictionary settings currently at the given index, or <code>null</code>
	 */
	public DictionarySettings getDictionarySettingsAt (int settingsIndex) {

		if (this.dictionarySettings.size() > settingsIndex) {
			return this.dictionarySettings.get (settingsIndex);
		}

		return null;

	}


	/**
	 * Add a new dictionary
	 *
	 * @param newDictionaryFile
	 */
	public void addDictionary (File newDictionaryFile) {

		Color lastColour = null;
		if (!this.dictionarySettings.isEmpty()) {
			lastColour = this.dictionarySettings.get (this.dictionarySettings.size() - 1).getHighlightBackgroundColour();
		}
		Color nextColour = Settings.getNextHighlightColour(lastColour);
		this.dictionarySettings.add (new Settings.DictionarySettings (DictionaryType.LOCAL, newDictionaryFile.getAbsolutePath(), "", nextColour));
		fireTableRowsInserted (this.dictionarySettings.size(), this.dictionarySettings.size());
		
	}


	/**
	 * Remove the dictionary at the given index
	 *
	 * @param selectionIndex
	 */
	public void removeDictionaryAtIndex (int selectionIndex) {

		this.dictionarySettings.remove (selectionIndex);
		fireTableRowsDeleted (selectionIndex, selectionIndex);
		
	}


	/**
	 * Move up the dictionary at the given index
	 *
	 * @param selectionIndex
	 */
	public void moveUpDictionaryAtIndex (int selectionIndex) {

		DictionarySettings settings = this.dictionarySettings.remove (selectionIndex);
		this.dictionarySettings.add (selectionIndex - 1, settings);
		fireTableRowsUpdated (selectionIndex - 1, selectionIndex);

	}


	/**
	 * Move down the dictionary at the given index
	 *
	 * @param selectionIndex
	 */
	public void moveDownDictionaryAtIndex (int selectionIndex) {

		DictionarySettings settings = this.dictionarySettings.remove (selectionIndex);
		this.dictionarySettings.add (selectionIndex + 1, settings);
		fireTableRowsUpdated (selectionIndex, selectionIndex + 1);
	
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName (int column) {

		switch (column) {
			case 0:
				return "Highlight";
			case 1:
				return "Dictionary Type";
			case 2:
				return "Dictionary Name";
				
		}

		return null;
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {

		return 3;

	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {

		return this.dictionarySettings.size();

	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {

		switch (columnIndex) {

			case 0:
				return this.dictionarySettings.get(rowIndex).getHighlightBackgroundColour();

			case 1:
				switch (this.dictionarySettings.get(rowIndex).getType()) {
					case SYSTEM:
						return "System Package";
					case LOCAL:
						return "Locally Installed";
					default:
						throw new IllegalStateException ("Impossible dictionary type set!");
				}

			case 2:
				String displayName = this.dictionarySettings.get(rowIndex).getDisplayName();
				if ("".equals(displayName)) {
					return this.dictionarySettings.get (rowIndex).getFileName();
				}
				return displayName;

		}

		return null;

	}


	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt (Object colour, int rowIndex, int columnIndex) {

		this.dictionarySettings.get(rowIndex).setHighlightBackgroundColour ((Color) colour);

	}


	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable (int rowIndex, int columnIndex) {

		if (columnIndex == 0) {
			return true;
		}

		return false;

	}


	/**
	 * @param dictionarySettings The dictionary settings collection to edit
	 */
	public DictionariesTableModel (ArrayList<DictionarySettings> dictionarySettings) {

		this.dictionarySettings = dictionarySettings;

	}

}