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

import java.awt.Dimension;


/**
 * Iterator to step through lines in a layout in either direction
 */
public class LayoutIterator {

	/**
	 * The FastTextView to iterate over
	 */
	private FastTextView fastTextView;

	/**
	 * The Document to iterate over
	 */
	private Document document;

	/**
	 * Layout size to iterate
	 */
	private Dimension size;

	/**
	 * Current position within the layout
	 */
	private LayoutPosition currentPosition;

	/**
	 * Current paragraph within the layout
	 */
	private ParagraphLayout currentLayout;


	/**
	 * Indicates if there are more lines following the current position
	 *
	 * @return true if there are more lines, otherwise false
	 */
	public boolean hasNext() {

		if (this.currentPosition.getLineIndex() < (this.currentLayout.lines.size() - 1)) {
			return true;
		}

		if (this.currentPosition.getParagraphIndex() < (this.document.getSize() - 1)) {
			return true;
		}

		return false;

	}


	/**
	 * Indicates if there are more lines preceding the current position
	 *
	 * @return true if there are more lines, otherwise false
	 */
	public boolean hasPrevious() {

		if (this.currentPosition.getLineIndex() > 0) {
			return true;
		}

		if (this.currentPosition.getParagraphIndex() > 0) {
			return true;
		}

		return false;

	}


	/**
	 * Advances to the next line of the layout and returns it
	 *
	 * @return The next line of the layout
	 */
	public LineLayout next() {

		if (this.currentPosition.getLineIndex() < (this.currentLayout.lines.size() - 1)) {
			this.currentPosition = new LayoutPosition (this.currentPosition.getParagraphIndex(), this.currentPosition.getLineIndex() + 1);
		} else if (this.currentPosition.getParagraphIndex() < (this.document.getSize() - 1)) {
			this.currentPosition = new LayoutPosition (this.currentPosition.getParagraphIndex() + 1, 0);
			this.currentLayout = this.fastTextView.getParagraphLayout (this.document, this.currentPosition.getParagraphIndex(), this.size);
		} else {
			this.currentLayout = null;
		}

		return this.currentLayout.lines.get (this.currentPosition.getLineIndex());

	}


	/**
	 * Advances to the previous line of the layout and returns it
	 *
	 * @return The previous line of the layout
	 */
	public LineLayout previous() {

		if (this.currentPosition.getLineIndex() > 0) {
			this.currentPosition = new LayoutPosition (this.currentPosition.getParagraphIndex(), this.currentPosition.getLineIndex() - 1);
		} else if (this.currentPosition.getParagraphIndex() > 0) {
			this.currentLayout = this.fastTextView.getParagraphLayout (this.document, this.currentPosition.getParagraphIndex() - 1, this.size);
			this.currentPosition = new LayoutPosition (this.currentPosition.getParagraphIndex() - 1, this.currentLayout.lines.size() - 1);
		}

		return this.currentLayout.lines.get (this.currentPosition.getLineIndex());

	}


	/**
	 * Returns the current LineLayout
	 *
	 * @return The current LineLayout
	 */
	public LineLayout current() {

		return this.currentLayout.lines.get (this.currentPosition.getLineIndex());

	}


	/**
	 * Returns the current position in the layout
	 *
	 * @return The current position
	 */
	public LayoutPosition currentPosition() {

		return this.currentPosition;

	}


	/**
	 * @param fastTextView The FastTextView to iterate over
	 * @param document The Document to iterate over
	 * @param size The size of the layout viewport
	 * @param position The initial position within the layout
	 */
	public LayoutIterator (FastTextView fastTextView, Document document, Dimension size, LayoutPosition position) {

		this.fastTextView = fastTextView;
		this.document = document;
		this.size = size;
		this.currentPosition = position;

		this.currentLayout = this.fastTextView.getParagraphLayout (this.document, this.currentPosition.getParagraphIndex(), this.size);

	}

}
