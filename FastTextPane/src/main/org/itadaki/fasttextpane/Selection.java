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

/**
 * Unified selection state for a FastTextView
 */
class Selection implements Cloneable {

	/**
	 * The Document this Selection is against
	 */
	public Document document;

	/**
	 * True if a selection is currently in progress
	 */
	public boolean selecting;

	/**
	 * Textual coordinates of the start of the text selection
	 */
	public SelectionAddress selectionStart;

	/**
	 * Textual coordinates of the end of the text selection
	 */
	public SelectionAddress selectionEnd;

	/**
	 * Edge-adjusted range of the text selection
	 */
	public SelectionRange selectionRange;


	/**
	 * Reports if a text selection is present
	 *
	 * @return True if a text selection is present
	 */
	public boolean isTextSelection() {

		return (this.selectionRange != null);
	}


	/**
	 * Reports if a paragraph selection is present
	 *
	 * @return True if a paragraph selection is present
	 */
	public boolean isParagraphSelection() {

		return ((this.selectionRange == null)
				&& (this.selectionStart != null)
				&& (this.selectionEnd != null)
				&& (this.selectionStart.getParagraphIndex() != this.selectionEnd.getParagraphIndex()));

	}


	/**
	 * Reports if a Document index is contained within the current paragraph selection (if any)
	 *
	 * @param documentIndex The Document index to test
	 * @return true if the index falls within the paragraph selection
	 */
	public boolean paragraphSelectionContains (int documentIndex) {

		if (isParagraphSelection()) {

			int startIndex = Math.min (this.selectionStart.getParagraphIndex(), this.selectionEnd.getParagraphIndex());
			int endIndex = Math.max (this.selectionStart.getParagraphIndex(), this.selectionEnd.getParagraphIndex());
			return ((startIndex <= documentIndex) && (endIndex >= documentIndex));

		}

		return false;

	}


	/**
	 * Reports if the current text selection (if any) is within the given Document index
	 *
	 * @param documentIndex The Document index to test
	 * @return true if the text selection falls within the given index
	 */
	public boolean textSelectionWithin (int documentIndex) {

		return (isTextSelection() && (this.selectionStart.getParagraphIndex() == documentIndex));

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected final Selection clone() {

		try {
			Selection cloneSelectionState = (Selection) super.clone();
			cloneSelectionState.selectionStart = (this.selectionStart == null) ? null : this.selectionStart.clone();
			cloneSelectionState.selectionEnd = (this.selectionEnd == null) ? null : this.selectionEnd.clone();
			cloneSelectionState.selectionRange = (this.selectionRange == null) ? null : this.selectionRange.clone();
			return cloneSelectionState;
		} catch (CloneNotSupportedException e) {
			// Can't happen
			e.printStackTrace();
		}
		return null;

	}

	
	/**
	 * @param document 
	 * @param selecting
	 * @param selectionStart
	 * @param selectionEnd
	 * @param selectionRange
	 */
	public Selection (Document document, boolean selecting, SelectionAddress selectionStart, SelectionAddress selectionEnd, SelectionRange selectionRange) {

		this.document = document;
		this.selecting = selecting;
		this.selectionStart = selectionStart;
		this.selectionEnd = selectionEnd;
		this.selectionRange = selectionRange;

	}


	/**
	 * 
	 */
	public Selection() {

		this (null, false, null, null, null);

	}


}