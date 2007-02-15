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

package org.itadaki.fasttextpane;

import java.awt.Color;
import java.text.AttributedString;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * Represents an append-only, Paragraph-based model of text
 */
public abstract class Document {

	/**
	 * Number of paragraphs in the Document
	 */
	private int size = 0;

	/**
	 * Listeners for document expansion
	 */
	private Map<DocumentListener, Integer> listeners = Collections.synchronizedMap (new WeakHashMap<DocumentListener, Integer>());


	/**
	 * Retrieves the current number of paragraphs in the Document
	 *
	 * @return The number of paragraphs in the Document
	 */
	public int getSize() {

		synchronized (this) {
			return this.size;
		}

	}


	/**
	 * Sets the new size of the Document. The new size must not be smaller than the existing size
	 *
	 * @param newSize The new size to set
	 */
	public void setSize (int newSize) {

		int oldSize;
		synchronized (this) {
			if (newSize < this.size) {
				throw new IllegalArgumentException ("New size must not be smaller than existing size");
			}

			oldSize = this.size;
			this.size = newSize;
		}
		notifyListeners (oldSize, newSize);

	}

	/**
	 * Notify listeners that the document has expanded
	 * @param oldSize The previous size
	 * @param newSize The new size
	 */
	private void notifyListeners (int oldSize, int newSize) {

		for (DocumentListener listener : this.listeners.keySet()) {
			listener.documentExpanded (oldSize, newSize);
		}

	}


	/**
	 * Add a listener for document expansion
	 *
	 * @param listener The listener to add
	 */
	public void addListener (DocumentListener listener) {

		this.listeners.put (listener, 1);

	}


	/**
	 * Remove a DocumentListener
	 *
	 * @param listener The listener to remove
	 */
	public void removeListener (DocumentListener listener) {

		this.listeners.remove (listener);

	}


	/**
	 * Retrieve an AttributedString representing the paragraph at the given index
	 *
	 * @param paragraphIndex The index of the paragraph
	 * @return The paragraph's AttributedString
	 */
	public abstract AttributedString getParagraph (int paragraphIndex);


	/**
	 * Retrieve a String representing the paragraph at the given index
	 *
	 * @param paragraphIndex The index of the paragraph
	 * @return The paragraph's String
	 */
	public abstract String getPlainParagraph (int paragraphIndex);


	/**
	 * Retrieve the requested background highlight colour of the paragraph at the given index
	 *
	 * @param paragraphIndex The index of the paragraph
	 * @return The paragraph's background highlight colour
	 */
	public abstract Color getBackground (int paragraphIndex);


}
