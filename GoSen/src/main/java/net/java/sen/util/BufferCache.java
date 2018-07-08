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

package net.java.sen.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;


/**
 * Provides a cache of open MappedByteBuffers to avoid exhausting address space
 * with duplicated buffers
 */
public class BufferCache {

	/**
	 * A cache of MappedByteBuffers
	 */
	private static Map<String,MappedByteBuffer> cache = new HashMap<String,MappedByteBuffer>();

	/**
	 * Gets a ByteBuffer mapped from the given file. The buffer that is returned
	 * is created by the .duplicate() method of a unique MappedByteBuffer on the
	 * whole file, and should thus share the address space of the original
	 * buffer (ByteBuffers are not thread safe and so must be duplicated, but
	 * the memory usage is at least minimal this way)
	 *
	 * @param file The file to return a buffer for
	 * @return A ByteBuffer mapped from the given file 
	 * @throws IOException
	 */
	public static synchronized ByteBuffer getBuffer(File file) throws IOException {

		MappedByteBuffer buffer = null;

		buffer = cache.get(file.getCanonicalPath());

		if (buffer == null) {
			RandomAccessFile randomFile = new RandomAccessFile(file, "r");
			buffer = randomFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomFile.length());
			cache.put(file.getCanonicalPath(), buffer);
			randomFile.close();
		}

		return buffer.duplicate();

	}

}
