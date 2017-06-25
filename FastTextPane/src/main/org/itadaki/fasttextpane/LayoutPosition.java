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
 * A position within a FastTextView
 */
public class LayoutPosition {

	/**
	 * The index of the paragraph within the Document
	 */
	private int paragraphIndex;

	/**
	 * The index of the line within the paragraph
	 */
	private int lineIndex;

	
	/**
	 * @return the lineIndex
	 */
	public int getLineIndex() {
		return this.lineIndex;
	}

	
	/**
	 * @return the paragraphIndex
	 */
	public int getParagraphIndex() {
		return this.paragraphIndex;
	}

    /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
	@Override
	public boolean equals (Object other) {

		LayoutPosition otherPosition = (LayoutPosition) other;

		if ((otherPosition.getParagraphIndex() == this.paragraphIndex)
				&& (otherPosition.getLineIndex() == this.lineIndex))
		{
			return true;
		}

		return false;

	}

    @Override
    public int hashCode() {
        int result = paragraphIndex;
        result = 31 * result + lineIndex;
        return result;
    }


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "LayoutPosition:{" + this.paragraphIndex + ":" + this.lineIndex + "}";

	}


	/**
	 * @param paragraphIndex The index of the paragraph within the Document
	 * @param lineIndex The index of the line within the paragraph
	 */
	public LayoutPosition (int paragraphIndex, int lineIndex) {
		this.paragraphIndex = paragraphIndex;
		this.lineIndex = lineIndex;
	}


}
