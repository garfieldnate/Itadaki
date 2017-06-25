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

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;


/**
 * Charset decoder for the EUC-JISX0213 encoding
 */
public class EUCJISX0213CharsetDecoder extends CharsetDecoder {

	/**
	 * Table of characters that can only be represented as pairs of combining
	 * characters in UCS4
	 */
	private static char __jisx0213_to_ucs_combining[][] =
	{
		{ 0x304b, 0x309a },
		{ 0x304d, 0x309a },
		{ 0x304f, 0x309a },
		{ 0x3051, 0x309a },
		{ 0x3053, 0x309a },
		{ 0x30ab, 0x309a },
		{ 0x30ad, 0x309a },
		{ 0x30af, 0x309a },
		{ 0x30b1, 0x309a },
		{ 0x30b3, 0x309a },
		{ 0x30bb, 0x309a },
		{ 0x30c4, 0x309a },
		{ 0x30c8, 0x309a },
		{ 0x31f7, 0x309a },
		{ 0x00e6, 0x0300 },
		{ 0x0254, 0x0300 },
		{ 0x0254, 0x0301 },
		{ 0x028c, 0x0300 },
		{ 0x028c, 0x0301 },
		{ 0x0259, 0x0300 },
		{ 0x0259, 0x0301 },
		{ 0x025a, 0x0300 },
		{ 0x025a, 0x0301 },
		{ 0x02e9, 0x02e5 },
		{ 0x02e5, 0x02e9 },
	};


	/**
	 * JISX0213 -> UCS2 translation table
	 * 
	 * Read in from a resource to avoid exceeding constructor size limits
	 */
	private static char __jisx0213_to_ucs_main[] = new char[120 * 94];


	/**
	 * JISX0213 -> UCS2 translation table
	 */
	private static int __jisx0213_to_ucs_pagestart[] =
	{
		0x0000,  0x0100,  0x0200,  0x0300,  0x0400,  0x1e00,  0x1f00,  0x2000,
		0x2100,  0x2200,  0x2300,  0x2400,  0x2500,  0x2600,  0x2700,  0x2900,
		0x3000,  0x3100,  0x3200,  0x3300,  0x3400,  0x3500,  0x3600,  0x3700,
		0x3800,  0x3900,  0x3a00,  0x3b00,  0x3c00,  0x3d00,  0x3e00,  0x3f00,
		0x4000,  0x4100,  0x4200,  0x4300,  0x4400,  0x4500,  0x4600,  0x4700,
		0x4800,  0x4900,  0x4a00,  0x4b00,  0x4c00,  0x4d00,  0x4e00,  0x4f00,
		0x5000,  0x5100,  0x5200,  0x5300,  0x5400,  0x5500,  0x5600,  0x5700,
		0x5800,  0x5900,  0x5a00,  0x5b00,  0x5c00,  0x5d00,  0x5e00,  0x5f00,
		0x6000,  0x6100,  0x6200,  0x6300,  0x6400,  0x6500,  0x6600,  0x6700,
		0x6800,  0x6900,  0x6a00,  0x6b00,  0x6c00,  0x6d00,  0x6e00,  0x6f00,
		0x7000,  0x7100,  0x7200,  0x7300,  0x7400,  0x7500,  0x7600,  0x7700,
		0x7800,  0x7900,  0x7a00,  0x7b00,  0x7c00,  0x7d00,  0x7e00,  0x7f00,
		0x8000,  0x8100,  0x8200,  0x8300,  0x8400,  0x8500,  0x8600,  0x8700,
		0x8800,  0x8900,  0x8a00,  0x8b00,  0x8c00,  0x8d00,  0x8e00,  0x8f00,
		0x9000,  0x9100,  0x9200,  0x9300,  0x9400,  0x9500,  0x9600,  0x9700,
		0x9800,  0x9900,  0x9a00,  0x9b00,  0x9c00,  0x9d00,  0x9e00,  0x9f00,
		0xf900,  0xfa00,  0xfe00,  0xff00, 0x20000, 0x20180, 0x20300, 0x20400,
		0x20500, 0x20600, 0x20700, 0x20800, 0x20900, 0x20a00, 0x20b00, 0x20d00,
		0x20e00, 0x20f00, 0x21200, 0x21300, 0x21400, 0x21500, 0x21600, 0x21700,
		0x21800, 0x21900, 0x21c00, 0x21d00, 0x21e00, 0x21f00, 0x22100, 0x22200,
		0x22300, 0x22600, 0x22800, 0x22900, 0x22a00, 0x22b00, 0x22c00, 0x22d00,
		0x23100, 0x23300, 0x23400, 0x23500, 0x23600, 0x23700, 0x23800, 0x23a00,
		0x23c00, 0x23d00, 0x23f00, 0x24000, 0x24100, 0x24300, 0x24600, 0x24700,
		0x24800, 0x24a00, 0x24b00, 0x24c00, 0x24d00, 0x24e00, 0x25000, 0x25100,
		0x25200, 0x25400, 0x25500, 0x25700, 0x25900, 0x25a00, 0x25b80, 0x25d00,
		0x25e00, 0x25f00, 0x26000, 0x26200, 0x26300, 0x26400, 0x26600, 0x26700,
		0x26800, 0x26900, 0x26a00, 0x26c00, 0x26e00, 0x26f00, 0x27080, 0x27380,
		0x27600, 0x27700, 0x27900, 0x27a00, 0x27b00, 0x27c00, 0x27d80, 0x27f00,
		0x28000, 0x28200, 0x28380, 0x28500, 0x28600, 0x28900, 0x28a00, 0x28b00,
		0x28d00, 0x28e00, 0x28f00, 0x29200, 0x29400, 0x29500, 0x29600, 0x29700,
		0x29800, 0x29a00, 0x29d00, 0x29e00, 0x29f00, 0x2a000, 0x2a100, 0x2a380,
		0x2a500, 0x2a600,
	};


