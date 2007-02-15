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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;


/**
 * Label implementing a hyperlink reflecting the enabled status and name of an Action
 */
public class HyperlinkLabel extends JLabel implements PropertyChangeListener, MouseListener {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Bound Action to reflect
	 */
	private Action labelAction;


	/**
	 * Update the label to reflect the current status
	 *
	 * @param enabled true to display an enabled link; false to display a disabled link
	 */
	private void updateLabel (final boolean enabled) {

		Runnable startSearchRunnable = new Runnable() {

			public void run() {

				String linkText = (String) HyperlinkLabel.this.labelAction.getValue (Action.NAME);
		
				String labelText;
				if (enabled) { 
					labelText = "<html><font color=\"#0000ff\"><u>" + linkText + "</u></font></html>";
				} else {
					labelText = "<html><font color=\"#C0C0C0\">" + linkText + "</font></html>";
				}
		
				setText (labelText);

			}

		};		

		if (SwingUtilities.isEventDispatchThread()) {
			startSearchRunnable.run();
		} else {
			SwingUtilities.invokeLater (startSearchRunnable);
		}

	}


	/**
	 * Sets the Action this label is bound to
	 *
	 * @param labelAction The action to bind to
	 */
	public void setAction (Action labelAction) {
		
		this.labelAction = labelAction;
		labelAction.addPropertyChangeListener (this);
		updateLabel (labelAction.isEnabled());

	}


	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange (PropertyChangeEvent evt) {

		updateLabel (this.labelAction.isEnabled());

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked (MouseEvent e) {

		this.labelAction.actionPerformed (null);

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered (MouseEvent e) {

		if (this.labelAction.isEnabled()) {
			setCursor (new Cursor (Cursor.HAND_CURSOR));
		}

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited (MouseEvent e) {

		setCursor (new Cursor (Cursor.DEFAULT_CURSOR));

	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed (MouseEvent e) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased (MouseEvent e) {
		// Do nothing
	}


	/**
	 * Constructor setting initial Action
	 * 
	 * @param labelAction The Action to bind to
	 */
	public HyperlinkLabel (Action labelAction) {

		this();

		setAction (labelAction);

	}


	/**
	 * Default constructor
	 */
	public HyperlinkLabel() {

		super();

		addMouseListener (this);

	}


}
