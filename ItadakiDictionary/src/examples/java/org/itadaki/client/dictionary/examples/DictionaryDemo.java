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
package org.itadaki.client.dictionary.examples;

import org.itadaki.client.dictionary.DictionaryService;
import org.itadaki.client.dictionary.SystemListener;
import org.itadaki.client.dictionary.SystemProvider;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.*;


/**
 * Standalone dictionary service demo
 */
public class DictionaryDemo {

	/**
	 * Set a native-look Look-And-Feel appropriate to the system
	 * (Dear Sun, printing unnecessary warnings to stderr is rude!)
	 */
	private static void setSystemLookAndFeel() {

		System.setErr (new PrintStream (new OutputStream() {
			@Override
			public void write (int b) throws IOException {
			}
		}));

		Exception e = null;

		try {
			UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			// Or whatever
			e = e1;
		}

		System.setErr (new PrintStream (new BufferedOutputStream (new FileOutputStream (FileDescriptor.out)), true));

		if (e != null) {
			e.printStackTrace();
		}

	}


	/**
	 * @param args Paths to any edict dictionaries to be loaded. If no dictionaries are provided, a test
	 * dictionary containing a single entry, "Boring", will be loaded.
	 */
	public static void main (final String[] args) {
		/**
		 * Do-nothing demo SystemProvider
		 */
		class DemoSystemProvider implements SystemProvider {

			/* (non-Javadoc)
			 * @see org.itadaki.client.dictionary.SystemProvider#getSystemDictionaries()
			 */
			public Map<String,String> getSystemDictionaries() {

				Map<String,String> dictionaries = new TreeMap<String,String>();
				if(args.length == 0){
				    String dictPath = DictionaryDemo.class.getClassLoader().getResource("nulldict").getPath();
					dictionaries.put (dictPath, "Nulldict");
				}else{
					for(int i = 0; i < args.length; i++){
						dictionaries.put(args[0], args[0]);
					}
				}
				return dictionaries;

			}


			/* (non-Javadoc)
			 * @see org.itadaki.client.dictionary.SystemProvider#setSystemListener(org.itadaki.client.dictionary.SystemListener)
			 */
			public void setSystemListener(SystemListener listener) {
				// Do nothing
			}


			public void setSearchOnSelect(boolean searchOnSelect) {
				// Do nothing
			}

		}


		SwingUtilities.invokeLater (new Runnable() {
			public void run() {

				setSystemLookAndFeel();

				DictionaryService.createInstance(new DemoSystemProvider());
				DictionaryService.getInstance().showWindow();

			}
		});

	}
}
