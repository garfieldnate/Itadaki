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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.itadaki.fasttextpane.FastTextView;


/**
 * Action to scroll a FastTextView down one line
 */
public class ScrollPageDownAction extends AbstractAction implements Action {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * FastTextView this action applies to
	 */
	private FastTextView fastTextView;


	public void actionPerformed(ActionEvent e) {

		this.fastTextView.scrollPage (false);

	}


	/**
	 * @param fastTextView
	 */
	public ScrollPageDownAction (FastTextView fastTextView) {

		super ("scroll-down-page");

		this.fastTextView = fastTextView;

	}

}
