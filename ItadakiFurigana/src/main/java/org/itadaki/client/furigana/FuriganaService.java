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

package org.itadaki.client.furigana;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

import net.java.sen.ReadingProcessor;
import net.java.sen.SenFactory;
import net.java.sen.ReadingProcessor.ReadingResult;
import net.java.sen.dictionary.Reading;
import net.java.sen.filter.reading.NumberFilter;

import org.itadaki.client.furigana.ui.FuriganaWindow;


/**
 * Itadaki furigana wizard tool
 */
public class FuriganaService {

	/**
	 * Shared FuriganaService instance 
	 */
	private static FuriganaService instance = null;

	/**
	 * The furigana wizard window
	 */
	private FuriganaWindow furiganaWindow = null;

	/**
	 * The Sen configuration filename
	 */
	private String configFilename = null;


	/**
	 * Retrieves the DictionaryService instance
	 * 
	 * @return The DictionaryService instance 
	 */
	public static synchronized FuriganaService getInstance() {

		if (instance == null) {
			instance = new FuriganaService();
		}

		return instance;

	}


	/**
	 * Sets the Sen configuration to use for analysis
	 *
	 * @param configFilename The Sen configuration filename
	 */
	public void setConfiguration (String configFilename) {

		this.configFilename = configFilename;

	}


	/**
	 * Process the given SentenceProvider within the wizard
	 *
	 * @param sentenceProvider The SentenceProvider to process
	 * @throws MissingDictionaryException 
	 */
	public synchronized void processInteractively (final SentenceProvider sentenceProvider) throws MissingDictionaryException {

		if (this.configFilename == null) {
			throw new MissingDictionaryException();
		}

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {

				if (FuriganaService.this.furiganaWindow == null) { 
					FuriganaService.this.furiganaWindow = new FuriganaWindow (FuriganaService.this.configFilename);
				} else {
					FuriganaService.this.furiganaWindow.setVisible (true);
				}

				FuriganaService.this.furiganaWindow.setSentenceProvider (sentenceProvider);
				
			}
			
		});
		
	}


	/**
	 * Process the given SentenceProvider from start to end without using the wizard
	 *
	 * @param sentenceProvider The SentenceProvider to process
	 * @throws MissingDictionaryException 
	 */
	public synchronized void processAll (SentenceProvider sentenceProvider) throws MissingDictionaryException {

		if (this.configFilename == null) {
			throw new MissingDictionaryException();
		}

		ReadingProcessor readingProcessor = SenFactory.getReadingProcessor (this.configFilename);
		readingProcessor.addFilter (0, new NumberFilter());

		readingProcessor.setText (sentenceProvider.getText());
		ReadingResult readingResult = readingProcessor.process();
		sentenceProvider.setReadings (new ArrayList<Reading> (readingResult.getDisplayReadings().values()));

		while (sentenceProvider.hasNext()) {

			sentenceProvider.next();
			readingProcessor.setText (sentenceProvider.getText());
			readingResult = readingProcessor.process();
			sentenceProvider.setReadings (new ArrayList<Reading> (readingResult.getDisplayReadings().values()));

		};

		sentenceProvider.dispose();

	}


	/**
	 * Private constructor. Call {@link #getInstance()} instead to get the
	 * shared instance
	 */
	private FuriganaService() {

	}


}
