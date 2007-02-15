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
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.itadaki.seashell.SearchMode;


/**
 * Action that tracks the availability of a result section from ResultMarshaller
 */
public class ResultCountLabel extends JLabel implements ResultMarshallerListener {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Time between updates
	 */
	private static final int updateInterval = 250;

	/**
	 * Current count of results
	 * Thread safe due to AtomicInteger
	 */
	private AtomicInteger resultCount = new AtomicInteger();

	/**
	 * Current status of search
	 * Thread safe due to AtomicBoolean
	 */
	private AtomicBoolean searching = new AtomicBoolean (false);

	/**
	 * Timer that periodically updates the label text. Ensures search progress
	 * indication neither sucks too much resource nor flashes past before the
	 * user sees it
	 */
	private Timer updateTimer = new Timer (updateInterval, new ActionListener() {

		public void actionPerformed(ActionEvent e) {

			if (ResultCountLabel.this.searching.get() == false) {
				ResultCountLabel.this.updateTimer.stop();
			}

			updateLabel();

		}
		
	});


	/**
	 * Update the result count label text
	 */
	public void updateLabel () {

		int resultCount = this.resultCount.get();

		final String labelText;
		if (this.searching.get() == true) {
			labelText = "Searching..." + ((resultCount > 0) ? (" (" + resultCount + " results)") : "");
		} else {
			labelText = "Results: " + resultCount;
		}


		if (SwingUtilities.isEventDispatchThread()) {

			setText (labelText);						

		} else {

			SwingUtilities.invokeLater (new Runnable() {
	
				public void run() {
					setText (labelText);				
				}
				
			});

		}

	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.dictionary.ResultMarshallerListener#resultCountUpdate(int)
	 */
	public void resultCountUpdate (int resultCount) {

		this.resultCount.set (resultCount);

	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.dictionary.ResultMarshallerListener#resultSectionCreated(org.itadaki.seashell.SearchMode, int)
	 */
	public void resultSectionCreated (SearchMode resultSection, int entryIndex) {
		// Do nothing
	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.dictionary.ResultMarshallerListener#resultsCleared()
	 */
	public void resultsCleared() {

		this.searching.set (false);
		this.resultCount.set (0);
		this.updateTimer.stop();
		updateLabel();

	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.dictionary.ResultMarshallerListener#resultsEnded()
	 */
	public void resultsEnded() {

		this.searching.set (false);
		this.updateTimer.stop();
		updateLabel();

	}


	/* (non-Javadoc)
	 * @see org.takadb.itadaki.ui.dictionary.ResultMarshallerListener#resultsStarted()
	 */
	public void resultsStarted() {

		this.resultCount.set (0);
		this.searching.set (true);
		updateLabel();
		this.updateTimer.start();

	}


	/**
	 * @param marshaller The ResultMarshaller to listen to
	 */
	public ResultCountLabel (ResultMarshaller marshaller) {

		super();

		updateLabel();

		marshaller.addListener (this);

	}


}
