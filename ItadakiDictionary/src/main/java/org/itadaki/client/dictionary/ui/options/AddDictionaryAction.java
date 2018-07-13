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
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.itadaki.client.dictionary.ui.OptionsWindow;
import org.itadaki.seashell.edict.EdictDictionary;


/**
 * Action to add a new dictionary
 */
public class AddDictionaryAction extends AbstractAction {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Parent frame to be modal upon
	 */
	private OptionsWindow parent;

	/**
	 * Dictionaries table model
	 */
	private DictionariesTableModel dictionariesTableModel;


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.showOpenDialog (this.parent);
		File newDictionaryFile = fileChooser.getSelectedFile();

		if (newDictionaryFile != null) {
			if (EdictDictionary.testFileFormat (newDictionaryFile.getAbsolutePath())) {
				this.dictionariesTableModel.addDictionary (newDictionaryFile);
			} else {
				JOptionPane.showMessageDialog(this.parent, "The selected dictionary could not be opened", "Problem opening dictionary", JOptionPane.ERROR_MESSAGE);
			}
		}

	}


	/**
	 * Default constructor 
	 * @param parent Parent frame to be modal upon
	 * @param dictionariesTableModel Dictionaries model to add to
	 */
	public AddDictionaryAction (OptionsWindow parent, DictionariesTableModel dictionariesTableModel) {

		super ("Add Dictionary");

		this.parent = parent;
		this.dictionariesTableModel = dictionariesTableModel;

	}
	
}