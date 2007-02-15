/*
 * Copyright (C) 2006
 * Matt Francis <asbel@neosheffield.co.uk>
 * 
 * Based on code from the GNU C Library version 2.4
 * Copyright (C) 2002, 2004 Free Software Foundation, Inc.
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

package org.itadaki.jisx0213;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Charset provider for JISX0213 related encodings
 * 
 * Current supports only EUC-JISX0213. As IANA has at the time of this class's
 * creation not yet blessed this encoding, the charset is registered with an
 * X- prefix in accordance with the CharsetProvider standard
 */
public class JISX0213CharsetProvider extends CharsetProvider {

	/**
	 * Map of charsets provided by this CharsetProvider
	 */
	static final Map<String,Charset> charsets = new HashMap<String,Charset>();


	/* (non-Javadoc)
	 * Static initialiser
	 */
	static {
		charsets.put ("X-EUC-JISX0213", new EUCJISX0213Charset());
	}


	/* (non-Javadoc)
	 * @see java.nio.charset.spi.CharsetProvider#charsetForName(java.lang.String)
	 */
	@Override
	public Charset charsetForName (String charsetName) {

		for (String name : charsets.keySet()) {
			if (name.equals (charsetName)) {
				return charsets.get (name);
			}
		}
		return null;

	}


	/* (non-Javadoc)
	 * @see java.nio.charset.spi.CharsetProvider#charsets()
	 */
	@Override
	public Iterator<Charset> charsets() {

		return charsets.values().iterator();

	}

}
