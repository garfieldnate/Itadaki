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


/**
 * Action to move a dictionary up the priority list
 */
public class MoveDownAction extends AbstractAction implements ListSelectionListener {

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

		this.dictionariesTableModel.moveDownDictionaryAtIndex (selectionIndex);
		this.dictionariesTable.getSelectionModel().setLeadSelectionIndex (selectionIndex + 1);

	}


	public void valueChanged (ListSelectionEvent e) {

		if (!this.dictionariesTable.getSelectionModel().isSelectionEmpty()
				&& this.dictionariesTable.getSelectionModel().getMinSelectionIndex() < (this.dictionariesTableModel.getRowCount() - 1))
		{
				this.setEnabled (true);
		} else {
			this.setEnabled (false);
		}

	}


	/**
	 * Default constructor 
	 * @param dictionariesTable Dictionaries table
	 * @param dictionariesTableModel Dictionaries model to remove from
	 */
	public MoveDownAction (JTable dictionariesTable, DictionariesTableModel dictionariesTableModel) {

		super ("Move Down");

		this.dictionariesTable = dictionariesTable;
		this.dictionariesTableModel = dictionariesTableModel;
		this.setEnabled (false);
		dictionariesTable.getSelectionModel().addListSelectionListener (this);

	}

	
}