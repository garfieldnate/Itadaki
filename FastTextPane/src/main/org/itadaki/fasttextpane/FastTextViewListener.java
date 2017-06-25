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
 * Listener for changes in a FastTextView's viewport
 */
public interface FastTextViewListener {

	/**
	 * Reports changes in the overflow status of the FastTextView. isOverflowing
	 * is false when the laid out document fits wholly within the viewport,
	 * and true otherwise
	 *
	 * @param isOverflowing true if the FastTextView is overflowing its bounds, false otherwise
	 * @return true if the FastTextView's bounds may have changed
	 */
    boolean displayOverflowing(boolean isOverflowing);


	/**
	 * Reports that the paragraph or line indices have changed
	 * 
	 * @param paragraphIndex The new paragraph index
	 * @param lineIndex The new line index within the paragraph
	 */
    void positionChanged(int paragraphIndex, int lineIndex);


	/**
	 * Reports that the size of the FastTextView's Document has changed
	 *
	 * @param newSize The new Document size
	 */
    void documentSizeChanged(int newSize);


}
