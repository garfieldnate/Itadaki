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

package test;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;


import org.junit.Test;
import org.itadaki.seashell.CharacterHandler;
import org.itadaki.seashell.edict.UTF8Handler;

import static org.junit.Assert.*;


/**
 * Tests org.itadaki.seashell.edict.UTF8Handler
 */
public class UTF8HandlerTest {

	/**
	 * Test that decoding a UTF-8 encoded String returns the original String 
	 *
	 * @param testString The String to test
	 * @throws Exception 
	 */
	private void utf8TestSuccessFixture (String testString) throws Exception {

		ByteBuffer testStringUTF8Buffer = ByteBuffer.wrap (testString.getBytes ("UTF-8"));

		StringBuffer decodedBuffer = new StringBuffer();

		CharacterHandler handler = new UTF8Handler();

		while (testStringUTF8Buffer.hasRemaining()) {
			char character = (char) handler.readCharacter (testStringUTF8Buffer);
			decodedBuffer.append (character);
		}

		assertEquals (testString, decodedBuffer.toString());

	}


	/**
	 * Decode the contents of a ByteBuffer as UTF-8 (where expected to fail)
	 * 
	 * @param testStringUTF8Buffer 
	 * @throws Exception 
	 */
	private void utf8TestFailureFixture (ByteBuffer testStringUTF8Buffer) throws Exception {

		CharacterHandler handler = new UTF8Handler();

		while (testStringUTF8Buffer.hasRemaining()) {
			handler.readCharacter (testStringUTF8Buffer);
		}

	}


	/**
	 * Test encoding of plain boring 7-bit ASCII
	 * 
	 * @throws Exception 
	 */
	@Test
	public void testOneByte() throws Exception {

		utf8TestSuccessFixture ("a");
	}

	/**
	 * Test encoding of "big" characters
	 *
	 * @throws Exception
	 */
	@Test
	public void testMultiByte() throws Exception {

		utf8TestSuccessFixture ("超");

	}

	
	/**
	 * Test expected failure of decoding 2-byte sequence that could fit in a 1-byte sequence
	 * 
	 * @throws Exception
	 */
	@Test(expected=CharacterCodingException.class)
	public void testOverLength2Byte() throws Exception {

		ByteBuffer testStringUTF8Buffer = ByteBuffer.wrap (new byte[] { (byte)0xC0, (byte)0xBF });

		utf8TestFailureFixture (testStringUTF8Buffer);

	}


	/**
	 * Test expected failure of decoding 3-byte sequence that could fit in a 2-byte sequence
	 * 
	 * @throws Exception
	 */
	@Test(expected=CharacterCodingException.class)
	public void testOverLength3Byte() throws Exception {

		ByteBuffer testStringUTF8Buffer = ByteBuffer.wrap (new byte[] { (byte)0xE0, (byte)0x9F, (byte)0xBF });

		utf8TestFailureFixture (testStringUTF8Buffer);

	}


	/**
	 * Test expected failure of decoding 4-byte sequence that could fit in a 3-byte sequence
	 *
	 * @throws Exception
	 */
	@Test(expected=CharacterCodingException.class)
	public void testOverLength4Byte() throws Exception {

		ByteBuffer testStringUTF8Buffer = ByteBuffer.wrap (new byte[] { (byte)0xF0, (byte)0x9F, (byte)0x9F, (byte)0xBF });

		utf8TestFailureFixture (testStringUTF8Buffer);

	}

}
