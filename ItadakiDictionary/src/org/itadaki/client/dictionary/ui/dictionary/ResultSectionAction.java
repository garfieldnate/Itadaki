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
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.itadaki.fasttextpane.FastTextView;
import org.itadaki.seashell.SearchMode;


/**
 * Action that tracks the availability of a result section from ResultMarshaller
 */
public class ResultSectionAction extends AbstractAction implements ResultMarshallerListener {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The FastTextView this action operates on
	 * Thread safe by finality
	 */
	private final FastTextView fastTextView;

	/**
	 * The result section this Action reflects
	 * Thread safe by finality
	 */
	private final SearchMode resultSection;

	/**
	 * Document index of the tracked section
	 * Thread safe due to AtomicInteger
	 */
	private AtomicInteger entryIndex = new AtomicInteger();


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.dictionary.ResultMarshallerListener#resultCountUpdate(int)
	 */
	public void resultCountUpdate (int resultCount) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.dictionary.ResultMarshallerListener#resultSectionCreated(org.itadaki.seashell.SearchMode, int)
	 */
	public void resultSectionCreated (SearchMode resultSection, int entryIndex) {

		if (resultSection == this.resultSection) {
			this.entryIndex.set (entryIndex);
			setEnabled (true);
		}

	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.dictionary.ResultMarshallerListener#resultsCleared()
	 */
	public void resultsCleared() {
		setEnabled (false);
	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.dictionary.ResultMarshallerListener#resultsEnded()
	 */
	public void resultsEnded() {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.dictionary.ResultMarshallerListener#resultsStarted()
	 */
	public void resultsStarted() {
		setEnabled (false);
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		this.fastTextView.setParagraph (this.entryIndex.get());

	}


	/**
	 * @param marshaller The ResultMarshaller to listen to
	 * @param fastTextView The FastTextView to bind to
	 * @param resultSection The result section to reflect
	 * @param actionName The Action name
	 */
	public ResultSectionAction (ResultMarshaller marshaller, FastTextView fastTextView, SearchMode resultSection, String actionName) {

		this.fastTextView = fastTextView;
		this.resultSection = resultSection;

		putValue (Action.NAME, actionName);
		setEnabled (false);

		marshaller.addListener (this);

	}


}
