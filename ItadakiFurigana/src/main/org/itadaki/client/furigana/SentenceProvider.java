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

package org.itadaki.client.furigana;

import java.util.List;

import net.java.sen.dictionary.Reading;


/**
 * An interface used to provide sentences of text and readings to the furigana
 * wizard and receive new reading data to apply to the originating text in
 * return
 */
public interface SentenceProvider {

	/**
	 * Indicates whether more sentences are available after the current sentence
	 *
	 * @return <code>true</code> if more sentences are available
	 */
	public boolean hasNext();


	/**
	 * Indicates whether more sentences are available before the current sentence
	 *
	 * @return <code>true</code> if more sentences are available
	 */
	public boolean hasPrevious();


	/**
	 * Moves to the next sentence
	 */
	public void next();


	/**
	 * Moves to the previous sentence
	 */
	public void previous();


	/**
	 * Retrieves the text of the current sentence
	 *
	 * @return The text of the current sentence
	 */
	public String getText();


	/**
	 * Retrieves any pre-existing readings from the current sentence
	 *
	 * @return A list of pre-existing readings from the current sentence
	 */
	public List<Reading> getReadings();


	/**
	 * Sets readings for the current sentence. Any existing readings are removed
	 *
	 * @param readings The readings to set on the current sentence
	 */
	public void setReadings(List<Reading> readings);


	/**
	 * Sets a SentenceListener to be informed of changes to the source text
	 *
	 * @param listener The SentenceListener to set
	 */
	public void setSentenceListener(SentenceListener listener);

	/**
	 * Indicates to the provider that it is no longer required
	 */
	public void dispose();

}
