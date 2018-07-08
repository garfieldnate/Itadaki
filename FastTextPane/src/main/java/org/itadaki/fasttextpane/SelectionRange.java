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
 * Description of a textual selection range within a Document paragraph
 */
public class SelectionRange implements Cloneable {

	/**
	 * The index within the Document of the paragraph in which the selection lies
	 */
	private int paragraphIndex;

	/**
	 * The first selected character within the paragraph
	 */
	private int startCharacterIndex;

	/**
	 * The last selected character within the paragraph
	 */
	private int endCharacterIndex;

	
	/**
	 * @return the documentIndex
	 */
	public int getParagraphIndex() {
		return this.paragraphIndex;
	}


	/**
	 * @return the endCharacterIndex
	 */
	public int getEndCharacterIndex() {
		return this.endCharacterIndex;
	}


	/**
	 * @return the startCharacterIndex
	 */
	public int getStartCharacterIndex() {
		return this.startCharacterIndex;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SelectionRange:{" + this.paragraphIndex + ":" + this.startCharacterIndex + " - " + this.paragraphIndex + ":" + this.endCharacterIndex + "}";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SelectionRange clone() {

		// Only fundamental types, so plain Object.clone() will do
		try {
			return (SelectionRange) super.clone();
		} catch (CloneNotSupportedException e) {
			// Can't happen
			e.printStackTrace();
		}
		return null;

	}


	/**
	 * Cook a pair of SelectionAddresses to produce the text selection the user
	 * would expect, accounting for factors such as selection direction in
	 * choosing which characters to include in the selected range. Regardless of
	 * the direction of the supplied SelectionAddresses, the resulting
	 * SelectionRange always selects forward (i.e. the end character index is
	 * always greater than or equal to the start character index)
	 * Returns null if no characters were included in the selection
	 *
	 * @param selectionStart
	 * @param selectionEnd
	 * @return The cooked SelectionRange, or null
	 */
	public static SelectionRange create (SelectionAddress selectionStart, SelectionAddress selectionEnd) {

		if (selectionEnd.getParagraphIndex() != selectionStart.getParagraphIndex()) {
			return null;
		}

		int comparison = selectionEnd.compareTo (selectionStart);

		if (comparison == 0) {
			return null;
		}

		int entryIndex;
		int startCharacterIndex;
		int endCharacterIndex;

		entryIndex = selectionStart.getParagraphIndex();

		if (comparison < 0) {

			SelectionAddress temp = selectionStart;
			selectionStart = selectionEnd;
			selectionEnd = temp;

			startCharacterIndex = selectionStart.getCharacterIndex();
			endCharacterIndex = selectionEnd.getCharacterIndex();

			if ((startCharacterIndex != endCharacterIndex) && selectionEnd.isLeading()) {
				endCharacterIndex--;
			}

		} else {

			startCharacterIndex = selectionStart.getCharacterIndex();
			endCharacterIndex = selectionEnd.getCharacterIndex();

			if ((startCharacterIndex != endCharacterIndex) && !selectionStart.isLeading()) {
				startCharacterIndex++;
			}

		}

		return new SelectionRange (entryIndex, startCharacterIndex, endCharacterIndex);

	}


	/**
	 * @param paragraphIndex The index within the Document of the paragraph in which the selection lies
	 * @param startCharacterIndex The first selected character within the paragraph
	 * @param endCharacterIndex The last selected character within the paragraph
	 */
	private SelectionRange (int paragraphIndex, int startCharacterIndex, int endCharacterIndex) {

		this.paragraphIndex = paragraphIndex;
		this.startCharacterIndex = startCharacterIndex;
		this.endCharacterIndex = endCharacterIndex;

	}


}
