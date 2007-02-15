/*
 * Copyright (C) 2004-2007 Sen Project
 * Masanori Harada <harada@ingrid.org>
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

package benchmark;

import java.io.IOException;
import java.io.RandomAccessFile;
import net.java.sen.StringTagger;
import net.java.sen.SenFactory;


/**
 * Performance benchmark for Sen
 */
class SenBench {

	/**
	 * Main method
	 *
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		if (args.length < 4) {
			System.out.println("usage: java SenBench <config> <repeats> <encoding> file [file ..]");
			System.exit(2);
		}

		String configFilename = args[0];
		Integer repeats = new Integer(args[1]);
		String encoding = args[2];

		StringTagger tagger = SenFactory.getStringTagger(configFilename);

		long processingTime = 0;
		long totalBytes = 0;
		long totalChars = 0;

		long startTime = System.currentTimeMillis();

		for (int i = 3; i < args.length; i++) {
			char[] text;
			try {
				RandomAccessFile file = new RandomAccessFile(args[i], "r");
				byte[] buffer = new byte[(int) file.length()];
				file.readFully(buffer);
				file.close();
				text = new String(buffer, encoding).toCharArray();
				totalBytes += buffer.length;
				totalChars += text.length;
			} catch (IOException ioe) {
				continue;
			}

			long analysisStartTime = System.currentTimeMillis();
			for (int j = 0; j < repeats; j++) {
				tagger.analyze(text);
			}
			long analysisEndTime = System.currentTimeMillis();
			processingTime += (analysisEndTime - analysisStartTime);
		}

		long endTime = System.currentTimeMillis();

		System.out.println("number of files: " + (args.length - 3));
		System.out.println("number of repeats: " + repeats);
		System.out.println("number of bytes: " + totalBytes);
		System.out.println("number of chars: " + totalChars);
		System.out.println("total time elapsed: " + (endTime - startTime) + " msec.");
		System.out.println("analysis time: " + (processingTime) + " msec.");

	}

}
