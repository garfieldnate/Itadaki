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
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;


/**
 * Charset representing EUC-JISX0213
 */
public class EUCJISX0213Charset extends Charset {

	@Override
	public boolean contains(Charset cs) {

		return false;

	}

	@Override
	public CharsetDecoder newDecoder() {

		return new EUCJISX0213CharsetDecoder (this);

	}

	@Override
	public CharsetEncoder newEncoder() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Default constructor
	 */
	public EUCJISX0213Charset() {

		super ("X-EUCJISX0213", null);

	}

	
}
