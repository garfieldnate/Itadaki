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
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.text.JTextComponent;

import org.itadaki.client.dictionary.settings.Settings;
import org.itadaki.client.dictionary.ui.DictionaryWindow;
import org.itadaki.client.dictionary.ui.OptionsWindow;
import org.itadaki.fasttextpane.FastTextView;


/**
 * Factory class to create popup menus tailored to a particular component of the dictionary window
 */
public class DictionaryPopupFactory {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Shared popup menu that's not attached to a particular text component
	 */
	private JPopupMenu genericPopupMenu = null;

	/**
	 * Dictionary window this factory attaches to
	 */
	private DictionaryWindow dictionaryWindow;

	/**
	 * Shared OnTopAction instance
	 */
	private OnTopAction onTopAction;

	/**
	 * Shared SearchOnSelectAction instance
	 */
	private SearchOnSelectAction searchOnSelectAction;

	/**
	 * Shared always-on-top menu item model
	 */
	private JToggleButton.ToggleButtonModel onTopModel;

	/**
	 * Shared search-on-select menu item model
	 */
	private JToggleButton.ToggleButtonModel searchOnSelectModel;


	/**
	 * An Action to invoke the options window
	 */
	private class OptionsAction extends AbstractAction {

		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed (ActionEvent e) {

			new OptionsWindow (DictionaryPopupFactory.this.dictionaryWindow);
			
		}


		/**
		 * Default constructor 
		 */
		public OptionsAction() {
			super ("Options");
		}

	}


	/**
	 * An Action to toggle the window always-on-top state
	 */
	private class OnTopAction extends AbstractAction {

		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed (ActionEvent e) {

			JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();

			boolean newState = item.isSelected();
			Settings.getInstance().setAlwaysOnTop (newState);
			DictionaryPopupFactory.this.dictionaryWindow.setAlwaysOnTop (newState);

		}

	}


	/**
	 * An Action to toggle the window search-on-select state
	 */
	private class SearchOnSelectAction extends AbstractAction {

		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = 1L;


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed (ActionEvent e) {

			JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();

			boolean newState = item.isSelected();
			Settings.getInstance().setSearchOnSelect (newState);

		}

	}


	/**
	 * Creates a popup menu with the given actions. Null actions are shown as disabled
	 * 
	 * @param searchSelectionAction 
	 * @param cutAction The Cut action
	 * @param copyAction The Copy action
	 * @param pasteAction The Paste action
	 * @return The constructed popup menu
	 */
	private JPopupMenu createPopupMenu (Action searchSelectionAction, Action cutAction, Action copyAction, Action pasteAction) {

		JPopupMenu popupMenu = new JPopupMenu();

		final JMenuItem searchSelectionItem = new JMenuItem ();

		searchSelectionItem.setAction (searchSelectionAction);
		searchSelectionItem.setText ("Search for \"\"");
		if (searchSelectionAction == null) {
			searchSelectionItem.setEnabled (false);
		}

		popupMenu.add (searchSelectionItem);

		popupMenu.addSeparator();

		JMenuItem cutItem = new JMenuItem (cutAction);
		cutItem.setText ("Cut");
		popupMenu.add (cutItem);
		if (cutAction == null) {
			cutItem.setEnabled (false);
		}

		JMenuItem copyItem = new JMenuItem (copyAction);
		copyItem.setText ("Copy");
		popupMenu.add (copyItem);
		if (copyAction == null) {
			copyItem.setEnabled (false);
		}

		JMenuItem pasteItem = new JMenuItem (pasteAction);
		pasteItem.setText ("Paste");
		popupMenu.add (pasteItem);
		if (pasteAction == null) {
			pasteItem.setEnabled (false);
		}

		popupMenu.addSeparator();

		JMenuItem onTopItem = new JCheckBoxMenuItem (this.onTopAction);
		onTopItem.setText ("Always On Top");
		onTopItem.setModel (this.onTopModel);
		popupMenu.add (onTopItem);

		JMenuItem searchOnSelectItem = new JCheckBoxMenuItem (this.searchOnSelectAction);
		searchOnSelectItem.setText ("Search On Select");
		searchOnSelectItem.setModel (this.searchOnSelectModel);
		popupMenu.add (searchOnSelectItem);

		popupMenu.addSeparator();

		Action optionsAction = new OptionsAction();
		popupMenu.add (optionsAction);

		return popupMenu;

	}


	/**
	 * Returns a shared popup menu for non-text components
	 *
	 * @return The popup menu
	 */
	public JPopupMenu genericMenu() {

		if (this.genericPopupMenu == null) {
			this.genericPopupMenu = createPopupMenu (null, null, null, null);
		}

		return this.genericPopupMenu;

	}


	/**
	 * Returns a popup menu with selection actions attached to the supplied JTextComponent
	 *
	 * @param textComponent The text component to attach to
	 * @return The popup menu
	 */
	public JPopupMenu menuForTextComponent (JTextComponent textComponent) {

		Action searchSelectionAction = TextComponentActionFactory.searchSelectionActionFor (this.dictionaryWindow, textComponent);
		Action cutAction = TextComponentActionFactory.cutActionFor (textComponent);
		Action copyAction = TextComponentActionFactory.copyActionFor (textComponent);
		Action pasteAction = TextComponentActionFactory.pasteActionFor (textComponent);

		return createPopupMenu (searchSelectionAction, cutAction, copyAction, pasteAction);

	}


	/**
	 * Returns a popup menu with selection actions attached to the supplied FastTextView
	 *
	 * @param fastTextView The FastTextView to create a menu for
	 * @return The popup menu
	 */
	public JPopupMenu menuForResultPane (FastTextView fastTextView) {

		Action searchSelectionAction = FastTextViewActionFactory.searchSelectionActionFor (this.dictionaryWindow, fastTextView);
		Action cutAction = null;
		Action copyAction = fastTextView.getActionMap().get ("copy-selected-text");
		Action pasteAction = null;

		return createPopupMenu (searchSelectionAction, cutAction, copyAction, pasteAction);

	}


	/**
	 * @param dictionaryWindow The dictionary window to attach to
	 */
	public DictionaryPopupFactory (DictionaryWindow dictionaryWindow) {

		this.dictionaryWindow = dictionaryWindow;

		this.onTopAction = new OnTopAction();
		this.searchOnSelectAction = new SearchOnSelectAction();
		this.onTopModel = new JToggleButton.ToggleButtonModel();
		this.searchOnSelectModel = new JToggleButton.ToggleButtonModel();

		this.onTopModel.setSelected (Settings.getInstance().getAlwaysOnTop());
		this.searchOnSelectModel.setSelected (Settings.getInstance().getSearchOnSelect());

	}


}
