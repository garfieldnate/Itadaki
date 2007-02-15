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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.itadaki.client.dictionary.settings.Settings.DictionarySettings;
import org.itadaki.client.dictionary.settings.Settings.DictionaryType;


/**
 * Action to remove a dictionary
 */
public class RemoveDictionaryAction extends AbstractAction implements ListSelectionListener {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Dictionaries table model
	 */
	private DictionariesTableModel dictionariesTableModel;

	/**
	 * Dictionaries table
	 */
	private JTable dictionariesTable;


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		int selectionIndex = this.dictionariesTable.getSelectionModel().getMinSelectionIndex();

		this.dictionariesTableModel.removeDictionaryAtIndex (selectionIndex);

	}


	public void valueChanged (ListSelectionEvent e) {

		if (this.dictionariesTable.getSelectionModel().isSelectionEmpty()) {
			this.setEnabled (false);
		} else {
			DictionarySettings settings = this.dictionariesTableModel.getDictionarySettingsAt(this.dictionariesTable.getSelectedRow());
			if (settings.getType() != DictionaryType.LOCAL) {
				this.setEnabled (false);
			} else {
				this.setEnabled (true);
			}
		}

	}


	/**
	 * Default constructor 
	 * @param dictionariesTable Dictionaries table
	 * @param dictionariesTableModel Dictionaries model to remove from
	 */
	public RemoveDictionaryAction (JTable dictionariesTable, DictionariesTableModel dictionariesTableModel) {

		super ("Remove Dictionary");

		this.dictionariesTable = dictionariesTable;
		this.dictionariesTableModel = dictionariesTableModel;
		this.setEnabled (false);
		dictionariesTable.getSelectionModel().addListSelectionListener (this);

	}

	
}