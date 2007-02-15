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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.itadaki.client.dictionary.ui.DictionaryWindow;
import org.itadaki.fasttextpane.FastTextView;
import org.itadaki.fasttextpane.SelectionListener;


/**
 * Factory to provide standard actions for a given FastTextView
 */
public class FastTextViewActionFactory {

	/**
	 * Action to search based on the current selection
	 */
	private static class SearchSelectionAction extends AbstractAction implements SelectionListener {
	
		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;
	
		/**
		 * Dictionary window to search in
		 */
		private DictionaryWindow dictionaryWindow;
	
		/**
		 * Result View to receive selections from
		 */
		private FastTextView fastTextView;
	
		/**
		 * Pattern used against selections by caretUpdate()
		 */
		private Pattern selectionHeadPattern = Pattern.compile ("^[^\r\n]{0,5}");
	
		/**
		 * Pattern used against selections by actionPerformed()
		 */
		private Pattern searchPattern = Pattern.compile ("^[^\r\n]{0,50}");		
	
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed (ActionEvent e) {
	
			String selection = this.fastTextView.getSelectedText();
	
			String searchQuery = "";
	
			if (selection != null) {
				Matcher matcher = this.searchPattern.matcher (selection);
				if (matcher.find()) {
					searchQuery = matcher.group();
				}
			}
	
			this.dictionaryWindow.search (searchQuery);
			
		}
	
	
		/* (non-Javadoc)
		 * @see org.takadb.itadaki.ui.resultpane.SelectionListener#selectionChanged()
		 */
		public void selectionChanged() {
	
			String selection = null;
			if (this.fastTextView.hasTextSelection()) {
				selection = this.fastTextView.getSelectedText();
			}
			String menuItemText = "";
	
			if (selection != null) {
				Matcher matcher = this.selectionHeadPattern.matcher (selection);
				if (matcher.find()) {
					menuItemText = matcher.group();
					if (selection.length() > 5) {
						menuItemText += "...";
					}
				}
				setEnabled (true);
			} else {
				setEnabled (false);
			}
	
			putValue (Action.NAME, String.format ("Search for \"%1$s\"", menuItemText));
	
		}
	
	
		/**
		 * @param dictionaryWindow 
		 * @param fastTextView 
		 */
		public SearchSelectionAction (DictionaryWindow dictionaryWindow, FastTextView fastTextView) {
	
			this.dictionaryWindow = dictionaryWindow;
			this.fastTextView = fastTextView;
	
			setEnabled (false);
	
			fastTextView.addSelectionListener (this);
	
		}
	
	}

	/**
	 * Creates a Selection Search action for the specified DictionaryWindow and FastTextView
	 *
	 * @param dictionaryWindow The DictionaryWindow to create the action for
	 * @param fastTextView The FastTextView to create the action for
	 * @return A Selection Search action for the specified DictionaryWindow and FastTextView
	 */
	public static Action searchSelectionActionFor (DictionaryWindow dictionaryWindow, FastTextView fastTextView) {
		return new SearchSelectionAction (dictionaryWindow, fastTextView);
	}


}
