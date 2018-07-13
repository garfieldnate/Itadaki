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

package org.itadaki.client.dictionary.ui.dictionary;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.itadaki.client.dictionary.ui.DictionaryWindow;


/**
 * Action to start the search in a DictionaryWindow
 */
public class SearchStartAction extends AbstractAction {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The DictionaryWindow to start
	 */
	private DictionaryWindow dictionaryWindow;


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed (ActionEvent e) {

		this.dictionaryWindow.startSearch();

	}


	/**
	 * @param dictionaryWindow The DictionaryWindow to start
	 */
	public SearchStartAction (DictionaryWindow dictionaryWindow) {

		this.dictionaryWindow = dictionaryWindow;

	}

}
