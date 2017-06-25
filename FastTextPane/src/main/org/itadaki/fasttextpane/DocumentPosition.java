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
 * A position within a given document
 */
public class DocumentPosition {

	/**
	 * The document in which the position lies
	 */
	private Document document;

	/**
	 * The width of layout to which the LayoutPosition's line index applies
	 */
	private int layoutWidth;

	
	/**
	 * The position within the laid out document
	 */
	private LayoutPosition layoutPosition;


	
	/**
	 * @return the document
	 */
	public Document getDocument() {
		return this.document;
	}


	
	/**
	 * @return the layoutWidth
	 */
	public int getLayoutWidth() {
		return this.layoutWidth;
	}


	
	/**
	 * @return the layoutPosition
	 */
	public LayoutPosition getLayoutPosition() {
		return this.layoutPosition;
	}



	/**
	 * @param document
	 * @param layoutWidth
	 * @param position
	 */
	public DocumentPosition(Document document, int layoutWidth, LayoutPosition position) {
		this.document = document;
		this.layoutWidth = layoutWidth;
		this.layoutPosition = position;
	}


}
