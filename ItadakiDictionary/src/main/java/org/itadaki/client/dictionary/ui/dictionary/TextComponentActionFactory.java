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
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

import org.itadaki.client.dictionary.ui.DictionaryWindow;


/**
 * Factory to provide standard actions for a given JTextComponent
 */
public class TextComponentActionFactory {

	/**
	 * Cut action that reflects selection availability and owner editability
	 */
	private static class CutAction extends AbstractAction implements CaretListener {
	
		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;
	
		/**
		 * Text component the action applies to
		 */
		private JTextComponent textComponent;
	
	
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed (ActionEvent e) {
	
			this.textComponent.cut();
	
		}
	
	
		/* (non-Javadoc)
		 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
		 */
		public void caretUpdate (CaretEvent e) {
	
			if (this.textComponent.isEditable() && (this.textComponent.getSelectionEnd() > this.textComponent.getSelectionStart())) {
				setEnabled (true);
			} else {
				setEnabled (false);
			}
	
		}
	
	
		/**
		 * @param textComponent The text component the action will apply to
		 */
		public CutAction (JTextComponent textComponent) {
	
			this.textComponent = textComponent;
			if (!textComponent.isEditable() || (textComponent.getSelectionEnd() > textComponent.getSelectionStart())) {
				setEnabled (false);
			}
			textComponent.addCaretListener (this);
	
		}
	
	}

	/**
	 * Copy action that reflects selection availability and owner editability
	 */
	private static class CopyAction extends AbstractAction implements CaretListener {
	
		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;
	
		/**
		 * Text component the action applies to
		 */
		private JTextComponent textComponent;
	
	
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed (ActionEvent e) {
	
			this.textComponent.copy();
	
		}
	
	
		/* (non-Javadoc)
		 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
		 */
		public void caretUpdate (CaretEvent e) {
	
			if (this.textComponent.getSelectionEnd() > this.textComponent.getSelectionStart()) {
				setEnabled (true);
			} else {
				setEnabled (false);
			}
	
		}
	
	
		/**
		 * @param textComponent The text component the action will apply to
		 */
		public CopyAction (JTextComponent textComponent) {
	
			this.textComponent = textComponent;
			if (textComponent.getSelectionEnd() == textComponent.getSelectionStart()) {
				setEnabled (false);
			}
			textComponent.addCaretListener (this);
	
		}
	
	}

	/**
	 * Paste action that reflects selection availability and owner editability
	 */
	private static class PasteAction extends AbstractAction implements CaretListener {
	
		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;
	
		/**
		 * Text component the action applies to
		 */
		private JTextComponent textComponent;
	
	
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed (ActionEvent e) {
	
			this.textComponent.paste();
	
		}
	
	
		/* (non-Javadoc)
		 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
		 */
		public void caretUpdate (CaretEvent e) {
	
			if (this.textComponent.isEditable()) {
				setEnabled (true);
			} else {
				setEnabled (false);
			}
	
		}
	
	
		/**
		 * @param textComponent The text component the action will apply to
		 */
		public PasteAction (JTextComponent textComponent) {
	
			this.textComponent = textComponent;
			if (!textComponent.isEditable()) {
				setEnabled (false);
			}
			textComponent.addCaretListener (this);
	
		}
	
	}


	/**
	 * Action to search based on the current selection
	 */
	private static class SearchSelectionAction extends AbstractAction implements CaretListener {
	
		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;
	
		/**
		 * Dictionary window to search in
		 */
		private DictionaryWindow dictionaryWindow;		
	
		/**
		 * Text component this action applies to
		 */
		private JTextComponent textComponent;
	
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
	
			String selection = this.textComponent.getSelectedText();
	
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
		 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
		 */
		public void caretUpdate (CaretEvent e) {
			
			String selection = this.textComponent.getSelectedText();
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
		 * @param dictionaryWindow The DictionaryWindow to search in
		 * @param textComponent The JTextComponent to get the selection from
		 */
		public SearchSelectionAction (DictionaryWindow dictionaryWindow, JTextComponent textComponent) {
	
			this.dictionaryWindow = dictionaryWindow;
			this.textComponent = textComponent;
	
			setEnabled (false);
	
			this.textComponent.addCaretListener (this);
	
		}
	}


	/**
	 * Creates a Cut action for the specified JTextComponent
	 *
	 * @param textComponent The JTextComponent to create the action for
	 * @return A Cut action for the JTextComponent
	 */
	public static Action cutActionFor (JTextComponent textComponent) {
		return new CutAction (textComponent);
	}


	/**
	 * Creates a Copy action for the specified JTextComponent
	 *
	 * @param textComponent The JTextComponent to create the action for
	 * @return A Copy action for the JTextComponent
	 */
	public static Action copyActionFor (JTextComponent textComponent) {
		return new CopyAction (textComponent);
	}


	/**
	 * Creates a Paste action for the specified JTextComponent
	 *
	 * @param textComponent The JTextComponent to create the action for
	 * @return A Paste action for the JTextComponent
	 */
	public static Action pasteActionFor (JTextComponent textComponent) {
		return new PasteAction (textComponent);
	}


	/**
	 * Creates a Selection Search action for the specified DictionaryWindow and JTextComponent
	 *
	 * @param dictionaryWindow The DictionaryWindow to create the action for 
	 * @param textComponent The JTextComponent to create the action for
	 * @return A Selection Search action for the specified DictionaryWindow and JTextComponent
	 */
	public static Action searchSelectionActionFor (DictionaryWindow dictionaryWindow, JTextComponent textComponent) {
		return new SearchSelectionAction (dictionaryWindow, textComponent);
	}


	/**
	 * Private constructor to prevent instantiation
	 */
	private TextComponentActionFactory() {
		
	}

}
