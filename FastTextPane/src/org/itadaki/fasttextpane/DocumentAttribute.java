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

import java.text.AttributedCharacterIterator.Attribute;


/**
 * Document related Attributes
 */
public class DocumentAttribute extends Attribute {

	/**
	 * Serial verison UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Document Attribute representing a hyperlink. The assigned value is
	 * passed to HyperlinkListeners on invocation
	 */
	public static final DocumentAttribute HYPERLINK = new DocumentAttribute ("hyperlink");

	/**
	 * @param name
	 */
	protected DocumentAttribute (String name) {
		super (name);
	}

}
