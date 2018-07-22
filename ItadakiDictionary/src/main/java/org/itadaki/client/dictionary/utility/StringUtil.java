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

package org.itadaki.client.dictionary.utility;


/**
 * String utilities
 */
public class StringUtil {

	/**
	 * Fold the character case of a string. Specifically, alphabetic upper -&gt; lower case, and katakana -&gt; hiragana
	 *
	 * @param text The text to fold
	 * @return The case-folded string
	 */
	public static String foldCase (String text) {
	
		StringBuffer foldedStringBuffer = new StringBuffer (text.toLowerCase());
		int length = foldedStringBuffer.length();
		for (int i = 0; i < length; i++) {
			char character = foldedStringBuffer.charAt(i);
			// Katakana -> hiragana
			if ((character >= 0x30a0) && (character < 0x3100)) {
				foldedStringBuffer.setCharAt (i, (char)(character - 96));
			}
		}
		return foldedStringBuffer.toString();
	
	}

}
