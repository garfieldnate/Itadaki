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

package org.itadaki.fasttextpane.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.itadaki.fasttextpane.FastTextView;
import org.itadaki.fasttextpane.SelectionListener;


/**
 * Action that copies the current selected text of a FastTextView to the clipboard
 */
public class CopyAction extends AbstractAction implements SelectionListener {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The FastTextView to copy from
	 */
	private FastTextView fastTextView;


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		String selectedText = this.fastTextView.getSelectedText();

		if (selectedText != null) {
			Transferable transferableText = new StringSelection (selectedText);
			Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			systemClipboard.setContents (transferableText, null);
		}
		
	}


	/* (non-Javadoc)
	 * @see org.itadaki.fasttextpane.SelectionListener#selectionChanged()
	 */
	public void selectionChanged() {

		setEnabled (this.fastTextView.hasSelection());

	}


	/**
	 * Default constructor
	 *  
	 * @param fastTextView The FastTextView to copy from 
	 */
	public CopyAction (FastTextView fastTextView) {

		super ("copy-selected-text");

		this.fastTextView = fastTextView;
		fastTextView.addSelectionListener (this);

	}

}