	/**
	 * Convert a JISX 0213 code to a UCS4 code
	 *
	 * @param row The JISX 0213 row
	 * @param col The JISX 0213 column
	 * @return The UCS4 code. Codes <= 0x80 are used to indicate codes to be
	 * represented as combining characters (from the array
	 * __jisx0213_to_ucs_combining)
	 */
	private int jisx0213_to_ucs4(int row, int col) {

		int val;

		if (row >= 0x121 && row <= 0x17e) {
			row -= 289;
		} else if (row == 0x221) {
			row -= 451;
		} else if (row >= 0x223 && row <= 0x225) {
			row -= 452;
		} else if (row == 0x228) {
			row -= 454;
		} else if (row >= 0x22c && row <= 0x22f) {
			row -= 457;
		} else if (row >= 0x26e && row <= 0x27e) {
			row -= 519;
		} else {
			return 0x0000;
		}

		if (col >= 0x21 && col <= 0x7e) {
			col -= 0x21;
		} else {
			return 0x0000;
		}

		val = __jisx0213_to_ucs_main[row * 94 + col] & 0xffff;
		val = __jisx0213_to_ucs_pagestart[val >> 8] + (val & 0xff);


		if (val == 0xfffd) {
			val = 0x0000;
		}

		return val;

	}


	/* (non-Javadoc)
	 * @see java.nio.charset.CharsetDecoder#decodeLoop(java.nio.ByteBuffer, java.nio.CharBuffer)
	 */
	@Override
	protected CoderResult decodeLoop (ByteBuffer in, CharBuffer out) {

		int position = in.position();

		try {

			while (in.hasRemaining()) {
	
				short b1 = (short) (in.get() & 0xff);
	
				if (b1 < 0x80) {
	
					// 1 byte character
	
					// Overflow if output space not available
					if (out.remaining() < 1) {
						return CoderResult.OVERFLOW;
					}
	
					out.put ((char)b1);
					position++;
	
				} else if (((b1 >= 0xa1) && (b1 <= 0xfe)) || (b1 == 0x8e) || (b1 == 0x8f)) {
	
					// 2 or 3 byte character
	
					// Underflow if more input bytes not available
					if (!in.hasRemaining()) {
						return CoderResult.UNDERFLOW;
					}
	
					short b2 = (short) (in.get() & 0xff);
	
					// Second byte must be 0xa1 .. 0xfe
					if ((b2 < 0xa1) || (b2 > 0xfe)) {
						return CoderResult.malformedForLength(1);
					}
	
					if (b1 == 0x8e) {
	
						// Half-width katakana - 2 bytes
	
						// Second byte must be 0x00 .. 0xdf
						if (b2 > 0xdf) {
							return CoderResult.malformedForLength(1);
						}
	
						// Overflow if output space not available
						if (out.remaining() < 1) {
							return CoderResult.OVERFLOW;
						}
	
						out.put ((char) (b2 + 0xfec0));
						position += 2;
	
					} else {
	
						int ch;
						int inSize;
	
						if (b1 == 0x8f) {
	
							// JISX 0213 Plane 2 - 3 bytes
	
							// Underflow if more input bytes not available
							if (!in.hasRemaining()) {
								return CoderResult.UNDERFLOW;
							}
	
							short b3 = (short) (in.get() & 0xff);
	
							ch = jisx0213_to_ucs4 (0x200 - 0x80 + b2, b3 ^ 0x80);
							inSize = 3;
	
						} else {
	
							// JISX 0213 Plane 1 - 2 bytes
	
							ch = jisx0213_to_ucs4 (0x100 - 0x80 + b1, b2 ^ 0x80);
							inSize = 2;
	
						}
	
						// Illegal character
						if (ch == 0) {
							return CoderResult.malformedForLength(1);
						}
	
						if (ch < 0x80) {
	
							// Combining character
	
							// Overflow if output space not available
							if (out.remaining() < 2) {
								return CoderResult.OVERFLOW;
							}
		
							char ch1 = __jisx0213_to_ucs_combining[ch - 1][0];
							char ch2 = __jisx0213_to_ucs_combining[ch - 1][1];
	
							out.put (ch1);
							out.put (ch2);
	
						} else if (ch <= 0xffff) {
	
							// Single character
							
							// Overflow if output space not available
							if (out.remaining() < 1) {
								return CoderResult.OVERFLOW;
							}
	
							out.put ((char)ch);
	
						} else {
	
							// Surrogate pair
	
							// Overflow if output space not available
							if (out.remaining() < 2) {
								return CoderResult.OVERFLOW;
							}
	
							ch -= 0x10000;
	
							out.put ((char) (0xd800 + (ch >> 10)));
							out.put ((char) (0xdc00 + (ch & 0x3ff)));
	
						}
	
						position += inSize;
	
					}
	
				} else {
	
					// Illegal character
					return CoderResult.malformedForLength(1);
	
				}
	
			}
	
			return CoderResult.UNDERFLOW;

		} finally {

			// Set position to the last known good position
			in.position (position);

		}

	}


	/*
	 * Static initialiser
	 */
	static {

		DataInputStream stream = new DataInputStream (EUCJISX0213CharsetDecoder.class.getClassLoader().getResourceAsStream("jisx0213_to_ucs2.bin"));

		try {
			for (int i = 0; i < 120 * 94; i++) {
				__jisx0213_to_ucs_main[i] = stream.readChar();
			}
		} catch (IOException e) {
			throw new IllegalStateException ("Could not load JISX0213 table!");
		}

	}

	/**
	 * @param cs The common EUCJISX0213Charset instance
	 */
	public EUCJISX0213CharsetDecoder (EUCJISX0213Charset cs) {

		super (cs, 3.0f, 3.0f);

	}


}
